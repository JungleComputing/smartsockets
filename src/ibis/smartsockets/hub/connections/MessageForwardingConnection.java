package ibis.smartsockets.hub.connections;

import ibis.smartsockets.direct.DirectSocket;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.hub.SmartSocketsProtocol;
import ibis.smartsockets.hub.Connections;
import ibis.smartsockets.hub.state.DirectionsSelector;
import ibis.smartsockets.hub.state.HubDescription;
import ibis.smartsockets.hub.state.HubList;
import ibis.smartsockets.hub.state.HubsForClientSelector;
import ibis.smartsockets.util.MalformedAddressException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class MessageForwardingConnection extends BaseConnection {

    protected final static Logger meslogger = 
        LoggerFactory.getLogger("ibis.smartsockets.hub.messages"); 
    
    protected final static Logger vclogger = 
        LoggerFactory.getLogger("ibis.smartsockets.hub.connections.virtual"); 
       
    protected final static int DEFAULT_CREDITS = 10; 
    
    private final String name; 
    
    protected MessageForwardingConnection(DirectSocket s, DataInputStream in, 
            DataOutputStream out, Connections connections, HubList hubs, 
           boolean master, String name) {
       
        super(s, in, out, connections, hubs);
        
        this.name = name;
    }

    // Directly sends a message to a hub.
    private boolean directlyToHub(DirectSocketAddress hub, ClientMessage cm) {

        HubConnection c = connections.getHub(hub); 
        
        if (c != null) {             
            return c.forwardClientMessage(cm);
        }   

        return false;
    }
    
    // Tries to forward a message to a given proxy, directly or indirectly.  
    private void forwardMessageToHub(HubDescription p, ClientMessage cm) {
        
        if (meslogger.isDebugEnabled()) { 
            meslogger.debug("Attempting to forward message to hub "
                    + p.hubAddress);
        }
        
        if (directlyToHub(p.hubAddress, cm)) {
            
            if (meslogger.isDebugEnabled()) {
                meslogger.debug("Succesfully forwarded message to hub " 
                        + p.hubAddressAsString + " using direct link");
            }
            return;            
        } 
         
        if (cm.hopsLeft == 0) {
            if (meslogger.isInfoEnabled()) {
                meslogger.info("Failed to forward message to hub " 
                        + p.hubAddressAsString + " and we are not allowed to use" 
                        +" an indirection!");
            }
            return;
        } 
            
        if (meslogger.isDebugEnabled()) {
            meslogger.debug("Failed to forward message to hub " 
                    + p.hubAddressAsString + " using direct link, " 
                    + "trying indirection");
        }

        // We don't have a direct connection, but we should be able to reach the
        // proxy indirectly
        HubDescription p2 = p.getIndirection();
        
        if (p2 == null) {
            // Oh dear, we don't have an indirection!
            meslogger.warn("Indirection address of " + p.hubAddressAsString
                    + " is null!");
            return;
        } 

        if (directlyToHub(p2.hubAddress, cm)) { 
            if (meslogger.isDebugEnabled()) {
                meslogger.debug("Succesfully forwarded message to hub " 
                        + p2.hubAddressAsString + " using direct link");
            }
            return;            
        } 

        if (meslogger.isInfoEnabled()) {
            meslogger.info("Failed to forward message to hub "
                    + p.hubAddressAsString + " or it's indirection " 
                    + p2.hubAddressAsString);
        }
    }   
    
    private boolean deliverLocally(ClientMessage cm) {

        // First check if we can find the target locally             
        ClientConnection c = connections.getClient(cm.getTarget());
        
        if (c == null) {
            meslogger.debug("Cannot find client address locally: " + cm.getTarget());                        
            return false;
        } 

        if (meslogger.isDebugEnabled()) {
            
            if (cm.returnToSender) { 
                meslogger.debug("Attempting to directly return message to sender " 
                        + cm.sourceAsString());                
            } else {             
                meslogger.debug("Attempting to directly forward message to client " 
                        + cm.targetAsString());
            }
        }
           
        // We found the target, so lets forward the message
        boolean result = c.forwardClientMessage(cm);
         
        if (meslogger.isDebugEnabled()) {
            if (cm.returnToSender) {
                meslogger.debug("Directly return message to sender " 
                        + cm.sourceAsString() + (result ? " succeeded!" : "failed!"));
            } else { 
                meslogger.debug("Directly forwarding message to client " 
                        + cm.targetAsString() + (result ? " succeeded!" : "failed!"));                
            }
        }
        
        return result;        
    }
    
    private boolean forwardToHub(ClientMessage cm, boolean setHops) {
        
        DirectSocketAddress hub = cm.getTargetHub();
        
        // Lets see if we directly know the targetHub and if it knows the target         
        if (hub == null) {
            if (meslogger.isDebugEnabled()) {
                meslogger.debug("Target hub not set!");
            }
            return false;
        }
        
        HubDescription p = knownHubs.get(hub);
           
        if (p == null) {
            if (meslogger.isDebugEnabled()) {
                meslogger.debug("Target hub " + hub + " does not know client " 
                        + cm.getTarget());
            }
            return false;
        }
         
        // The targetHub exists so forward the message.
        if (setHops) {         
            cm.hopsLeft = p.getHops();
        }
        
        forwardMessageToHub(p, cm);                
                
        if (meslogger.isDebugEnabled()) {
            meslogger.debug("Directly forwarded message to hub: " + hub);                
        }
        
        return true;
    }
    
    
    protected void forward(ClientMessage m, boolean setHops) {   
        
        if (m.getSourceHub() == null) { 
            // should never happen, but we like to program defensively
            m.setSourceHub(getLocalHub());
        }
        
        DirectSocketAddress hub = m.getTargetHub();
        
        if (hub == null) { 
       
            // Try to deliver the message to a local client.
            if (deliverLocally(m)) { 
                return;
            }
            
            // If this fails, we try to find the right hub and forward the 
            // message... 
            //
            // TODO: reimplement this with a bcast to all hubs instead of  
            //       relying on client info to be gossiped in advance ?         
            HubsForClientSelector hss = new HubsForClientSelector(
                    m.getTarget(), false);
            
            knownHubs.select(hss);
            
            LinkedList<HubDescription> result = hss.getResult();
            
            if (result.size() == 0) {
                // No hubs were found that known the client, so lets return the 
                // message to the sender...
                if (m.returnToSender) {
                    if (meslogger.isDebugEnabled()) {
                        meslogger.debug("Cannot return message to sender, since it " 
                                + " has disappeared! (source= " + m.targetAsString() 
                                + " target=" + m.sourceAsString());                
                    }
                } else { 
                    if (meslogger.isDebugEnabled()) {
                        meslogger.debug("Return message to sender, since target is " 
                                + " not known! (source= " + m.sourceAsString()
                                + " target=" + m.targetAsString());                
                    }
                    returnToSender(m);
                }
                return;
            }
                        
            for (HubDescription h : result) {
                
                if (setHops) {             
                    m.hopsLeft = h.getHops();
                }
                
                forwardMessageToHub(h, m);
            }    
            
        } else {
             
            if (isLocalHub(hub)) { 

                if (deliverLocally(m)) { 
                    return;
                } else { 
                    returnToSender(m);
                }
                
            } else {
                
                if (forwardToHub(m, setHops)) { 
                    return;
                } else { 
                    returnToSender(m);    
                }
            }
        } 
    }    
    
    private void returnToSender(ClientMessage m) {
        
      //  System.out.println("**** Returning to sender....");
        
        if (m.returnToSender) {
            // should never happen!
     //       System.out.println("**** Returning to sender says EEK");
            return;
        }
        
   //     System.out.println("**** Returning to sender says her I go!");
      
        m.returnToSender = true;
        forward(m, true);
    }

    protected final boolean forwardClientMessage(ClientMessage m) { 
        
        try {
            synchronized (out) {
                out.writeByte(SmartSocketsProtocol.INFO_MESSAGE);            
                m.write(out);            
                out.flush();
            }
            return true;
        } catch (Exception e) {
            handleDisconnect(e);
            
            if (meslogger.isDebugEnabled()) { 
                meslogger.debug("Forwarding message failed: " + m, e);                
            } else { 
                // TODO: remove
                meslogger.warn("Forwarding message failed: " + m, e);
            }
            
            return false;
        }                
    }
    
 
    
    private final void skipBytes(int bytes) throws IOException { 
       
        while (bytes > 0) { 
            bytes -= in.skipBytes(bytes);
        }
    }
    
   
    
    protected final void handleClientMessage() throws IOException {        
        ClientMessage cm = new ClientMessage(in);        
        
        if (meslogger.isDebugEnabled()) {
            meslogger.debug("Got info message: " + cm);
        }
        
        forward(cm, false);          
    }
        
    private void forwardData(byte [] data) throws UnknownHostException, MalformedAddressException {

    	DirectSocketAddress hub = DirectSocketAddress.fromBytes(data, 4);

    	//System.err.println("Hub is " + hub);
    	
    	int off = 4 + (hub == null ? 4 : 4 + hub.getAddress().length);    	
    	
    	//System.err.println("Off is " + off);
    	
    	DirectSocketAddress node = DirectSocketAddress.fromBytes(data, off);

    	//System.err.println("Node is " + node);
    	
    	off += node.getAddress().length;

    	//System.err.println("Dropping data for " + node + " @ " + hub + " : length " + (data.length-off));    	
    }
    
    
    protected final void handleDataMessage() throws IOException {        
        
    	int len = in.readInt();
    	
    	byte [] data = new byte[len];
    	
    	in.readFully(data);
    	
    	if (meslogger.isDebugEnabled()) {
            meslogger.debug("Got data message: [" + data.length + "]");
        }
    	
    	try { 
    		forwardData(data);
    	} catch (Exception e) {
    		meslogger.warn("Failed to process data message: [" + data.length + "]", e);
    	}
    }
    
    protected abstract void handleDisconnect(Exception e); 
    
    protected abstract String getUniqueID(long index);
    
    public void printStatistics() {
     
        if (true) { 
/*
            System.out.println(name + " ----- Connection statistics -----");
            System.out.println(name + " ");
            System.out.println(name + " Connections: " + connectionsTotal);
            System.out.println(name + "    - failed: " + connectionsFailed);
            System.out.println(name + "    - lost  : " + connectionsRepliesLost);
            System.out.println(name + "    - error : " + connectionsRepliesError);
            System.out.println(name + " ");            
            System.out.println(name + " Replies    : " + connectionsReplies);
            System.out.println(name + "  - ACK     : " + connectionsACKs);
            System.out.println(name + "  - rejected: " + connectionsNACKs);
            System.out.println(name + "  - lost    : " + connectionsRepliesLost);
            System.out.println(name + "  - error   : " + connectionsRepliesError);            
            System.out.println(name + " ");
            System.out.println(name + " Messages   : " + messages);
            System.out.println(name + "  - bytes   : " + messagesBytes);
            System.out.println(name + "  - lost    : " + messagesLost);
            System.out.println(name + "  - error   : " + messagesError);            
            System.out.println(name + " ");
            System.out.println(name + " Mess. ACKS : " + messageACK);
            System.out.println(name + "     - lost : " + messageACKLost);
            System.out.println(name + "     - error: " + messageACK_Error);  
            */ 
        }
    }
    
    protected abstract boolean handleOpcode(int opcode); 
    
    protected final boolean runConnection() {
        
        try {
            int opcode = in.read();
            
            switch (opcode) { 
        
            case -1:
            case SmartSocketsProtocol.DISCONNECT:
                if (vclogger.isInfoEnabled()) { 
                    vclogger.info("Connection got disconnect(" + opcode + ")!");
                }
                handleDisconnect(null);
                return false;
                
            case SmartSocketsProtocol.INFO_MESSAGE:
                if (meslogger.isInfoEnabled()) {
                    meslogger.info("HubConnection got info message!");
                }
                handleClientMessage();
                return true;
            
            case SmartSocketsProtocol.DATA_MESSAGE:
                if (meslogger.isInfoEnabled()) {
                    meslogger.info("HubConnection got data message!");
                }
                handleDataMessage();
                return true;
                
            default:
                // Ask the subclass to handle this opcode!
                return handleOpcode(opcode);  
            }
                        
        } catch (Exception e) {
            handleDisconnect(e);
        }
        
        return false;
    }
    
}

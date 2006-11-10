package smartsockets.hub.connections;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import smartsockets.direct.DirectSocket;
import smartsockets.direct.DirectSocketFactory;
import smartsockets.direct.SocketAddressSet;
import smartsockets.hub.HubProtocol;
import smartsockets.hub.state.ClientDescription;
import smartsockets.hub.state.HubDescription;
import smartsockets.hub.state.HubList;
import smartsockets.hub.state.StateCounter;
import smartsockets.hub.state.StateSelector;

public class HubConnection extends MessageForwardingConnection {

    protected static Logger conlogger = 
        ibis.util.GetLogger.getLogger("smartsockets.hub.connections.hub"); 
    
    protected static Logger goslogger = 
        ibis.util.GetLogger.getLogger("smartsockets.hub.gossip"); 
          
    private final HubDescription peer;
    private final HubDescription local;    
    
    // Keeps the current state of the system. 
    private final StateCounter state;        
                         
    // Indicates the value of the local state the last time any data was send 
    // to the peer. Remembering this allows us to send delta's. 
    private long lastSendState;    
            
    public HubConnection(DirectSocket s, DataInputStream in, 
            DataOutputStream out, HubDescription peer, 
            Map<SocketAddressSet, BaseConnection> connections, 
            HubList proxies, StateCounter state) {
        
        super(s, in, out, connections, proxies);
        
        this.peer = peer;        
        this.state = state;
        local = proxies.getLocalDescription();
    }
        
    public synchronized void setLastSendState() {
        lastSendState = state.get();
    }
    
    public synchronized void writeMessage(ClientMessage m) { 
        
        try {
            out.writeByte(HubProtocol.CLIENT_MESSAGE);            
            m.write(out);            
            out.flush();
        } catch (IOException e) {
            meslogger.warn("Unhandled exception in writeMessage!!", e);            
            // TODO: handle exception
        }        
    }
    
    public synchronized void gossip() { 
        
        long newSendState = state.get(); 

        if (goslogger.isInfoEnabled()) {             
            goslogger.info("Gossiping with: " + peer.hubAddress);
        }
        
        StateSelector ss = new StateSelector(lastSendState);
        
        knownHubs.select(ss);
        
        try {
            int writes = 0;

            for (HubDescription tmp : ss.getResult()) { 
                
                if (goslogger.isInfoEnabled()) { 
                    goslogger.info("    Writing proxy: " + tmp.hubAddressAsString);
                }
                
                if (goslogger.isDebugEnabled()) {
                    goslogger.debug("      since lastLocalUpdate="  
                            + tmp.getLastLocalUpdate()  
                            + " > lastSendState= " + lastSendState);
                }
                    
                writeHub(tmp);                    
                writes++;
            }        
            
            if (writes == 0) {
                // No proxies where written, so write a ping instead.                 
                writePing();
            } 
            
            out.flush();
            
        } catch (Exception e) {
            goslogger.warn("Unhandled exception in HubConnection!!", e);            
        }
        
        lastSendState = newSendState;
        
        peer.setContactTimeStamp(false);        
    }
    
    private void writePing() throws IOException {        
        out.write(HubProtocol.PING);
    } 
    
    private void writeHub(HubDescription d) throws IOException {        
        out.write(HubProtocol.GOSSIP);
        
        out.writeUTF(d.hubAddress.toString());
        out.writeUTF(d.getName());
        out.writeInt(d.getHops());
        
        if (d.isLocal()) { 
            out.writeLong(d.getLastLocalUpdate());
        } else { 
            out.writeLong(d.getHomeState());
        } 
        
        ArrayList<ClientDescription> clients = d.getClients(null);        
        
        out.writeInt(clients.size()); 

        for (ClientDescription c : clients) {
            c.write(out);
        }    
        
        String [] connectedTo = d.connectedTo();
        
        if (connectedTo == null || connectedTo.length == 0) {         
            out.writeInt(0);
            return;
        }  
          
        out.writeInt(connectedTo.length);
        
        for (String c : connectedTo) { 
            out.writeUTF(c);
        }    
    } 
        
    private void readProxy() throws IOException {
                
        SocketAddressSet address = new SocketAddressSet(in.readUTF());        
        String name = in.readUTF();
        
        HubDescription tmp = knownHubs.add(address);
               
        int hops = in.readInt();
        
        long state = in.readLong();
        
        int clients = in.readInt();        
        
        ClientDescription [] c = new ClientDescription[clients];
        
        for (int i=0;i<clients;i++) {
            c[i] = ClientDescription.read(in);                        
        }
        
        int conns = in.readInt();        
                
        String [] a = new String[conns];
        
        for (int i=0;i<conns;i++) {
            a[i] = in.readUTF();                        
        }
                        
        if (local.hubAddress.equals(address)) {
            // Just received information about myself!
            if (hops == 0) {
                peer.setCanReachMe();
            } else { 
                peer.setCanNotReachMe();
            }
        } else if (tmp == peer) {
            // The peer send information about itself. This should 
            // always be up-to-date.
            if (state > tmp.getHomeState()) { 
                tmp.update(c, a, name, state);
            } else { 
                goslogger.warn("EEK: got information directly from " 
                        + peer.hubAddressAsString 
                        + (name.length() > 0 ? (" (" + name + ")") : "") 
                        + " which seems to be out of date! " + state 
                        + " " + tmp.getHomeState());
            }
        } else {
            // We got information about a 'third party'.               
            if (hops+1 < tmp.getHops()) {
                // We seem to have found a shorter route to the target
                tmp.addIndirection(peer, hops+1);
            } 
            
            // Check if the information is more recent than what I know...
            if (state > tmp.getHomeState()) { 
                tmp.update(c, a, name, state);
            } else {
                String pn = peer.getName();
        
                if (goslogger.isDebugEnabled()) {
                    goslogger.debug("Ignoring outdated information about " 
                        + tmp.hubAddressAsString 
                        + (name.length() > 0 ? (" (" + name + ")") : "")
                        + " from " + peer.hubAddressAsString 
                        + (pn.length() > 0 ? (" (" + pn + ") ") : " ")                        
                        + state + " " 
                        + tmp.getHomeState());
                }
            }
        }
        
        peer.setContactTimeStamp(false);
    }
        
    private void handlePing() {
        if (goslogger.isInfoEnabled()) {
            goslogger.debug("Got ping from " + peer.hubAddress);
        }
        
        peer.setContactTimeStamp(false);
    }
  
    private void handleClientMessage() throws IOException {        
        ClientMessage cm = new ClientMessage(in);        
        
        if (meslogger.isDebugEnabled()) {
            meslogger.debug("Got message: " + cm);
        }
        
        forward(cm, false);          
    }
    
    protected String getName() { 
        return "HubConnection(" + peer.hubAddress + ")";
    }
    
    private void disconnect() {
                
        // Update the administration
        connections.remove(peer.hubAddress);        
        local.removeConnectedTo(peer.hubAddressAsString);
        
        DirectSocketFactory.close(s, out, in);            
    } 
    
    protected boolean runConnection() {
    
        try { 
            int opcode = in.read();
            
            switch (opcode) { 
        
            case -1:
                if (conlogger.isInfoEnabled()) { 
                    conlogger.info("HubConnection got EOF!");
                }
                disconnect();
                return false;
                
            case HubProtocol.GOSSIP:
                if (goslogger.isInfoEnabled()) {
                    goslogger.info("HubConnection got gossip!");
                }
                readProxy();
                return true;
    
            case HubProtocol.PING:
                if (goslogger.isInfoEnabled()) {
                    goslogger.info("HubConnection got ping!");
                }
                handlePing();
                return true;

            case HubProtocol.CLIENT_MESSAGE:
                if (meslogger.isInfoEnabled()) {
                    meslogger.info("HubConnection got message!");
                }
                handleClientMessage();
                return true;
               
            default:
                conlogger.warn("HubConnection got junk!");                   
                disconnect();
                return false;
            }
                        
        } catch (Exception e) {
            conlogger.warn("HubConnection got exception!", e);
            disconnect();
        }
        
        return false;
    }
}

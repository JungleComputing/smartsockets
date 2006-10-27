package smartsockets.hub;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import smartsockets.direct.DirectServerSocket;
import smartsockets.direct.DirectSocket;
import smartsockets.direct.DirectSocketFactory;
import smartsockets.direct.SocketAddressSet;
import smartsockets.hub.connections.ClientConnection;
import smartsockets.hub.connections.Connections;
import smartsockets.hub.connections.HubConnection;
import smartsockets.hub.state.HubDescription;
import smartsockets.hub.state.HubList;
import smartsockets.hub.state.StateCounter;

public class Acceptor extends CommunicationThread {
    
    protected static Logger hconlogger = 
        ibis.util.GetLogger.getLogger("smartsockets.hub.connections.hub"); 
    
    protected static Logger cconlogger = 
        ibis.util.GetLogger.getLogger("smartsockets.hub.connections.client"); 
    
    protected static Logger reglogger = 
        ibis.util.GetLogger.getLogger("smartsockets.hub.registration"); 
    
    protected static Logger reqlogger = 
        ibis.util.GetLogger.getLogger("smartsockets.hub.request"); 
    
    
    private DirectServerSocket server;
    private boolean done = false;

    private class SpliceInfo { 
        
        String connectID;
        long bestBefore; 
        
        DirectSocket s;
        DataInputStream in;
        DataOutputStream out;

    }
    
    private long nextInvalidation = -1;
   
    private HashMap spliceInfo = new HashMap();
        
    Acceptor(int port, StateCounter state, Connections connections, 
            HubList knownProxies, DirectSocketFactory factory) 
            throws IOException {

        super("HubAcceptor", state, connections, knownProxies, factory);        

        server = factory.createServerSocket(port, 50, null);        
        setLocal(server.getAddressSet());
        
        System.err.println("Hub listnening at: " + localAsString);
    }

    private boolean handleIncomingHubConnect(DirectSocket s, 
            DataInputStream in, DataOutputStream out) throws IOException { 

        String otherAsString = in.readUTF();        
        SocketAddressSet addr = new SocketAddressSet(otherAsString); 

        hconlogger.debug("Got connection from " + addr);

        HubDescription d = knownHubs.add(addr);        
        d.setCanReachMe();

        HubConnection c = 
            new HubConnection(s, in, out, d, connections, knownHubs, state);

        if (!d.createConnection(c)) { 
            // There already was a connection with this hub...            
            hconlogger.info("Connection from " + addr + " refused (duplicate)");

            out.write(HubProtocol.CONNECTION_REFUSED);
            out.flush();
            return false;
        } else {                         
            // We just created a connection to this hub.
            hconlogger.info("Connection from " + addr + " accepted");

            out.write(HubProtocol.CONNECTION_ACCEPTED);            
            out.flush();

            // Now activate it. 
            c.activate();
            
            connections.addConnection(addr, c);
            return true;
        }     
    }

    private boolean handlePing(DirectSocket s, 
            DataInputStream in, DataOutputStream out) throws IOException {

        String sender = in.readUTF();         
        //logger.info("Got ping from: " + sender);      
        return false;
    }    

    private boolean handleServiceLinkConnect(DirectSocket s, DataInputStream in,
            DataOutputStream out) {

        try { 
            String src = in.readUTF();
            
            SocketAddressSet srcAddr = new SocketAddressSet(src);
                        
            if (connections.getConnection(srcAddr) != null) { 
                if (cconlogger.isDebugEnabled()) { 
                    cconlogger.debug("Incoming connection from " + src + 
                    " refused, since it already exists!"); 
                } 

                out.write(HubProtocol.SERVICELINK_REFUSED);
                out.flush();
                DirectSocketFactory.close(s, out, in);
                return false;
            }

            if (cconlogger.isDebugEnabled()) { 
                cconlogger.debug("Incoming connection from " + src + " accepted"); 
            } 

            out.write(HubProtocol.SERVICELINK_ACCEPTED);
            out.writeUTF(server.getAddressSet().toString());            
            out.flush();

            ClientConnection c = new ClientConnection(srcAddr, s, in, out, 
                    connections, knownHubs);
            
            connections.addConnection(srcAddr, c);                                               
            c.activate();

            knownHubs.getLocalDescription().addClient(srcAddr);
            
            reglogger.info("Added client: " + src);            
            
            return true;

        } catch (IOException e) { 
            cconlogger.warn("Got exception while handling connect!", e);
            DirectSocketFactory.close(s, out, in);
        }  

        return false;
    }

    private boolean handleBounce(DirectSocket s, DataInputStream in, 
            DataOutputStream out) throws IOException {
        
        out.write(s.getLocalAddress().getAddress());
        out.flush();
        
        return false;
    }
        
    private boolean handleSpliceInfo(DirectSocket s, DataInputStream in, 
            DataOutputStream out) throws IOException {

        boolean result = false;
        
        String connectID = in.readUTF();
        int time = in.readInt();
        
        reqlogger.info("Got request for splice info: " + connectID + " " + time);
        
        SpliceInfo info = (SpliceInfo) spliceInfo.remove(connectID);
        
        if (info == null) {
            
            reqlogger.info("Request " + connectID + " is first");
                        
            // We're the first...            
            info = new SpliceInfo();
            
            info.connectID = connectID;
            info.s = s;
            info.out = out;
            info.in = in;             
            info.bestBefore = System.currentTimeMillis() + time;
            
            spliceInfo.put(connectID, info);
            
            if (nextInvalidation == -1 || info.bestBefore < nextInvalidation) { 
                nextInvalidation = info.bestBefore;
            }            
            
            // Thats it for now. Return true so the connection is kept open 
            // until the other side arrives...
            result = true;        
        } else {
            reqlogger.info("Request " + connectID + " is second");            
            
            // The peer is already waiting...
            
            // We now echo the essentials of the two connection that we see to 
            // each client. Its up to them to decide what to do with it....
            
            try {                               
                InetSocketAddress tmp = 
                    (InetSocketAddress) s.getRemoteSocketAddress();

                info.out.writeUTF(tmp.getAddress().toString());
                info.out.writeInt(tmp.getPort());
                info.out.flush();
                
                reqlogger.info("Reply to first " + tmp.getAddress() + ":" 
                        + tmp.getPort()); 
                
                tmp = (InetSocketAddress) info.s.getRemoteSocketAddress();

                out.writeUTF(tmp.getAddress().toString());
                out.writeInt(tmp.getPort());
                out.flush();

                reqlogger.info("Reply to second " + tmp.getAddress() + ":" 
                        + tmp.getPort()); 
                
            } catch (Exception e) {                
                // The connections may have been closed already....
                reqlogger.info("Failed to forward splice info!", e);               
            } finally { 
                // We should close the first connection. The second will be 
                // closed for us when we return false
                DirectSocketFactory.close(info.s, info.out, info.in);
            }            
        }
        
        // Before we return, we do some garbage collection on the spliceInfo map
        if (nextInvalidation != -1) {  

            long now = System.currentTimeMillis();
              
            if (now >= nextInvalidation) { 

                nextInvalidation = Long.MAX_VALUE;
                
                // Traverse the map, removing all entries which are out of date, 
                // and recording the next time at which we should do a 
                // traversal.  
                Iterator itt = spliceInfo.keySet().iterator();
                
                while (itt.hasNext()) { 
                    String key = (String) itt.next();                   
                    SpliceInfo tmp = (SpliceInfo) spliceInfo.get(key);
                    
                    if (tmp.bestBefore < now) { 
                        spliceInfo.remove(key);
                    } else { 
                        if (tmp.bestBefore < nextInvalidation) { 
                            nextInvalidation = tmp.bestBefore;
                        } 
                    } 
                }

                if (spliceInfo.size() == 0) { 
                    nextInvalidation = -1;
                }
            } 
        } 

        return result;
    }
    
    private void doAccept() {

        DirectSocket s = null;
        DataInputStream in = null;
        DataOutputStream out = null;
        boolean result = false;

        hublogger.debug("Waiting for connection...");
        
        try {
            s = server.accept();                
            in = new DataInputStream(
                    new BufferedInputStream(s.getInputStream()));

            out = new DataOutputStream(
                    new BufferedOutputStream(s.getOutputStream()));

            int opcode = in.read();

            switch (opcode) {
            case HubProtocol.CONNECT:
                result = handleIncomingHubConnect(s, in, out);                   
                break;

            case HubProtocol.PING:                
                result = handlePing(s, in, out);                   
                break;
              
            case HubProtocol.SERVICELINK_CONNECT:
                result = handleServiceLinkConnect(s, in, out);
                break;                

            case HubProtocol.BOUNCE_IP:
                result = handleBounce(s, in, out);
                break;                
            
            case HubProtocol.GET_SPLICE_INFO:
                result = handleSpliceInfo(s, in, out);
                break;                
                            
            default:
                break;
            }
        } catch (Exception e) {
            hublogger.warn("Failed to accept connection!", e);
            result = false;
        }

        if (!result) { 
            DirectSocketFactory.close(s, out, in);
        }   
    }

    public void run() { 

        while (!done) {           
            doAccept();            
        }
    }       
}

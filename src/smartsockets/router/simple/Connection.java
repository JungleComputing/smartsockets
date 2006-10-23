/**
 * 
 */
package smartsockets.router.simple;


import ibis.util.ThreadPool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import smartsockets.direct.SocketAddressSet;
import smartsockets.hub.servicelink.ClientInfo;
import smartsockets.util.Forwarder;
import smartsockets.util.ForwarderCallback;
import smartsockets.virtual.VirtualSocket;
import smartsockets.virtual.VirtualSocketAddress;
import smartsockets.virtual.VirtualSocketFactory;

class Connection implements Runnable, Protocol, ForwarderCallback {       

    private final Router parent;
    
    private final String localID;
    
    private String from;
    private String to;
    private long linkID;
            
    private VirtualSocket socketToClient;
    private DataOutputStream outToClient;
    private DataInputStream inFromClient;    
    
    private VirtualSocket socketToTarget;
    private DataOutputStream outToTarget;
    private DataInputStream inFromTarget;
    
    private String label1;    
    private String label2;    
    
    private Forwarder forwarder1;
    private Forwarder forwarder2;
        
    private int forwarderDone = 0;    
   
    private long time;
    private long bytes;
    
    Connection(VirtualSocket sc, String ID, Router parent) throws IOException {
        
        Router.logger.debug("Created new connection to " + sc);
        
        this.localID = ID;        
        this.socketToClient = sc;
        this.parent = parent;
        
        // No extra buffer here, since we will mostly send byte[]
        outToClient = new DataOutputStream(sc.getOutputStream());
        inFromClient = new DataInputStream(sc.getInputStream());
    }
    
    
    private boolean connect(VirtualSocketAddress target, long timeout) {  
    
        try { 
            socketToTarget = parent.connect(target, timeout);
            outToTarget = new DataOutputStream(socketToTarget.getOutputStream());
            inFromTarget = new DataInputStream(socketToTarget.getInputStream());            
            to = target.machine().toString();                        
            return true; 
        } catch (IOException e) {
            Router.logger.debug("Failed to create connection to " + target);
            VirtualSocketFactory.close(socketToTarget, outToTarget, inFromTarget);
            return false;
        }
    }
       
    private boolean connectViaRouter(ClientInfo router, 
            VirtualSocketAddress target, long timeout) {

        boolean succes = false;
        
        try {
            Router.logger.debug("Connecting to router: " + router);
                        
            VirtualSocketAddress r = router.getPropertyAsAddress("router");            
           
            if (connect(r, timeout)) {
                Router.logger.debug("Connection to router created!!");
                Router.logger.debug("Forwarding target: " + target.toString());
                                                
                outToTarget.writeUTF(parent.getLocalAddress().toString());                                
                outToTarget.writeUTF(target.toString());                
                outToClient.writeLong(linkID);                
                outToTarget.writeLong(timeout);
                outToTarget.flush();

                Router.logger.debug("Waiting for router reply...");
                        
                // TODO set timeout!!!
                int result = inFromTarget.readByte();
                
                switch (result) {
                case REPLY_OK:
                    Router.logger.debug("Connection setup succesfull!");            
                    succes = true;
                    break;

                case REPLY_FAILED:
                    Router.logger.debug("Connection setup failed!");
                    break;
                
                default:
                    Router.logger.debug("Connection setup returned junk!");
                }
            }            
        } catch (IOException e) {
            Router.logger.debug("Connection setup resulted in exception!", e);
        }
            
        if (!succes) { 
            VirtualSocketFactory.close(socketToTarget, outToTarget, inFromTarget);
        }
        
        return succes;
    }

    
    private boolean connectToTarget(VirtualSocketAddress target, long timeout, 
            SocketAddressSet [] directions) { 
                        
        // Check if the target is local to my proxy. If so, my local proxy 
        // address should be in the first entry of the directions array.
        SocketAddressSet proxy = parent.getHubAddress();

        if (proxy == null) { 
            Router.logger.debug("Cannot forward connection: failed to get " +
                    "local proxy address!");
            return false;
        }
        
        int startIndex = 0;
        
        if (directions[0].equals(proxy)) {
             
            Router.logger.debug("Client is local to my proxy!!");
                        
            if (connect(target, timeout)) { 
                Router.logger.debug("Created local connection to client " 
                        + target);
                return true;
            }
            
            startIndex++;
            
        } else { 
            Router.logger.debug("Client is NOT local to my proxy!!");
            Router.logger.debug("I am         : " + proxy);
            Router.logger.debug("First hop is : " + directions[0]);
        }
          
        Router.logger.debug("Directions to client machine: "); 

        for (int i=0;i<directions.length;i++) {
            Router.logger.debug(" " + i + " : " + directions[i]);
        }
        
        // The target wasn't local. We now traverse each of the proxies one by
        // one, and check if we can find any router processes associated with 
        // them. If there are, we try to connect to the target via one of these
        // routers...        
        for (int i=startIndex;i<directions.length;i++) {
            
            ClientInfo [] result = null;
            
            try { 
                result = parent.findClients(directions[i], "router");
            } catch (Exception e) {
                Router.logger.debug("Failed to contact proxy!", e);                                
            }

            if (result == null || result.length == 0) { 
                Router.logger.debug("Proxy " + directions[i] + " does not "
                        + "know any routers!");                    
            } else { 
                Router.logger.debug("Proxy " + directions[i] + " knows the"
                        + " following routers:");
                    
                for (int x=0;x<result.length;x++) { 
                    Router.logger.debug(" " + i + " - " + result[x]);                            
                }
                
                for (int x=0;x<result.length;x++) {
                    
                    // TODO: should we change the target address to include the
                    // new proxy ?
                    
                    if (connectViaRouter(result[i], target, timeout)) { 
                        return true;                        
                    }
                } 
            } 
        }
        
        return false;
    }
       
    private boolean connectToTarget() throws IOException { 
         
        boolean succes = false;
        
        Router.logger.debug("Connection " + socketToClient + " waiting for opcode");
               
        try {
            from = inFromClient.readUTF();            
            String dest = inFromClient.readUTF();                        
            linkID = inFromClient.readLong();            
            
            VirtualSocketAddress target = new VirtualSocketAddress(dest);
            
            long timeout = inFromClient.readLong();
        
            Router.logger.debug("Connection " + socketToClient + " got:");
            Router.logger.debug("     source : " + from);
            Router.logger.debug("     dest   : " + dest);            
            Router.logger.debug("     timeout: " + timeout);

            SocketAddressSet machine = target.machine();
            SocketAddressSet proxy = target.hub();
            
            if (proxy != null) { 
                SocketAddressSet [] dir = new SocketAddressSet [] { proxy };                 
                succes = connectToTarget(target, timeout, dir);
         
                if (!succes) { 
                    Router.logger.warn("Connection " + socketToClient + " failed"
                            + " to connect to: " + machine + "@" + proxy);         
                }
            }
            
            if (!succes) {                
                // Failed to connect to target. The proxy was invalid or null.
                SocketAddressSet [] directions = parent.locateMachine(machine);
            
                if (directions == null || directions.length == 0) { 
                    Router.logger.warn("Connection " + socketToClient + " failed"
                            + " to locate machine: " + machine + "@" + proxy);
                } else { 
                    succes = connectToTarget(target, timeout, directions);                
                }
            }
            
        } catch (Exception e) {
            Router.logger.debug("Got exception in connect: " + e);
        }
        
        Router.logger.debug("Connection " + socketToClient + " setup succes: " + succes);
        
        if (succes) {         
            outToClient.write(REPLY_OK);
        } else { 
            outToClient.write(REPLY_FAILED);
        } 

        outToClient.flush();
        
        return succes;        
    }
                
    public void done(String label) {
        Router.logger.info("Received callback for forwarder " + label);
        
        // Check which forwarder thread produced the callback. Should be a 
        // real reference to label, so we don't have to use "equals".
        if (label == label1) {
            try { 
                outToTarget.flush();
                socketToTarget.shutdownOutput();            
                socketToClient.shutdownInput();
            } catch (Exception e) {
                Router.logger.warn("Failed to properly shutdown " + label1, e);                    
            }
        }
        
        if (label == label2) {
            try { 
                outToClient.flush();
                socketToClient.shutdownOutput();            
                socketToTarget.shutdownInput();
            } catch (Exception e) {
                Router.logger.warn("Failed to properly shutdown " + label2, e);
            }
        }
    
        synchronized (this) {          
            forwarderDone++; 
        
            if (forwarderDone == 2) { 
                Router.logger.info("Removing connections since they are done!");
                
                VirtualSocketFactory.close(socketToClient, outToClient, inFromClient);
                VirtualSocketFactory.close(socketToTarget, outToTarget, inFromTarget);
                
                parent.done(localID);                
            } else { 
                Router.logger.info("Cannot remove connections yet!");
            }
        }         
    }

    private void startForwarders() { 
        
        label1 = "[" 
            + socketToClient.getRemoteSocketAddress() 
            + ":" 
            + socketToClient.getPort() 
            + " --> " 
            + to 
            + ":" 
            + socketToTarget.getPort() 
            + "]";
        
        label2 = "[" 
            + to 
            + ":" 
            + socketToTarget.getPort()         
            + " --> " 
            + socketToClient.getRemoteSocketAddress() 
            + ":"
            + socketToClient.getPort()         
            + "]";
    
        forwarder1 = new Forwarder(inFromClient, outToTarget, this, label1); 
        forwarder2 = new Forwarder(inFromTarget, outToClient, this, label2); 
                
        ThreadPool.createNew(forwarder1, "ForwarderThread" + label1);        
        ThreadPool.createNew(forwarder2, "ForwarderThread" + label2);
    }
    
    public long getThroughput() {      

        // returns the throughput in bytes/s since the last request...        
        if (time == 0 || forwarder1 == null || forwarder2 == null) { 
            time = System.currentTimeMillis();
            return 0;            
        }
        
        long currTime = System.currentTimeMillis();         
        long currBytes = forwarder1.getBytes() + forwarder2.getBytes();
        
        long deltaT = (currTime - time) / 1000;
        
        if (deltaT == 0) { 
            return 0;
        } 

        long result = (((currBytes - bytes) * 8)) / deltaT;
        
        bytes = currBytes;
        time = currTime;
        
        return result;        
    } 
    
    public String from() { 
        return from;
    }
    
    public String to() { 
        return to;
    }    
    
    public long linkID() { 
        return linkID;
    }
    
    public void run() { 
        
        try {            
            if (!connectToTarget()) {
                System.err.println("Failed to setup connection for " + socketToClient);
                VirtualSocketFactory.close(socketToClient, outToClient, inFromClient);
                return;
            }              

            // We have a connnection, so start forwarding!!
            startForwarders();            
            parent.add(localID, this);            
        } catch (Exception e) {
            // TODO: handle exception
            VirtualSocketFactory.close(socketToClient, outToClient, inFromClient);
            e.printStackTrace();
        }
    }
    
    public String toString() { 
        return "Connection from " + socketToClient; 
    }

   
    

}
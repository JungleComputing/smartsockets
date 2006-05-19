package ibis.connect.gossipproxy;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ibis.connect.virtual.VirtualServerSocket;
import ibis.connect.virtual.VirtualSocket;
import ibis.connect.virtual.VirtualSocketAddress;
import ibis.connect.virtual.VirtualSocketFactory;

public class GossipProxyClient extends Thread {
  /*
    protected static Logger logger = 
        ibis.util.GetLogger.getLogger(GossipProxyClient.class.getName());
    
    private static final int DEFAULT_PORT = 17877;
    private static final int DEFAULT_TIMEOUT = 1000;
    private static final HashMap CONNECT_PROPERTIES = new HashMap();    
    
    private VirtualSocketFactory factory;
    private VirtualServerSocket server;
    
    private HashMap knownProxies = new HashMap();
    private List reachableProxies = new ArrayList();
    
    private boolean done = false;
  
    private class ProxyInfo { 
        final VirtualSocketAddress address; 
        boolean reachable; 
        boolean canReachMe;
        
        ProxyInfo(VirtualSocketAddress address, boolean reachable, boolean canReachMe) { 
            this.address = address;
            this.reachable = reachable;
            this.canReachMe = canReachMe;
        }
    }
        
    public GossipProxyClient() throws IOException {
                
        CONNECT_PROPERTIES.put("allowed.modules", "direct");
        
        factory = VirtualSocketFactory.getSocketFactory();
        
        server = 
            factory.createServerSocket(DEFAULT_PORT, 50, CONNECT_PROPERTIES);
        
        logger.info("Created GossipProxyClient on " + server);
    }
        
    public GossipProxyClient(VirtualSocketAddress proxy) throws IOException {         
        this();        
        addProxy(proxy);
    }
        
    public boolean addProxy(VirtualSocketAddress proxy) {

        logger.info("Adding proxy " + proxy);
        
        // Check if the proxy is already known
        if (knownProxies.containsKey(proxy)) {
            logger.info("Proxy " + proxy + " already known!");
            return true;
        }

        ProxyInfo info = new ProxyInfo(proxy, false, false);
        
        // Add the candidate to the list known proxies
        synchronized (this) {         
            knownProxies.put(proxy, info);
        } 
        
        // Otherwise, try to set up a connection
        try { 
            VirtualSocket s = factory.createClientSocket(proxy, DEFAULT_TIMEOUT,
                    CONNECT_PROPERTIES);
            
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            
            out.write(Protocol.CLIENT_REQUEST_CONNECT);
            out.writeUTF(server.getLocalSocketAddress().toString());
            out.flush();            
            out.close();            
            s.close();
                        
        } catch (IOException e) {           
            logger.warn("Could not contact Proxy " + proxy, e);
            return false;
        }
        
        synchronized (this) {
            // Could reach the machine, so update the proxy info
            info.reachable = true;
            reachableProxies.add(info);
        }            
    }
    
    
    private void handleProxyTestConnect(VirtualSocket s, DataInputStream in) 
        throws IOException { 
        
        VirtualSocketAddress proxy = new VirtualSocketAddress(in.readUTF()); 
        
        logger.info("Got connection test from " + proxy);
        
        

        
        
    }
    
    public void run() { 
        
        while (!done) { 
            try {
                VirtualSocket s = server.accept();                
                DataInputStream in = new DataInputStream(s.getInputStream());
                
                int opcode = in.read();
                
                switch (opcode) {
                case Protocol.PROXY_TEST_CONNECT:
                    handleProxyTestConnect(s, in);
                    
                    break;
                default:
                    break;
                }
                    
                
                
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
           
        
        
    }
    */
}


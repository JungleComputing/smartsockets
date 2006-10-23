package smartsockets.router.simple;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import smartsockets.virtual.VirtualSocket;
import smartsockets.virtual.VirtualSocketAddress;
import smartsockets.virtual.VirtualSocketFactory;

public class RouterClient implements Protocol {

    private static Logger logger = 
        ibis.util.GetLogger.getLogger(RouterClient.class.getName());
    
    private static long clientID = 0;
    private static HashMap properties;       
    private static VirtualSocketFactory factory;     
    
    public final long id;
    public final VirtualSocket s;
    public final DataOutputStream out;
    public final DataInputStream in;   
               
    RouterClient(long id, VirtualSocket s, DataOutputStream out, DataInputStream in) {
        this.id = id;
        this.s = s;        
        this.out = out;
        this.in = in;
    }
    
    public VirtualSocket connectToClient(VirtualSocketAddress target, 
            int timeout) throws IOException {
                        
        logger.info("Sending connect request to router!");
        
        out.writeUTF(factory.getLocalHost().toString());                
        out.writeUTF(target.toString());             
        out.writeLong(id);
        out.writeLong(timeout);
        out.flush();

        logger.info("Waiting for router reply...");
                
        // TODO set timeout!!!
        int result = in.readByte();
        
        switch (result) {
        case REPLY_OK:
            logger.info("Connection setup succesfull!");            
            return s;

        case REPLY_FAILED:
            logger.info("Connection setup failed!");
            return null;
        
        default:
            logger.info("Connection setup returned junk!");
            return null;
        }
    }
               
    public static synchronized long getID() { 
        return clientID++;
    }
    
    public static RouterClient connectToRouter(VirtualSocketAddress router, 
            int timeout) throws IOException {
        
        if (properties == null) {            
            logger.info("Initializing client-side router code");
            
            properties = new HashMap();
            properties.put("connect.module.skip", "routed");            
            factory = VirtualSocketFactory.getSocketFactory();          
        }
        
        VirtualSocket s = null;
        DataOutputStream out = null;
        DataInputStream in = null;   
            
        try { 
            s = factory.createClientSocket(router, timeout, properties);
            out = new DataOutputStream(s.getOutputStream());
            in = new DataInputStream(s.getInputStream());                                   
        } catch (IOException e) {
            logger.info("Failed to connect to router at " + router);
            VirtualSocketFactory.close(s, out, in);
            throw e;
        }
        
        return new RouterClient(getID(), s, out, in);
    }
}
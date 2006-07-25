package test.router;

import ibis.connect.router.simple.Router;
import ibis.connect.router.simple.RouterClient;
import ibis.connect.virtual.VirtualServerSocket;
import ibis.connect.virtual.VirtualSocket;
import ibis.connect.virtual.VirtualSocketAddress;
import ibis.connect.virtual.VirtualSocketFactory;

import java.io.IOException;
import java.net.UnknownHostException;

public class RouterTest {
    
    private static long timeout = 60000;
    private static boolean startRouter = false;
    private static boolean startClient = false;
    private static boolean serverSide  = false;
        
    private static VirtualSocketFactory factory;
    private static VirtualSocketAddress routerAddress;
    private static VirtualSocketAddress serverAddress;     
            
    private static void parseOptions(String [] args) { 
    
        int index = 0;
        
        while (index < args.length) { 
         
            if (args[index].equals("-router")) { 
                startRouter = true;
            } else if (args[index].equals("-client")) { 
                startClient = true;
            } else if (args[index].equals("-server")) { 
                serverSide = true;
            } else if (args[index].equals("-targetRouter")) { 
                try {
                    routerAddress = new VirtualSocketAddress(args[++index]);
                } catch (UnknownHostException e) {
                    System.out.println("Failed to parse router address " + e);
                }
            } else if (args[index].equals("-timeout")) { 
                timeout = Integer.parseInt(args[++index]);
            } else if (args[index].equals("-targetServer")) { 
                try { 
                    serverAddress = new VirtualSocketAddress(args[++index]);
                } catch (Exception e) {
                    System.out.println("Failed to parse server address " + e);
                }
            }              
            
            index++;
        }        
    }
        
    public static void main(String [] args) {
    
        Router r = null;
        
        parseOptions(args);
                
        factory = VirtualSocketFactory.getSocketFactory();
        
        if (startRouter) { 
            try {
                r = new Router();
                r.start();                
            } catch (IOException e) {
                System.out.println("Failed to start router: " + e);
                e.printStackTrace();
                System.exit(1);
            }
        } 

        if (startRouter && routerAddress == null) { 
            // use local router
            routerAddress = r.getAddress();            
        }
   
        if (startClient && routerAddress == null) {
            System.out.println("Cannot start client without router address!");
            System.exit(1);        
        }

        if (startClient && serverAddress== null) {
            System.out.println("Cannot start client without server address!");
            System.exit(1);        
        }

        try {
            if (serverSide) {
            
                VirtualServerSocket ss = factory.createServerSocket(5555, 1, null);
                
                System.out.println("Server accepting at " + ss);
                                
                VirtualSocket s = ss.accept();
            
                System.out.println("Server got connection from " + s);
                VirtualSocketFactory.close(s, null, null);
                                
            } else if (startClient) {         
                
                RouterClient c = RouterClient.connect(routerAddress);
         
                if (c.connect(serverAddress, timeout)) { 
                    System.out.println("Connected to " + serverAddress);                                
                } else { 
                    System.out.println("Failed to connected to " + serverAddress);
                }
                    
                c.close();
            }            
                
        } catch (Exception e) {
            System.err.println("Oops: " + e);
            e.printStackTrace(System.err);
        } 
    }
}

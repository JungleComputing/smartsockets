package ibis.smartsockets.hub2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.direct.DirectSocketFactory;

abstract class CommunicationThread implements Runnable {

    protected static final int DEFAULT_TIMEOUT = 5000;
       
    protected final String name;
    
//    protected final StateCounter state;         
    
    protected static final Logger hublogger = 
        LoggerFactory.getLogger("ibis.smartsockets.hub"); 
        
    protected final Connections connections;     
    
//    protected final HubList knownHubs;
 
    protected final DirectSocketFactory factory;    
    
    protected DirectSocketAddress local;
    protected String localAsString;
    protected Thread thread;
    
    private boolean end = false;
    
    protected CommunicationThread(String name, Connections connections, DirectSocketFactory factory) {

        this.name = name;
//        this.state = state;
        this.connections = connections;
//        this.knownHubs = knownHubs;
        this.factory = factory;        
    }
    
    protected void setLocal(DirectSocketAddress local) { 
        this.local = local;
        this.localAsString = local.toString();        
    }
    
    protected DirectSocketAddress getLocalAddress() {
        return local;
    }
    
    protected String getLocalAddressAsString() {
        return localAsString;
    }
    
    protected synchronized boolean getDone() { 
        return end;
    }
    
    public synchronized void end() { 
        end = true;
    
        try { 
            thread.interrupt();
        } catch (Exception e) {
            // ignore ? 
        }
    }
    
    public void activate() {        
        // thread = ThreadPool.createNew(this, name);        
        thread = new Thread(this, name);
        thread.setDaemon(true);
        thread.start();        
    }
}

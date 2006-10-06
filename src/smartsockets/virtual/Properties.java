package smartsockets.virtual;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import smartsockets.util.TypedProperties;


public class Properties {

    public static final String DEFAULT_FILE = "smartsockets.properties";
    
    public static final String PREFIX = "smartsockets.";
    
    public static final String FILE    = PREFIX + "file";         
    public static final String PROXY   = PREFIX + "proxy";    
    public static final String ROUTERS = PREFIX + "routers";  
        
    public static final String BACKLOG = PREFIX + "backlog";  
    public static final String TIMEOUT = PREFIX + "timeout";  
    
    public static final String MODULES_PREFIX = PREFIX + "modules.";
    public static final String MODULES_DEFINE = MODULES_PREFIX + "define";
    public static final String MODULES_ORDER  = MODULES_PREFIX + "order";
    public static final String MODULES_SKIP   = MODULES_PREFIX + "skip";
    
    public static final String DISCOVERY_PREFIX  = PREFIX + "discovery.";
    public static final String DISCOVERY_PORT    = DISCOVERY_PREFIX + "port";
    public static final String DISCOVERY_TIMEOUT = DISCOVERY_PREFIX + "timeout";
    
    public static final String CLUSTER_PREFIX = PREFIX + "cluster.";
    public static final String CLUSTER_DEFINE = CLUSTER_PREFIX + "define";
    public static final String CLUSTER_MEMBER = CLUSTER_PREFIX + "member";
    
    // NOTE: These properties require a which can only be determined at runtime.  
    public static final String CLUSTER_PREFERENCE = "preference.";    
    public static final String CLUSTER_MEMBERSHIP = "preference.membership";
    public static final String CLUSTER_INSIDE     = "preference.inside";
    public static final String CLUSTER_DEFAULT    = "preference.default";
    
    private static final String [] defaults = new String [] {
            BACKLOG,           "20", 
            TIMEOUT,           "1000", 
            
            MODULES_DEFINE,    "direct,reverse,splice,routed", 
            MODULES_ORDER,     "direct,reverse,splice,routed",
            
            DISCOVERY_PORT,    "24545", 
            DISCOVERY_TIMEOUT, "5000",             
    };

    
    private static TypedProperties defaultProperties;

    protected static Logger logger =         
            ibis.util.GetLogger.getLogger("smartsocket.properties");
    
    private static TypedProperties getPropertyFile(String file) {
        
        InputStream in = null;

        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.info("Property file \"" + file + "\" not found!");
        }
                
        if (in == null) {

            ClassLoader loader = ClassLoader.getSystemClassLoader();

            in = loader.getResourceAsStream(file);

            if (in != null) {
                logger.info("Found property file in classpath: \""
                        + loader.getResource(file) + "\"");
            } else {
                logger.info("Property file \"" + file + "\" not found "
                        + "in classpath, giving up!");
                return null;
            }
        }

        try {
            TypedProperties p = new TypedProperties();
            p.load(in);

            return p;
        } catch (IOException e) {
            logger.warn("Error while loading property file: " + file, e);

            try {
                in.close();
            } catch (Exception x) {
                // ignore
            }
        }
        
        return null;
    }
    
    
    public static TypedProperties getDefaultProperties() {
        
        if (defaultProperties == null) { 
            defaultProperties = new TypedProperties();

            // Start by inserting the default values.            
            for (int i=0;i<defaults.length;i+=2) { 
                defaultProperties.put(defaults[i], defaults[i+1]);            
            }
      
            // Get the smartsockets related properties from the commandline. 
            TypedProperties system = 
                new TypedProperties(System.getProperties()).filter(PREFIX);

            // Check what property file we should load.
            String file = system.getProperty(FILE, DEFAULT_FILE); 

            // If the file is not explicitly set to null, we try to load it.
            if (file != null) {
                
                TypedProperties fromFile = getPropertyFile(file);
                
                if (fromFile == null && !file.equals(DEFAULT_FILE)) { 
                    // If we fail to load the user specified file, we give an
                    // error, since only the default file may fail silently.                     
                    logger.error("User specified preferences \"" + file 
                            + "\" not found!");                                            
                } else {                  
                    // If we managed to load the file, we add the properties to 
                    // the 'defaultProperties' possibly overwriting defaults.
                    defaultProperties.putAll(fromFile);
                }
            }
            
            // Finally, add the smartsockets related properties from the command
            // line to the result, possibly overriding entries fromfile or the 
            // defaults.            
            defaultProperties.putAll(system);
        } 
        
        return defaultProperties;        
    }
}

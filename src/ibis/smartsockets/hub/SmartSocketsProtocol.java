package ibis.smartsockets.hub;

public interface SmartSocketsProtocol {

    // Basic connect opcodes
	public static final byte HUB_CONNECT             = 1;       
    public static final byte SERVICELINK_CONNECT     = 2;       
    public static final byte DISCONNECT              = 5;
    
    // Basic connect replies
    public static final byte CONNECTION_ACCEPTED     = 3;    
    public static final byte CONNECTION_REFUSED      = 4;

    // Misc. opcodes
    public static final byte PING                    = 7;
    public static final byte GET_SPLICE_INFO         = 8;
    public static final byte GOSSIP                  = 20;
    public static final byte DATA_MESSAGE            = 68;
    public static final byte INFO_MESSAGE            = 69;
  
    // Registration of client property opcodes -- Used by servicelink 
    public static final byte REGISTER_PROPERTY = 30;
    public static final byte UPDATE_PROPERTY   = 31;
    public static final byte REMOVE_PROPERTY   = 32;
    
    public static final byte PROPERTY_ACK      = 33;    
    public static final byte PROPERTY_ACCEPTED = 34;
    public static final byte PROPERTY_REJECTED = 35;
    
    // Client info request opcodes -- Used by servicelink
    public static final byte HUBS              = 40;
    public static final byte HUB_FOR_CLIENT    = 41;    
    public static final byte CLIENTS_FOR_HUB   = 42;
    public static final byte ALL_CLIENTS       = 43;
    public static final byte HUB_DETAILS       = 44;    
    public static final byte DIRECTION         = 45; 
    public static final byte INFO_REPLY        = 49;
}

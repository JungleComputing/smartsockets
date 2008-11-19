package ibis.smartsockets.hub2.state;

import ibis.smartsockets.direct.DirectSocketAddress;

import java.util.UUID;

public class HubInfo {

	
	
	public final UUID uuid; 
	
	private DirectSocketAddress hubAddress;
	private int linkNumber; 
	private int hops;
	
	private long lastLocalUpdate;
	private long homeState;
	
	public HubInfo(UUID uuid) { 
		this.uuid = uuid;
	}
	
	
}

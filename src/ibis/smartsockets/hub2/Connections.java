package ibis.smartsockets.hub2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.hub.connections.ClientConnection;
import ibis.smartsockets.hub.connections.HubConnection;

public class Connections {
	
	private final MessageCallback callback;
	
	private int nextLinkNumber = 0;
	private final Map<Integer, Connection> map;	
	
	public Connections(MessageCallback callback) { 
		this.callback = callback;		
		map = new HashMap<Integer, Connection>();
	}
	
	public synchronized int nextLinkNumber() { 
		return nextLinkNumber++;
	}
	
	public synchronized void addConnection(Connection c) { 		
		map.put(c.linkNumber, c);
	}
	
	public void sendMessage(int linkNumber, Message m) { 
		
		Connection c = null; 
			
		synchronized (this) { 
			c = map.get(linkNumber);
		}

		if (c == null) { 
			// FIXME: drop packet ?
			return; 
		}
		
		c.send(m);
	}
	
	
	/*
	
	
	
	
	// NOTE: should this be a single datastructure ?
	
	public DirectSocketAddress[] hubAddresses() {
		// TODO Auto-generated method stub
		return null;
	}

	public UUID[] hubUUIDs() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public HubConnection getHub(UUID a) {
		// TODO Auto-generated method stub
		return null;
	}

	public DirectSocketAddress[] clientAddresses() {
		// TODO Auto-generated method stub
		return null;
	}

	public UUID[] clientUUIDs() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ClientConnection getClient(UUID a) {
		// TODO Auto-generated method stub
		return null;
	}
*/
}

package ibis.smartsockets.hub2;

public abstract class Connection {

	public final int linkNumber; 
	
	public Connection(int linkNumber) {
		this.linkNumber = linkNumber;
	}
	
	public abstract void send(Message m); 
	
	
	
}

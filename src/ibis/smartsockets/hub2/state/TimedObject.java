package ibis.smartsockets.hub2.state;

public class TimedObject<T> {

	public final long time; 
	public final T object;
	
	public TimedObject(final long time, final T object) {
		super();
		this.time = time;
		this.object = object;
	}
}

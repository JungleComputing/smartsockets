package ibis.smartsockets.hub2.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TimedHash<T> {

	private final Map<UUID, TimedObject<T>> map; 
	private final Set<UUID> changes;
	
	public TimedHash() {
		super();
		this.map = new HashMap<UUID, TimedObject<T>>();
		this.changes = new HashSet<UUID>();
	}
	
	public synchronized void putIfNewer(UUID key, T data, long time) { 
		
		TimedObject<T> tmp = map.get(key);
		
		if (tmp == null || (tmp.time <= time)) { 
			put(key, new TimedObject<T>(time, data));
		}
	}

	public synchronized void putIfNewer(UUID key, TimedObject<T> data) { 
		
		TimedObject<T> tmp = map.get(key);
		
		if (tmp == null || (tmp.time <= data.time)) { 
			put(key, data);
		}
	}

	public synchronized void put(UUID key, TimedObject<T> data) { 
		map.put(key, data);
		changes.add(key);
	}		
	
	public synchronized TimedObject<T> get(UUID key) { 
		return map.get(key);
	}
	
	public T getData(UUID key) { 
		
		TimedObject<T> tmp = get(key);
		
		if (tmp != null) { 
			return tmp.object;
		}
		
		return null;	
	}
	
	public long getTime(UUID key) { 
		
		TimedObject<T> tmp = get(key);
		
		if (tmp != null) { 
			return tmp.time;
		}
		
		return -1;	
	}
	
	public synchronized UUID [] getChangedKeys() { 
		
		UUID [] result = null;
		
		if (changes.size() > 0) { 
			result = changes.toArray(new UUID[changes.size()]);
			changes.clear();
		} else { 
			result = new UUID[0];
		}
		
		return result;
	}
}



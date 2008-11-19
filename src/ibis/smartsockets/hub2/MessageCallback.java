package ibis.smartsockets.hub2;

import java.nio.ByteBuffer;

public interface MessageCallback {
	public void gotMessage(int opcode1, int opcode2, ByteBuffer buffer);
}

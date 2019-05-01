package ibis.smartsockets.util.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.client.channel.ChannelDirectTcpip;

public class LocalStreamForwarder {

    private final ChannelDirectTcpip channel;

    public LocalStreamForwarder(ChannelDirectTcpip channel) throws IOException {
        this.channel = channel;
    }

    /**
     * @return An <code>InputStream</code> object.
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        return channel.getInvertedOut();
    }

    /**
     * @return An <code>OutputStream</code> object.
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        return channel.getInvertedIn();
    }

    /**
     * Close the underlying SSH forwarding channel and free up resources.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        channel.close();
    }
}

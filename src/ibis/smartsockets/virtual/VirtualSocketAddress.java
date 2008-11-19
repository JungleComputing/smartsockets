package ibis.smartsockets.virtual;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.util.MalformedAddressException;
import ibis.smartsockets.util.TransferUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;


public class VirtualSocketAddress extends SocketAddress implements Serializable { 

    private static final long serialVersionUID = 3340517955293464166L;
    
    private final DirectSocketAddress machine;
    private final int port;
    
    // This field indicates which 'virtual cluster' the machine is part of.
    private final String cluster;
    
    // Cache for the coded form of this address
    private transient byte [] codedForm;

    public VirtualSocketAddress(DataInput in) 
        throws IOException, MalformedAddressException {

        int mlen = in.readShort();         
        int clen = in.readShort();

        byte [] m = new byte[mlen];        
        in.readFully(m);        
        machine = DirectSocketAddress.fromBytes(m);        
        
        port = in.readInt();
        
        if (clen > 0) { 
            byte [] c = new byte[clen];
            in.readFully(c);
            cluster = new String(c);
        } else {
            cluster= null;
        }     
    }
        
    public VirtualSocketAddress(DirectSocketAddress machine, int port) {
        this(machine, port, null);
    }
    
    public VirtualSocketAddress(DirectSocketAddress machine,
            int port, String cluster) {
        
        this.machine = machine;
        this.port = port;
        this.cluster = cluster;
    }
    
    /**
     * Construct a new VirtualSocketAddress starting from a String with the 
     * following format: 
     * 
     *   MACHINEADDRESS:PORT[#CLUSTER]
     *   
     * The '@MACHINEADDRESS' part is optional and indicates the hub where the 
     * machine can be found. The '#CLUSTER' part is also optional and indicates
     * which virtual cluster the machine belongs to. 
     *  
     * @param address 
     * @throws UnknownHostException 
     */
    public VirtualSocketAddress(String address) 
        throws UnknownHostException, MalformedAddressException { 
        
        int index2 = address.lastIndexOf('#');                

        if (index2 != -1) {
        	cluster = address.substring(index2+1);
        	address = address.substring(0, index2);
        } else { 
            // both index1 and index2 are '-1'
            cluster = null;
        }
            
        int index = address.lastIndexOf(':');

        if (index == -1) { 
            throw new MalformedAddressException("String \"" + address + "\"" +
                    " does not contain VirtualSocketAddress!");
        }
                
        try { 
            machine = DirectSocketAddress.getByAddress(
                    address.substring(0, index));
            
            port = Integer.parseInt(address.substring(index+1));
        } catch (NumberFormatException e) {
            throw new MalformedAddressException("String \"" + address + "\"" +
                    " does not contain VirtualSocketAddress!", e);
        }
    }

    public VirtualSocketAddress(String machine, int port) 
        throws UnknownHostException, MalformedAddressException {
        
        this(DirectSocketAddress.getByAddress(machine), port, null);
    }
    
    public void write(DataOutput out) throws IOException {

        byte [] m = machine.getAddress();
        
        byte [] c = null;
        
        if (cluster != null) { 
            c = cluster.getBytes();
        }
        
        out.writeShort(m.length);
        out.writeShort(c == null ? 0 : c.length);
        
        out.write(m);
        out.writeInt(port);
        
        if (c != null) { 
            out.write(c);
        }        
    }
    
    public DirectSocketAddress machine() { 
        return machine;
    }

    public String cluster() { 
        return cluster;
    }
    
    public int port() { 
        return port;
    }    
    
    public byte [] toBytes() {
        
        if (codedForm == null) { 
            
             byte [] m = machine.getAddress();             
             byte [] c = cluster == null ? new byte[0] : cluster.getBytes();
             
             int len = 3*2 + 4  + m.length; 
             
             if (c != null) { 
                 len += c.length;                 
             }
             
             codedForm = new byte[len];
             
             TransferUtils.storeShort((short) m.length, codedForm, 0);
             TransferUtils.storeShort((short) c.length, codedForm, 4);
             
             System.arraycopy(m, 0, codedForm, 6, m.length);
             
             int off = 6 + m.length;
             
             TransferUtils.storeInt(port, codedForm, off);
             off += 4;
             
             System.arraycopy(c, 0, codedForm, off, c.length);
        }
        
        return codedForm.clone();
    }

    
    
    public String toString() {         
        return machine.toString() + ":" + port 
            + (cluster == null ? "" : ("#" + cluster)); 
    }
    
    public boolean equals(Object other) { 
     
        if (this == other) {            
            return true;
        }
        
        if (other == null) { 
            return false;
        }
                
        if (!(other instanceof VirtualSocketAddress)) {
            return false;
        }
        
        // Now compare the addresses. Note that the hub field is not compared, 
        // since it is only a hint of the location of the machine. It may be 
        // null in some cases or contain different values if the machine is 
        // registered at multiple proxies.       
        VirtualSocketAddress tmp = (VirtualSocketAddress) other;
        
        // The ports must be the same. 
        if (port != tmp.port) { 
            return false;
        }
        
        // The machine must be the same 
        return machine.equals(tmp.machine);
    }
    
    public int hashCode() {
        return machine.hashCode() ^ port;        
    }
    
    public static VirtualSocketAddress fromBytes(byte [] source, int offset) 
        throws UnknownHostException, MalformedAddressException {

        int mlen = TransferUtils.readShort(source, offset);
        int clen = TransferUtils.readShort(source, offset+4);

        int off = offset + 6;
        
        DirectSocketAddress machine = DirectSocketAddress.fromBytes(source, off);        
        off += mlen;
        
        int port = TransferUtils.readInt(source, off);
        off += 4;
                
        String cluster = null;
        
        if (clen > 0) { 
            cluster = new String(source, off, clen);
            // off += clen;
        }
        
        return new VirtualSocketAddress(machine, port, cluster);
    }
    
    public static VirtualSocketAddress partialAddress(InetAddress host, 
            int realport, int virtualport) throws UnknownHostException {
        return new VirtualSocketAddress(
                DirectSocketAddress.getByAddress(
                        new InetSocketAddress(host, realport)), virtualport);
    }
    
    
    public static VirtualSocketAddress partialAddress(String hostname, 
            int realport, int virtualport) throws UnknownHostException {
     
        return new VirtualSocketAddress(
                DirectSocketAddress.getByAddress(hostname, realport), virtualport);
    }
    
    public static VirtualSocketAddress partialAddress(String hostname, 
            int port) throws UnknownHostException {     
        return partialAddress(hostname, port, port);
    }    
    
    public static VirtualSocketAddress partialAddress(InetAddress host, 
            int port) throws UnknownHostException {     
        return partialAddress(host, port, port);
    }  
}

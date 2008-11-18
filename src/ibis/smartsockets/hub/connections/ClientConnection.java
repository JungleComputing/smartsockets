package ibis.smartsockets.hub.connections;

import ibis.smartsockets.direct.DirectSocket;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.direct.DirectSocketFactory;
import ibis.smartsockets.hub.Connections;
import ibis.smartsockets.hub.SmartSocketsProtocol;
import ibis.smartsockets.hub.state.AddressAsStringSelector;
import ibis.smartsockets.hub.state.ClientsByTagAsStringSelector;
import ibis.smartsockets.hub.state.DetailsSelector;
import ibis.smartsockets.hub.state.DirectionsAsStringSelector;
import ibis.smartsockets.hub.state.HubDescription;
import ibis.smartsockets.hub.state.HubList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConnection extends MessageForwardingConnection {

    private static Logger conlogger = 
        LoggerFactory.getLogger("ibis.smartsockets.hub.connections.client"); 
    
    private static Logger reqlogger = 
        LoggerFactory.getLogger("ibis.smartsockets.hub.request"); 
    
    private static Logger reglogger = 
        LoggerFactory.getLogger("ibis.smartsockets.hub.registration"); 
    
    private final DirectSocketAddress clientAddress;
    
    private final String clientAddressAsString;
    
    private final String uniquePrefix;
    
    public ClientConnection(DirectSocketAddress clientAddress, DirectSocket s, 
            DataInputStream in, DataOutputStream out, Connections connections,
            HubList hubs) {
     
        super(s, in, out, connections, hubs, false, 
                "Client(" + clientAddress.toString() + ")");        
     
        this.clientAddress = clientAddress;        
        this.clientAddressAsString = clientAddress.toString();
        
        this.uniquePrefix = clientAddressAsString + "__";
        
        if (conlogger.isDebugEnabled()) {
            conlogger.debug("Created client connection: " + clientAddress);
        }
    }
        
    protected String getUniqueID(long index) {
        return uniquePrefix + index;
    }
                
    protected void handleDisconnect(Exception e) {
        
        if (knownHubs.getLocalDescription().removeClient(clientAddress)) {
            if (conlogger.isDebugEnabled()) {
                conlogger.debug("Removed client connection " + clientAddress);
            }
        } else if (conlogger.isDebugEnabled()) {
                conlogger.debug("Failed to removed client connection " 
                        + clientAddress + "!");
        }
   
        connections.removeClient(clientAddress);
        DirectSocketFactory.close(s, out, in);      
       
    } 
    
    private void handleListHubs() throws IOException { 
        
        int id = in.readInt();
        
        if (reqlogger.isDebugEnabled()) {
            reqlogger.debug("Connection " + clientAddress + " return id: " + id);
        }
                
        AddressAsStringSelector as = new AddressAsStringSelector();
        
        knownHubs.select(as);
                
        LinkedList<String> result = as.getResult();
        
        synchronized (out) {
            out.write(SmartSocketsProtocol.INFO_REPLY);           
            out.writeInt(id);            
            out.writeInt(result.size());
        
            for (String s : result) {  
                out.writeUTF(s);
            } 
            
            out.flush();
        }
    } 

    private void handleListHubDetails() throws IOException { 
        
        int id = in.readInt();
        
        if (reqlogger.isDebugEnabled()) {
            reqlogger.debug("Connection " + clientAddress + " return id: " + id);
        }
        
        DetailsSelector as = new DetailsSelector();
        
        knownHubs.select(as);
        
        LinkedList<String> result = as.getResult();
        
        synchronized (out) {
            out.write(SmartSocketsProtocol.INFO_REPLY);           
            out.writeInt(id);            
            out.writeInt(result.size());
            
            if (reqlogger.isDebugEnabled()) {
                reqlogger.debug("Connection " + clientAddress + " result: " 
                        + result.size() + " " + result);
            }
        
            for (String s : result) { 
                out.writeUTF(s);
            } 
            
            out.flush();
        }
    } 

    
    private void handleListClientsForHub() throws IOException { 
        int id = in.readInt();
        
        String hub = in.readUTF();
        String tag = in.readUTF();
        
        if (reqlogger.isDebugEnabled()) {
            reqlogger.debug("Connection " + clientAddress + " return id: " + id); 
        }
        
        LinkedList<String> result = new LinkedList<String>();
        
        try { 
            HubDescription d = knownHubs.get(DirectSocketAddress.getByAddress(hub));            
            d.getClientsAsString(result, tag);            
        } catch (UnknownHostException e) {
            reqlogger.warn("Connection " + clientAddress + " got illegal hub " 
                    + "address: " + hub); 
        }
      
        synchronized (out) {
            out.write(SmartSocketsProtocol.INFO_REPLY);           
            out.writeInt(id);            
            out.writeInt(result.size());

            for (String s : result) { 
                out.writeUTF(s);
            } 
            
            out.flush();
        }
    } 

    private void handleListClients() throws IOException { 
        
        int id = in.readInt();
        String tag = in.readUTF();
        
        if (reqlogger.isDebugEnabled()) {
            reqlogger.debug("Connection " + clientAddress + " return id: " + id);
        }
        
        ClientsByTagAsStringSelector css = new ClientsByTagAsStringSelector(tag);
        
        knownHubs.select(css);
        
        LinkedList<String> result = css.getResult();

        synchronized (out) {
            out.write(SmartSocketsProtocol.INFO_REPLY);           
            out.writeInt(id);            
            out.writeInt(result.size());

            if (reqlogger.isDebugEnabled()) {
                reqlogger.debug("Connection " + clientAddress + " returning : " 
                        + result.size() + " clients: " + result);
            }
        
            for (String s : result) {
                out.writeUTF(s);
            } 
            
            out.flush();
        }
    } 

    private void handleGetDirectionsToClient() throws IOException { 
        int id = in.readInt();
        String client = in.readUTF();
         
        if (reqlogger.isDebugEnabled()) {
            reqlogger.debug("Connection " + clientAddress + " return id: " + id);
        }
        
        DirectionsAsStringSelector ds = 
            new DirectionsAsStringSelector(DirectSocketAddress.getByAddress(client));
        
        knownHubs.select(ds);
        
        LinkedList<String> result = ds.getResult();
        
        synchronized (out) {
            out.write(SmartSocketsProtocol.INFO_REPLY);           
            out.writeInt(id);            
            out.writeInt(result.size());

            if (reqlogger.isDebugEnabled()) {
                reqlogger.debug("Connection " + clientAddress + " returning : " 
                        + result.size() + " possible directions: " + result);
            }
        
            for (String tmp : result) { 
                out.writeUTF(tmp);
            } 
            
            out.flush();
        }
    } 
    
    private void registerProperty() throws IOException { 
        
        int id = in.readInt();
        String tag = in.readUTF();
        String info = in.readUTF();

        if (reqlogger.isDebugEnabled()) {
            reglogger.debug("Connection " + clientAddress + " return id: " + id +  
                    " adding info: " + tag + " " + info);
        }
               
        HubDescription localHub = knownHubs.getLocalDescription();
        
        synchronized (out) {
            out.write(SmartSocketsProtocol.PROPERTY_ACK);           
            out.writeInt(id);            
            
            if (localHub.addService(clientAddress, tag, info)) { 
                out.writeInt(SmartSocketsProtocol.PROPERTY_ACCEPTED);
            } else { 
                out.writeInt(SmartSocketsProtocol.PROPERTY_REJECTED);
            }
            
            out.flush();
        }
    } 

    private void updateProperty() throws IOException { 
        
        int id = in.readInt();
        String tag = in.readUTF();
        String info = in.readUTF();

        if (reqlogger.isDebugEnabled()) {
            reglogger.debug("Connection " + clientAddress + " return id: " + id +  
                    " updating info: " + tag + " " + info);         
        }
        
        HubDescription localHub = knownHubs.getLocalDescription();
        
        synchronized (out) {
            out.write(SmartSocketsProtocol.PROPERTY_ACK);           
            out.writeInt(id);            
            
            if (localHub.updateService(clientAddress, tag, info)) {
                out.writeInt(SmartSocketsProtocol.PROPERTY_ACCEPTED);
            } else { 
                out.writeInt(SmartSocketsProtocol.PROPERTY_REJECTED);
            }
            
            out.flush();
        }
    } 

    private void handleRemoveProperty() throws IOException { 
        
        int id = in.readInt();
        String tag = in.readUTF();
        
        if (reqlogger.isDebugEnabled()) {
            reglogger.debug("Connection " + clientAddress + " return id: " + id +  
                    " removing info: " + tag);
        }
               
        HubDescription localHub = knownHubs.getLocalDescription();
        
        synchronized (out) {
            out.write(SmartSocketsProtocol.PROPERTY_ACK);            
            out.writeInt(id);
            
            if (localHub.removeService(clientAddress, tag)) {
                out.writeInt(SmartSocketsProtocol.PROPERTY_ACCEPTED);
            } else { 
                out.writeInt(SmartSocketsProtocol.PROPERTY_REJECTED);
            }                
            
            out.flush();
        }
    } 
        
    protected String getName() {
        return "ClientConnection(" + clientAddress + ")";
    }

    protected boolean handleOpcode(int opcode) {           
                     
        try { 
            switch (opcode) { 
            
            case SmartSocketsProtocol.HUBS:
                if (reqlogger.isDebugEnabled()) {
                    reqlogger.debug("Connection " + clientAddress + " requests " 
                            + "hubs");
                } 
                handleListHubs();
                return true;

            case SmartSocketsProtocol.HUB_DETAILS:
                if (reqlogger.isDebugEnabled()) {
                    reqlogger.debug("Connection " + clientAddress + " requests " 
                            + "hub details");
                } 
                handleListHubDetails();
                return true;
                
            case SmartSocketsProtocol.CLIENTS_FOR_HUB:
                if (reqlogger.isDebugEnabled()) {
                    reqlogger.debug("Connection " + clientAddress + " requests" 
                            + " local clients");
                } 
                handleListClientsForHub();
                return true;
            
            case SmartSocketsProtocol.ALL_CLIENTS:
                if (reqlogger.isDebugEnabled()) {
                    reqlogger.debug("Connection " + clientAddress + " requests" 
                            + " all clients");
                }
                handleListClients();
                return true;
            
            case SmartSocketsProtocol.DIRECTION:
                if (reqlogger.isDebugEnabled()) {
                    reqlogger.debug("Connection " + clientAddress + " requests" 
                            + " direction to other client");
                }
                handleGetDirectionsToClient();
                return true;
            
            case SmartSocketsProtocol.REGISTER_PROPERTY:
                if (reglogger.isDebugEnabled()) {
                    reglogger.debug("Connection " + clientAddress + " requests" 
                            + " info registration");
                }
                registerProperty();
                return true;
            
            case SmartSocketsProtocol.UPDATE_PROPERTY:
                if (reglogger.isDebugEnabled()) {
                    reglogger.debug("Connection " + clientAddress + " requests" 
                            + " info update");
                }
                updateProperty();
                return true;
            
            case SmartSocketsProtocol.REMOVE_PROPERTY:
                if (reglogger.isDebugEnabled()) {
                    reglogger.debug("Connection " + clientAddress + " requests" 
                            + " info removal");
                }
                handleRemoveProperty();
                return true;
                
            default:
                conlogger.warn("Connection " + clientAddress 
                        + " got unknown " + "opcode " + opcode 
                        + " -- disconnecting");
                handleDisconnect(null);
                return false;                
            } 
            
        } catch (Exception e) { 
            conlogger.warn("Connection to " + clientAddress + " is broken!", e);
            handleDisconnect(e);
        }
        
        return false;
    }

   
}

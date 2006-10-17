package smartsockets.viz;

import java.awt.Color;

import smartsockets.hub.servicelink.ClientInfo;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;

public class ClientNode extends Node {
    
    Edge edge;
    HubNode hub;
    ClientInfo client;
    
    public ClientNode(ClientInfo info, HubNode hub) { 
        super(info.getClientAddress().toString());
        
        this.hub = hub;
        
        String adr = info.getClientAddress().toString();

        setType(Node.TYPE_CIRCLE);
        
        if (info.offersService("router")) {
            System.out.println("Adding router " + adr);
            setMouseOverText(new String[] { "Router:", adr });
            setBackColor(Color.decode("#FF7F24"));
            setNodeBorderInactiveColor(Color.decode("#CD661D"));
            setLabel("R");
        } else if (info.offersService("visualization")) {
            System.out.println("Adding visualization " + adr);
            setMouseOverText(new String[] { "Visualization:", adr });
            setBackColor(Color.decode("#8000A0"));
            setNodeBorderInactiveColor(Color.decode("#54006A")); 
            setLabel("V");
        } else {
            System.out.println("Adding client " + adr);
            setMouseOverText(new String[] { "Client:", adr });
            setLabel("C");
        }
        
        edge = new Edge(this, hub);
    }

    public Edge getEdge() {
        return edge;
    }
}

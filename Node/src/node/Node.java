/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.*;
import overlaynetworknode.OverlayNetworkNode;

/**
 *
 * @author eduardo
 */
public class Node {
    private static int id ;
    private static final String MASTER_HOSTNAME = "localhost";
    private static final int MASTER_PORT = 6789;
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        id = Integer.parseInt(args[0]);
        
        OverlayNetworkNode node = new OverlayNetworkNode(id, MASTER_HOSTNAME, MASTER_PORT);
        node.start();
    }   
    
    public static int getId() {
        return id;
    }
}

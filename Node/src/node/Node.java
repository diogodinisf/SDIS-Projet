/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import overlaynetworknode.OverlayNetworkNode;

/**
 *
 * @author eduardo
 */
public class Node {
    private static int id ;
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        
        id = Integer.parseInt(args[0]);
        System.out.println("Sou o Nó: " + id);
        
        OverlayNetworkNode node = new OverlayNetworkNode(id);
        node.start();
    }   
    
    public static int getId() {
        return id;
    }
}

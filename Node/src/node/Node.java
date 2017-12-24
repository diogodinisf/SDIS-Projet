/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.*;
import overlaynetworknode.NodeDatagramSocket;
import overlaynetworknode.OverlayNetworkNode;

/**
 *
 * @author eduardo
 */
public class Node {
    private static int id ;
    private static final String MASTER_HOSTNAME = "localhost";
    private static final int MASTER_PORT = 6789;
    private DatagramSocket socketToReceive;
    private NodeDatagramSocket socketToSend;
    private Thread thread_send;
    private Thread thread_recv;
    
    public void run(String id) {
        Node.id = Integer.parseInt(id);
        
        OverlayNetworkNode node = new OverlayNetworkNode(Node.id, MASTER_HOSTNAME, MASTER_PORT);
        node.start();
        socketToReceive = node.getSocketToReceive();
        socketToSend = node.getSocketToSend();
        
        thread_recv = new Thread(new ThreadToReceive(socketToReceive)); //multiplicar para checkar
        thread_recv.start();
        thread_send = new Thread(new ThreadToSend(socketToSend)); //multiplicar para checkar
        thread_send.start();
    }
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        new Node().run(args[0]);
    }

    public static int getId() {
        return id;
    }
    
    public static void closeProgram() {
        ThreadToSend.close();
    }
}
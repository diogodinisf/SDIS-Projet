/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.*;
import nodedatagramsocket.socket.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class Node {
    private static final String MASTER_HOSTNAME = "192.168.1.80";
    private static final int MASTER_PORT = 6789;
    
    private int port;
    private NodeDatagramSocket socket;
    private ThreadToSend threadToSend;
    private Thread thread_send;
    private Thread thread_recv;
    
    public void run(int id) throws SocketException {
        port = 35555 + id;
        
        socket = new NodeDatagramSocket(port, MASTER_HOSTNAME, MASTER_PORT);
        
        thread_recv = new Thread(new ThreadToReceive(socket, this)); //multiplicar para checkar
        thread_recv.start();
        thread_send = new Thread(threadToSend = new ThreadToSend(socket)); //multiplicar para checkar
        thread_send.start();
    }
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        new Node().run(Integer.parseInt(args[0]));
    }
    
    public void closeProgram() {
        threadToSend.close();
    }
}
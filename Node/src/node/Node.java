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
    private Thread thread_send;
    private Thread thread_recv;
    
    public void start(int id) throws SocketException {
        port = 35555 + id;   
        socket = new NodeDatagramSocket(port, MASTER_HOSTNAME, MASTER_PORT);   
        run(socket);
    }
    
    public void start() throws SocketException {
        socket = new NodeDatagramSocket(MASTER_HOSTNAME, MASTER_PORT);   
        run(socket);
    }
    
    public void run(NodeDatagramSocket socket) throws SocketException {
        thread_recv = new Thread(new ThreadToReceive(socket, this)); //multiplicar para checkar
        thread_recv.start();
        thread_send = new Thread(new ThreadToSend(socket)); //multiplicar para checkar
        thread_send.start();
    }
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        if ((args.length == 0) || (Integer.parseInt(args[0]) == -1)) {
            new Node().start();
        } else {
            new Node().start(Integer.parseInt(args[0]));        
        }
    }
    
    public void closeProgram() {
        System.exit(0);
    }
}
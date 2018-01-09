package node;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import nodedatagramsocket.socket.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class Node {
    private static final String MASTER_HOSTNAME = "172.30.0.125";
    private static final int MASTER_PORT = 6789;
    
    private int firstId = -1;
    private int num = 0;
    private final List<ThreadToSend> threadsToSend;

    public Node() {
        this.threadsToSend = new ArrayList<>();
    }
   
    public void start(String[] args) throws SocketException {
        if (args.length == 0) {
            num = 1;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("-h")) {
                help();
            }
        } else if ((args.length == 2) || (args.length == 4))  {
            if (args[0].equalsIgnoreCase("-n")) {
                num = Integer.parseInt(args[1]);
            } else if (args[0].equalsIgnoreCase("-p")) {
                firstId = Integer.parseInt(args[1]);
            }
            
            if (args.length == 4) {
                if (args[2].equalsIgnoreCase("-n")) {
                    num = Integer.parseInt(args[3]);
                } else if (args[2].equalsIgnoreCase("-p")) {
                    firstId = Integer.parseInt(args[3]);
                }
            }
        } else {
            return;
        }
        
        if (num == 0) {
            if (firstId == -1) {
                return;
            } else {
                num = 1;
            }
        } 
       
        run();     
    }
    
    public void run() throws SocketException {
        NodeDatagramSocket socket;
        ThreadToSend threadToSend;
        
        for (int i = 0; i < num; i++) {
            if (firstId == -1) {
                socket = new NodeDatagramSocket(MASTER_HOSTNAME, MASTER_PORT);
            } else {
                socket = new NodeDatagramSocket((firstId + i), MASTER_HOSTNAME, MASTER_PORT);
            }
            
            (new Thread(new ThreadToReceive(socket, this))).start();
            (new Thread(threadToSend = new ThreadToSend(socket))).start();
            
            threadsToSend.add(threadToSend);
        }
    }
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        new Node().start(args);
    }
    
    public void closeProgram() {
        if (num == 1) {
            System.exit(0);
        } else {
            for (ThreadToSend thread : threadsToSend) {
                thread.close();
            }
        }
    }
    
    public static void help() {
        System.out.println("-h:\tMostra esta mensagem\t[-h]");
        System.out.println("-n:\tNúmero de nós nesta JVM\t[-n numero]");
        System.out.println("-p:\tPrmeiro ID nesta JVM\t[-n id]");
        System.out.println("\tNota: A primeira porta é ID + 35555");
    }
} 

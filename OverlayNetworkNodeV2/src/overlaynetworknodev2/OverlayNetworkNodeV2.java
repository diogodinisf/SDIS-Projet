/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknodev2;

import java.net.SocketException;

/**
 *
 * @author eduardo
 */
public class OverlayNetworkNodeV2 {
    
    private static final String MASTER_HOSTNAME = "172.30.2.196";
    private static final int MASTER_PORT = 6789;
    
    private int port;
    private NodeDatagramSocket socket;

    private Thread thread_send;
    private Thread thread_recv;
    
    public void start(int id) throws SocketException {
        port = 35555 + id;
        socket = new NodeDatagramSocket(port, MASTER_HOSTNAME); //A PORTA VAI NO FICHEIRO
        run();
    }
    
    public void start() throws SocketException {
        socket = new NodeDatagramSocket( MASTER_HOSTNAME);
        run();
    }
    
    public void run(){
        Thread thread_recv = new Thread(new threadToReceive(socket, this)); 
        thread_recv.start();
        thread_send = new Thread(new threadToSend(socket)); 
        thread_send.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException {
        
        if ((args.length == 0) || (Integer.parseInt(args[0]) == -1)) {
            new OverlayNetworkNodeV2().start();
        } else {
            new OverlayNetworkNodeV2().start(Integer.parseInt(args[0]));        
        }
    }
    
    public void closeProgram() {
        System.exit(0);
    }
    
}

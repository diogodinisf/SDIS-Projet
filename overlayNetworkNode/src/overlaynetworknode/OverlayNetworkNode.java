/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diogo
 */
public class OverlayNetworkNode {
    private int port;
    private final int id;
    private static double timeInit;
    private final String master_hostname;
    private final int master_port;
    
    public OverlayNetworkNode(int id, String master_hostname, int master_port) {
        this.id = id;
        this.master_hostname = master_hostname;
        this.master_port = master_port;
    }
    
    public DatagramSocket start() {
        port = 35555 + this.id;
        timeInit = System.currentTimeMillis();
        DatagramSocket socket = null;
        
        System.out.println("Sou o nó da porta: " + port);
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(OverlayNetworkNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Thread thread_recv = new Thread(new ThreadToReceive(port)); //multiplicar para checkar
        thread_recv.start();
        Thread thread_send = new Thread(new ThreadToSend(port)); //multiplicar para checkar
        thread_send.start();
        
        joinOverlayNetwork(socket);
        
        return socket;
    }
    
    public void joinOverlayNetwork(DatagramSocket socket) {
        try {
            String str = String.valueOf(port);
            
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), InetAddress.getByName(master_hostname), master_port);
            socket.send(packet);
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(OverlayNetworkNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OverlayNetworkNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int getPort() {
        return port;
    }
    
    public int getId() {
        return id;
    }
    
    public static double getInitTime() {
        return timeInit;
    }
}

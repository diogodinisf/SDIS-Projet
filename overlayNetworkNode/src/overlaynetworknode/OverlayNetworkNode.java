/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
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
    
    public OverlayNetworkNode(int id) {
        this.id = id;
    }
    
    public void start() {
        this.port = 35555 + this.id;
        timeInit = System.currentTimeMillis();
        
        Thread thread_recv = new Thread(new ThreadToReceive(port + 5000)); //multiplicar para checkar
        thread_recv.start();
        Thread thread_send = new Thread(new ThreadToSend(port + 10000)); //multiplicar para checkar
        thread_send.start();
        
        joinMulticastGroup();
    }
    
    public void joinMulticastGroup() {
        String group = "228.5.6.7";
        int multicastPort = 6789;
        
        try {
            NodeMulticastSocket socket = new NodeMulticastSocket(group, multicastPort);
            String str = id + "_" + port;
            
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), InetAddress.getByName(group), multicastPort);
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

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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
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
    private String hostname;
    private static DatagramSocket socketToReceive;
    private NodeDatagramSocket socketToSend;
    
    public OverlayNetworkNode(int id, String master_hostname, int master_port) {
        this.id = id;
        this.master_hostname = master_hostname;
        this.master_port = master_port;
    }
    
    public void start() {
        port = 35555 + this.id;
        timeInit = System.currentTimeMillis();
        
        System.out.println("Sou o nó da porta: " + port);
        
        // obter o hostname, credo que esta treta demora, vamos acreditar que aqui não se usa endereços do tipo 10.x.x.x
        Enumeration e;
        try {
            e = NetworkInterface.getNetworkInterfaces();
        
            while(e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
        
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String host = i.getHostAddress();
                    if (host.contains(".")) {
                        if ((host.split("\\."))[0].equalsIgnoreCase("192")) {
                            hostname = host;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(OverlayNetworkNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            socketToSend = new NodeDatagramSocket(port);
            socketToReceive = new DatagramSocket(port);
        } catch (SocketException ex) {
            Logger.getLogger(OverlayNetworkNode.class.getName()).log(Level.SEVERE, null, ex);
        }

        joinOverlayNetwork();
    }
    
    public static void close() {
        socketToReceive.close();
    }
    
    public DatagramSocket getSocketToReceive() {
        return socketToReceive;
    }
    
    public NodeDatagramSocket getSocketToSend() {
        return socketToSend;
    }
    
    public void joinOverlayNetwork() {
        DatagramSocket socket;
        
        try {
            socket = new DatagramSocket();
            String str = hostname + "_" + port;
            
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), InetAddress.getByName(master_hostname), master_port);
            socket.send(packet);
            socket.close();
            
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

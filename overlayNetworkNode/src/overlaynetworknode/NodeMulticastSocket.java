/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class NodeMulticastSocket {
    private boolean running = true;
    private final MulticastSocket socket;
    
    public NodeMulticastSocket (String groupIP, int port) throws UnknownHostException, IOException {
        InetAddress group = InetAddress.getByName(groupIP);
        socket = new MulticastSocket (port);
        
        socket.joinGroup(group);
        
        Thread thread_recv = new Thread(new ReceiveFromGroup()); //multiplicar para checkar
        thread_recv.start();
        
    }
    
    public void send (DatagramPacket packet) throws IOException{
        socket.send(packet);
    }
    
    public class ReceiveFromGroup implements Runnable {

        @Override
        public void run() {
            
            while (running) {
                try {
                    byte[] receiveData = new byte[64 * 1024];
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(packet);
                    String str = new String(packet.getData(), 0, packet.getLength());
                    String[] split = str.split("_");
                    
                    NodeDatagramSocket.addNode(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                
                } catch (IOException ex) {
                    Logger.getLogger(NodeMulticastSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}

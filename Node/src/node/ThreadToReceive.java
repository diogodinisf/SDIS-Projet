/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nodedatagramsocket.utils.Display;
import nodedatagramsocket.socket.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class ThreadToReceive implements Runnable {
    private final NodeDatagramSocket socket;
    private final Node father;
    
    public ThreadToReceive(NodeDatagramSocket socket, Node father) {
        this.socket = socket;
        this.father = father;
    }
    
    @Override
    public void run() {
        try {
            
            while (true) {
                byte[] receiveData = new byte[64 * 1024];
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String str = new String(packet.getData(), 0, packet.getLength());
                Display.receive("Recebido de: " + packet.getSocketAddress().toString().replace("/", "") + ": " + str);
            }
        } catch (SocketException ex) {
            // socket foi fechado, é algo normal
            father.closeProgram();
            //Logger.getLogger(ThreadToReceive.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadToReceive.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
}

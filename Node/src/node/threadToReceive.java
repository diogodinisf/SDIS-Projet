/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class threadToReceive implements Runnable {

    static DatagramSocket socket;
    
    @Override
    public void run(){
        try {
            socket = new DatagramSocket(Node.getPort());
            while(true){
                byte[] receiveData=new byte[ 64*1024 ];
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String str = new String(packet.getData(), 0, packet.getLength());
               // System.out.println(str);      
            }
        } catch (SocketException ex) {
            Logger.getLogger(threadToReceive.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(threadToReceive.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

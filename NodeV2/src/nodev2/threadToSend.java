/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodev2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class threadToSend implements Runnable {

    @Override
    public void run(){
        
        try {
            NodeDatagramSocket socket = new NodeDatagramSocket();
   
       
            InetAddress address = InetAddress.getByName("localhost");
            while(true){
         
               // Scanner scanner = new Scanner(System.in);
                //System.out.print("Enter your msg: ");
                //String str = scanner.nextLine();
                //System.out.print("To which node ?: ");
                //int toId = scanner.nextInt();
                //int PORT = (int) nodeDatagramSocket.ports[toId];
                
                //DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), address, PORT);
                //socket.send(packet);
            }
        } catch (IOException ex) {
            Logger.getLogger(threadToSend.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
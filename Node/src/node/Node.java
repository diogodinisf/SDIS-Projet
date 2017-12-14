/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class Node {
    private static int port;
    
    public static void joinMulticastGroup(int id){
        String group = "228.5.6.7";
        int multicastPort =6789;
        try {
            NodeMulticastSocket s = new NodeMulticastSocket(group, multicastPort);

            String str = id + "_"+port;
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), InetAddress.getByName(group), multicastPort);
            s.send(packet);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {

        System.out.print("Sou o Node: ");
        System.out.println(args[1]);
        int id = Integer.parseInt(args[1]);

        port = 35555 + id;
        
        Thread thread_recv = new Thread(new threadToReceive()); //multiplicar para checkar
        thread_recv.start();
        Thread thread_send = new Thread(new threadToSend()); //multiplicar para checkar
        thread_send.start();
        
        
        sleep(1000); //mamke sure the threads are created
        joinMulticastGroup(id);
 
    
    }   
    
    public static int getPort() {
        return port;
    }
}

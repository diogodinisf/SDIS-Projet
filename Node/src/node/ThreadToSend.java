/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import overlaynetworknode.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class ThreadToSend implements Runnable {
    private final NodeDatagramSocket socket;
    private Scanner scanner;
    
    public ThreadToSend(NodeDatagramSocket socket) {
        this.socket = socket;
    }
    
    
    @Override
    public void run() {
        scanner = new Scanner(System.in);
        
        try {
            while (true) {
                System.out.print("Enter your msg: ");
                String str = scanner.nextLine();
                System.out.print("To which node ?: ");
                int toPort = scanner.nextInt() + 35555;
                String toIp = "192.168.1.80";
                scanner.nextLine();
                InetAddress address = InetAddress.getByName(toIp);
                
                DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), address, toPort);
                socket.send(packet);
            }
            
        } catch (SocketException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

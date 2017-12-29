/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import nodedatagramsocketv2.socket.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class threadToSend implements Runnable {
    private final NodeDatagramSocket socket;
    private ScannerProtocol scannerProtocol;
    private Scanner scanner;
    private boolean running;
    
    public threadToSend (NodeDatagramSocket socket){
        this.socket=socket;
    }

    @Override
    public void run(){
        
        running=true;
        
        try {
          
            
            scanner = new Scanner(System.in);
            scannerProtocol = new ScannerProtocol();
            
            while(running){
            
                String str = scanner.nextLine();
                scannerProtocol.protocol(str, socket);
            }
            
            scanner.close();
        } catch (IOException ex) {
            Logger.getLogger(threadToSend.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
}

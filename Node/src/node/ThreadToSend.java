/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
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
    private boolean running;
    
    public ThreadToSend(NodeDatagramSocket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        scanner = new Scanner(System.in);
        running = true;
        
        try {
            while (running) {
                String message = scanner.nextLine();
                ScannerProtocol.protocol(message, socket);
            }
            
        } catch (SocketException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

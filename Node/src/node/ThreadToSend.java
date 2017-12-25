/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import overlaynetworknode.Display;
import overlaynetworknode.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class ThreadToSend implements Runnable {
    private final NodeDatagramSocket socket;
    private Scanner scanner;
    private static boolean running;
    
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
        } catch (NoSuchElementException e) {
            Display.alert(socket.getMyAddress() + ":" + socket.getPort() + " Perdeu o Scanner Input devido ao terminal partilhado");
        } catch (SocketException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void close() {
        running = false;
    }
}

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
import nodedatagramsocket.utils.Display;
import nodedatagramsocket.socket.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class ThreadToSend implements Runnable {
    private final NodeDatagramSocket socket;
    private Scanner scanner;
    private boolean running;
    private ScannerProtocol scannerProtocol;
    
    public ThreadToSend(NodeDatagramSocket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        running = true;
        scanner = new Scanner(System.in);
        scannerProtocol = new ScannerProtocol();
        
        try {
            while (running) {
                String message = scanner.nextLine();
                scannerProtocol.protocol(message, socket);
            }
            
            scanner.close();
        } catch (NoSuchElementException e) {
            Display.alert(socket.getMyAddress() + ":" + socket.getPort() + " Perdeu o Scanner Input devido ao terminal partilhado");
        } catch (SocketException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadToSend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close() {
        running = false;
    }
}

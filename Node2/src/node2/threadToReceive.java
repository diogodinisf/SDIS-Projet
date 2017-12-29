/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node2;

import node2.Node2;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nodedatagramsocketv2.socket.NodeDatagramSocket;

/**
 *
 * @author eduardo
 */
public class threadToReceive implements Runnable {

    private NodeDatagramSocket socket;
    private final Node2 father;
    private boolean running;
    
    
    threadToReceive(NodeDatagramSocket socket, Node2 father){
        this.socket=socket;
        this.father=father;
        running=true;
    }
    
    @Override
    public void run(){
        try{
            while(running){
                byte[] receiveData=new byte[ 64*1024 ];
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String str = new String(packet.getData(), 0, packet.getLength());
                System.out.println(str);
            }
        }catch (SocketException ex) {
            // socket foi fechado, é algo normal
            running=false;
            father.closeProgram();
        } catch (IOException ex) {
            Logger.getLogger(threadToReceive.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknodev2;

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

    private NodeDatagramSocket socket;
    private final OverlayNetworkNodeV2 father;
    
    
    threadToReceive(NodeDatagramSocket socket, OverlayNetworkNodeV2 father){
        this.socket=socket;
        this.father=father;
    }
    
    @Override
    public void run(){
        try{
            while(true){
                byte[] receiveData=new byte[ 64*1024 ];
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String str = new String(packet.getData(), 0, packet.getLength());
                System.out.println(str);
            }
        }catch (SocketException ex) {
            // socket foi fechado, é algo normal
            father.closeProgram();
        } catch (IOException ex) {
            Logger.getLogger(threadToReceive.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

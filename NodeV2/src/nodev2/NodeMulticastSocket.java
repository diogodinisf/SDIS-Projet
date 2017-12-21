/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodev2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class NodeMulticastSocket {
    
    MulticastSocket s;
    
    public NodeMulticastSocket (String groupIP, int port) throws UnknownHostException, IOException{
        
        InetAddress group = InetAddress.getByName(groupIP);
        s = new MulticastSocket (port);
        s.joinGroup(group);
        
        Thread thread_recv = new Thread(new ReceiveFromGroup()); //multiplicar para checkar
        thread_recv.start();
        
    }
    
    /*
    Envia para o grupo multicast
    */
    public void send (DatagramPacket packet) throws IOException{
        s.send(packet);
    }
    
    /*
    Recebe do grupo muticast. Cada string é um nova edge.Forma da string: "v1_v2_w"
    */
    public class ReceiveFromGroup implements Runnable {

        @Override
        public void run(){
            
            while(true){
                try {
                    byte[] receiveData=new byte[ 64*1024 ];
                    int receivedBytes = 0;
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                    s.receive(packet);
                    String str = new String(packet.getData(), 0, packet.getLength());
                    /*
                    Caso seja para receber mais do q 1 edge por novo node descomentar isto:
                    if(str == "DONE" ){
                        NodeDatagramSocket.makeDijkstra();
                        continue;
                    }
                    */
                    String[] split = str.split("_");
                    NodeDatagramSocket.addEdge(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[2]));
                    NodeDatagramSocket.MakeDijkstra(); //comentar isto se for para usar o "DONE"
                } catch (IOException ex) {
                    Logger.getLogger(NodeMulticastSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}

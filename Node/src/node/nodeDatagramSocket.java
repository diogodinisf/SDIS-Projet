/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class nodeDatagramSocket {
    DatagramSocket socket;
    double[] delayVal = new double[1000];
    int selfId;
    
    public nodeDatagramSocket(int id) throws SocketException{
        socket= new DatagramSocket();
        this.selfId = id;
        Thread thread = new Thread(new getDataFromControler());
        thread.start();
    }
    
    public nodeDatagramSocket(int port, int id) throws SocketException{
        socket= new DatagramSocket(port);
        this.selfId = id;
        Thread thread = new Thread(new getDataFromControler());
        thread.start();
    }
    
    public void send(DatagramPacket packet) throws IOException{
        int toId = packet.getPort()-5555;
        double wait = delayVal[toId];
        if(wait == 0 && toId != selfId){ //no path for that node
            return;
        }
        Thread thread = new Thread(new sendData(packet,wait*10000)); //multiplicar para checkar
        thread.start();
        
    }
    
    //thread para enviar as mensagens
    public class sendData implements Runnable {
        
        DatagramPacket packet;
        double wait;
        
        public sendData(DatagramPacket packet, double wait){
            this.packet=packet;
            this.wait=wait;
        }
        @Override
        public void run(){
            
            try {
                System.out.println("waiting "+ wait + " seconds");
                Thread.sleep((long) wait);
                socket.send(packet);
            } catch (InterruptedException ex) {
                Logger.getLogger(nodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(nodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    //receber a tabela de delays do djusktra, é lançado assim que se cria uma socket. 
    //tenho de correr em loop???
    //verificar
    public class getDataFromControler implements Runnable {

    @Override
    public void run(){
        while(true){
            System.out.println("MyRunnable running");
             byte[] receiveData=new byte[ 64*1024 ];
             int Nodes = 4;
             int PORT=4444 + selfId;
             System.out.println("porta "+PORT);
            
             int receivedBytes = 0;
             double[] delay = new double[Nodes] ; //nao sei como criar um vetor de outra maneira
            try {
                DatagramSocket socketDelay = new DatagramSocket(PORT);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                
                
                socketDelay.receive(packet); 
                receivedBytes = packet.getLength();
                    
                byte[] myObject = new byte[receivedBytes];
                 for(int i = 0; i < receivedBytes; i++)
                 {
                  myObject[i] = receiveData[i];
                 }
                 ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(myObject));
                 double[] data = (double[]) iStream.readObject();
                 iStream.close();
                 delayVal =new double[data.length];
                 delayVal = data;
                 for(int i =0; i< Nodes ; i++){
                     System.out.println(delayVal[i]);
                 }
                 System.out.println("recebi o delay");
                 socketDelay.close();
                
            } catch (SocketException ex) {
                Logger.getLogger(nodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }catch(IOException e){
                 System.out.println("IOException in UdpReceiver.receive: "+e);
             } catch (ClassNotFoundException ex) {
                Logger.getLogger(nodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }

             
        }  
    }
  }
}


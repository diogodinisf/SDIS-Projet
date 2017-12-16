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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class nodeDatagramSocket {
    static DatagramSocket socket;
    double[] delayVal = new double[1000];
    //int selfId;
    static double[] ports = new double[1000];
    
    public nodeDatagramSocket() throws SocketException{
        socket= new DatagramSocket();
        //this.selfId = id;
        DatagramSocket socketDelay = new DatagramSocket(Node.getPort() + 5000); //receber do master os delays
        DatagramSocket socketPorts = new DatagramSocket(Node.getPort() + 10000); //receber do master as portas AO INICAR O NO
        Thread threadDelay = new Thread(new getDataFromControler(socketDelay, "delay"));
        threadDelay.start();
        Thread threadPorts = new Thread(new getDataFromControler(socketPorts, "ports"));
        threadPorts.start();
    }
    
    public nodeDatagramSocket(int port) throws SocketException{
        socket= new DatagramSocket(port);
        //this.selfId = id;
        DatagramSocket socketDelay = new DatagramSocket(Node.getPort() + 5000);
        DatagramSocket socketPorts = new DatagramSocket(Node.getPort() + 10000);
        Thread threadDelay = new Thread(new getDataFromControler(socketDelay, "delay"));
        threadDelay.start();
        Thread threadPorts = new Thread(new getDataFromControler(socketPorts, "ports"));
        threadPorts.start();
    }
    
    public void send(DatagramPacket packet) throws IOException{
        int toPort = packet.getPort();
        int toId=0;
        for (int i=0 ; i<ports.length ; i++){
            if(toPort == ports[i]){
                toId = i;
                break;
            }
        }
        double wait = delayVal[toId];

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
               // System.out.println("waiting "+ wait + " seconds");
                Thread.sleep((long) wait);
                socket.send(packet);
            } catch (InterruptedException ex) {
                Logger.getLogger(nodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(nodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    //funcao que cada no usa para adicionar uma nova porta que é recebida por multicast
    public static void addNode (int length, double port){
        
        ports=Arrays.copyOf(ports, length+1);
        ports[length]=port;
      //  System.out.println("New port arrived");
      //  for (int i =0 ; i<=length ; i++){
       //     System.out.println(ports[i]);
       // }
    }
    
    //receber a tabela de delays do djusktra, as portas é lançado assim que se cria uma socket. 
    //tenho de correr em loop???
    //verificar
    public class getDataFromControler implements Runnable {
        
        DatagramSocket socketDelay;
        String dataRecv;
        
        public getDataFromControler(DatagramSocket delay , String data){
            socketDelay = delay;
            dataRecv=data;
        }

    @Override
    public void run(){
        boolean tempo = false;
        while(true){
            byte[] receiveData=new byte[ 64*1024 ];
        
            
            int receivedBytes = 0;
            try {
                
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
                if(dataRecv.equals("delay")){
                  //  System.out.println("New delay vectors");
                    delayVal =new double[data.length];
                    delayVal = data;
                   /* for(int i =0; i< delayVal.length ; i++){
                        System.out.println(delayVal[i]);
                    }*/
                    //System.out.println("recebi o delay");
                    if (tempo==false){
                        double total_time = System.currentTimeMillis();
                        System.out.println("no "+ Node.id +" demorou "+(total_time - Node.time_init));
                        tempo=true;
                    }
                    

                }
                else if (dataRecv.equals("ports")){
                    //System.out.println("New port vectors");
                    ports = new double[data.length];
                    ports = data;
                    /*for(int i =0; i< ports.length ; i++){
                        System.out.println(ports[i]);
                    }
                    System.out.println("recebi as ports");*/
                }
                

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


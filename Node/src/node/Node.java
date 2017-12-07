/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

/**
 *
 * @author eduardo
 */
public class Node {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException, IOException {

        System.out.print("Sou o Node: ");
        System.out.println(args[1]);
        int id = Integer.parseInt(args[1]);
        int Nodes =4; // so para teste
        
        //double[] delay = getDataFromControler(id);
        //for (int i=0; i< Nodes; i++){
        //    System.out.println(delay[i]);
        //}
        Thread thread_send = new Thread(new threadToSend(id)); //multiplicar para checkar
        thread_send.start();
        
        Thread thread_recv = new Thread(new threadToReceive(id)); //multiplicar para checkar
        thread_recv.start();
        
        /*if (id == 1){
            nodeDatagramSocket socket = new nodeDatagramSocket(id);
            InetAddress address = InetAddress.getByName("localhost");
            while(true){
         
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter your msg: ");
                String str = scanner.next();
                int PORT = 5555 + 2;
                
                DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), address, PORT);
                socket.send(packet);
            }
            
        }
        else if(id ==2){
                int PORT = 5555 + id;
                DatagramSocket socket = new DatagramSocket(PORT);
            while(true){
                byte[] receiveData=new byte[ 64*1024 ];
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String str = new String(packet.getData(), 0, packet.getLength());  
                System.out.println(str);      
            }
        }
   */
  
    }
    
    //PROBLEMA: esta funcao exige saber o numero total de nodes do sistema, vai ser necessario lidar com isto
    //talvez ter o master a dizer quantos ha ? 
    
    public static double[] getDataFromControler(int id) throws SocketException{
        
        byte[] receiveData=new byte[ 64*1024 ];
        int PORT = 4444 + id;
        int Nodes = 4;
        DatagramSocket socket = new DatagramSocket(PORT);
        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
        double[] delay = new double[Nodes] ; //nao sei como criar um vetor de outra maneira
     
        int receivedBytes = 0;
        try{
            socket.receive(packet); 
            receivedBytes = packet.getLength();
        }catch(IOException e){
            System.out.println("IOException in UdpReceiver.receive: "+e);
            return null;
        }

        byte[] myObject = new byte[receivedBytes];

        for(int i = 0; i < receivedBytes; i++)
        {
             myObject[i] = receiveData[i];
        }
        
        try
        {
            ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(myObject));
            double[] data = (double[]) iStream.readObject();
            iStream.close();
            delay = data;

        }catch(Exception e){
                System.out.println("Erro "+e);
         }
        socket.close();
        
        return delay;
    }
    
}

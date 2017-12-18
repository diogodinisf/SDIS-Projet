/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class NodeDatagramSocket {
    private static DatagramSocket socket;
    private final int port;
    private double[] delayVal = new double[1000];
    private static Integer[] ports = new Integer[1000];
    private static final Map nodeMap = Collections.synchronizedMap(new HashMap<Integer, Double>());
    //int selfId;
    
    /*public NodeDatagramSocket() throws SocketException{
        socket = new DatagramSocket();
        //this.selfId = id;
        DatagramSocket socketDelay = new DatagramSocket(Node.getPort() + 5000); //receber do master os delays
        DatagramSocket socketPorts = new DatagramSocket(Node.getPort() + 10000); //receber do master as portas AO INICAR O NO
        
        Thread threadDelay = new Thread(new getDataFromControler(socketDelay, "delay"));
        threadDelay.start();
        Thread threadPorts = new Thread(new getDataFromControler(socketPorts, "ports"));
        threadPorts.start();
    }*/
    
    public NodeDatagramSocket(int port) throws SocketException {
        socket = new DatagramSocket(port);
        //this.selfId = id;
        
        this.port  = port;
        
        DatagramSocket socketDelay = new DatagramSocket(port + 5000);
        DatagramSocket socketPorts = new DatagramSocket(port + 10000);
        
        Thread threadDelay = new Thread(new getDelaysFromControler(socketDelay));
        threadDelay.start();
        Thread threadPorts = new Thread(new getPortsFromControler(socketPorts));
        threadPorts.start();
    }
    
    public void send(DatagramPacket packet) throws IOException {
        int toPort = packet.getPort();
        int toId = 0;
        
        for (int i = 0 ; i < ports.length ; i++) {
            if (toPort == ports[i]) {
                toId = i;
                break;
            }
        }
        
        double wait = delayVal[toId];
        
        Thread thread = new Thread(new sendData(packet, wait * 10000)); //multiplicar para checkar
        thread.start();
        
    }
    
    //thread para enviar as mensagens
    public class sendData implements Runnable {
        private final DatagramPacket packet;
        private final double wait;
        
        public sendData(DatagramPacket packet, double wait) {
            this.packet = packet;
            this.wait = wait;
        }

        @Override
        public void run() {
            
            try {
                Thread.sleep((long) wait);
                socket.send(packet);
                
            } catch (InterruptedException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //funcao que cada no usa para adicionar uma nova porta que é recebida por multicast
    public static void addNode (int length, int port) {
        nodeMap.put(port, null);
    }
    
    //receber a tabela de delays do djusktra, as portas é lançado assim que se cria uma socket. 
    //tenho de correr em loop???
    //verificar
    public class getDelaysFromControler implements Runnable {
        private final DatagramSocket socketDelay;
        
        public getDelaysFromControler(DatagramSocket delay){
            socketDelay = delay;
        }

        @Override
        public void run() {
            boolean tempo = false;
            
            while (true) {
                byte[] receiveData = new byte[64 * 1024];
                int receivedBytes;
                
                try {
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                
                    socketDelay.receive(packet);
                    receivedBytes = packet.getLength();
                    byte[] myObject = new byte[receivedBytes];
                    
                    for(int i = 0; i < receivedBytes; i++) {
                        myObject[i] = receiveData[i];
                    }
                    
                    ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(myObject));
                    Double[][] data = (Double[][]) iStream.readObject();
                    iStream.close();
                    
                    for (int i = 0; i < data.length; i++) {
                        nodeMap.put(data[i][0].intValue(), data[i][1]);
                    }
                    
                    if (tempo == false){
                        double total_time = System.currentTimeMillis();
                        System.out.println("porta " + port + " demorou " + (total_time - OverlayNetworkNode.getInitTime()));
                        tempo = true;
                    }
                    
                } catch (SocketException | ClassNotFoundException ex) {
                    Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
                }catch(IOException e){
                    System.out.println("IOException in UdpReceiver.receive: " + e);
                }
            }  
        }
    }
    
    public class getPortsFromControler implements Runnable {
        private final DatagramSocket socketDelay;
        
        public getPortsFromControler(DatagramSocket delay) {
            socketDelay = delay;
        }

        @Override
        public void run() {
            while (true) {
                byte[] receiveData = new byte[64 * 1024];
                int receivedBytes;
                
                try {
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                
                    socketDelay.receive(packet);
                    receivedBytes = packet.getLength();
                    byte[] myObject = new byte[receivedBytes];
                    
                    System.arraycopy(receiveData, 0, myObject, 0, receivedBytes);
                    
                    ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(myObject));
                    Integer[] data = (Integer[]) iStream.readObject();
                    iStream.close();
                    
                    for (Integer port1: data) {
                        nodeMap.put(port1, null);
                    }
                    
                } catch (SocketException | ClassNotFoundException ex) {
                    Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
                }catch(IOException e){
                    System.out.println("IOException in UdpReceiver.receive: " + e);
                }
            }  
        }
    }
}


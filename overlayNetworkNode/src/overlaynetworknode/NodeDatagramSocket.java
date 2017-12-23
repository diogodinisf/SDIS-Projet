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
import java.net.SocketException;
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
    private boolean running;
    private final DatagramSocket socket;
    private final DatagramSocket socketDelay;
    private final int port;
    private Map<Node_type, Double> nodeMap = Collections.synchronizedMap(new HashMap<>());

    public NodeDatagramSocket(int port) throws SocketException {
        this.port  = port;
        socket = new DatagramSocket();
        
        socketDelay = new DatagramSocket(this.port + 5000);
        Thread threadDelay = new Thread(new getOverlayNetworkFromControler(socketDelay));
        threadDelay.start();
    }
    
    public void printNodesMap() {
        nodeMap.entrySet().forEach((node) -> {
            System.out.println("(Nó " + port + ") " + (node.getKey()).toString() + " :: Delay: " + node.getValue());
        });
    }
    
    public void send(DatagramPacket packet) throws IOException {
        int toPort = packet.getPort();
        double wait = 0;
        
        for (Map.Entry<Node_type, Double> node : nodeMap.entrySet()) {
            if ((node.getKey()).getPort() == toPort) {
                wait = (double) node.getValue();
                break;
            }
        }
        
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
                
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //receber a tabela de delays do djusktra, as portas é lançado assim que se cria uma socket. 
    //tenho de correr em loop???
    //verificar - podes correr em loop - ele vai bloquear no socketDelay.receive(), logo só percorre o loop quando recebe um pacote
    public class getOverlayNetworkFromControler implements Runnable {
        private final DatagramSocket socketDelay;
        
        public getOverlayNetworkFromControler(DatagramSocket socketDelay){
            this.socketDelay = socketDelay;
        }

        @Override
        public void run() {
            boolean tempo = false;
            running = true;
            
            while (running) {
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
                    Object object = iStream.readObject();
                    
                    try {
                        String toClose = (String)object;
                        
                         if (toClose.contentEquals("close")) {
                        Display.alert("\nMandado fechar");
                        running = false;
                         }
                        
                    } catch (ClassCastException e) {
                        nodeMap.clear();
                        nodeMap.putAll((Map<Node_type, Double>)object);

                        if (tempo == false){
                            double total_time = System.currentTimeMillis();
                            Display.info("\nporta " + port + " demorou " + (total_time - OverlayNetworkNode.getInitTime()));
                            tempo = true;
                        }
                    }
                    iStream.close();
                } catch (SocketException | ClassNotFoundException ex) {
                    Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
                } catch(IOException e){
                    System.out.println("IOException in UdpReceiver.receive: " + e);
                }
            }  
            
            socket.close();
            socketDelay.close();
        }
    }
}


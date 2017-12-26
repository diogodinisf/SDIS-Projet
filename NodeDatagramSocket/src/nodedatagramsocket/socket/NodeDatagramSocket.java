/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodedatagramsocket.socket;

import nodedatagramsocket.utils.Display;
import nodedatagramsocket.utils.Node_type;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class NodeDatagramSocket {
    private int id;
    private boolean running;
    
    private final int masterPort;
    private final String masterHostname;
    private final double timeInit;
    private final String hostname;
    private final DatagramSocket socket;
    private final DatagramSocket socketDelay;
    private final int port;
    private Map<Node_type, Double> nodeMap = Collections.synchronizedMap(new HashMap<>());

    public NodeDatagramSocket(int port, String masterHostname, int masterPort) throws SocketException {
        this.port = port;
        this.masterHostname = masterHostname;
        this.masterPort = masterPort;
        
        timeInit = System.currentTimeMillis();
        
        hostname = getMyAddress();
        Display.alive("(" + id + ") " + hostname + ":" + port);
        
        socket = new DatagramSocket(this.port);
        socketDelay = new DatagramSocket(this.port + 5000);
        Thread threadDelay = new Thread(new getOverlayNetworkFromControler(socketDelay));
        threadDelay.start();
        
        joinOverlayNetwork();
    }
    
    public NodeDatagramSocket(String masterHostname, int masterPort) throws SocketException {
        this.masterHostname = masterHostname;
        this.masterPort = masterPort;
        
        timeInit = System.currentTimeMillis();
        
        socket = new DatagramSocket();
        this.port = socket.getPort();
        hostname = getMyAddress();
        Display.alive("(" + id + ") " + hostname + ":" + port);
        
        socketDelay = new DatagramSocket(this.port + 5000);
        Thread threadDelay = new Thread(new getOverlayNetworkFromControler(socketDelay));
        threadDelay.start();
        
        joinOverlayNetwork();
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
    
    public void receive(DatagramPacket packet) throws IOException {
        socket.receive(packet);
    }
    
    private void joinOverlayNetwork() {
        try {
            String str = hostname + "_" + port;
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), InetAddress.getByName(masterHostname), masterPort);
            socket.send(packet);
        } catch (UnknownHostException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //thread para enviar as mensagens
    private class sendData implements Runnable {
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
    private class getOverlayNetworkFromControler implements Runnable {
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
                    String toClose = new String(packet.getData(), 0, packet.getLength());
                    
                    if (toClose.contentEquals("close")) {
                        Display.alert(getMyAddress() + ":" + port + " Sockets fechados pelo Manager");
                        running = false;
                    } else {
                        receivedBytes = packet.getLength();
                        byte[] myObject = new byte[receivedBytes];

                        for(int i = 0; i < receivedBytes; i++) {
                            myObject[i] = receiveData[i];
                        }

                        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(myObject));
                        Object object = iStream.readObject();
                            
                        nodeMap.clear();
                        nodeMap.putAll((Map<Node_type, Double>)object);

                        if (tempo == false) {
                            double total_time = System.currentTimeMillis();
                            Display.info(getMyAddress() + ":" + port + " adquiriu lista de atrados em " + (total_time - getInitTime()));
                            tempo = true;
                        }

                        iStream.close();
                    }
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
    
    public int getPort() {
        return port;
    }
    
    public DatagramSocket getSocket() {
        return socket;
    }
    
    public double getInitTime() {
        return timeInit;
    }
    
    public static String getMyAddress() {
        String address = null;

        // obter o hostname, credo que esta treta demora, vamos acreditar que aqui não se usa endereços do tipo 10.x.x.x
        Enumeration e;
        try {
            e = NetworkInterface.getNetworkInterfaces();
        
            while(e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
        
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String host = i.getHostAddress();
                    if (host.contains(".")) {
                        if ((host.split("\\."))[0].equalsIgnoreCase("192")) {
                            address = host;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return address;
    }
    
    public void printNodesMap() {
        nodeMap.entrySet().forEach((node) -> {
            if (port == node.getKey().getPort()) {
                Display.receive((node.getKey()).toString() + " :: Delay: " + node.getValue());
            } else {
                System.out.println((node.getKey()).toString() + " :: Delay: " + node.getValue());
            }
        });
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodedatagramsocket.socket;

import nodedatagramsocket.utils.Display;
import nodedatagramsocket.utils.NodeType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
    private int myId = -1;
    private boolean running;
    
    private final int masterPort;
    private final String masterHostname;
    private final double timeInit;
    private final String hostname;
    private final DatagramSocket socket;
    private final DatagramSocket socketDelay;
    private final int port;
    private final int delayPort;
    private Map<NodeType, double[]> nodeMap = Collections.synchronizedMap(new HashMap<>());

    public NodeDatagramSocket(int port, String masterHostname, int masterPort) throws SocketException {
        this.port = port;
        this.delayPort = port + 5000;
        this.masterHostname = masterHostname;
        this.masterPort = masterPort;
        this.hostname = getMyAddress();
        
        timeInit = System.currentTimeMillis();

        socket = new DatagramSocket(this.port);
        socketDelay = new DatagramSocket(this.delayPort);
        Thread threadDelay = new Thread(new getOverlayNetworkFromControler(socketDelay));
        threadDelay.start();
        
        Display.alive(hostname + ":" + port + " | delay port: " + delayPort);
        
        joinOverlayNetwork();
    }
    
    public NodeDatagramSocket(String masterHostname, int masterPort) throws SocketException {
        this.masterHostname = masterHostname;
        this.masterPort = masterPort;
        this.hostname = getMyAddress();
        
        timeInit = System.currentTimeMillis();
        
        socket = new DatagramSocket();
        socketDelay = new DatagramSocket();
        this.port = socket.getLocalPort();
        this.delayPort = socketDelay.getLocalPort();
        
        Thread threadDelay = new Thread(new getOverlayNetworkFromControler(socketDelay));
        threadDelay.start();
        
        Display.alive(hostname + ":" + port + " | delay port: " + delayPort);
        
        joinOverlayNetwork();
    }

    public void send(DatagramPacket packet) throws IOException {
        int toPort = packet.getPort();
        int id = -1;
        double wait = 0;
        double errorRate = 0;
        
        for (Map.Entry<NodeType, double[]> node : nodeMap.entrySet()) {
            if ((node.getKey()).getPort() == toPort) {
                wait = (node.getValue())[0];
                errorRate = (node.getValue())[1];
                id = node.getKey().getId();
                break;
            }
        }
        
        if (Math.random() > errorRate) {
            (new Thread(new sendData(packet, wait * 1000))).start();
        } else {
            Display.alert("Falhou envio para " + id);
        }
    }
    
    public void receive(DatagramPacket packet) throws IOException {
        socket.receive(packet);
    }
    
    private void joinOverlayNetwork() {
        try {
            String str = "Hello_" + delayPort;
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
        private final int timeout = 1000; // 1 segundo à espera que o master responda
        
        public getOverlayNetworkFromControler(DatagramSocket socketDelay){
            this.socketDelay = socketDelay;
        }

        @Override
        public void run() {
            boolean tempo = false;
            running = true;
            
            try {
                socketDelay.setSoTimeout(timeout);
            } catch (SocketException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            while (running) {
                byte[] receiveData = new byte[64 * 1024];
                int receivedBytes;
                
                try {
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                
                    socketDelay.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    
                    if (message.contentEquals("hello")) {
                        socketDelay.setSoTimeout(0); // retira o timeout - espera indefinidamente
                    } else if (message.contentEquals("close")) {
                        if (getId() == -1) {
                            Display.alert("Socket fechado pelo Manager");
                        } else {
                            Display.alert("(" + getId() + ")" + " Socket fechado pelo Manager");
                        }
                        
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
                        nodeMap.putAll((Map<NodeType, double[]>)object);

                        if (tempo == false) {
                            double total_time = System.currentTimeMillis();
                            getMyId();
                            Display.receive(getMyAddress() + ":" + port + " recebeu o ID: " + getId());
                            Display.info("(" + getId() + ")" + " adquiriu lista de atrados em " + (total_time - getInitTime()) + "ms");
                            tempo = true;
                            socketDelay.setSoTimeout(0); // retira o timeout - espera indefinidamente
                        }

                        iStream.close();
                    }
                } catch(SocketTimeoutException ex) {
                    Display.alert("Controlador não respondeu ao pedido de rede, timeout de " + timeout + " ms");
                    Display.alive("Verifique que inicializou o controlador.");
                    running = false;
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
    
    private void getMyId() {
        for (Map.Entry<NodeType, double[]> node : nodeMap.entrySet()) {
            if (port == node.getKey().getPort()) {
                if (node.getKey().getIp().equalsIgnoreCase(hostname)) {
                    myId = node.getKey().getId();
                }
            } 
        }
    }
    
    public int getId() {
        return myId;
    }
    
    
    public int getIdByAddress(String address) {
        int id = -1;
        String[] split = address.split(":");
        
        for (Map.Entry<NodeType, double[]> node : nodeMap.entrySet()) {
            if (Integer.parseInt(split[1]) == node.getKey().getPort()) {
                if (node.getKey().getIp().equalsIgnoreCase(split[0])) {
                    id = node.getKey().getId();
                }
            } 
        }
        
        return id;
    }
    
    public DatagramSocket getSocket() {
        return socket;
    }
    
    public double getInitTime() {
        return timeInit;
    }
    
    public int getPortById(int id) throws NullPointerException {
        int port = -1;
        
        for (Map.Entry<NodeType, double[]> node : nodeMap.entrySet()) {
            if (id == node.getKey().getId()) {
                port = node.getKey().getPort();
            } 
        }
        
        if (port == -1) {
            throw new NullPointerException("Não existe nenhum nó com o ID " + id + "!");
        }
        
        return port;
    }
    
    public String getIpById(int id) {
        String address = null;
        
        for (Map.Entry<NodeType, double[]> node : nodeMap.entrySet()) {
            if (id == node.getKey().getId()) {
                address = node.getKey().getIp();
            } 
        }
        
        if (address == null) {
            throw new NullPointerException("Não existe nenhum nó com o ID " + id + "!");
        }
        
        return address;
    }
    
    public static String getMyAddress() {
        String address = null;
        
        // isto não é uma maneira muito bonita de encontrar o endereço da máquina
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
                        
                        if ((host.split("\\."))[0].equalsIgnoreCase("193")) {
                            address = host;
                        }
                        
                        if ((host.split("\\."))[0].equalsIgnoreCase("172")) {
                            address = host;
                        }
                        
                        if ((host.split("\\."))[0].equalsIgnoreCase("173")) {
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
        for (Map.Entry<NodeType, double[]> node : nodeMap.entrySet()) {
            if (port == node.getKey().getPort()) {
                Display.receive((node.getKey()).toString());
            } else {
                System.out.println((node.getKey()).toString() + " | Delay: " + node.getValue()[0] + " | T.falhas: " + node.getValue()[1]);
            }
        }
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;


//import edu.princeton.cs.algs4.DijkstraUndirectedSP;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.StdOut;

//import edu.princeton.cs.algs4.EdgeWeightedGraph;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nodedatagramsocket.utils.NodeType;

/**
 * @author eduardo
 */
public class Controller {
    private static final int MASTER_PORT = 6789;
    private final List<NodeType> nodes = new ArrayList<>();
    
    private boolean running;
    private int Nodes = 0;
    private EdgeWeightedGraph G;
    
    // For each node calculates the shortest path to every other node. Calls sendArray() to send an array with the
    // transmission delay to each node.
    // @params: EdgeWeightedGraph G - Graph made using edu.princeton.cs.algs4.EdgeWeightedGraph;
    public void makeDjikstra(EdgeWeightedGraph G) {
        int s, t;
        
        for (NodeType node : nodes) {
            Map<NodeType, Double> nodeMap = new HashMap<>();
            
            s = node.getId();
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            
            for (NodeType otherNode : nodes) {
                t = otherNode.getId();
                
                 if (sp.hasPathTo(t)) {
                    nodeMap.put(otherNode, sp.distTo(t));
                }
            }
            
            sendOverlayNetwork(node.getIp(), node.getDelayPort(), nodeMap);
        }
    }
    
    public void showDjikstra() {
        for (int s = 0; s <  G.V(); s++) {
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            
            for (int t = 0; t < G.V(); t++) {
                if (sp.hasPathTo(t)) {
                    StdOut.printf("%d to %d (%.2f)", s, t, sp.distTo(t));
                    
                    for (Edge e : sp.pathTo(t)) {
                        StdOut.print(" " + e);
                    }
                    StdOut.println();
                } else {
                    StdOut.printf("%d to %d " + "\u001B[31m" + "no path" + "\u001B[0m" + "\n", s, t);
                }
            }
        }
    }
    
    public void closeAllNodes() {
        String msg = "close";
        
        try {    
            DatagramSocket socket = new DatagramSocket();
            
            nodes.forEach((NodeType node) -> {
                sendMessage("close", node, socket);
            });
            
            socket.close();
        } catch (SocketException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeNode(int id) {
        String msg = "close";
        
        try {    
            DatagramSocket socket = new DatagramSocket();
            
            NodeType node = getNodeById(id);
            sendMessage("close", node, socket);
            
            G = G.removeNode(G, id);
            nodes.remove(getNodeById(id));
            makeDjikstra(G);
            
            socket.close();
        } catch (NullPointerException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.WARNING, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public NodeType getNodeById(int id) throws NullPointerException {
        NodeType node = null;
        
        for (NodeType node1: nodes) {
            if (node1.getId() == id) {
                node = node1;
            }
        }
        
        if (node == null) {
            throw new NullPointerException("O nó que está a tentar fechar não existe no sistema!");
        }
        
        return node;
    }
    
    // pede a todos os nós para fecharem
    public void sendMessage(String msg, NodeType node, DatagramSocket socket) {
        try {
            InetAddress address = InetAddress.getByName(node.getIp());
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), address, node.getDelayPort());
            socket.send(packet);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Sends to each node, via UDP, and array of doubles with the weights to every other node.
    //@params: data - array with wights, calculated in makeDjikstra(); id - node id from command line.
    public void sendOverlayNetwork(String ip, int port, Map nodeMap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(nodeMap);
            oos.close();
            byte[] obj= baos.toByteArray();
            baos.close();
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(obj, obj.length, address, port);
            socket.send(packet);
            socket.close();
        } catch(Exception e) {
            System.out.println("Erro " + e);
        } 
    }

    public void run() throws SocketException, IOException {
        DatagramSocket socket = new DatagramSocket(MASTER_PORT);
        G = new EdgeWeightedGraph(Nodes); 
        running = true;
        socket.setSoTimeout(5); // para usar com o close
        
        Thread managerScanner = new Thread(new ManagerScanner(this));
        managerScanner.start();   
     
        while (running) {
            byte[] receiveData = new byte[1024];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            
            try {
                socket.receive(packet);
                
                String str = new String(packet.getData(), 0, packet.getLength());
                String[] split = str.split("_");
                
                if (split[0].equalsIgnoreCase("Hello")) {
                    NodeType node = new NodeType(Nodes, packet.getAddress().getHostAddress().replace("/", ""), packet.getPort(), Integer.parseInt(split[1]));
                    nodes.add(node);

                    Nodes = Nodes + 1;
                    if (Nodes == 1) {
                        G = new EdgeWeightedGraph(Nodes); 
                        
                        try {    
                            DatagramSocket socketHello = new DatagramSocket();
                            sendMessage("hello", node, socketHello);
                            socketHello.close();
                        } catch (SocketException ex) {
                            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        G.addNode();
                        int vertice1 = (int)(Math.random() * (Nodes - 1));
                        int vertice2 = Nodes - 1 ;

                        Edge E = new Edge(vertice1, vertice2, Math.random());
                        G.addEdge(E);
                        makeDjikstra(G);
                    }
                }
            } catch (SocketTimeoutException e) { 
                //do nothing
            }
        }
    }
        
    public void printNodesList() {
        nodes.forEach((NodeType node) -> {
            System.out.println(node.toString());
        });
    }
    
    public void printNodesMap(Map<NodeType, Double> nodeMap) {
        nodeMap.entrySet().forEach((node) -> {
            System.out.println((node.getKey()).toString() + " :: Delay: " + node.getValue());
        });
    }
    
    public EdgeWeightedGraph getEdgeWeightGraph() {
        return G;
    }
    
    public void close() {
        running = false;
        closeAllNodes();
    }
    
    public static void main(String[] args) throws SocketException, IOException {
        Controller master = new Controller();
        master.run();        
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;


//import edu.princeton.cs.algs4.DijkstraUndirectedSP;
import nodedatagramsocket.utils.Node_type;
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

/**
 * @author eduardo
 */
public class Controller {
    private static final int MASTER_PORT = 6789;
    private final List<Node_type> nodes = new ArrayList<>();
    
    private boolean running;
    private int Nodes = 0;
    private EdgeWeightedGraph G;
    
    // For each node calculates the shortest path to every other node. Calls sendArray() to send an array with the
    // transmission delay to each node.
    // @params: EdgeWeightedGraph G - Graph made using edu.princeton.cs.algs4.EdgeWeightedGraph;
    public void makeDjikstra(EdgeWeightedGraph G) {
        int s, t;
        
        for (Node_type node : nodes) {
            Map<Node_type, Double> nodeMap = new HashMap<>();
            
            s = node.getId();
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            
            for (Node_type otherNode : nodes) {
                t = otherNode.getId();
                
                 if (sp.hasPathTo(t)) {
                    nodeMap.put(otherNode, sp.distTo(t));
                }
            }
            
            sendOverlayNetwork(node.getIp(), node.getPort(), nodeMap);
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
    
    // pede a todos os nós para fecharem - porreiro para debugging
    public void sendClose() {
        String msg = "close";
        
        try {    
            DatagramSocket socket = new DatagramSocket();
            
            nodes.forEach((node) -> {
                try {
                    InetAddress address = InetAddress.getByName(node.getIp());
                    DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), address, node.getPort() + 5000);
                    socket.send(packet);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
            socket.close();
        } catch (SocketException ex) {
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
            DatagramPacket packet = new DatagramPacket(obj, obj.length, address, port + 5000);
            socket.send(packet);
            socket.close();
        } catch(Exception e) {
            System.out.println("Erro " + e);
        } 
    }

    public void run() throws SocketException, IOException {
        DatagramSocket socket = new DatagramSocket (MASTER_PORT);
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

                Node_type node;
                node = new Node_type(Nodes, split[0], Integer.parseInt(split[1]));
                nodes.add(node);

                Nodes = Nodes + 1;
                if (Nodes == 1) {
                    G = new EdgeWeightedGraph(Nodes); 
                } else {
                    G.addNode();
                    int vertice1 = (int)(Math.random() * (Nodes - 1));
                    int vertice2 = Nodes - 1 ;

                    Edge E = new Edge(vertice1, vertice2, Math.random());
                    G.addEdge(E);
                    makeDjikstra(G);
                }   
            } catch (SocketTimeoutException e) { 
                //do nothing
            }
        }
    }
        
    public void printNodesList() {
        nodes.forEach((Node_type node) -> {
            System.out.println(node.toString());
        });
    }
    
    public void printNodesMap(Map<Node_type, Double> nodeMap) {
        nodeMap.entrySet().forEach((node) -> {
            System.out.println((node.getKey()).toString() + " :: Delay: " + node.getValue());
        });
    }
    
    public EdgeWeightedGraph getEdgeWeightGraph() {
        return G;
    }
    
    public void close() {
        running = false;
        sendClose();
    }
    
    public static void main(String[] args) throws SocketException, IOException {
        Controller master = new Controller();
        master.run();        
    }
}

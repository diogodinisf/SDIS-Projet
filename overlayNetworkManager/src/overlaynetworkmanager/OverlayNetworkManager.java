/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworkmanager;


//import edu.princeton.cs.algs4.DijkstraUndirectedSP;
import overlaynetworknode.Node_type;
import edu.princeton.cs.algs4.Edge;

//import edu.princeton.cs.algs4.EdgeWeightedGraph;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager da rede. Multicast group no 228.5.6.7:6789. Nós ao serem criados têm de avisar o multicast group da porta onde estao a ouvir.
 * @author eduardo
 */
public class OverlayNetworkManager {
    
    static int Nodes = 0;
    static int Edges = 0;
    static List<Node_type> nodes = new ArrayList<>();
    
    // For each node calculates the shortest path to every other node. Calls sendArray() to send an array with the
    // transmission delay to each node.
    // @params: EdgeWeightedGraph G - Graph made using edu.princeton.cs.algs4.EdgeWeightedGraph;
    public void makeDjikstra(EdgeWeightedGraph G) {
        int s, t;
        
        for (Node_type node : nodes) {
            Map<Node_type,Double> nodeMap = new HashMap<>();
            
            s = node.getId();
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            
            for (Node_type otherNode : nodes) {
                t = otherNode.getId();
                
                 if (sp.hasPathTo(t)) {
                    nodeMap.put(otherNode, sp.distTo(t));
                }
            }
            
            printNodesMap(nodeMap);
            sendOverlayNetwork(node.getPort(), nodeMap);
        }
    }
    
  
    //Sends to each node, via UDP, and array of doubles with the weights to every other node.
    //@params: data - array with wights, calculated in makeDjikstra(); id - node id from command line.
    public static void sendOverlayNetwork(int port, Map nodeMap){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(nodeMap);
            oos.close();
            byte[] obj= baos.toByteArray();
            baos.close();
            DatagramSocket socket= new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(obj, obj.length, address, port + 5000);
            socket.send(packet);
        } catch(Exception e) {
            System.out.println("Erro " + e);
        } 
    }

    public void run() throws SocketException, IOException {
        DatagramSocket socket = new DatagramSocket (6789);
        EdgeWeightedGraph G = new EdgeWeightedGraph(Nodes); 
        
        while (true) {
            byte[] receiveData = new byte[1024];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);
            String str = new String(packet.getData(), 0, packet.getLength());
            String[] split = str.split("_");
            
            Node_type node;
            if (split.length > 1) {
                node = new Node_type(Nodes, split[0], Integer.parseInt(split[1])); // com ip - para futuro
            } else {
                node = new Node_type(Nodes, Integer.parseInt(split[0]));
            }
            
            System.out.println("Apareceu nó com porta: " + node.getPort());
            nodes.add(node);
            
            //printNodesList();
            
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
           // System.out.println("I know these ports");
            /*for (int i =0 ; i< Nodes ; i++){
                System.out.println(ports[i]);
            }*/
            //System.out.println("end of known ports");
            //sendPorts(ports, Nodes-1);
                   
        }
        
        //EdgeWeightedGraph G = new EdgeWeightedGraph(Nodes,Edges);
        //StdOut.println(G);
    }
        
    public static void printNodesList() {
        nodes.forEach((Node_type node) -> {
            System.out.println(node.toString());
        });
    }
    
    public void printNodesMap(Map<Node_type, Double> nodeMap) {
        nodeMap.entrySet().forEach((node) -> {
            System.out.println((node.getKey()).toString() + " :: Delay: " + node.getValue());
        });
    }
    
    public static void main(String[] args) throws SocketException, IOException {
        OverlayNetworkManager master = new OverlayNetworkManager();
        master.run();        
    }
}

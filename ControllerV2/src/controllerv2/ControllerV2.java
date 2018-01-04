/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllerv2;

import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.StdOut;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Integer.min;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import nodedatagramsocketv2.utils.DijkstraUndirectedSP;
import nodedatagramsocketv2.utils.Display;
import nodedatagramsocketv2.utils.EdgeWeightedGraph;

/**
 *
 * @author eduardo
 */
public class ControllerV2 {
    
    private MulticastSocket s ;
    private DatagramSocket socket;
    private EdgeWeightedGraph G;
    private int Nodes=0;
    private boolean running;
    private int[] ports= new int[1];
    private double[] delay;
    private String[] nodesIP = new String[1];
    private InetAddress group; 
    private String groupIP="228.5.6.7";
    private int groupPort=6789;
    
    public void makeDjikstra(EdgeWeightedGraph G){
        
        
        
        delay = new double[G.V()];
        for (int s = 0; s< G.V() ; s++){
           
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            for (int t = 0; t < G.V(); t++) {
                if (sp.hasPathTo(t)) {
                  //  StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                    for (Edge e : sp.pathTo(t)) {
                    //    StdOut.print(e );
                    }
                    //StdOut.println();
                    delay[t]= sp.distTo(t);
                }
                else {
                    //StdOut.printf("%d to %d         no path\n", s, t);
                }
            }  
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
    
    /*
    Sempre que se junta um node atualiza o ficheiro para que novos nodes leiam.
    */
    public void writeFile() throws IOException{
        
       // System.out.println(G);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/rui/Desktop/data.txt"));
        writer.write(Integer.toString(G.V()) + " " + Integer.toString(socket.getLocalPort()) + "\n");
        for(int i = 0; i<G.V() ; i++){
            
            for(Edge e: G.adj(i)){
                String str =  e.either() + " " + e.other(e.either()) + " " + e.weight() + " " +  Integer.toString(ports[e.either()]) + " "+Integer.toString(ports[e.other(e.either())]) + " " + nodesIP[e.either()] + " " + nodesIP[e.other(e.either())] ;
                writer.write(str + "\n");
            }
        }
        writer.close();
      
    }
    
    public void printNodesList(){
        for (int i =0; i< min(ports.length,delay.length); i++){
            if(G.degree(i)==0){
                System.out.println("Node "+i+" No path");
            }else{
                System.out.println("Node "+i+ " ip "+ nodesIP[i] + " port "+ports[i]);
            }
        }
    }
    
    public EdgeWeightedGraph getEdgeWeightedGraph(){
        return G;
    }
    
    public void run() throws IOException{
        group = InetAddress.getByName(groupIP);
        s = new MulticastSocket (groupPort);
        s.joinGroup(group);
        socket = new DatagramSocket();
        G= new EdgeWeightedGraph(Nodes); 
        running=true;
        
        writeFile();
        Thread mannagerScanner = new Thread(new ManagerScanner(this)); //comandos do utilizador na linha de comands
        mannagerScanner.start();
        
        while(running){
            
            byte[] receiveData=new byte[ 1024 ];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet); //recebe HELLO de novos nodes
            
            String str = new String(packet.getData(), 0, packet.getLength());
            Display.info(str);
            //aloca mais memoria para guardar as portas dos nodes
            int[] ports_aux = new int[Nodes+1];
            String [] nodesIP_aux = new String[Nodes +1];
            System.arraycopy(ports, 0, ports_aux, 0, Nodes);
            System.arraycopy(nodesIP, 0, nodesIP_aux, 0, Nodes);
            ports = new int[Nodes+1];
            nodesIP = new String[Nodes +1];
            System.arraycopy(ports_aux, 0, ports, 0, Nodes+1);
            System.arraycopy(nodesIP_aux, 0, nodesIP, 0, Nodes+1);
            ports[Nodes] = packet.getPort();
            nodesIP[Nodes]=String.valueOf(packet.getAddress()).replaceAll("/","");

            
            Nodes = Nodes +1 ;
            if (Nodes == 1){ 
                G= new EdgeWeightedGraph(Nodes,1); //é preciso adicionar 1 edge para que o segundo node saiba a parte do primeiro
                writeFile();//update do ficheiro partilhado
            }
            else{
                G.addNode();
                int vertice1 = (int)(Math.random() * (G.V()-1));
                int vertice2 = G.V()-1 ;
                double weight = Math.random();
                Edge E = new Edge (vertice1, vertice2, weight);
                G.addEdge(E);
                
                writeFile();//update do ficheiro partilhado
                
                //envia por multicast as novas edges
                String newEdge = vertice1 + "_" + vertice2 + "_" + weight + "_" + ports[vertice1] + "_" +ports[vertice2] + "_" + nodesIP[vertice1]+ "_" + nodesIP[vertice2];
                DatagramPacket packetWithEdge = new DatagramPacket(newEdge.getBytes(), newEdge.length(), group, groupPort);
                s.send(packetWithEdge);

                /*
                Esta parte serve para o caso de quando um no se junta criar mais do q 1 Edge. Nesse caso cada edge é enviada e depois é enviado um
                "DONE". Quando um node recebe "DONE" faz o djikstra. Neste momento o codigo só faz 1 edge cada vez q 1 node se junta.
                String done = "DONE";
                DatagramPacket packetDone = new DatagramPacket(done.getBytes(), done.length(), group, 6789);
                s.send(packetDone);
                */

                makeDjikstra(G);
            }
        }
    }
    
    public void close(String id) {
        if(id.equals("all")){
            running = false;
        }
        sendClose(id);
    }
    
        // pede a todos os nós para fecharem - porreiro para debugging
    public void sendClose(String nodeId) {
        String msg = "close "+nodeId;
        
        
        try {
           DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group, groupPort);
           s.send(packet);
           if(nodeId.equals("all")){
                s.close();
                System.exit(1);
           }
           else{
               G = G.removeNode(G, Integer.parseInt(nodeId));
               writeFile();
               makeDjikstra(G);
           }
        } catch (IOException ex) {
            Logger.getLogger(ControllerV2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        
        
        
        
        ControllerV2 master = new ControllerV2();
        master.run();
    }
    
}

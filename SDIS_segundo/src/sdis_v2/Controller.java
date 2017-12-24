/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdis_v2;

import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.StdOut;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

/**
 *
 * @author eduardo
 */
public class Controller {
    static int Nodes = 0;
    static int Edges = 0;
    static DatagramSocket socket;
    static EdgeWeightedGraph G;
    static double[] delay ;
    
    /*
    Faz o dijkstra guarda os valors no vetor delay.
    */
    
    
    public static void makeDjikstra(EdgeWeightedGraph G){
        
        delay = new double[G.V()];
        for (int s = 0; s< G.V() ; s++){
           
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            for (int t = 0; t < G.V(); t++) {
                if (sp.hasPathTo(t)) {
                    StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                    for (Edge e : sp.pathTo(t)) {
                        StdOut.print(e );
                    }
                    StdOut.println();
                    delay[t]= sp.distTo(t);
                }
                else {
                    StdOut.printf("%d to %d         no path\n", s, t);
                }
            }
            
        }
        
    }
    /*
    Sempre que se junta um node atualiza o ficheiro para que novos nodes leiam.
    */
    public static void writeFile() throws IOException{
        
        System.out.println(G);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("../data.txt"));
        writer.write(Integer.toString(G.V()) + " " + Integer.toString(socket.getLocalPort()) + "\n");
        for(int i = 0; i<G.V() ; i++){
            
            for(Edge e: G.adj(i)){
                String str =  e.either() + " " + e.other(e.either()) + " " + e.weight() + " ";
                writer.write(str + "\n");
            }
        }
        writer.close();
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        InetAddress group = InetAddress.getByName("228.5.6.7");
        MulticastSocket s = new MulticastSocket (6789);
        s.joinGroup(group);
        socket = new DatagramSocket();
        G= new EdgeWeightedGraph(Nodes); 
        
        writeFile();
        Thread thread_recv = new Thread(new ReceiveCommands()); //multiplicar para checkar
        thread_recv.start();
        int nodePort=0; //isto vai ser um vetor, provavelmente vai tar no ficheiro
        
        while(true){
            byte[] receiveData=new byte[ 1024 ];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);
            String str = new String(packet.getData(), 0, packet.getLength());
            System.out.println(str);
            Nodes = Nodes +1 ;
            if (Nodes == 1){ 
                G= new EdgeWeightedGraph(Nodes); 
                
            }
            else{
                G.addNode();
                int vertice1 = (int)(Math.random() * (G.V()-1));
                int vertice2 = G.V()-1 ;
                double weight = Math.random();
                Edge E = new Edge (vertice1, vertice2, weight);
                G.addEdge(E);

                writeFile();

                String newEdge = vertice1 + "_" + vertice2 + "_" + weight;
                DatagramPacket packetWithEdge = new DatagramPacket(newEdge.getBytes(), newEdge.length(), group, 6789);
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
    
    public static class ReceiveCommands implements Runnable {

        @Override
        public void run(){
            
            while(true){
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter 'draw' to draw the graph: ");
                String str = scanner.nextLine();
                if(str.equals("draw")){
                    DrawGraph.main();
                }
            }
            
           
        }
    }
}
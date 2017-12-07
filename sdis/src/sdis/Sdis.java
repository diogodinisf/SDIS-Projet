/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdis;


import edu.princeton.cs.algs4.DijkstraUndirectedSP;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedGraph;
import edu.princeton.cs.algs4.StdOut;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 *
 * @author eduardo
 */
public class Sdis {
    
        // For each node calculates the shortest path to every other node. Calls sendArray() to send an array with the
        // transmission delay to each node.
        // @params: EdgeWeightedGraph G - Graph made using edu.princeton.cs.algs4.EdgeWeightedGraph;
        public static void makeDjikstra(EdgeWeightedGraph G){
        
        for (int s = 0; s< G.V() ; s++){
            double[]val=new double[G.V()];
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            for (int t = 0; t < G.V(); t++) {
                if (sp.hasPathTo(t)) {
                    StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                    for (Edge e : sp.pathTo(t)) {
                        StdOut.print(e + "   lol");
                    }
                    StdOut.println();
                    val[t]= sp.distTo(t);
                }
                else {
                    StdOut.printf("%d to %d         no path\n", s, t);
                }
            }
            sendArray(val,s);
            
        }
        
    }
    
        
    //Sends to each node, via UDP, and array of doubles with the weights to every other node.
    //@params: data - array with wights, calculated in makeDjikstra(); id - node id from command line.
    public static void sendArray(double[] data, int id){
        int PORT = 4444+id;
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.close();
            byte[] obj= baos.toByteArray();
            baos.close();
            DatagramSocket socket= new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(obj, obj.length, address, PORT);
            socket.send(packet);
        }
        catch(Exception e) {
            System.out.println("Erro "+e);
        } 
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException, IOException {
        int Nodes = 4;
        int Edges = 5;
        
        EdgeWeightedGraph G = new EdgeWeightedGraph(Nodes,Edges);
        StdOut.println(G);
        
        makeDjikstra(G);
    }
    
    
}

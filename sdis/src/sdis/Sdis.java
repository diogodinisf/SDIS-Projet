/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdis;


//import edu.princeton.cs.algs4.DijkstraUndirectedSP;
import edu.princeton.cs.algs4.Edge;

//import edu.princeton.cs.algs4.EdgeWeightedGraph;
import edu.princeton.cs.algs4.StdOut;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static java.lang.Math.random;
import java.net.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Manager da rede. Multicast group no 228.5.6.7:6789. Nós ao serem criados têm de avisar o multicast group da porta onde estao a ouvir.
 * @author eduardo
 */
public class Sdis {
    
    static int Nodes = 0;
    static int Edges = 0;
    static double[] ports = new double[1];
    
    // For each node calculates the shortest path to every other node. Calls sendArray() to send an array with the
    // transmission delay to each node.
    // @params: EdgeWeightedGraph G - Graph made using edu.princeton.cs.algs4.EdgeWeightedGraph;
    public static void makeDjikstra(EdgeWeightedGraph G){
        
        for (int s = 0; s< G.V() ; s++){
            double[]val=new double[G.V()];
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            for (int t = 0; t < G.V(); t++) {
                if (sp.hasPathTo(t)) {
                  //  StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                    for (Edge e : sp.pathTo(t)) {
                   //     StdOut.print(e + "   lol");
                    }
                   // StdOut.println();
                    val[t]= sp.distTo(t);
                }
                else {
                    //StdOut.printf("%d to %d         no path\n", s, t);
                }
            }
            sendArray(val,s);
            
        }
        
    }
    
        
    //Sends to each node, via UDP, and array of doubles with the weights to every other node.
    //@params: data - array with wights, calculated in makeDjikstra(); id - node id from command line.
    public static void sendArray(double[] data, int id){
        int PORT = (int)ports[id]+5000;
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
    
    public static void sendPorts (double[] ports, int nodeId) throws SocketException, UnknownHostException{
        DatagramSocket s= new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(ports);
            oos.close();
            byte[] obj= baos.toByteArray();
            baos.close();
            DatagramPacket packet = new DatagramPacket(obj, obj.length, address, (int) ports[nodeId]+10000);
          //  System.out.println("enviado para o no "+nodeId +"para a porta "+ports[nodeId]);
            s.send(packet);
        } catch (IOException ex) {
            Logger.getLogger(Sdis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException, IOException {

        
        //join multicast group
        InetAddress group = InetAddress.getByName("228.5.6.7");
        MulticastSocket s = new MulticastSocket (6789);
        s.joinGroup(group);
        EdgeWeightedGraph G= new EdgeWeightedGraph(Nodes); 
        
        while(true){
            byte[] receiveData=new byte[ 1024 ];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            s.receive(packet);
            String str = new String(packet.getData(), 0, packet.getLength());
            String[] split = str.split("_");
            Nodes = Nodes +1;
            //double [] temp_ports = new double[Nodes];
            ports=Arrays.copyOf(ports, Nodes);
            ports[Nodes-1]=Integer.parseInt(split[1]);
            
            if (Nodes == 1){
                G= new EdgeWeightedGraph(Nodes); 
            }
            else{
                G.addNode();
                int vertice1 = (int)(Math.random() * (Nodes-1));
                int vertice2 = Nodes-1 ;

                Edge E = new Edge (vertice1, vertice2, Math.random());
                G.addEdge(E);
              //  StdOut.println(G);
                makeDjikstra(G);
            }
           // System.out.println("I know these ports");
            /*for (int i =0 ; i< Nodes ; i++){
                System.out.println(ports[i]);
            }*/
            //System.out.println("end of known ports");
            sendPorts(ports, Nodes-1);
                   
        }
        
        //EdgeWeightedGraph G = new EdgeWeightedGraph(Nodes,Edges);
        //StdOut.println(G);
        
    }
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodev2;

import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.StdOut;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Integer.max;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author eduardo
 */
public class NodeDatagramSocket {
    
    static EdgeWeightedGraph G;
    DatagramSocket socket;
    int port;
    int MasterPort;
    static double[] delay;
    static int[] ports;
    int Nodes = 0;

    
    /*
    Construtor com porta aleatoria
    */
    
    public NodeDatagramSocket() throws SocketException{
        socket= new DatagramSocket();
        port= socket.getLocalPort();
        JoinMulticast();
        ReadGraph();
        SendHELLO();
    }
    
    /*
    construtor com porta definida
    */
    
    public NodeDatagramSocket(int port) throws SocketException{
        this.port = port;
        socket= new DatagramSocket(port);
        JoinMulticast();
        ReadGraph();
        SendHELLO();
    }
    
    /*
    Cria um thread pa enviar um packet com delay
    */
    
    public void send(DatagramPacket packet, int nodeId) throws IOException{
        int toPort = ports[nodeId];
        InetAddress address = InetAddress.getByName("localhost");
        packet.setPort(toPort);
        packet.setAddress(address);
        
        double wait=delay[nodeId];
        Thread thread = new Thread(new sendData(packet,wait*10000)); //multiplicar para checkar
        thread.start();
    }
    
    public void receive(DatagramPacket packet){
        try {
            socket.receive(packet);
        } catch (IOException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
    Thread pa enviar com delay
    */
    
    public class sendData implements Runnable {
        
        DatagramPacket packet;
        double wait;
        
        public sendData(DatagramPacket packet, double wait){
            this.packet=packet;
            this.wait=wait;
        }
        @Override
        public void run(){
            
            try {
                System.out.println("waiting "+ wait + " seconds, port "+ packet.getPort());
                Thread.sleep((long) wait);
                socket.send(packet);
            } catch (InterruptedException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    /*
    Le o ficheiro, cria o grafo G.
    */
    
    public void ReadGraph() {
        FileReader fileReader = null;
        try {

            int NodeCount =0;
            String line = null;
            fileReader = new FileReader("/home/data.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            Boolean firstLine = true;
            String[] fileMatrix= null;
            while((line = bufferedReader.readLine()) != null) {
                
                if (firstLine == true){
                    String[] data = line.split(" ");
                    Nodes = Integer.valueOf(data[0]);
                    MasterPort =  Integer.valueOf(data[1]);
                    firstLine = false;
                    G= new EdgeWeightedGraph(Nodes);
                    fileMatrix = new String[Nodes];
                    ports = new int[Nodes];
                    continue;
                }
                
                String data[] = line.split(" ");
                
                int v1  = Integer.valueOf(data[0]);
                int v2 = Integer.valueOf(data[1]);
                double weight = Double.parseDouble(data[2]);
                int portV1 = Integer.valueOf(data[3]);
                int portV2 = Integer.valueOf(data[4]);

                if(IntStream.of(ports).anyMatch(x -> x == portV1) == false){
                    ports[v1]=portV1;
                }
                if(IntStream.of(ports).anyMatch(x -> x == portV2) == false){
                    ports[v2]=portV2;
                }
                System.out.println("known ports");
                for (int i =0 ; i< ports.length; i++){
                    System.out.println(ports[i]);
                }
                
                if (Arrays.asList(fileMatrix).contains(line) == false){
                    //System.out.println("NEW edge "+v1+" "+v2+" "+ " "+weight);
                    Edge E = new Edge (v1, v2, weight);
                    G.addEdge(E);
                    fileMatrix[NodeCount] = line;
                    NodeCount++;
                }
              
            }   
            //System.out.println(G);
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileReader.close();
            } catch (IOException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        
    }
    
    /*
    Envia  HELLO para o master para ele saber que ha um novo node e guardar a porta. 
    */
    
    public void SendHELLO() {
        try {
            String str = "Hello";
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), address, MasterPort);
            Thread thread = new Thread(new sendData(packet,0)); //multiplicar para checkar
            thread.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
    Nova ligaçao entre nós. Verifica se os vertices ja existem ou nao. Se nao, adiciona o node antes de adicionar a Edge.
    */
    
    public static void addEdge(int v1, int v2, double w, int portV1, int portV2){
        //System.out.println("v1 "+v1);
        //System.out.println("v2 " + v2);
        //System.out.println("G.v() "+ G.V());
        if(G.V() == 0){ // Caso de ser dos primeiros nós e o ficheiro estar vazio
            G = new EdgeWeightedGraph(1);
        }
        if(v1 > G.V()-1 || v2 > G.V()-1 ){
            G.addNode();
            int maxVal = max(v1,v2);
            int[] ports_aux = new int[maxVal];
            System.arraycopy(ports, 0, ports_aux, 0, ports.length);
            ports = new int[maxVal+1];
            System.arraycopy(ports_aux, 0, ports, 0, ports_aux.length);
        }
        Edge e = new Edge(v1,v2,w);
        G.addEdge(e);
       // System.out.println(G);
        if(IntStream.of(ports).anyMatch(x -> x == v1) == false){
            ports[v1]=portV1;
        }
        if(IntStream.of(ports).anyMatch(x -> x == v2) == false){
            ports[v2]=portV2;
        }
        
  
    }
    
    /*
    Juntar-se ao multicast entre os nodes. 
    */
    
    public void JoinMulticast(){
        try {
            String group = "228.5.6.7";
            int multicastPort =6789;
            NodeMulticastSocket s = new NodeMulticastSocket(group, multicastPort);
        } catch (IOException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
    Dijkstra self explanatory. Delays guardados no vetor delay.
    */
    public static void MakeDijkstra(){
        delay=new double[G.V()];
        
        for (int s = 0; s< G.V() ; s++){
              
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, s);
            for (int t = 0; t < G.V(); t++) {
                if (sp.hasPathTo(t)) {
                  //  StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                    for (Edge e : sp.pathTo(t)) {
                    //    StdOut.print(e );
                    }
                    StdOut.println();
                    delay[t]= sp.distTo(t);
                }
                else {
                    //StdOut.printf("%d to %d         no path\n", s, t);
                }
            }
            
        }
    }
}

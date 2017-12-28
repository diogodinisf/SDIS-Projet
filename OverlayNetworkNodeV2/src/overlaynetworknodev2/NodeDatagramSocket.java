/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknodev2;

import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.StdOut;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author eduardo
 */
public class NodeDatagramSocket {
    
    private int masterPort;
    private final String masterHostname;
    private final String hostname;
    private final DatagramSocket socket;
    private final int port;
    private final double timeInit;
    private int Nodes;
    private EdgeWeightedGraph G;
    private int[] ports;
    private double[] delay;
    private Map<NodeType, Double> nodeMap = Collections.synchronizedMap(new HashMap<>());
    private MulticastSocket s;
    private int id=0;
    private boolean running=true;
    
    public NodeDatagramSocket (int port, String masterHostname) throws SocketException{
        
        this.port=port;
        this.masterHostname=masterHostname;
        this.hostname=getMyAddress();
        
        timeInit = System.currentTimeMillis();
        
        socket= new DatagramSocket(port);
        
        Display.alive(hostname + ":" + port );
        
        JoinMulticast();
        ReadGraph();
        SendHELLO();
        
        
        
    }
    
    public NodeDatagramSocket(String masterHostname) throws SocketException{
        
        this.masterHostname=masterHostname;
        this.hostname=getMyAddress();
        
        timeInit = System.currentTimeMillis();
        
        socket= new DatagramSocket();
        port= socket.getLocalPort();
        
        JoinMulticast();
        ReadGraph();
        SendHELLO();
       // StdOut.println(G);
        
    }
    
    public void receive(DatagramPacket packet) throws IOException{
       
            socket.receive(packet);
    
    }
    
    public void send(DatagramPacket packet, int nodeId) throws IOException{
        //int toPort = ports[nodeId];
        //InetAddress address = InetAddress.getByName("localhost");//para ja localhost, vou alterar pa saber o ip pelo ficheiro
        //packet.setPort(toPort);
        //packet.setAddress(address);
        
        double wait=delay[nodeId];
        Thread thread = new Thread(new sendData(packet,wait*10000)); //multiplicar para checkar
        thread.start();
    }
    
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
    
                Thread.sleep((long) wait);
                socket.send(packet);
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    public int getPort(){
        return port;
    }
    
    public int getPortById(int id) {
        int port =0;
        port=ports[id];
        return port;
    }
    
    public void printNodesMap(){
        
        for (int i=0; i<min(ports.length, delay.length); i++){
            Display.receive("node "+i +" port "+ ports[i] + " delay "+delay[i]);
        }
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
                    masterPort =  Integer.valueOf(data[1]);
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

                
                if (Arrays.asList(fileMatrix).contains(line) == false ){
                    //System.out.println("NEW edge "+v1+" "+v2+" "+ " "+weight);
                    System.out.println(line);
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
    
    public void SendHELLO() {
        try {
            String str = "Hello";
            InetAddress address = InetAddress.getByName(masterHostname);
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), address, masterPort);
            Thread thread = new Thread(new sendData(packet,0)); //multiplicar para checkar
            thread.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void JoinMulticast(){
        try {
            String group = "228.5.6.7";
            int multicastPort =6789;
            NodeMulticastSocket(group, multicastPort);
        } catch (IOException ex) {
            Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        /*
    Nova ligaçao entre nós. Verifica se os vertices ja existem ou nao. Se nao, adiciona o node antes de adicionar a Edge.
    */
    
    public void addEdge(int v1, int v2, double w, int portV1, int portV2){

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
        
        if(id ==0){

            for (int i =0; i<ports.length; i++){
                if(ports[i]==port){
                    id=i;
                }
            }
        }
        //StdOut.println(G);

    }
    
    public void MakeDijkstra(){
        delay=new double[G.V()];
    
              
            DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, id);
            for (int t = 0; t < G.V(); t++) {
                if (sp.hasPathTo(t)) {
                  //  StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                    for (Edge e : sp.pathTo(t)) {
                    //    StdOut.print(e );
                    }
                   // StdOut.println();
                    delay[t]= sp.distTo(t);
                }
                else {
                    //StdOut.printf("%d to %d         no path\n", s, t);
                }
            }
            
        
    }
    
    
    public void NodeMulticastSocket (String groupIP, int port) throws UnknownHostException, IOException{
        
        InetAddress group = InetAddress.getByName(groupIP);
        s = new MulticastSocket (port);
        s.joinGroup(group);
        
        Thread thread_recv = new Thread(new ReceiveFromGroup()); 
        thread_recv.start();
        
    }
    
    
    /*
    Recebe do grupo muticast. Cada string é um nova edge.Forma da string: "v1_v2_w_p1_p2"
    */
    public class ReceiveFromGroup implements Runnable {

        @Override
        public void run(){
            
            while(running){
                try {
                    byte[] receiveData=new byte[ 64*1024 ];
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                    s.receive(packet);
                    String str = new String(packet.getData(), 0, packet.getLength());
                    /*
                    Caso seja para receber mais do q 1 edge por novo node descomentar isto:
                    if(str == "DONE" ){
                        NodeDatagramSocket.makeDijkstra();
                        continue;
                    }
                    */
                    String myNodeId = "close "+id;
                    if("close all".equals(str) || myNodeId.equals(str) ){
                        Display.alert("(" + id + ")" + " Sockets fechados pelo Manager");
                        running = false;
                        continue;
                    }
                    else if (str.startsWith("close")){
                        String[] strData = str.split(" ");
                        G = G.removeNode(G, Integer.parseInt(strData[1]));
                        MakeDijkstra();
                        
                        continue;
                    }
                    
                    String[] split = str.split("_");
                    
                    addEdge(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[2]),Integer.parseInt(split[3]), Integer.parseInt(split[4]));
                    MakeDijkstra(); //comentar isto se for para usar o "DONE"
                } catch (IOException ex) {
                    Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            s.close();
            socket.close();
            System.exit(1);
        }
    }
}


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodedatagramsocketv2.socket;

import nodedatagramsocketv2.utils.NodeType;
import nodedatagramsocketv2.utils.Display;
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
import nodedatagramsocketv2.utils.DijkstraUndirectedSP;
import nodedatagramsocketv2.utils.EdgeWeightedGraph;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private double[] error;
    private Map<NodeType, Double> nodeMap = Collections.synchronizedMap(new HashMap<>());
    private MulticastSocket s;
    private int id;
    private boolean running=true;
    private String[] nodesIP;
    private boolean idSet=false;
    
    private ScheduledThreadPoolExecutor threadPool;
    
    public NodeDatagramSocket (int port, String masterHostname) throws SocketException{
        
        this.port=port;
        this.masterHostname=masterHostname;
        this.hostname=getMyAddress();
        
        this.threadPool = new ScheduledThreadPoolExecutor(50); // alterar para valor maximo desejado da queue
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
        this.threadPool = new ScheduledThreadPoolExecutor(50); // alterar para valor maximo desejado da queue
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
        double errorRate = error[nodeId];
        double wait=delay[nodeId];
        if(wait == -1){
            System.out.println("Node "+nodeId+" doesn't exist anymore !");
        }
        else if(wait == 0 && nodeId != id){
            System.out.println("There is no path for that node");
        }
        else {
            if (Math.random() > errorRate) {
     //           Thread thread = new Thread(new sendData(packet,wait*10000)); //multiplicar para checkar
       //         thread.start();
        threadPool.schedule(new sendData(packet), (long) (wait*10000), TimeUnit.MILLISECONDS);
            }else {
                Display.alert("Falhou envio para " + nodeId);
            }
        }
    }
    
    public class sendData implements Runnable {
        
        DatagramPacket packet;
        //double wait;
        
        public sendData(DatagramPacket packet){
            this.packet=packet;
            //this.wait=wait;
        }
        @Override
        public void run(){
            
            try {
    
                //Thread.sleep((long) wait);
                socket.send(packet);
            } catch (IOException ex) {
                Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    public int getPort(){
        return port;
    }
    
    public int getPortById(int id) {
        if(id >= ports.length){
            return -1;
        }
        int port =0;
        port=ports[id];
        return port;
    }
    
    public String getIpById(int id){
        String ip = nodesIP[id];
        return ip;
    }
    
    public void printNodesMap(){
        
        for (int i=0; i<min(ports.length, delay.length); i++){
            if(ports[i] == -1 || delay[i]==-1 || ports[i]==0 || nodesIP[i] == null || G.degree(i)==0){
                Display.receive("node "+i+" No path");
            }else if(delay[i]==0 && i!= id){
                Display.receive("node "+i+" No path");
            }
            else{
                Display.receive("node "+i +" ip " +nodesIP[i]+ " port "+ ports[i] + " delay "+delay[i] + " error rate " +error[i]);
            }
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
    
    public void ReadGraph() {
        FileReader fileReader = null;
        try {

            int NodeCount =0;
            String line = null;
            fileReader = new FileReader("/home/rui/Desktop/data.txt");
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
                    nodesIP = new String[Nodes];
                    continue;
                }
                
                String data[] = line.split(" ");
                
                int v1  = Integer.valueOf(data[0]);
                int v2 = Integer.valueOf(data[1]);
                double weight = Double.parseDouble(data[2]);
                int portV1 = Integer.valueOf(data[3]);
                int portV2 = Integer.valueOf(data[4]);
                String ipV1 = data[5];
                String ipV2 = data[6];
                //cuidado com isto ja nao sei bem o que fiz aqui e como resolver para dar pa usar ips
                if(IntStream.of(ports).anyMatch(x -> x == portV1) == false ){//se a porta nao existe entao nao ha problemas com ips
                    ports[v1]=portV1;
                    nodesIP[v1]=ipV1;
                }else if(IntStream.of(ports).anyMatch(x -> x == portV1) == true && Arrays.asList(nodesIP).contains(ipV1)==false){//se a porta existir é preciso ver se o ip existe ou nao
                    ports[v1]=portV1;
                    nodesIP[v1]=ipV1;
                    
                }
                if(IntStream.of(ports).anyMatch(x -> x == portV2) == false){
                    ports[v2]=portV2;
                    nodesIP[v2]=ipV2;
                }else if(IntStream.of(ports).anyMatch(x -> x == portV1) == true && Arrays.asList(nodesIP).contains(ipV1)==false){
                    ports[v2]=portV2;
                    nodesIP[v2]=ipV2;
                }

                
                if (Arrays.asList(fileMatrix).contains(line) == false ){
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
    
    public void SendHELLO() {
        try {
            String str = "Hello";
            InetAddress address = InetAddress.getByName(masterHostname);
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), address, masterPort);
            threadPool.schedule(new sendData(packet), 0, TimeUnit.MILLISECONDS);
     //       Thread thread = new Thread(new sendData(packet,0)); //multiplicar para checkar
       //     thread.start();
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
    
    public void addEdge(int v1, int v2, double w, int portV1, int portV2, String ipV1, String ipV2){

        if(G.V() == 0){ // Caso de ser dos primeiros nós e o ficheiro estar vazio
            G = new EdgeWeightedGraph(1);
        }
        if(v1 > G.V()-1 || v2 > G.V()-1 ){
            G.addNode();
            int maxVal = max(v1,v2);
            int[] ports_aux = new int[maxVal];
            String[] nodesIP_aux = new String[maxVal];
            System.arraycopy(ports, 0, ports_aux, 0, ports.length);
            System.arraycopy(nodesIP, 0, nodesIP_aux, 0, nodesIP.length);
            ports = new int[maxVal+1];
            nodesIP = new String[maxVal +1];
            System.arraycopy(ports_aux, 0, ports, 0, ports_aux.length);
            System.arraycopy(nodesIP_aux, 0, nodesIP, 0, nodesIP_aux.length);
        }
        Edge e = new Edge(v1,v2,w);
        G.addEdge(e);
       // System.out.println(G);
        if(IntStream.of(ports).anyMatch(x -> x == portV1) == false ){//se a porta nao existe entao nao ha problemas com ips
            ports[v1]=portV1;
            nodesIP[v1]=ipV1;
        }else if(IntStream.of(ports).anyMatch(x -> x == portV1) == true && Arrays.asList(nodesIP).contains(ipV1)==false){//se a porta existir é preciso ver se o ip existe ou nao
            ports[v1]=portV1;
            nodesIP[v1]=ipV1;       
        }
        if(IntStream.of(ports).anyMatch(x -> x == portV2) == false){
            ports[v2]=portV2;
            nodesIP[v2]=ipV2;
        }else if(IntStream.of(ports).anyMatch(x -> x == portV1) == true && Arrays.asList(nodesIP).contains(ipV1)==false){
            ports[v2]=portV2;
            nodesIP[v2]=ipV2;
        }
        
        if(!idSet){
           
            for (int i =0; i<ports.length; i++){
                if(ports[i]==port && nodesIP[i].equals(hostname)){
                    id=i;
                    idSet=true;
                }
            }
            
        }
        //StdOut.println(G);

    }
    
    public void MakeDijkstra(){
        double successRate;
        double errorRate, totalErrorRate;
        delay=new double[G.V()];
        error=new double[G.V()];
              
        DijkstraUndirectedSP sp = new DijkstraUndirectedSP(G, id);
        for (int t = 0; t < G.V(); t++) {
            successRate = 1;
            if (sp.hasPathTo(t)) {

                for (Edge e : sp.pathTo(t)) {
                    errorRate = Math.abs(1.0 - e.weight());
                    successRate = successRate * (1 - errorRate);
                }
                totalErrorRate = (1 - successRate) * 1; //poderação para não ser demasiado elevado
                error[t]=totalErrorRate;
                delay[t]= sp.distTo(t);
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
                        int nodeToRemove = Integer.parseInt(strData[1]);
                        if(nodeToRemove >= ports.length){
                            continue;
                        }
                        G = G.removeNode(G, nodeToRemove);
                        delay[nodeToRemove]=-1;
                        ports[nodeToRemove]=-1;
                        MakeDijkstra();
                        
                        continue;
                    }
                    
                    String[] split = str.split("_");
                    
                    addEdge(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[2]),Integer.parseInt(split[3]), Integer.parseInt(split[4]), split[5], split[6]);
                    MakeDijkstra(); //comentar isto se for para usar o "DONE"
                } catch (IOException ex) {
                    Logger.getLogger(NodeDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            s.close();
            socket.close();
            //System.exit(1);
        }
    }
}


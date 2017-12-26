package nodev2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import edu.princeton.cs.algs4.Edge;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/**
 *
 * @author eduardo
 */
public class NodeV2 {
    static int Nodes =0;
    static int PORT;
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        int id = Integer.parseInt(args[0]);
        PORT = 40000 + id;
        NodeDatagramSocket socket = new NodeDatagramSocket (PORT);
        Thread thread_recv = new Thread(new threadToReceive(socket)); //multiplicar para checkar
        thread_recv.start();
        Thread thread_send = new Thread(new threadToSend(socket)); //multiplicar para checkar
        thread_send.start();
    }
    
}

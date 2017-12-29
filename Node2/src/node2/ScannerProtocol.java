/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import nodedatagramsocketv2.utils.Display;
import nodedatagramsocketv2.socket.NodeDatagramSocket;

/**
 *
 * @author diogo
 */
public class ScannerProtocol {
    private NodeDatagramSocket socket;
    
     public enum MessageType {
        HELP("help", "Displays this message",
                "help", Arrays.asList("h")),
        MESSAGE("msg", "Send a message to node i", 
                "msg <address> <port> <text>", Arrays.asList("mensagem", "message", "sms")),
        WHOAMI("whoami", "Return the IP:PORT of node", 
                "whoami", Arrays.asList("who", "quem", "info", "information")),
        DELAY("delay", "Show delays for other nodes",
                "delay", Arrays.asList("latency", "atrasos", "latencias", "delays", "atrasos", "latencia"));
        

        private final String word;
        private final String help;
        private final String usage;
        private final Collection<String> alternatives;

        MessageType(String word, String help, String usage, Collection<String> alternatives) {
            this.word = word;
            this.help = help;
            this.usage = usage;
            this.alternatives = alternatives;
        }

        public String getHelpMessage() {
            String result = word + ": " + help + " [" + usage + "]";
            return result;
        }

        public Collection<String> getKeys() {
            Collection<String> words = new HashSet<>(alternatives);
            words.add(word);
            return words;
        }
    }

    public boolean protocol(String message, NodeDatagramSocket socket) throws UnknownHostException, IOException {
        this.socket = socket;
        List<String> words = splitMessage(message);

        if(words.isEmpty()) {
            return true;
        }

        String word = words.get(0).toLowerCase();

        if(MessageType.HELP.getKeys().contains(word)) {
            help();
        }
       
        if(MessageType.DELAY.getKeys().contains(word)) {
            delay();
        }
        
        if(MessageType.WHOAMI.getKeys().contains(word)) {
            whoami();
        }

        if(MessageType.MESSAGE.getKeys().contains(word)) {
            sendMessage(Integer.parseInt(words.get(1)), message.substring(message.indexOf(words.get(1)) + words.get(1).length()).trim());
        }

        return false;
    }

    private static List<String> splitMessage(String message) {
        List<String> words = (new ArrayList<>());
        words.addAll(Arrays.asList(message.split("\\s+")));
        while(words.remove("")) { }
        return words;
    }

    private void delay() {
        socket.printNodesMap();
    }

    private static void help() {
        for(MessageType messageType: MessageType.values()) {
            Display.help(messageType.getHelpMessage());
        }
    }
    
    private void whoami() {
        Display.info(NodeDatagramSocket.getMyAddress() + ":" + socket.getPort());
    }
    
    private void sendMessage(int toId, String msg) throws UnknownHostException, IOException {
        //String toIp = socket.getIpById(toId);
        String toIp = "localhost"; //so por agora
        int toPort = socket.getPortById(toId);

        if(toPort == -1){
            System.out.println("Node "+toId+" doesn't exist !");
        }
        else{
            InetAddress address = InetAddress.getByName(toIp);
            Display.info("Enviado: " + msg);
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), address, toPort);
            socket.send(packet, toId);
        }
    }
}
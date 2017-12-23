/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworkmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import static overlaynetworkmanager.OverlayNetworkManager.printNodesList;

/**
 *
 * @author diogo
 */
public class ManagerScannerProtocol {
    
    public enum MessageType {
        HELP("help", "Displays this message",
                "help", Arrays.asList("h")),
        CLOSE("close", "Closes the overlay network",
                "close", Arrays.asList("disconnect", "exit", "kill")),
        INFO("network", "Show network nodes list",
                "network", Arrays.asList("status", "web", "net", "graph"));
        

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

    public static boolean protocol(String message) {

        List<String> words = splitMessage(message);

        if(words.isEmpty()) {
            return true;
        }

        String word = words.get(0).toLowerCase();

        if(MessageType.HELP.getKeys().contains(word)) {
            help();
        }
       
        if(MessageType.INFO.getKeys().contains(word)) {
            info();
        }

        if(MessageType.CLOSE.getKeys().contains(word)) {
            close();
        }

        return false;
    }

    private static List<String> splitMessage(String message) {
        List<String> words = (new ArrayList<>());
        words.addAll(Arrays.asList(message.split("\\s+")));
        while(words.remove("")) { }
        return words;
    }

    private static void close() {
        ManagerScanner.close();
    }

    private static void info() {
        OverlayNetworkManager.printNodesList();
    }

    private static void help() {
        for(MessageType messageType: MessageType.values()) {
            System.out.println(messageType.getHelpMessage());
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overlaynetworknodev2;

import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduardo
 */
public class OverlayNetworkNodeV2 {
    
    private static final String MASTER_HOSTNAME = "172.16.1.114";
   
    
    private int port;
    private NodeDatagramSocket socket;

    private Thread thread_send;
    private Thread thread_recv;
    private static boolean oneNodePerJVM;
    
    public void start(int id) throws SocketException {
        port = 35555 + id;
        socket = new NodeDatagramSocket(port, MASTER_HOSTNAME); //A PORTA VAI NO FICHEIRO
        run();
    }
    
    public void start() throws SocketException {
        socket = new NodeDatagramSocket( MASTER_HOSTNAME);
        run();
    }
    
    public void run(){
        Thread thread_recv = new Thread(new threadToReceive(socket, this)); 
        thread_recv.start();
        thread_send = new Thread(new threadToSend(socket)); 
        thread_send.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException {
        
        if(args.length <=1 ){
            oneNodePerJVM=true;
            if ((args.length == 0) || (Integer.parseInt(args[0]) == -1)) {
                new OverlayNetworkNodeV2().start();
            } else {
                new OverlayNetworkNodeV2().start(Integer.valueOf(args[0]));        
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                Logger.getLogger(OverlayNetworkNodeV2.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else if(args.length >1){
            int id = Integer.valueOf(args[0]);
            int max_id = Integer.valueOf(args[1]);
            for(int i=id ; i< (id+max_id) ; i++){
                if ((args.length == 0) || (Integer.parseInt(args[0]) == -1)) {
                    new OverlayNetworkNodeV2().start();
                } else {
                    new OverlayNetworkNodeV2().start(i);        
                }
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OverlayNetworkNodeV2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }      
        }

    }
    
    public void closeProgram() {
       
        if(oneNodePerJVM){
            System.exit(0);
        } //isto fecha a jvm
    }
    
}

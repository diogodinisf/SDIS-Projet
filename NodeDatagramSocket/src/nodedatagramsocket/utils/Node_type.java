/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodedatagramsocket.utils;

/**
 *
 * @author diogo
 */
public class Node_type implements java.io.Serializable {
    private final int port;
    private final int id;
    private String ip = null;
    
    public Node_type(int id, String ip, int port) {
        this(id, port);
        this.ip = ip;
    }
    
    public Node_type(int id, int port) {
        this.port = port;
        this.id = id;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getId() {
        return id;
    }
    
    public String getIp() {
        return ip;
    }
    
    @Override
    public String toString() {
        return "ID: " + id + " :: IP: " + ip + " :: PORT: " + port;
    }
}


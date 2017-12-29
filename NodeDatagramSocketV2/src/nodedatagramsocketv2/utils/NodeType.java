/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodedatagramsocketv2.utils;

public class NodeType implements java.io.Serializable {
    private final int port;
    private final int id;
    private String ip = null;
    
    public NodeType(int id, String ip, int port) {
        this.port = port;
        this.id = id;
        this.ip = ip;
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
        return "ID: " + id + " :: Endereço: " + ip + ":" + port ;
    }
}
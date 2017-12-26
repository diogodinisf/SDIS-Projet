package nodedatagramsocket.utils;

public class NodeType implements java.io.Serializable {
    private final int port;
    private final int delayPort;
    private final int id;
    private String ip = null;
    
    public NodeType(int id, String ip, int port, int delayPort) {
        this.port = port;
        this.delayPort = delayPort;
        this.id = id;
        this.ip = ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getDelayPort() {
        return delayPort;
    }
    
    public int getId() {
        return id;
    }
    
    public String getIp() {
        return ip;
    }
    
    @Override
    public String toString() {
        return "ID: " + id + " :: Endereço: " + ip + ":" + port + " | delay port: " + delayPort;
    }
}

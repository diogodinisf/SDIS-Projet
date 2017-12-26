package controller;

import java.util.Scanner;

public class ManagerScanner implements Runnable {
    private boolean running;
    private Scanner scanner;
    private ManagerScannerProtocol scannerProtocol;
    final Controller father; /* packet */ 
    
    public ManagerScanner(Controller father) {
        this.father = father;
    }
    
    @Override
    public void run() {
        running = true;
        scanner = new Scanner(System.in);
        scannerProtocol = new ManagerScannerProtocol(this);

        while (running) {
            String message = scanner.nextLine();
            scannerProtocol.protocol(message);
        }
        
        scanner.close();
        father.close();
    }

    public void close() {
        running = false;
    }
}

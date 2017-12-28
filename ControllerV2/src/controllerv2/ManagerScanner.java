/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllerv2;

import java.util.Scanner;

public class ManagerScanner implements Runnable {
    private boolean running;
    private Scanner scanner;
    private ManagerScannerProtocol scannerProtocol;
    final ControllerV2 father; /* packet */ 
    
    public ManagerScanner(ControllerV2 father) {
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
        
    }

    public void close(String id) {
        father.close(id);
        if(id.equals("all")){
            running = false;
        }
    }
}
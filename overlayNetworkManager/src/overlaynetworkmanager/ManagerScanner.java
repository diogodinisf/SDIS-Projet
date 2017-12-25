package overlaynetworkmanager;

import java.util.Scanner;

/**
 *
 * @author diogo
 */
public class ManagerScanner implements Runnable {
    private static Scanner scanner;
    private static boolean running;

    @Override
    public void run() {
        scanner = new Scanner(System.in);
        running = true;

        while (running) {
            String message = scanner.nextLine();
            ManagerScannerProtocol.protocol(message);
        }
        
        scanner.close();
        OverlayNetworkManager.close();
    }

    public static void close() {
        running = false;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllerv2;

/**
 *
 * @author diogo
 */
public class Display {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static boolean display(String message) {
        return displayWithColor(message, ANSI_RESET);
    }
    
    public static boolean receive(String message) {
        return displayWithColor(message, ANSI_GREEN);
    }
    
    public static boolean alive(String message) {
        return displayWithColor(message, ANSI_YELLOW);
    }

    public static boolean info(String message) {
        return displayWithColor(message, ANSI_CYAN);
    }
    
    public static boolean help(String message) {
        return displayWithColor("\t" + message, ANSI_PURPLE);
    }
    
    public static boolean alert(String message) {
        return displayWithColor(message, ANSI_RED);
    }

    private static boolean displayWithColor(String message, String color) {
        System.out.println(color +  message + ANSI_RESET);
        return true;
    }
}
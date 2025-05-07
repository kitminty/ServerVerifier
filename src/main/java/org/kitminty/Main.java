package org.kitminty;

//cats are cool

/// find how masscan appends json to the output file

public class Main {
    public static String serverjsonlist = "";
    public static String outputtxt = "";
    public static String ANSI_RESET = "\u001B[0m";
    public static String ANSI_RED = "\u001B[31m";
    public static String ANSI_GREEN = "\u001B[32m";

    public static int chunksize = 1;

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--serverjsonlist=")) {
                serverjsonlist = arg.substring("--serverjsonlist=".length());
                System.out.println(serverjsonlist);
            }
            if (arg.startsWith("--outputtxt=")) {
                outputtxt = arg.substring("--outputtxt=".length());
                System.out.println(outputtxt);
            }
        }

        for (int i = 0; i < ServerJsonProcessor.Chunklist(serverjsonlist, Main.chunksize).size(); i++) {
            ServerScanner object = new ServerScanner();
            object.start();
        }
    }
}
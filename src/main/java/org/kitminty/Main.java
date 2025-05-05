package org.kitminty;

import java.io.*;
import java.util.List;
import com.tekgator.queryminecraftserver.api.Protocol;
import com.tekgator.queryminecraftserver.api.QueryStatus;
//cats are cool
public class Main {
    public static void main(String[] args) {
        String serverjsonlist = "";
        String outputtxt = "";
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";

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

        List<Masscan> serverList = ServerJsonProcessor.parse(serverjsonlist);
        String outputTxt = outputtxt;
        for (Masscan server : serverList) {
            String json;
            String address = server.ip();
            short port = server.ports().getFirst().port();
            try {
                json = new QueryStatus.Builder(address)
                        .setProtocol(Protocol.TCP)
                        .build()
                        .getStatus()
                        .toJson();

                System.out.println(ANSI_GREEN + "Server " + address + ":" + port + " Is On" + ANSI_RESET);

                BufferedWriter out = new BufferedWriter(new FileWriter(outputTxt, true));
                out.write("Server " + address + ":" + port + " Is On, Json: (" + json + ")");
                out.newLine();
                out.close();
            } catch (Exception ignored) {
                System.out.println(ANSI_RED + "Server " + address + ":" + port + " Is Off" + ANSI_RESET);
            }
        }
    }
}
package org.kitminty;

import com.tekgator.queryminecraftserver.api.Protocol;
import com.tekgator.queryminecraftserver.api.QueryStatus;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class ServerScanner extends Thread {
    public void run() {
        List<Masscan> serverList = ServerJsonProcessor.parseonlyjson(ServerJsonProcessor.Chunking(Main.serverjsonlist, Main.chunksize).get(Integer.parseInt(Thread.currentThread().getName().substring(7))).toString());
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

                System.out.println("\u001B[32m" + "Server " + address + ":" + port + " Is On" + "\u001B[0m");
                BufferedWriter out = new BufferedWriter(new FileWriter(Main.outputtxt, true));
                out.write("Server " + address + ":" + port + " Is On, Json: (" + json + ")");
                out.newLine();
                out.close();
            } catch (Exception ignored) {
                System.out.println("\u001B[31m" + "Server " + address + ":" + port + " Is Off" + "\u001B[0m");
            }
        }
    }
}
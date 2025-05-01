package org.kitminty;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        if (serverList == null) return;
        for (Masscan server : serverList) {
            try {
                String address = server.ip();
                short port = server.ports().getFirst().port();

                HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.mcsrvstat.us/3/" + address)).build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject jsonResponse = (JsonObject) new JsonParser().parse(response.body());
                boolean ping = jsonResponse.get("debug").getAsJsonObject().get("ping").getAsBoolean();

                BufferedWriter out = new BufferedWriter(new FileWriter(outputTxt, true));
                if (ping) {
                    out.write("Server " + address + ":" + port + " Is On");
                    out.newLine();
                }
                out.close();

                System.out.println((ping ? ANSI_GREEN + "Server " + address + ":" + port + " Is On" : ANSI_RED + "Server " + address + ":" + port + " Is Off") + ANSI_RESET);
            } catch (Exception ignored) { }
        }
    }
}
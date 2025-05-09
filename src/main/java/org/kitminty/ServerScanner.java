package org.kitminty;

import com.mongodb.MongoException;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.tekgator.queryminecraftserver.api.Protocol;
import com.tekgator.queryminecraftserver.api.QueryStatus;
import org.bson.Document;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import java.util.List;

public class ServerScanner extends Thread {
    public void run() {
        List<Masscan> serverList = ServerJsonProcessor.parseonlyjson(ServerJsonProcessor.Chunking(Main.serverjsonlist, Main.chunksize).get(Integer.parseInt(Thread.currentThread().getName().substring(7))).toString());
        for (Masscan server : serverList) {
            String address = server.ip();
            short port = server.ports().getFirst().port();
            try {
                String Description = new QueryStatus.Builder(address)
                        .setProtocol(Protocol.TCP)
                        .build()
                        .getStatus()
                        .getDescription();
                int Players = new QueryStatus.Builder(address)
                        .setProtocol(Protocol.TCP)
                        .build()
                        .getStatus()
                        .getPlayers()
                        .getOnlinePlayers();

                System.out.println("\u001B[32m" + "Server " + address + ":" + port + " Is On" + "\u001B[0m");

                //--- think this will work keep it close to this

                Document document = new Document().append("ip", address).append("description", Description).append("players", Players); //make it get just players from the json

                try (MongoClient mongoClient = MongoClients.create(Main.settings)) {
                    try {
                        MongoDatabase database = mongoClient.getDatabase("Servers");
                        MongoCollection<Document> Serverlist = database.getCollection("Serverlist");

                        Publisher<InsertOneResult> insertPublisher = Serverlist.insertOne(document);
                        Mono.from(insertPublisher).block();
                    } catch (MongoException e) {
                        e.printStackTrace();
                    }
                }

                //---

                /* current writer
                BufferedWriter out = new BufferedWriter(new FileWriter(Main.outputtxt, true));
                out.write("Server " + address + ":" + port + " Is On, Json: (" + json + ")");
                out.newLine();
                out.close();
                */

            } catch (Exception ignored) {
                System.out.println("\u001B[31m" + "Server " + address + ":" + port + " Is Off" + "\u001B[0m");
            }
        }
    }
}
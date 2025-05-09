package org.kitminty;

import com.mongodb.MongoWriteException;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.*;
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
            try {
                String description = new QueryStatus.Builder(address)
                        .setProtocol(Protocol.TCP)
                        .build()
                        .getStatus()
                        .getDescription();
                int players = new QueryStatus.Builder(address)
                        .setProtocol(Protocol.TCP)
                        .build()
                        .getStatus()
                        .getPlayers()
                        .getOnlinePlayers();

                Document document = new Document().append("_id", address).append("description", description).append("players", players);
                try (MongoClient mongoClient = MongoClients.create(Main.settings)) {
                    MongoDatabase database = mongoClient.getDatabase("Servers");
                    MongoCollection<Document> Serverlist = database.getCollection("Serverlist");
                    Publisher<InsertOneResult> insertPublisher = Serverlist.insertOne(document);
                    Mono.from(insertPublisher).block();
                    System.out.println("\u001B[32m" + "Server " + address + " is on" + "\u001B[0m");
                } catch (MongoWriteException e) {
                    if (e.getCode() == 11000) {
                        System.out.println("\u001B[38;5;11m" + "Server " + document.get("_id") + " is duplicate" + "\u001B[0m");
                    }
                }
            } catch (Exception ignored) {
                System.out.println("\u001B[31m" + "Server " + address + " is off" + "\u001B[0m");
            }
        }
    }
}
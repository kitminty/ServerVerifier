package org.kitminty;

import com.mongodb.MongoWriteException;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.*;
import com.tekgator.queryminecraftserver.api.Protocol;
import com.tekgator.queryminecraftserver.api.QueryStatus;
import org.bson.Document;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class ServerScanner extends Thread {
    public void run() {
        MongoDatabase database = Main.mongoClient.getDatabase("Servers");
        MongoCollection<Document> Serverlist = database.getCollection("Serverlist");
        List<Masscan> serverList = ServerJsonProcessor.parseonlyjson(ServerJsonProcessor.Chunking(Main.serverjsonlist, Main.manualchunksize ? Main.chunksize : Math.toIntExact(round(sqrt(ServerJsonProcessor.parsejsonfile(Main.serverjsonlist).size())))).get(Integer.parseInt(Thread.currentThread().getName().substring(7))).toString());
        for (Masscan server : serverList) {
            String address = server.ip();
            try {
                String description = new QueryStatus.Builder(address)
                        .setProtocol(Protocol.TCP)
                        .setTimeout(5000)
                        .build()
                        .getStatus()
                        .getDescription();
                int players = new QueryStatus.Builder(address)
                        .setProtocol(Protocol.TCP)
                        .setTimeout(5000)
                        .build()
                        .getStatus()
                        .getPlayers()
                        .getOnlinePlayers();

                Document document = new Document().append("_id", address).append("description", description).append("players", players);
                try {
                    Publisher<InsertOneResult> insertPublisher = Serverlist.insertOne(document);
                    Mono.from(insertPublisher).timeout(Duration.ofSeconds(5)).block();
                    System.out.println("\u001B[32m" + "Server " + address + " is on, added to database" + "\u001B[0m");
                } catch (MongoWriteException e) {
                    if (e.getCode() == 11000) {
                        System.out.println("\u001B[38;5;11m" + "Server " + document.get("_id") + " is duplicate" + "\u001B[0m");
                    }
                }
            } catch (Exception e) {
                System.out.println("\u001B[31m" + "Server " + address + " is off" + "\u001B[0m" + " error:" + e);
            }
        }
    }
}
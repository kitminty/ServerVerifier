package org.kitminty;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.*;
import com.tekgator.queryminecraftserver.api.Protocol;
import com.tekgator.queryminecraftserver.api.QueryStatus;
import org.bson.Document;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import static com.mongodb.client.model.Filters.eq;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class DBVerify extends Thread {
    public void run() {
        List<Masscan> serverList = ServerJsonProcessor.parseonlydbjson(ServerJsonProcessor.DBChunking(Main.DBserverList, Main.manualchunksize ? Main.chunksize : Math.toIntExact(round(sqrt(ServerJsonProcessor.parseonlydbjson(Main.DBserverList).size())))).get(Integer.parseInt(Thread.currentThread().getName().substring(7))-2).toString());
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
                MongoDatabase database = Main.mongoClient.getDatabase("Servers");
                MongoCollection<Document> Serverlist = database.getCollection("Serverlist");
                Publisher<UpdateResult> replacePublisher = Serverlist.replaceOne(eq("_id", address), document);
                Mono.from(replacePublisher).timeout(Duration.ofSeconds(5)).block();
                System.out.println("\u001B[32m" + "Server " + address + " is still on, updated database" + "\u001B[0m");
            } catch (Exception ignored) {
                try (MongoClient mongoClient = MongoClients.create(Main.settings)) {
                    MongoDatabase database = mongoClient.getDatabase("Servers");
                    MongoCollection<Document> Serverlist = database.getCollection("Serverlist");
                    Publisher<DeleteResult> deletePublisher = Serverlist.deleteOne(eq("_id", address));
                    Mono.from(deletePublisher).timeout(Duration.ofSeconds(5)).block();
                    System.out.println("\u001B[31m" + "Server " + address + " is off, removed from database" + "\u001B[0m");
                }
            }
        }
    }
}
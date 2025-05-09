package org.kitminty;

//cats are cool

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mongodb.*;
import com.mongodb.reactivestreams.client.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import java.util.List;

public class Main {
    public static boolean devmode = true;
    public static Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").load();
    public static String serverjsonlist = devmode ? dotenv.get("SERVERJSONLIST") : "";
    public static String databaseuri = devmode ? dotenv.get("DATABASEURI") : "";
    public static ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    public static MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(databaseuri))
            .serverApi(serverApi).build();

    public static int chunksize = 20;

    public static void main(String[] args) {
        ((Logger) LoggerFactory.getLogger("org.mongodb.driver")).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger("reactor.util.Loggers")).setLevel(Level.WARN);
        if (!devmode) {
            for (String arg : args) {
                if (arg.startsWith("--serverjsonlist=")) {
                    serverjsonlist = arg.substring("--serverjsonlist=".length());
                    System.out.println(serverjsonlist);
                }
                if (arg.startsWith("--databaseuri=")) {
                    databaseuri = arg.substring("--databaseuri=".length());
                    System.out.println(databaseuri);
                }
            }
        }

        VerifyServersOnSDB();
        //ScanServerJson();
    }

    public static void ScanServerJson() {
        for (int i = 0; i < ServerJsonProcessor.Chunking(serverjsonlist, Main.chunksize).size(); i++) {
            ServerScanner object = new ServerScanner();
            object.start();
        }
    }

    public static void VerifyServersOnSDB() {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("Servers");
            MongoCollection<Document> Serverlist = database.getCollection("Serverlist");

            List<Masscan> DBserverList = ServerJsonProcessor.parseonlydbjson(String.valueOf(Flux.from(Serverlist.find()).map(Document::toJson).collectList().block()));
            System.out.println(DBserverList);
        }
    }
}
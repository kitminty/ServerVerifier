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
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class Main {
    public static boolean devmode = true;
    public static Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").load();
    public static String serverjsonlist = devmode ? dotenv.get("SERVERJSONLIST") : "";
    public static String databaseuri = devmode ? dotenv.get("DATABASEURI") : "";
    public static String operation = "";
    public static String DBserverList;
    public static ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    public static MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(databaseuri))
            .serverApi(serverApi).build();
    public static MongoClient mongoClient = MongoClients.create(settings);
    public static boolean manualchunksize = true;
    public static int chunksize = 1000; ///if this is too low serverscanner it will slow down and stop

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
                if (arg.startsWith("--operation=")) {
                    operation = arg.substring("--operation=".length());
                    if (operation.equals("VerifyServersOnSDB")) {
                        VerifyServersOnSDB();
                    } else if (operation.equals("ScanServerJson")) {
                        ScanServerJson();
                    }
                    System.out.println(operation);
                }
            }
        }

        //VerifyServersOnSDB();
        ScanServerJson();
    }

    public static void ScanServerJson() {
        for (int i = 0; i < ServerJsonProcessor.Chunking(serverjsonlist, manualchunksize ? chunksize : Math.toIntExact(round(sqrt(ServerJsonProcessor.parsejsonfile(serverjsonlist).size())))).size(); i++) {
            ServerScanner object = new ServerScanner();
            object.start();
        }
    }

    public static void VerifyServersOnSDB() {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("Servers");
            MongoCollection<Document> Serverlist = database.getCollection("Serverlist");
            DBserverList = String.valueOf(Flux.from(Serverlist.find()).map(Document::toJson).collectList().block());
            for (int i = 0; i < ServerJsonProcessor.DBChunking(DBserverList, manualchunksize ? chunksize : Math.toIntExact(round(sqrt(ServerJsonProcessor.parseonlydbjson(DBserverList).size())))).size(); i++) {
                DBVerify object = new DBVerify();
                object.start();
            }
        }
    }
}
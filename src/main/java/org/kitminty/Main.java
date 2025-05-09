package org.kitminty;

//cats are cool

import com.mongodb.*;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.reactivestreams.Publisher;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationOptions;
import reactor.core.publisher.Mono;


public class Main {
    public static boolean devmode = true;
    public static Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").load();
    public static String serverjsonlist = devmode ? dotenv.get("SERVERJSONLIST") : "";
    public static String outputtxt = devmode ? dotenv.get("OUTPUTTXT") : "";
    public static String databaseuri = devmode ? dotenv.get("DATABASEURI") : "";
    public static ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    public static MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(databaseuri))
            .serverApi(serverApi).build();

    public static int chunksize = 20;

    public static void main(String[] args) {
        if (!devmode) {
            for (String arg : args) {
                if (arg.startsWith("--serverjsonlist=")) {
                    serverjsonlist = arg.substring("--serverjsonlist=".length());
                    System.out.println(serverjsonlist);
                }
                if (arg.startsWith("--outputtxt=")) {
                    outputtxt = arg.substring("--outputtxt=".length());
                    System.out.println(outputtxt);
                }
                if (arg.startsWith("--databaseuri=")) {
                    databaseuri = arg.substring("--databaseuri=".length());
                    System.out.println(databaseuri);
                }
            }
        }

        for (int i = 0; i < ServerJsonProcessor.Chunking(serverjsonlist, Main.chunksize).size(); i++) {
            ServerScanner object = new ServerScanner();
            object.start();
        }
    }
}
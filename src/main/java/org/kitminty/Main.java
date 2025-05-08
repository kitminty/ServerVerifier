package org.kitminty;

//cats are cool

import com.mongodb.*;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;

public class Main {
    public static boolean devmode = false;
    public static Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").load();
    public static String serverjsonlist = devmode ? dotenv.get("SERVERJSONLIST") : "";
    public static String outputtxt = devmode ? dotenv.get("OUTPUTTXT") : "";
    public static String databaseuri = devmode ? dotenv.get("DATABASEURI") : "";

    public static int chunksize = 2700;

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
        /*
        for (int i = 0; i < ServerJsonProcessor.Chunking(serverjsonlist, Main.chunksize).size(); i++) {
            ServerScanner object = new ServerScanner();
            object.start();
        }
         */

        System.out.println(databaseuri);
        /*
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(databaseuri))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
         */
    }
}
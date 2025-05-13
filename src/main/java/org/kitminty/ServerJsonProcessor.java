package org.kitminty;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServerJsonProcessor {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Masscan.class, (JsonDeserializer<Masscan>) (jsonElement, type, ctx) -> {
                JsonObject obj = jsonElement.getAsJsonObject();
                String ip = obj.get("ip").getAsString();
                JsonArray portArr = obj.get("ports").getAsJsonArray();
                List<Port> ports = new ArrayList<>(portArr.size());
                ports.add(ctx.deserialize(portArr.get(0), Port.class));
                return new Masscan(ip, ports);
            })
            .registerTypeAdapter(Port.class, (JsonDeserializer<Port>) (jsonElement, type, ctx) -> new Port(jsonElement.getAsJsonObject().get("port").getAsShort()))
            .create();
    private static final Gson DBGSON = new GsonBuilder()
            .registerTypeAdapter(Masscan.class, (JsonDeserializer<Masscan>) (jsonElement, type, ctx) -> {
                JsonObject obj = jsonElement.getAsJsonObject();
                String ip = obj.get("_id").getAsString();
                return new Masscan(ip, null);
            })
            .registerTypeAdapter(Port.class, (JsonDeserializer<Port>) (jsonElement, type, ctx) -> new Port(jsonElement.getAsJsonObject().get("port").getAsShort()))
            .create();

    public static List<Masscan> parseonlyjson(String json) {
        Type serverList = new TypeToken<List<Masscan>>() {}.getType();
        return GSON.fromJson(json, serverList);
    } ///combine with Chunking()

    public static List<Masscan> parsejsonfile(String address) {
        JSONArray jsonArray = null;
        Type serverList = new TypeToken<List<Masscan>>() {}.getType();
        try {
            String content = new String(Files.readAllBytes(Paths.get(address)));
            jsonArray = new JSONArray(content);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return GSON.fromJson(String.valueOf(jsonArray), serverList);
    }

    public static List<Masscan> parseonlydbjson(String json) {
        Type serverList = new TypeToken<List<Masscan>>() {}.getType();
        return DBGSON.fromJson(json, serverList);
    }

    public static List<JSONArray> Chunking(String address, int threadchunks) {
        List<JSONArray> chunks = null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(address)));
            JSONArray jsonArray = new JSONArray(content);
            chunks = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i += threadchunks) {
                int endIndex = Math.min(i + threadchunks, jsonArray.length());
                JSONArray chunk = new JSONArray();
                for (int j = i; j < endIndex; j++) {
                    chunk.put(jsonArray.get(j));
                }
                chunks.add(chunk);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return chunks;
    }

    public static List<JSONArray> DBChunking(String list, int threadchunks) {
        List<JSONArray> chunks;
        JSONArray jsonArray = new JSONArray(list);
        chunks = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i += threadchunks) {
            int endIndex = Math.min(i + threadchunks, jsonArray.length());
            JSONArray chunk = new JSONArray();
            for (int j = i; j < endIndex; j++) {
                chunk.put(jsonArray.get(j));
            }
            chunks.add(chunk);
        }
        return chunks;
    }
}
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

    public static List<Masscan> parseonlyjson(String json) {
        Type serverList = new TypeToken<List<Masscan>>() {}.getType();
        return GSON.fromJson(json, serverList);
    }

    public static List<JSONArray> Chunklist(String address, int threadchunks) {
        List<JSONArray> chunks = null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(address)));
            JSONArray jsonArray = new JSONArray(content);
            chunks = splitArray(jsonArray, threadchunks);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return chunks;
    }

    public static List<JSONArray> splitArray(JSONArray array, int chunkSize) {
        List<JSONArray> chunks = new ArrayList<>();
        for (int i = 0; i < array.length(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, array.length());
            JSONArray chunk = new JSONArray();
            for (int j = i; j < endIndex; j++) {
                chunk.put(array.get(j));
            }
            chunks.add(chunk);
        }
        return chunks;
    }
}

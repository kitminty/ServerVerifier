package org.kitminty;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
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

    public static List<Masscan> parse(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            Type serverList = new TypeToken<List<Masscan>>() {}.getType();
            return GSON.fromJson(reader, serverList);
        } catch (IOException e) {
            throw new RuntimeException("cant find output file");
        }
    }
}

package me.grabsky.azure.storage.objects.deserializers;

import com.google.gson.*;
import me.grabsky.azure.storage.objects.JsonLocation;

import java.lang.reflect.Type;

public class JsonLocationDeserializer implements JsonDeserializer<JsonLocation> {

    @Override
    public JsonLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject o = json.getAsJsonObject();
        return new JsonLocation(
                o.get("world").getAsString(),
                o.get("x").getAsFloat(),
                o.get("y").getAsFloat(),
                o.get("z").getAsFloat(),
                o.get("yaw").getAsFloat(),
                o.get("pitch").getAsFloat()
        );
    }
}

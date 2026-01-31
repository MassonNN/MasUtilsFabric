package ru.massonnn.masutils.client.telemetry;

import com.google.gson.*;
import java.awt.Color;
import java.lang.reflect.Type;

public class ColorAdapter implements JsonSerializer<Color> {
    @Override
    public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(String.format("#%02x%02x%02x%02x",
                src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha()));
    }
}
package com.vertex.vos.Utilities;

import com.google.gson.*;
import lombok.Getter;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class GsonUtils {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString());
                }
            })
            .create();


}

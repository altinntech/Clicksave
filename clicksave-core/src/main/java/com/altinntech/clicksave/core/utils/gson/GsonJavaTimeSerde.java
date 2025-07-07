package com.altinntech.clicksave.core.utils.gson;

import com.google.gson.*;
import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

@AllArgsConstructor
public class GsonJavaTimeSerde<T extends TemporalAccessor> implements JsonSerializer<T>, JsonDeserializer<T> {

    private final DateTimeFormatter formatter;
    private final TemporalQuery<T> query;

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return formatter.parse(json.getAsString(), query);
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }

    public static GsonJavaTimeSerde<OffsetDateTime> offsetDateTime() {
        return new GsonJavaTimeSerde<>(DateTimeFormatter.ISO_OFFSET_DATE_TIME, OffsetDateTime::from);
    }

    public static GsonJavaTimeSerde<LocalDateTime> localDateTime() {
        return new GsonJavaTimeSerde<>(DateTimeFormatter.ISO_LOCAL_DATE_TIME, LocalDateTime::from);
    }

    public static GsonJavaTimeSerde<Instant> instant() {
        return new GsonJavaTimeSerde<>(DateTimeFormatter.ISO_INSTANT, Instant::from);
    }

    public static GsonJavaTimeSerde<LocalDate> localDate() {
        return new GsonJavaTimeSerde<>(DateTimeFormatter.ISO_LOCAL_DATE, LocalDate::from);
    }
}

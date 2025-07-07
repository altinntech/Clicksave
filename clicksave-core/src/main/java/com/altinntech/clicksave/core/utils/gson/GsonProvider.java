package com.altinntech.clicksave.core.utils.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class GsonProvider {

    private GsonProvider() {}

    public static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, GsonJavaTimeSerde.instant())
                .registerTypeAdapter(LocalDate.class, GsonJavaTimeSerde.localDate())
                .registerTypeAdapter(LocalDateTime.class, GsonJavaTimeSerde.localDateTime())
                .registerTypeAdapter(OffsetDateTime.class, GsonJavaTimeSerde.offsetDateTime())
                .create();
    }
}

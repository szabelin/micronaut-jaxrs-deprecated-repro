package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Deprecated
@Controller("/api/v1/native-things")
public class NativeThingsController {

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public List<NativeThing> getThings() {
        return List.of(new NativeThing("hello"));
    }

    @Serdeable
    public record NativeThing(String name) {}
}

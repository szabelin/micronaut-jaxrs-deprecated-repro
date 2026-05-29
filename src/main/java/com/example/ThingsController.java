package com.example;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Deprecated // <-- removing this resolves the bug
@Path("/api/v1/things")
public class ThingsController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Thing> getThings() {
        return List.of(new Thing("hello"));
    }

    @Serdeable
    public record Thing(String name) {}
}

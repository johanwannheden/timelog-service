package org.example.timelog.logging.boundary;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.example.timelog.logging.model.TimelogEntity;
import org.example.timelog.logging.service.TimelogService;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("timelog")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Timed(value = "timelog", percentiles = {0.80, 0.90, 0.95}, histogram = true)
public class TimelogResource {

    private static final Logger LOGGER = Logger.getLogger(TimelogResource.class);

    private final TimelogService service;
    private final MeterRegistry registry;

    TimelogResource(TimelogService service, MeterRegistry registry) {
        this.service = service;
        this.registry = registry;
    }

    @Operation(
            summary = "Add entry",
            description = "Persist a Timelog record and returns it"
    )
    @PUT
    @Path("create")
    public Response createLogEntry(@RequestBody TimelogEntity entry) {
        registry.counter("logentry");
        LOGGER.info("Adding entry " + entry);
        return Response.accepted(service.persistEntry(entry)).build();
    }

    @Operation(
            summary = "Get entries",
            description = "Reads all Timelog record"
    )
    @GET
    @Path("all")
    @Timed(value = "logentries.getall")
    @Counted(value = "logentries.getall")
    public Response getLogEntries() {
        registry.counter("logentry");
        var allEntries = service.getAllEntries();
        var response = Response.ok(allEntries).build();
        return response;
    }

    @Operation(
            summary = "Update entry",
            description = "Updates a Timelog record"
    )
    @POST
    @Path("update/{id}")
    @Timed(value = "logentries.update")
    @Counted(value = "logentries.update")
    public void updateLogEntry(@PathParam("id") @NotNull String id, @RequestBody TimelogEntity entry) {
        service.updateEntry(id, entry);
    }

    @Operation(
            summary = "Delete entry",
            description = "Deletes a Timelog record"
    )
    @DELETE
    @Path("delete/{id}")
    @Counted("logentries.delete")
    public void deleteLogEntry(@PathParam("id") @NotNull String id) {
        service.deleteEntry(id);
    }
}

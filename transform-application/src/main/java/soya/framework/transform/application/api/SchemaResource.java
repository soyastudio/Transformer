package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soya.framework.transform.application.service.SchemaService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/schema")
@Api(value = "Schema Commandline Service")
public class SchemaResource {

    @Autowired
    private SchemaService schemaService;

    @GET
    @Path("/help")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response help(@QueryParam("q") String query) {
        try {
            return Response.ok(schemaService.help(query)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/cmd")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response execute(@HeaderParam("cmd") String cmd, String msg) {
        try {
            return Response.ok(schemaService.execute(cmd, msg)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

}

package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/transform")
@Api(value = "Transform Service")
public class TransformerResource {

    @POST
    @Path("/evaluate")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response evaluate(@HeaderParam("chain") String chain, String json) {
        StringBuilder builder = new StringBuilder();
        builder.append("JOLT").append("\n");
        return Response.status(200).entity(builder.toString()).build();
    }


    @POST
    @Path("/chain")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response transform(@HeaderParam("chain") String chain, String json) {
        StringBuilder builder = new StringBuilder();
        builder.append("JOLT").append("\n");
        return Response.status(200).entity(builder.toString()).build();
    }
}

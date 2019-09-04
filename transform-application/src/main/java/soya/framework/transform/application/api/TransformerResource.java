package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Component
@Path("/transformer")
@Api(value = "Transform Service")
public class TransformerResource {

    @POST
    @Path("/jolt")
    public Response jolt() {
        return Response.status(200).entity("Hello JOLT Transformer").build();
    }
}

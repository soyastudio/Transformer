package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import soya.framework.commons.commandline.CommandDelegate;
import soya.framework.commons.commandline.CommandMapping;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/convert")
@Api(value = "Common Command Service")
public class CommonCommandResource extends CommandRestAdapter {

    @Autowired
    @Qualifier("CommonCommandDelegate")
    private CommandDelegate delegate;

    @GET
    @Path("/help")
    @Produces({MediaType.TEXT_PLAIN})
    public Response help(@QueryParam("cmd") String cmd) throws Exception {
        return Response.ok(delegate.context().toString(cmd)).build();
    }

    @POST
    @Path("/{cmd}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @CommandMapping(command = "{{0}}", template = "-m {{1}}")
    public Response produce(@PathParam("cmd") String cmd,  String message) throws Exception {
        return Response.ok(delegate("produce", new Object[]{cmd, message})).build();
    }

}

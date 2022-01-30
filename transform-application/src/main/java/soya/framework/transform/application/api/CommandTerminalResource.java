package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soya.framework.transform.application.service.CommandService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/terminal")
@Api(value = "Command Terminal Service")
public class CommandTerminalResource {

    @Autowired
    private CommandService commandService;

    @GET
    @Path("/help")
    @Produces({MediaType.TEXT_PLAIN})
    public Response help(@QueryParam("cmd") String cmd) throws Exception {
        return Response.ok(commandService.help()).build();
    }

    @GET
    @Path("/properties")
    @Produces({MediaType.TEXT_PLAIN})
    public Response properties(@QueryParam("group") String group) throws Exception {
        return Response.ok(commandService.properties(group)).build();
    }

    @POST
    @Path("/execute")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response execute(@HeaderParam("delegate") String delegate, String command) throws Exception {
        return Response.ok(commandService.execute(delegate, command)).build();
    }

    @POST
    @Path("/process")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response execute(@HeaderParam("delegate") String delegate,
                            @HeaderParam("command") String command,
                            @HeaderParam("options") String options,
                            String input) throws Exception {

        return Response.ok(commandService.execute(delegate, command, options, input)).build();
    }
}

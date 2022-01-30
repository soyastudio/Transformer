package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import soya.framework.commons.cli.CommandExecutor;
import soya.framework.commons.cli.CommandDispatcher;
import soya.framework.commons.cli.CommandMapping;
import soya.framework.commons.cli.OptionMapping;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/tool")
@Api(value = "Common Toolkit Service")
public class CommonCommandResource extends CommandDispatcher {

    public CommonCommandResource(@Autowired
                                 @Qualifier("CommonCommandDelegate")
                                         CommandExecutor delegate) {
        super(delegate);
    }

    @GET
    @Path("/help")
    @Produces({MediaType.TEXT_PLAIN})
    public Response help(@QueryParam("cmd") String cmd) throws Exception {
        return Response.ok(_help(cmd)).build();
    }

    @POST
    @Path("/{cmd}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @CommandMapping(command = "{{0}}",
            options = {
                    @OptionMapping(option = "m", parameterIndex = 2)
            },
            template = "{{1}}")
    public Response process(@PathParam("cmd") String cmd, @HeaderParam("options") String options, String message) throws Exception {
        return Response.ok(_dispatch("process", new Object[]{cmd, options, message})).build();
    }

}

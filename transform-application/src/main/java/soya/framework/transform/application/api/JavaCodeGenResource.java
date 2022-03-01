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

//@Component
//@Path("/java")
//@Api(value = "Java Code Generation Service")
public class JavaCodeGenResource extends CommandDispatcher {
    public JavaCodeGenResource(@Autowired
                               @Qualifier("JavaCodegenCommandExecutor")
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
                    @OptionMapping(option = "p", parameterIndex = 1),
                    @OptionMapping(option = "c", parameterIndex = 2)

            },
            template = "{{3}}")
    public Response process(@PathParam("cmd") String cmd,
                            @HeaderParam("packageName") String packageName,
                            @HeaderParam("className") String className,
                            String options) throws Exception {

        String opts = options;
        if (opts != null) {
            opts = opts.replaceAll("\n", " ");
        }

        return Response.ok(_dispatch("process", new Object[]{cmd, packageName, className, opts})).build();
    }
}

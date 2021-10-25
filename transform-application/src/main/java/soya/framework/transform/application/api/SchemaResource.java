package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soya.framework.commons.cli.CommandLines;
import soya.framework.transform.application.configuration.CommandLinesConfiguration;
import soya.framework.transform.schema.SchemaCommands;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

@Component
@Path("/schema")
@Api(value = "Schema Service")
public class SchemaResource {

    private CommandLines.Evaluator evaluator = new CommandLines.Evaluator() {
        @Override
        public String evaluate(String v, Options options, Properties properties) {
            String token = v;
            if(token.toLowerCase().endsWith(".xsd")) {
                if(token.startsWith("/")) {
                    token = properties.getProperty("workspace.cmm.dir") + token;

                } else {
                    token = properties.getProperty("workspace.cmm.dir") + "/" + token;
                }
            }

            return token;
        }
    };

    @POST
    @Path("/json")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response execute(@HeaderParam("cmd") String cmd, String msg) {
        try {


            return Response.ok(CommandLines.execute(cmd, SchemaCommands.class, evaluator)).build();

        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}

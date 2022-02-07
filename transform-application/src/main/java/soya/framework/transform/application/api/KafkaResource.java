package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import soya.framework.commons.cli.CommandExecutor;
import soya.framework.commons.cli.CommandDispatcher;
import soya.framework.commons.cli.CommandMapping;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/kafka")
@Api(value = "Kafka Command Service")
public class KafkaResource extends CommandDispatcher {

    public KafkaResource(@Autowired
                                @Qualifier("KafkaCommandExecutor")
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
    @Path("/execute")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response execute(String command) throws Exception {
        return Response.ok(_execute(command)).build();
    }

    @POST
    @Path("/produce")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @CommandMapping(command = "produce", template = "-p {{0}} -m {{1}}")
    public Response produce(@HeaderParam("topic") String topic,  String message) throws Exception {
        return Response.ok(_dispatch("produce", new Object[]{topic, message})).build();
    }
}

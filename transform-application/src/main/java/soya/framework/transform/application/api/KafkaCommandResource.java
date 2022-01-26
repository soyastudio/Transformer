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
@Path("/kafka")
@Api(value = "Kafka Command Service")
public class KafkaCommandResource extends CommandRestAdapter {
    @Autowired
    @Qualifier("KafkaCommandDelegate")
    private CommandDelegate delegate;

    @GET
    @Path("/help")
    @Produces({MediaType.TEXT_PLAIN})
    public Response help(@QueryParam("cmd") String cmd) throws Exception {
        return Response.ok(delegate.context().toString(cmd)).build();
    }

    @POST
    @Path("/delegate")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response delegate(String command) throws Exception {
        return Response.ok(delegate.execute(command)).build();
    }

    @POST
    @Path("/produce")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @CommandMapping(command = "produce", template = "-p {{0}} -m {{1}}")
    public Response produce(@HeaderParam("topic") String topic,  String message) throws Exception {
        return Response.ok(delegate("produce", new Object[]{topic, message})).build();
    }
}

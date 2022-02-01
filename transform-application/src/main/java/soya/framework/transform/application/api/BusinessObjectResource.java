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
@Path("/bod")
@Api(value = "Business Object Development Service")
public class BusinessObjectResource extends CommandDispatcher {

    public BusinessObjectResource(@Autowired
                                         @Qualifier("BusinessObjectCommandExecutor")
                                                 CommandExecutor executor) {
        super(executor);
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

    @GET
    @Path("/{bod}/schema")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "schema", template = "-r {{workspace.home}} -b {{0}}")
    public Response schema(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("schema", new Object[]{bod})).build();
    }

    @GET
    @Path("/{bod}/sample-xml")
    @Produces({MediaType.APPLICATION_XML})
    @CommandMapping(command = "sample-xml", template = "-r {{workspace.home}} -b {{0}}")
    public Response sampleXml(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("sampleXml", new Object[]{bod})).build();
    }

    @GET
    @Path("/{bod}/avsc")
    @Produces({MediaType.APPLICATION_JSON})
    @CommandMapping(command = "avsc", template = "-r {{workspace.home}} -b {{0}}")
    public Response avsc(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("avsc", new Object[]{bod})).build();
    }

    @GET
    @Path("/{bod}/create")
    @Produces({MediaType.APPLICATION_JSON})
    @CommandMapping(command = "create", template = "-r {{workspace.home}} -b {{0}}")
    public Response create(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("create", new Object[]{bod})).build();
    }


}

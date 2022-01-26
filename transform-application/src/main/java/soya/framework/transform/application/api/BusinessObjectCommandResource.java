package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import soya.framework.commons.commandline.CommandAdapter;
import soya.framework.commons.commandline.CommandDelegate;
import soya.framework.commons.commandline.CommandMapping;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/cmd")
@Api(value = "BOD Service")
public class BusinessObjectCommandResource {

    @Autowired
    @Qualifier("BusinessObjectCommandDelegate")
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

    @GET
    @Path("/{bod}/schema")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "schema", template = "-r {{workspace.home}} -b {{0}}")
    public Response schema(@PathParam("bod") String bod) throws Exception {
        return Response.ok(delegate("schema", new Object[]{bod})).build();
    }

    @GET
    @Path("/{bod}/sample-xml")
    @Produces({MediaType.APPLICATION_XML})
    @CommandMapping(command = "sample-xml", template = "-r {{workspace.home}} -b {{0}}")
    public Response sampleXml(@PathParam("bod") String bod) throws Exception {
        return Response.ok(delegate("sampleXml", new Object[]{bod})).build();
    }

    @GET
    @Path("/{bod}/avsc")
    @Produces({MediaType.APPLICATION_JSON})
    @CommandMapping(command = "avsc", template = "-r {{workspace.home}} -b {{0}}")
    public Response avsc(@PathParam("bod") String bod) throws Exception {
        return Response.ok(delegate("avsc", new Object[]{bod})).build();
    }

    @GET
    @Path("/{bod}/create")
    @Produces({MediaType.APPLICATION_JSON})
    @CommandMapping(command = "create", template = "-r {{workspace.home}} -b {{0}}")
    public Response create(@PathParam("bod") String bod) throws Exception {
        return Response.ok(delegate("create", new Object[]{bod})).build();
    }

    protected String delegate(String methodName, Object[] args) throws Exception {
        return CommandAdapter.execute(getClass(), methodName, args, delegate);
    }


}

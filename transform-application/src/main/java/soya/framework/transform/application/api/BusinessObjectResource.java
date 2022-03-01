package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import soya.framework.commons.cli.CommandDispatcher;
import soya.framework.commons.cli.CommandExecutor;
import soya.framework.commons.cli.CommandMapping;
import soya.framework.commons.cli.Resources;
import soya.framework.commons.util.StringCompressUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
    @Path("/schema/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-schema", template = "-r {{workspace.home}} -b {{0}}")
    public Response schema(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("schema", new Object[]{bod})).build();
    }

    @GET
    @Path("/sample-xml/{bod}")
    @Produces({MediaType.APPLICATION_XML})
    @CommandMapping(command = "bod-sample-xml", template = "-r {{workspace.home}} -b {{0}}")
    public Response sampleXml(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("sampleXml", new Object[]{bod})).build();
    }

    @GET
    @Path("/json-types/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-json-types", template = "-r {{workspace.home}} -b {{0}}")
    public Response jsonTypes(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("jsonTypes", new Object[]{bod})).build();
    }

    @GET
    @Path("/avsc/{bod}")
    @Produces({MediaType.APPLICATION_JSON})
    @CommandMapping(command = "bod-avsc", template = "-r {{workspace.home}} -b {{0}}")
    public Response avsc(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("avsc", new Object[]{bod})).build();
    }

    @GET
    @Path("/sample-avro/{bod}")
    @Produces({MediaType.APPLICATION_XML})
    @CommandMapping(command = "bod-sample-avro", template = "-r {{workspace.home}} -b {{0}}")
    public Response sampleAvro(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("sampleAvro", new Object[]{bod})).build();
    }

    @POST
    @Path("/create/{bod}")
    @Produces({MediaType.APPLICATION_JSON})
    @CommandMapping(command = "bod-create", template = "-r {{workspace.home}} -b {{0}}")
    public Response create(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("create", new Object[]{bod})).build();
    }

    @GET
    @Path("/source-schema/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-json-schema", template = "-r {{workspace.home}} -b {{0}}")
    public Response sourceSchema(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("sourceSchema", new Object[]{bod})).build();
    }

    @GET
    @Path("/xlsx-mappings/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-xlsx-mapping", template = "-r {{workspace.home}} -b {{0}}")
    public Response xlsx(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("xlsx", new Object[]{bod})).build();
    }

    @GET
    @Path("/mappings/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-xpath-mappings", template = "-r {{workspace.home}} -b {{0}}")
    public Response mappings(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("mappings", new Object[]{bod})).build();
    }

    @GET
    @Path("/mappings-validate/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-mappings-validate", template = "-r {{workspace.home}} -b {{0}}")
    public Response validateMappings(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("validateMappings", new Object[]{bod})).build();
    }

    @POST
    @Path("/mappings-override/{bod}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-mappings-override", template = "-r {{workspace.home}} -b {{0}} -o {{1}}")
    public Response overrideMappings(@PathParam("bod") String bod, String override) throws Exception {
        return Response.ok(_dispatch("overrideMappings", new Object[]{bod, encodeMessage(override)})).build();
    }

    @GET
    @Path("/construct-annotate/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-construct-annotate", template = "-r {{workspace.home}} -b {{0}}")
    public Response constructAnnotate(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("constructAnnotate", new Object[]{bod})).build();
    }

    @GET
    @Path("/construct/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-construct", template = "-r {{workspace.home}} -b {{0}} -c xpath-construct.properties")
    public Response construct(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("construct", new Object[]{bod})).build();
    }

    @GET
    @Path("/arrays/{bod}")
    @Produces({MediaType.APPLICATION_JSON})
    @CommandMapping(command = "bod-arrays", template = "-r {{workspace.home}} -b {{0}} -c xpath-construct.properties")
    public Response arrays(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("arrays", new Object[]{bod})).build();
    }

    @GET
    @Path("/esql-template/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-esql-template", template = "-r {{workspace.home}} -b {{0}}")
    public Response esqlTemplate(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("esqlTemplate", new Object[]{bod})).build();
    }

    @GET
    @Path("/esql/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-esql", template = "-r {{workspace.home}} -b {{0}}")
    public Response esql(@PathParam("bod") String bod) throws Exception {
        return Response.ok(_dispatch("esql", new Object[]{bod})).build();
    }

    @POST
    @Path("/esql-validate/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    @CommandMapping(command = "bod-esql-validate", template = "-r {{workspace.home}} -b {{0}} -c {{1}}")
    public Response esqlValidate(@PathParam("bod") String bod, String contents) throws Exception {

        String encoded = new String(Base64.getEncoder().encode(contents.getBytes(StandardCharsets.UTF_8)));

        return Response.ok(_dispatch("esqlValidate", new Object[]{bod, encoded})).build();
    }


}

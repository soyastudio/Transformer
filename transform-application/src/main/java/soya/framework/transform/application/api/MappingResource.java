package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soya.framework.transform.application.service.MappingService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/mapping")
@Api(value = "Mapping Commandline Service")
public class MappingResource {

    @Autowired
    private MappingService mappingService;

    @GET
    @Path("/help")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response help(@QueryParam("q") String query) {
        try {
            return Response.ok(mappingService.help(query)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/cmd")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response execute(@HeaderParam("cmd") String cmd, String msg) {
        try {
            return Response.ok(mappingService.execute(cmd, msg)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{bod}/{cmd}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response dispatch(@PathParam("bod") String bod, @PathParam("cmd") String cmd, @HeaderParam("opt") String opt) {
        try {
            String cl = mappingService.defaultCommandLine(bod, cmd);

            if(opt != null) {
                cl = cl + " " + opt;
            }

            return Response.ok(mappingService.execute(cl, null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/schema/{bod}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response schema(@PathParam("bod") String bod) {
        try {
            StringBuilder builder = new StringBuilder("-a schema").append(" -b ").append(bod);

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/mappings/{bod}/{xlsx}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response schema(@PathParam("bod") String bod, @PathParam("xlsx") String xlsx, @QueryParam("v") String v) {
        try {
            if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
            }

            StringBuilder builder = new StringBuilder("-a mapping")
                    .append(" -b ").append(bod)
                    .append(" -m ").append(xlsx);

            if(v != null) {
                builder.append(" -v ").append(v);
            }


            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/adjustment/{bod}/{xlsx}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response adjust(@PathParam("bod") String bod, @PathParam("xlsx") String xlsx, @QueryParam("v") String v) {
        try {
            if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
            }

            StringBuilder builder = new StringBuilder("-a adjust")
                    .append(" -b ").append(bod)
                    .append(" -m ").append(xlsx)
                    .append(" -j xpath-adjustment.properties");

            if(v != null) {
                builder.append(" -v ").append(v);
            }


            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/validate/{bod}/{xlsx}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validate(@PathParam("bod") String bod, @PathParam("xlsx") String xlsx) {
        try {
            if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
            }

            StringBuilder builder = new StringBuilder("-a validate")
                    .append(" -b ").append(bod)
                    .append(" -m ").append(xlsx)
                    .append(" -j ").append("xpath-adjustment.properties");


            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/construct/{bod}/{xlsx}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response construct(@PathParam("bod") String bod, @PathParam("xlsx") String xlsx) {
        try {
            if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
            }

            StringBuilder builder = new StringBuilder("-a construct")
                    .append(" -b ").append(bod)
                    .append(" -m ").append(xlsx)
                    .append(" -j ").append("xpath-adjustment.properties");

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


}

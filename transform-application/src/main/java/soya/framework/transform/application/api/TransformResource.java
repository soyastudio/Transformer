package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soya.framework.transform.application.service.TransformService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/transform")
@Api(value = "Transform Service")
public class TransformResource {

    @Autowired
    private TransformService transformService;

    @GET
    @Path("/schema/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response schema(@PathParam("bod") String bod) {

        StringBuilder builder = new StringBuilder()
                .append("-a ").append("schema")
                .append(" -b ").append(bod);

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/sample-xml/{bod}")
    @Produces({MediaType.APPLICATION_XML})
    public Response sampleXml(@PathParam("bod") String bod) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("sampleXml")
                .append(" -b ").append(bod);

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/avsc/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response avsc(@PathParam("bod") String bod) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("avsc")
                .append(" -b ").append(bod);

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/sample-avro/{bod}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response sampleAvro(@PathParam("bod") String bod) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("sampleAvro")
                .append(" -b ").append(bod);
        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/mapping/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response mapping(@PathParam("bod") String bod, @QueryParam("sheet") String sheet) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("mapping")
                .append(" -b ").append(bod);

        if(sheet != null) {
            builder.append(" -s ").append(sheet);
        }

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/unknown-paths/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response unknownPaths(@PathParam("bod") String bod, @QueryParam("sheet") String sheet) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("unknownPaths")
                .append(" -b ").append(bod);

        if(sheet != null) {
            builder.append(" -s ").append(sheet);
        }

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/validate/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response validate(@PathParam("bod") String bod, @QueryParam("sheet") String sheet) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("validate")
                .append(" -b ").append(bod);

        if(sheet != null) {
            builder.append(" -s ").append(sheet);
        }

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/adjustment/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response adjustment(@PathParam("bod") String bod, @QueryParam("sheet") String sheet) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("adjust")
                .append(" -b ").append(bod);

        if(sheet != null) {
            builder.append(" -s ").append(sheet);
        }

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/construct/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response construct(@PathParam("bod") String bod, @QueryParam("sheet") String sheet) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("construct")
                .append(" -b ").append(bod);

        if(sheet != null) {
            builder.append(" -s ").append(sheet);
        }

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/json-type-mappings/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response xpathJsonType(@PathParam("bod") String bod) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("xpathJsonType")
                .append(" -b ").append(bod);

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/json-type-functions/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response xpathJsonTypeFunctions(@PathParam("bod") String bod) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append("xpathJsonTypeFunctions")
                .append(" -b ").append(bod);

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    //@GET
    //@Path("/help")
    //@Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response help() {
        try {
            return Response.ok(transformService.help()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    //@GET
    //@Path("/{bod}/{cmd}")
    //@Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response execute(@PathParam("bod") String bod, @PathParam("cmd") String cmd, @HeaderParam("opt") String opt) {
        StringBuilder builder = new StringBuilder()
                .append("-a ").append(cmd)
                .append(" -b ").append(bod);

        if (opt != null) {
            String[] arr = opt.trim().split("-");
            for (String s : arr) {
                String token = s.trim();
                if (token.length() > 1) {
                    String o = token;
                    if (token.contains(" ")) {
                        int index = token.indexOf(" ");
                        o = token.substring(0, index);
                    }

                    if (!o.equals("a") && !o.equals("b")) {
                        builder.append("-").append(token);
                    }

                }
            }
        }

        try {
            return Response.ok(transformService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


}

package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soya.framework.tool.Project;
import soya.framework.transform.application.service.KafkaService;
import soya.framework.transform.application.service.MappingService;
import soya.framework.transform.application.service.ProjectService;
import soya.framework.transform.application.service.SchemaService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/work")
@Api(value = "Business Object Development Service")
public class BusinessObjectResource {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private KafkaService kafkaService;

    @POST
    @Path("/project/create/{bod}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_PLAIN})
    public Response create(@PathParam("bod") String bod, Project project) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a create")
                    .append(" -b ").append(bod);
            return Response.ok(projectService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/project/{bod}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response project(@PathParam("bod") String bod) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a get")
                    .append(" -b ").append(bod);
            return Response.ok(projectService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/project/{bod}/readme")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_PLAIN})
    public Response get(@PathParam("bod") String bod) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a readme")
                    .append(" -b ").append(bod);
            return Response.ok(projectService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/project/{bod}/versioning/{version}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response version(@PathParam("bod") String bod, @PathParam("version") String version) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a version")
                    .append(" -b ").append(bod)
                    .append(" -v ").append(version);

            return Response.ok(projectService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/project/{bod}/cutoff/{version}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response cutoff(@PathParam("bod") String bod, @PathParam("version") String version) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a cutoff")
                    .append(" -b ").append(bod)
                    .append(" -v ").append(version);

            return Response.ok(projectService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/schema/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response schema(@PathParam("bod") String bod) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a xpathSchema")
                    .append(" -b ").append(bod);
            return Response.ok(schemaService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/sampleXml/{bod}")
    @Produces({MediaType.APPLICATION_XML})
    public Response sampleXml(@PathParam("bod") String bod) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a sampleXml")
                    .append(" -b ").append(bod);
            return Response.ok(schemaService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/avsc/{bod}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response avsc(@PathParam("bod") String bod) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-a xsdToAvsc")
                    .append(" -b ").append(bod);
            return Response.ok(schemaService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/mapping/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response mapping(@PathParam("bod") String bod, @HeaderParam("xlsx") String xlsx, @HeaderParam("sheet") String sheet) {
        try {
            StringBuilder builder = new StringBuilder("-a mapping")
                    .append(" -b ").append(bod);

            if (xlsx != null) {
                if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
                }

                builder.append(" -m ").append(xlsx);

            }

            if (sheet != null) {
                builder.append(" -s ").append(sheet);
            }

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/unknown/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response unknown(@PathParam("bod") String bod, @HeaderParam("xlsx") String xlsx, @HeaderParam("sheet") String sheet) {
        try {
            StringBuilder builder = new StringBuilder("-a unknownPaths")
                    .append(" -b ").append(bod);

            if (xlsx != null) {
                if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
                }

                builder.append(" -m ").append(xlsx);

            }

            if (sheet != null) {
                builder.append(" -s ").append(sheet);
            }

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/adjustment/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response adjust(@PathParam("bod") String bod, @HeaderParam("xlsx") String xlsx, @HeaderParam("sheet") String sheet) {
        try {
            StringBuilder builder = new StringBuilder("-a adjust")
                    .append(" -b ").append(bod);

            if (xlsx != null) {
                if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
                }

                builder.append(" -m ").append(xlsx);

            }

            if (sheet != null) {
                builder.append(" -s ").append(sheet);
            }

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/validate/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response validate(@PathParam("bod") String bod, @HeaderParam("xlsx") String xlsx, @HeaderParam("sheet") String sheet) {
        try {
            StringBuilder builder = new StringBuilder("-a validate")
                    .append(" -b ").append(bod);

            if (xlsx != null) {
                if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
                }

                builder.append(" -m ").append(xlsx);

            }

            if (sheet != null) {
                builder.append(" -s ").append(sheet);
            }

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/construct/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response construct(@PathParam("bod") String bod, @HeaderParam("xlsx") String xlsx, @HeaderParam("sheet") String sheet) {
        try {
            StringBuilder builder = new StringBuilder("-a construct")
                    .append(" -b ").append(bod);

            if (xlsx != null) {
                if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
                }

                builder.append(" -m ").append(xlsx);

            }

            if (sheet != null) {
                builder.append(" -s ").append(sheet);
            }

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/arrays/{bod}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response arrays(@PathParam("bod") String bod, @HeaderParam("xlsx") String xlsx, @HeaderParam("sheet") String sheet) {
        try {
            StringBuilder builder = new StringBuilder("-a arrays")
                    .append(" -b ").append(bod);

            if (xlsx != null) {
                if (!xlsx.toLowerCase().endsWith(".xlsx")) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Xlsx file is required.").build();
                }

                builder.append(" -m ").append(xlsx);

            }

            if (sheet != null) {
                builder.append(" -s ").append(sheet);
            }

            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/kafka/{bod}/{test}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response kafka(@PathParam("bod") String bod, @PathParam("test") String test, @HeaderParam("options") String options) {
        try {
            StringBuilder builder = new StringBuilder()
                    .append("-b ").append(bod)
                    .append(" -u ").append(test);

            if(options != null) {

                builder.append(" ").append(options);
            }

            return Response.ok(kafkaService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

}

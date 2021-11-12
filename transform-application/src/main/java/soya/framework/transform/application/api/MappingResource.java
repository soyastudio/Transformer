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
@Api(value = "Mapping Service")
public class MappingResource {

    @Autowired
    private MappingService mappingService;

    @GET
    @Path("/help")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response help() {
        try {
            return Response.ok(mappingService.help()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{bod}/{cmd}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
            return Response.ok(mappingService.execute(builder.toString(), null)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


}

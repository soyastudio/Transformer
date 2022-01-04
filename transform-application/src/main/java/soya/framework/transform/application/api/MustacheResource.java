package soya.framework.transform.application.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soya.framework.transform.application.service.MustacheService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/mustache")
@Api(value = "Mustache Service")
public class MustacheResource {

    @Autowired
    private MustacheService mustacheService;

    @GET
    @Path("/template")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response template(@PathParam("mustache") String mustache) {
        return null;
    }

}

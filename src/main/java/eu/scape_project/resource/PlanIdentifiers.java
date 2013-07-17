/**
 *
 */
package eu.scape_project.resource;

import java.util.UUID;

import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author frank asseg
 *
 */
@Component
@Scope("prototype")
@Path("/scape/plan-id/reserve")
public class PlanIdentifiers {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response reservePlanIdentifier() throws RepositoryException {
        final String id = UUID.randomUUID().toString();
        return Response.ok(id, MediaType.TEXT_PLAIN).build();
    }
}

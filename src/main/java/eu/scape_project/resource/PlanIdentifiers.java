/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
 * JAX-RS Resource for Plan Identifiers
 * 
 * @author frank asseg
 * 
 */
@Component
@Scope("prototype")
@Path("/scape/plan-id/reserve")
public class PlanIdentifiers {
    /**
     * Exposes a HTTP end point to reserve a plan id in fedora. In the current
     * implementation plan UUID's are used to mock a reservation mechanism
     * 
     * @return a {@link Response} which maps to a corresponding HTTP response,
     *         containing the generated id
     * @throws RepositoryException
     *             if an error occurred while generating the id
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response reservePlanIdentifier() throws RepositoryException {
        final String id = UUID.randomUUID().toString();
        return Response.ok(id, MediaType.TEXT_PLAIN).build();
    }
}

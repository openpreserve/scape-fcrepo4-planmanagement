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

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.kernel.Datastream;
import org.fcrepo.kernel.FedoraObject;
import org.fcrepo.kernel.exception.InvalidChecksumException;
import org.fcrepo.kernel.rdf.GraphProperties;
import org.fcrepo.kernel.services.DatastreamService;
import org.fcrepo.kernel.services.NodeService;
import org.fcrepo.kernel.services.ObjectService;
import org.fcrepo.http.commons.session.InjectedSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * JAX-RS Resource for Plans
 *
 * @author frank asseg
 *
 */
@Component
@Scope("prototype")
@Path("/scape/plan")
public class Plans {

    public static final String PLAN_FOLDER = "objects/scape/plans/";

    @InjectedSession
    private Session session;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ObjectService objectService;

    @Autowired
    private DatastreamService datastreamService;

    @PUT
    @Path("{id}")
    public Response deployPlan(@PathParam("id")
    final String planId, @Context
    UriInfo uriInfo, final InputStream src) throws RepositoryException,
            IOException, InvalidChecksumException {

        /* create a top level object for the plan */
        final String path = PLAN_FOLDER + planId;
        final FedoraObject plan =
                objectService.createObject(this.session, path);

        /* add the properties to the RDF graph of the exec state object */
        StringBuilder sparql = new StringBuilder();

        /* add the exec state to the parent */
        sparql.append("INSERT {<info:fedora/" + plan.getPath() +
                "> <http://scapeproject.eu/model#hasType> \"PLAN\"} WHERE {};");
        sparql.append("INSERT {<info:fedora/" + plan.getPath() +
                "> <http://scapeproject.eu/model#hasLifecycleState> \"ENABLED\"} WHERE {};");

        /* execute the sparql update */
        plan.updatePropertiesDataset(sparql.toString());

        final Dataset update = plan.updatePropertiesDataset(sparql.toString());
        final Model problems =
                update.getNamedModel(GraphProperties.PROBLEMS_MODEL_NAME);

        //TODO: check the problems and throw an error if applicable

        /* add a datastream holding the plato XML data */
        final Node ds =
                datastreamService.createDatastreamNode(this.session, path +
                        "/plato-xml", "text/xml", src);

        /* and persist the changes in fcrepo */
        this.session.save();
        return Response.created(uriInfo.getAbsolutePath()).entity(
                uriInfo.getAbsolutePath().toASCIIString()).header(
                "Content-Type", "text/plain").build();
    }

    @GET
    @Path("{id}")
    public Response retrievePlan(@PathParam("id")
    final String planId) throws RepositoryException {
        /* fetch the plan form the repository */
        final Datastream ds =
                this.datastreamService.getDatastream(this.session, PLAN_FOLDER +
                        planId + "/plato-xml");
        return Response.ok(ds.getContent(), ds.getMimeType()).build();
    }
}

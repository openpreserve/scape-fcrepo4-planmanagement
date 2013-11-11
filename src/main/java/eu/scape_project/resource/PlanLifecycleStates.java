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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.fcrepo.FedoraObject;
import org.fcrepo.rdf.GraphProperties;
import org.fcrepo.rdf.SerializationUtils;
import org.fcrepo.services.ObjectService;
import org.fcrepo.session.InjectedSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * JAX-RS Resource for Plan life cycle states
 *
 * @author frank asseg
 *
 */
@Component
@Scope("prototype")
@Path("/scape/plan-state")
public class PlanLifecycleStates {

    @InjectedSession
    private Session session;

    @Autowired
    private ObjectService objectService;

    @GET
    @Path("{id}")
    public Response retrievePlanLifecycleState(@PathParam("id")
    final String planId, @Context
    UriInfo uriInfo) throws RepositoryException {
        /* fetch the plan RDF from fedora */
        final String planUri = "/" + Plans.PLAN_FOLDER + planId;
        final FedoraObject plan =
                this.objectService.getObject(this.session, planUri);

        /* get the relevant information from the RDF dataset */
        final Dataset data = plan.getPropertiesDataset();
        final Model rdfModel = SerializationUtils.unifyDatasetModel(data);
        String subject = "info:fedora" + planUri;
        final String lifecycle =
                rdfModel.listStatements(
                        rdfModel.getResource(subject),
                        rdfModel.getProperty("http://scapeproject.eu/model#hasLifecycleState"),
                        (RDFNode) null).next().getObject().asLiteral()
                        .getString();
        return Response.ok(lifecycle, MediaType.TEXT_PLAIN).build();
    }

    @PUT
    @Path("{id}/{state}")
    public Response updateLifecycleState(@PathParam("id")
    final String planId, @PathParam("state")
    String state) throws RepositoryException, JAXBException, IOException {
        /* fetch the plan RDF from fedora */
        final String planUri = "/" + Plans.PLAN_FOLDER + planId;
        final FedoraObject plan =
                this.objectService.getObject(this.session, planUri);

        if (!state.equals("ENABLED") && !state.equals("DISABLED")) {
            throw new RepositoryException("Illegal state: '" + state +
                    "' only one of [ENABLED,DISABLED] is allowed");
        }

        /* delete the existing lifecyclestate and add the new one */
        StringBuilder sparql = new StringBuilder();
        sparql.append("DELETE {<info:fedora/" +
                plan.getPath() +
                "> <http://scapeproject.eu/model#hasLifecycleState> \"ENABLED\"} WHERE {};");
        sparql.append("INSERT {<info:fedora/" + plan.getPath() +
                "> <http://scapeproject.eu/model#hasLifecycleState> \"" +
                state + "\"} WHERE {};");
        final Model errors = plan.updatePropertiesDataset(sparql.toString()).getNamedModel(GraphProperties.PROBLEMS_MODEL_NAME);
        // TODO: check for errors

        this.session.save();
        return Response.ok().build();
    }
}

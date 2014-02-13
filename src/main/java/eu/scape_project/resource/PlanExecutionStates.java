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
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.fcrepo.http.commons.session.InjectedSession;
import org.fcrepo.kernel.FedoraObject;
import org.fcrepo.kernel.RdfLexicon;
import org.fcrepo.kernel.rdf.SerializationUtils;
import org.fcrepo.kernel.rdf.impl.DefaultGraphSubjects;
import org.fcrepo.kernel.services.DatastreamService;
import org.fcrepo.kernel.services.NodeService;
import org.fcrepo.kernel.services.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.scape_project.model.plan.PlanExecutionState;
import eu.scape_project.model.plan.PlanExecutionState.ExecutionState;
import eu.scape_project.model.plan.PlanExecutionStateCollection;
import eu.scape_project.rdf.ScapeRDFVocabulary;
import eu.scape_project.util.ScapeMarshaller;

/**
 * JAX-RS Resource for Plan Execution States
 *
 * @author frank asseg
 *
 */
@Component
@Scope("prototype")
@Path("/scape/plan-execution-state")
public class PlanExecutionStates {

    @InjectedSession
    private Session session;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ObjectService objectService;

    @Autowired
    private DatastreamService datastreamService;

    private final ScapeMarshaller marshaller;

    public PlanExecutionStates()
            throws JAXBException {
        this.marshaller = ScapeMarshaller.newInstance();
    }

    @GET
    @Path("{id}")
    public Response retrievePlanExecutionState(@PathParam("id")
    final String planId, @Context
    UriInfo uriInfo) throws RepositoryException {
        /* fetch the plan RDF from fedora */
        final String planUri = "/" + Plans.PLAN_FOLDER + planId;
        final FedoraObject plan =
                this.objectService.getObject(this.session, planUri);

        final List<PlanExecutionState> states = new ArrayList<>();
        Value[] vals = plan.getNode().getProperty(ScapeRDFVocabulary.HAS_EXEC_STATE).getValues();
        for (Value val : vals) {
        	final int posSep = val.getString().indexOf(':');
        	final Date ts = new Date(Long.parseLong(val.getString().substring(0,posSep)));
        	final String state = val.getString().substring(posSep + 1);
        	states.add(new PlanExecutionState(ts, ExecutionState.valueOf(state)));
        }
        Collections.sort(states);
        final PlanExecutionStateCollection coll =
                new PlanExecutionStateCollection(planUri, states);

        return Response.ok(new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                try {
                    marshaller.serialize(coll, output);
                } catch (JAXBException e) {
                    throw new IOException(e);
                }
            }
        }).build();
    }

    @POST
    @Path("{id}")
    public Response addExecutionState(@PathParam("id")
    final String planId, InputStream src) throws RepositoryException,JAXBException {

        final PlanExecutionState state = marshaller.deserialize(PlanExecutionState.class, src);
        final String planPath = "/" + Plans.PLAN_FOLDER + planId;
        /* fetch the plan from the repository */
        final FedoraObject plan =
                this.objectService.getObject(this.session, planPath);

        final FedoraObject execState = this.objectService.createObject(this.session, planPath + "/" + UUID.randomUUID());
        execState.getNode().setProperty(ScapeRDFVocabulary.HAS_EXEC_STATE, state.getState().name());
        execState.getNode().setProperty(ScapeRDFVocabulary.HAS_TIMESTAMP, state.getTimeStamp().getTime());
        plan.getNode().setProperty(ScapeRDFVocabulary.HAS_EXEC_STATE, execState.getPath());
        
        this.session.save();
        return Response.created(URI.create(planPath)).build();
    }

}

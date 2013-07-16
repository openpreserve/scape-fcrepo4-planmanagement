/**
 *
 */

package eu.scape_project.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.fcrepo.FedoraObject;
import org.fcrepo.rdf.SerializationUtils;
import org.fcrepo.services.DatastreamService;
import org.fcrepo.services.NodeService;
import org.fcrepo.services.ObjectService;
import org.fcrepo.session.InjectedSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.scapeproject.model.plan.PlanExecutionState;
import eu.scapeproject.model.plan.PlanExecutionState.ExecutionState;
import eu.scapeproject.model.plan.PlanExecutionStateCollection;
import eu.scapeproject.util.ScapeMarshaller;

/**
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

        /* get the relevant information from the RDF dataset */
        final Dataset data = plan.getPropertiesDataset();
        final Model rdfModel = SerializationUtils.unifyDatasetModel(data);
        String subject = "info:fedora" + planUri;
        rdfModel.write(System.out);
        final StmtIterator execs =
                rdfModel.listStatements(
                        rdfModel.getResource(subject),
                        rdfModel.getProperty("http://scapeproject.eu/model#hasExecState"),
                        (RDFNode) null);

        /* create the response from the data saved in fcrepo */
        List<PlanExecutionState> states = new ArrayList<>();
        while (execs.hasNext()) {
            final Resource res = rdfModel.createResource(execs.next().getObject().asLiteral().getString());
            final String state =
                    rdfModel.listStatements(
                            res,
                            rdfModel.getProperty("http://scapeproject.eu/model#hasExecutionState"),
                            (RDFNode) null).next().getObject().asLiteral()
                            .getString();
            final long timestamp =
                    rdfModel.listStatements(
                            res,
                            rdfModel.getProperty("http://scapeproject.eu/model#hasTimeStamp"),
                            (RDFNode) null).next().getObject().asLiteral()
                            .getLong();
            states.add(new PlanExecutionState(new Date(timestamp),
                    ExecutionState.valueOf(state)));
        }
        final PlanExecutionStateCollection coll =
                new PlanExecutionStateCollection(planUri, states);

        return Response.ok(new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                try{
                    marshaller.serialize(coll, output);
                }catch(JAXBException e){
                    throw new IOException(e);
                }
            }
        }).build();
    }

}

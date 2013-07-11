package eu.scape_project.resource;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.FedoraObject;
import org.fcrepo.api.FedoraNodes;
import org.fcrepo.api.rdf.HttpGraphSubjects;
import org.fcrepo.exception.InvalidChecksumException;
import org.fcrepo.services.DatastreamService;
import org.fcrepo.services.NodeService;
import org.fcrepo.services.ObjectService;
import org.fcrepo.session.InjectedSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Dataset;

@Component
@Scope("prototype")
@Path("/scape/plan")
public class Plans {

    @InjectedSession
    private Session session;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ObjectService objectService;

    @Autowired
    private DatastreamService datastreamService;
    
    
    
    @PostConstruct
    public void setup(){
        
    }

    @PUT
    @Path("{id}")
    public Response deployPlan(@PathParam("id") final String planId, @Context UriInfo uriInfo, final InputStream src)
            throws RepositoryException, IOException, InvalidChecksumException {
        
        /* create a top level object for the plan */
        final String path = "/objects/scape/plans/" + planId;
        final FedoraObject fo = objectService.createObject(this.session, path);
        final HttpGraphSubjects subjects =new HttpGraphSubjects(FedoraNodes.class, uriInfo, session);
        
        /* add the properties to the RDF graph */
        String sparql = "INSERT {<" + uriInfo.getBaseUri() + "> <http://scapeproject.eu/model:hasFoo> \"test\"} WHERE {}";
        System.out.println(sparql);
        fo.updatePropertiesDataset(sparql);
        
        /* add a datastream holding the plato XML data */
        final Node ds = datastreamService.createDatastreamNode(this.session, path + "/plato-xml", "text/xml", src);
        
        /* and persist the changes in fcrepo */
        this.session.save();
        return Response.created(uriInfo.getAbsolutePath())
                .entity(uriInfo.getAbsolutePath().toASCIIString())
                .header("Content-Type", "text/plain")
                .build();
    }

    @GET
    @Path("{id}")
    public Response retrievePlan(@PathParam("id") final String planId) {
        return Response.ok().build();
    }
}

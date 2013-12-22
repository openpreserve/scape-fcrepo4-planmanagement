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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.fcrepo.http.commons.session.InjectedSession;
import org.fcrepo.kernel.services.DatastreamService;
import org.fcrepo.kernel.services.NodeService;
import org.fcrepo.kernel.services.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.scape_project.model.plan.PlanData;
import eu.scape_project.util.ScapeMarshaller;

/**
 * JAX-RS Resource for Plans
 *
 * @author frank asseg
 *
 */
@Component
@Scope("prototype")
@Path("/scape/plan-list")
public class PlanList {

    public static final String PLAN_FOLDER = "objects/scape/plans/";

    @InjectedSession
    private Session session;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ObjectService objectService;

    @Autowired
    private DatastreamService datastreamService;

    private final ScapeMarshaller marshaller;

    public PlanList()
            throws JAXBException {
        marshaller = ScapeMarshaller.newInstance();
    }
    @GET
    public Response retrievePlanList() throws RepositoryException{
        return retrievePlanList(0l, 0l);
    }

    @GET
    @Path("{limit}/{offset}")
    public Response retrievePlanList(@PathParam("limit")
    final long limit, @PathParam("offset")
    final long offset) throws RepositoryException {
        final List<PlanData> plans = new ArrayList<>();
        NodeIterator nodes = this.retrievePlanNodes(0, 0);
        System.out.println("Found:" + nodes.getSize() + " nodes");
        while (nodes.hasNext()) {
            Node plan = (Node) nodes.next();
            PropertyIterator props = plan.getProperties("scape:*");
            while (props.hasNext()) {
                Property prop = (Property) props.next();
                for (Value val : prop.getValues()) {
                    System.out.println(prop.getName() + ": " + val.getString());
                }
            }
        }
        return Response.ok().build();
    }

    private NodeIterator retrievePlanNodes(long limit, long offset)
            throws RepositoryException {
        this.session.getWorkspace().getQueryManager();
        final QueryManager queryManager =
                this.session.getWorkspace().getQueryManager();
        final QueryObjectModelFactory factory = queryManager.getQOMFactory();

        final Source selector =
                factory.selector("scape:plan", "resourcesSelector");
        final Constraint constraints =
                factory.fullTextSearch("resourcesSelector", null, factory
                        .literal(session.getValueFactory().createValue("*")));

        final Query query =
                factory.createQuery(selector, constraints, null, null);

        if (limit > 0) {
            query.setLimit(limit);
        }
        if (offset > 0) {
            query.setOffset(offset);
        }
        return query.execute().getNodes();
    }
}

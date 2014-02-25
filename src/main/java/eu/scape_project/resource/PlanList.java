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
import java.io.OutputStream;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;

import org.fcrepo.http.commons.session.InjectedSession;
import org.fcrepo.kernel.services.DatastreamService;
import org.fcrepo.kernel.services.NodeService;
import org.fcrepo.kernel.services.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.scape_project.model.Identifier;
import eu.scape_project.model.plan.PlanData;
import eu.scape_project.model.plan.PlanDataCollection;
import eu.scape_project.model.plan.PlanLifecycleState;
import eu.scape_project.model.plan.PlanLifecycleState.PlanState;
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

    public PlanList() throws JAXBException {
        marshaller = ScapeMarshaller.newInstance();
    }

    /**
     * Retrieve a {@link PlanList} of plans stored in Fedora
     * 
     * @return a {@link Response} which maps to a corresponding HTTP response
     *         containing a {@link PlanList}'s XML representation
     * @throws RepositoryException
     */
    @GET
    public Response retrievePlanList() throws RepositoryException {
        return retrievePlanList(0l, 0l);
    }

    /**
     * Retrieve a {@link PlanList} from Fedora
     * @param limit the maximum number of entries in the list
     * @param offset the offset of the list
     * @return a {@link Response} which maps to a corresponding HTTP response, containing a {@link PlanList}'s XML representation
     * @throws RepositoryException
     */
    @GET
    @Path("{limit}/{offset}")
    public Response retrievePlanList(@PathParam("limit")
    final long limit, @PathParam("offset")
    final long offset) throws RepositoryException {
        final List<PlanData> plans = new ArrayList<>();
        NodeIterator nodes = this.retrievePlanNodes(limit, offset);
        while (nodes.hasNext()) {
            Node plan = (Node) nodes.next();
            PropertyIterator props = plan.getProperties("scape:*");
            PlanData.Builder data = new PlanData.Builder();
            data.identifier(new Identifier(plan.getPath().substring(plan.getPath().lastIndexOf('/') + 1)));
            while (props.hasNext()) {
                Property prop = (Property) props.next();
                for (Value val : prop.getValues()) {
                    if (prop.getName().equals("scape:hasTitle")) {
                        data.title(val.getString());
                    }
                    if (prop.getName().equals("scape:hasDescription")) {
                        data.description(val.getString());
                    }
                    if (prop.getName().equals("scape:hasLifecycleState")) {
                        String state = val.getString();
                        int pos;
                        if ((pos = state.indexOf(':')) != -1) {
                            data.lifecycleState(new PlanLifecycleState(PlanState.valueOf(state.substring(0, pos)), state.substring(pos + 1)));
                        } else {
                            data.lifecycleState(new PlanLifecycleState(PlanState.valueOf(state), ""));
                        }
                    }
                }
            }
            plans.add(data.build());
        }
        return Response.ok(new StreamingOutput() {

            @Override
            public void write(OutputStream sink) throws IOException, WebApplicationException {
                try {
                    marshaller.serialize(new PlanDataCollection(plans), sink);
                } catch (JAXBException e) {
                    throw new IOException(e);
                }
            }
        }).build();
    }

    private NodeIterator retrievePlanNodes(long limit, long offset) throws RepositoryException {
        this.session.getWorkspace().getQueryManager();
        final QueryManager queryManager = this.session.getWorkspace().getQueryManager();
        final QueryObjectModelFactory factory = queryManager.getQOMFactory();

        final Source selector = factory.selector("scape:plan", "resourcesSelector");
        final Constraint constraints = factory.fullTextSearch("resourcesSelector", null, factory.literal(session.getValueFactory().createValue("*")));

        final Query query = factory.createQuery(selector, constraints, null, null);

        if (limit > 0) {
            query.setLimit(limit);
        }
        if (offset > 0) {
            query.setOffset(offset);
        }
        return query.execute().getNodes();
    }
}

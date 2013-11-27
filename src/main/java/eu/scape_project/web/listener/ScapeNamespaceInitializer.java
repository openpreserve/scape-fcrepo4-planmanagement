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

package eu.scape_project.web.listener;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.ws.rs.ext.Provider;

import org.fcrepo.http.commons.session.SessionFactory;
import org.fcrepo.kernel.RdfLexicon;
import org.fcrepo.kernel.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.update.UpdateAction;
import com.sun.jersey.api.model.AbstractResourceModelContext;
import com.sun.jersey.api.model.AbstractResourceModelListener;

/**
 * JAX-RS Provider for initialization of the web application
 *
 * @author frank asseg
 *
 */
@Component
@Provider
public class ScapeNamespaceInitializer implements AbstractResourceModelListener {

    private static final Logger LOG = LoggerFactory
            .getLogger(ScapeNamespaceInitializer.class);

    @Autowired
    private NodeService nodeService;

    @Autowired
    private SessionFactory sessionFactory;

    /* (non-Javadoc)
     * @see com.sun.jersey.api.model.AbstractResourceModelListener#onLoaded(com.sun.jersey.api.model.AbstractResourceModelContext)
     */
    @Override
    public void onLoaded(AbstractResourceModelContext modelContext) {
        try {
            final Session session= this.sessionFactory.getInternalSession();
            /* make sure that the scape namespace is available in fcrepo */
            final Dataset namespace =
                    this.nodeService.getNamespaceRegistryDataset(session);
            UpdateAction.parseExecute(
                    "INSERT {<http://scapeproject.eu/model#> <" +
                            RdfLexicon.HAS_NAMESPACE_PREFIX + "> \"scape\"} WHERE {}",
                    namespace);
            session.save();
            // Get the node type manager ...
            final NodeTypeManager mgr =
                    session.getWorkspace().getNodeTypeManager();

            // Create templates for the node types ...
            final NodeTypeTemplate planType = mgr.createNodeTypeTemplate();
            planType.setName("scape:plan");
            planType.setDeclaredSuperTypeNames(new String[] {
                    "fedora:resource", "fedora:object"});
            planType.setMixin(true);
            planType.setQueryable(true);
            planType.setAbstract(false);
            planType.getPropertyDefinitionTemplates().add(createMultiPropertyDefTemplate(session, mgr, "scape:hasExecState"));


            // and register them
            mgr.registerNodeTypes(new NodeTypeDefinition[] {planType}, true);

        } catch (RepositoryException e) {
            LOG.error("Error while setting up scape namespace", e);
            throw new RuntimeException("Unable to setup scape on fedora");
        }
    }

    private PropertyDefinitionTemplate createMultiPropertyDefTemplate(final Session session, final NodeTypeManager mgr, final String name) throws UnsupportedRepositoryOperationException, RepositoryException {
        PropertyDefinitionTemplate propDefn = mgr.createPropertyDefinitionTemplate();
        propDefn.setName(name);
        propDefn.setRequiredType(PropertyType.STRING);
        ValueFactory valueFactory = session.getValueFactory();
        propDefn.setMultiple(true);
        propDefn.setFullTextSearchable(false);
        propDefn.setQueryOrderable(false);
        return propDefn;
    }
}

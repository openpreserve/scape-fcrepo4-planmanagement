/**
 *
 */

package eu.scape_project.web.listener;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.ext.Provider;

import org.fcrepo.RdfLexicon;
import org.fcrepo.services.NodeService;
import org.fcrepo.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.update.UpdateAction;
import com.sun.jersey.api.model.AbstractResourceModelContext;
import com.sun.jersey.api.model.AbstractResourceModelListener;

/**
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
            final Session session= this.sessionFactory.getSession();
            /* make sure that the scape namespace is available in fcrepo */
            final Dataset namespace =
                    this.nodeService.getNamespaceRegistryGraph(session);
            UpdateAction.parseExecute(
                    "INSERT {<http://scapeproject.eu/model#> <" +
                            RdfLexicon.HAS_NAMESPACE_PREFIX + "> \"scape\"} WHERE {}",
                    namespace);
            session.save();
        } catch (RepositoryException e) {
            LOG.error("Error while setting up scape namespace", e);
            throw new RuntimeException("Unable to setup scape on fedora");
        }



    }
}

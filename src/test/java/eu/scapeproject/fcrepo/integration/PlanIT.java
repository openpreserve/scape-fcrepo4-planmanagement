package eu.scapeproject.fcrepo.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jgroups.util.UUID;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.scapeproject.model.plan.PlanExecutionState.ExecutionState;
import eu.scapeproject.model.plan.PlanExecutionStateCollection;
import eu.scapeproject.util.ScapeMarshaller;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/integration-tests/test-container.xml" })
public class PlanIT {
    private static final String SCAPE_URL = "http://localhost:8080/rest/scape";
    private static final String FEDORA_URL = "http://localhost:8080/rest/";

    private final DefaultHttpClient client = new DefaultHttpClient();

    private ScapeMarshaller marshaller;

    private static final Logger LOG = LoggerFactory.getLogger(PlanIT.class);

    @Before
    public void setup() throws Exception {
        this.marshaller = ScapeMarshaller.newInstance();
    }

    @Test
    public void testDeployPlan() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f = new File(this.getClass().getClassLoader().getResource("test-plan.xml").getFile());

        String uri = putPlan(planId, new FileInputStream(f), f.length());
        assertNotNull(uri);
        assertEquals(planUri, uri);
        
        LOG.info("fetching plan from URI " + FEDORA_URL + "/objects/scape/plans/" + planId);
        HttpGet get = new HttpGet(FEDORA_URL + "/objects/scape/plans/" + planId);
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        System.out.println(EntityUtils.toString(resp.getEntity()));
    }

    @Test
    @Ignore
    public void testDeployAndRetrievePlan() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f = new File(this.getClass().getClassLoader().getResource("test-plan.xml").getFile());

        putPlan(planId, new FileInputStream(f), f.length());

        /* check that the plan can be retrieved */
        LOG.debug("retrieving plan from " + planUri);
        HttpGet get = new HttpGet(planUri);
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());

        /* check that the xml is the same as deployed */
        final String planXml = EntityUtils.toString(resp.getEntity());
        assertEquals(IOUtils.toString(new FileInputStream(f)), planXml);
        get.releaseConnection();
    }

    @Test
    @Ignore
    public void testDeployAndRetreiveExecState() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f = new File(this.getClass().getClassLoader().getResource("test-plan.xml").getFile());

        putPlan(planId, new FileInputStream(f), f.length());

        final HttpGet get = new HttpGet(SCAPE_URL + "/plan-execution-state/" + planId);
        HttpResponse resp = this.client.execute(get);
        PlanExecutionStateCollection coll = ScapeMarshaller.newInstance().deserialize(PlanExecutionStateCollection.class,
                resp.getEntity().getContent());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        assertEquals(1, coll.executionStates.size());
        assertEquals(ExecutionState.ENABLED, coll.getExecutionStates().get(0).getState());
        get.releaseConnection();
    }

    private String putPlan(String planId, InputStream src, long length) throws IOException {
        /* create and ingest a test plan */
        HttpPut put = new HttpPut(SCAPE_URL + "/plan/" + planId);
        put.setEntity(new InputStreamEntity(src, length));
        HttpResponse resp = this.client.execute(put);
        assertEquals(201, resp.getStatusLine().getStatusCode());
        String uri = EntityUtils.toString(resp.getEntity());
        put.releaseConnection();
        return uri;
    }
}

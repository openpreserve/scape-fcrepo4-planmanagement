
package eu.scapeproject.fcrepo.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jgroups.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.scapeproject.model.plan.PlanExecutionState;
import eu.scapeproject.model.plan.PlanExecutionState.ExecutionState;
import eu.scapeproject.model.plan.PlanExecutionStateCollection;
import eu.scapeproject.util.ScapeMarshaller;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/integration-tests/test-container.xml"})
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
        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(planId, new FileInputStream(f), f.length());

        /* check that the plan exists in fedora */
        HttpGet get =
                new HttpGet(FEDORA_URL + "/objects/scape/plans/" + planId);
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        get.releaseConnection();

    }

    @Test
    public void testDeployAndRetrievePlan() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(planId, new FileInputStream(f), f.length());

        /* check that the plan can be retrieved */
        HttpGet get = new HttpGet(planUri);
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());

        /* check that the xml is the same as deployed */
        final String planXml = EntityUtils.toString(resp.getEntity());
        assertEquals(IOUtils.toString(new FileInputStream(f)), planXml);
        get.releaseConnection();
    }

    @Test
    public void testDeployAndRetrieveLifecycleState() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(planId, new FileInputStream(f), f.length());

        final HttpGet get = new HttpGet(SCAPE_URL + "/plan-state/" + planId);
        HttpResponse resp = this.client.execute(get);
        String state = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        assertEquals("ENABLED", state);
        get.releaseConnection();
    }

    @Test
    public void testDeployAndUpdateLifecycleState() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(planId, new FileInputStream(f), f.length());
        putPlanLifecycleState(planId, "DISABLED");

        final HttpGet get = new HttpGet(SCAPE_URL + "/plan-state/" + planId);
        HttpResponse resp = this.client.execute(get);
        String state = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        assertEquals("DISABLED", state);
        get.releaseConnection();
    }

    @Test
    public void testDeployAndRetrieveExecState() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(planId, new FileInputStream(f), f.length());

        final HttpGet get =
                new HttpGet(SCAPE_URL + "/plan-execution-state/" + planId);
        HttpResponse resp = this.client.execute(get);
        PlanExecutionStateCollection coll =
                ScapeMarshaller.newInstance().deserialize(
                        PlanExecutionStateCollection.class,
                        resp.getEntity().getContent());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        assertEquals(0, coll.executionStates.size());
        get.releaseConnection();
    }

    @Test
    public void testDeployAndAddExecState() throws Exception {
        final String planId = UUID.randomUUID().toString();
        final String planUri = SCAPE_URL + "/plan/" + planId;
        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(planId, new FileInputStream(f), f.length());

        putPlanExecutionState(planId, ExecutionState.EXECUTION_SUCCESS);
        putPlanExecutionState(planId, ExecutionState.EXECUTION_SUCCESS);
        putPlanExecutionState(planId, ExecutionState.EXECUTION_FAIL);
        putPlanExecutionState(planId, ExecutionState.EXECUTION_SUCCESS);

        HttpGet get =
                new HttpGet(SCAPE_URL + "/plan-execution-state/" + planId);
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        PlanExecutionStateCollection states =
                (PlanExecutionStateCollection) marshaller.deserialize(resp
                        .getEntity().getContent());
        get.releaseConnection();
        assertEquals(4, states.executionStates.size());
        assertEquals(ExecutionState.EXECUTION_SUCCESS, states.executionStates
                .get(0).getState());
        assertEquals(ExecutionState.EXECUTION_SUCCESS, states.executionStates
                .get(1).getState());
        assertEquals(ExecutionState.EXECUTION_FAIL, states.executionStates.get(
                2).getState());
        assertEquals(ExecutionState.EXECUTION_SUCCESS, states.executionStates
                .get(3).getState());
    }

    @Test
    public void testReserveIdentifier() throws Exception {
        HttpGet get = new HttpGet(SCAPE_URL + "/plan-id/reserve");
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        String id = EntityUtils.toString(resp.getEntity());
        assertTrue(0 < id.length());
        get.releaseConnection();
    }

    @Test
    public void testSearchPlans() throws Exception {
        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(UUID.randomUUID().toString(), new FileInputStream(f), f.length());
        putPlanAndAssertCreated(UUID.randomUUID().toString(), new FileInputStream(f), f.length());
        putPlanAndAssertCreated(UUID.randomUUID().toString(), new FileInputStream(f), f.length());


        HttpGet get = new HttpGet(SCAPE_URL + "/plan/sru?version=1&operation=searchRetrieve&query=*");
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        String xml = EntityUtils.toString(resp.getEntity(),"UTF-8");
        System.out.println(xml);
        assertTrue(0 < xml.length());
        get.releaseConnection();
    }

    private void putPlanLifecycleState(String planId, String state)
            throws IOException {
        HttpPut put =
                new HttpPut(SCAPE_URL + "/plan-state/" + planId + "/" + state);
        HttpResponse resp = this.client.execute(put);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        put.releaseConnection();
    }

    private void putPlanExecutionState(String planId,
            ExecutionState executionState) throws JAXBException, IOException {

        PlanExecutionState state =
                new PlanExecutionState(new Date(), executionState);
        HttpPost post =
                new HttpPost(SCAPE_URL + "/plan-execution-state/" + planId);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        ScapeMarshaller.newInstance().serialize(state, sink);
        post.setEntity(new StringEntity(new String(sink.toByteArray()),
                ContentType.TEXT_XML));
        HttpResponse resp = this.client.execute(post);
        assertEquals(201, resp.getStatusLine().getStatusCode());
        post.releaseConnection();
    }

    private void putPlanAndAssertCreated(String planId, InputStream src,
            long length) throws IOException {
        /* create and ingest a test plan */
        HttpPut put = new HttpPut(SCAPE_URL + "/plan/" + planId);
        put.setEntity(new InputStreamEntity(src, length));
        HttpResponse resp = this.client.execute(put);
        assertEquals(201, resp.getStatusLine().getStatusCode());
        put.releaseConnection();
    }
}

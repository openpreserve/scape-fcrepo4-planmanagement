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

import eu.scape_project.model.plan.PlanDataCollection;
import eu.scape_project.model.plan.PlanExecutionState;
import eu.scape_project.model.plan.PlanExecutionState.ExecutionState;
import eu.scape_project.model.plan.PlanExecutionStateCollection;
import eu.scape_project.util.ScapeMarshaller;

/**
 *
 * @author frank asseg
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/integration-tests/test-container.xml"})
public class PlanIT {

    private static final String PORT = (System.getProperty("test.port") != null) ? System.getProperty("test.port") : "8092";

    private static final String SCAPE_URL = "http://localhost:" + PORT +
            "/scape";

    private static final String FEDORA_URL = "http://localhost:" + PORT +
            "/";

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
        assertEquals("ENABLED:Initial deployment", state);
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

        HttpGet get = new HttpGet(SCAPE_URL + "/plan-state/" + planId);
        HttpResponse resp = this.client.execute(get);
        String state = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        assertEquals("DISABLED", state);
        get.releaseConnection();

        putPlanLifecycleState(planId, "ENABLED:foo");

        get = new HttpGet(SCAPE_URL + "/plan-state/" + planId);
        resp = this.client.execute(get);
        state = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        assertEquals("ENABLED:foo", state);
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
    public void testDeployAndListPlans() throws Exception {
        final String[] ids = new String[3];
        final String[] uris = new String[3];

        ids[0] = UUID.randomUUID().toString();
        ids[1] = UUID.randomUUID().toString();
        ids[2] = UUID.randomUUID().toString();

        uris[0]= SCAPE_URL + "/plan/" + ids[0];
        uris[1]= SCAPE_URL + "/plan/" + ids[1];
        uris[2] = SCAPE_URL + "/plan/" + ids[2];

        final File f =
                new File(this.getClass().getClassLoader().getResource(
                        "plato-plan.xml").getFile());

        putPlanAndAssertCreated(ids[0], new FileInputStream(f), f.length());
        putPlanAndAssertCreated(ids[1], new FileInputStream(f), f.length());
        putPlanAndAssertCreated(ids[2], new FileInputStream(f), f.length());

        HttpGet get = new HttpGet(SCAPE_URL + "/plan-list");
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        PlanDataCollection plans =
                (PlanDataCollection) marshaller.deserialize(resp.getEntity()
                        .getContent());
        get.releaseConnection();
        int numPlans = plans.getPlanData().size();
        assertTrue(numPlans >= 3);

        /* check the limit and offset feature */
        get = new HttpGet(SCAPE_URL + "/plan-list/1/0");
        resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        plans =
                (PlanDataCollection) marshaller.deserialize(resp.getEntity()
                        .getContent());
        get.releaseConnection();
        assertEquals(1,plans.getPlanData().size());

        /* check the limit and offset feature */
        get = new HttpGet(SCAPE_URL + "/plan-list/0/" + (numPlans - 1));
        resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        plans =
                (PlanDataCollection) marshaller.deserialize(resp.getEntity()
                        .getContent());
        get.releaseConnection();
        assertEquals(1,plans.getPlanData().size());
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

        putPlanAndAssertCreated(UUID.randomUUID().toString(),
                new FileInputStream(f), f.length());
        putPlanAndAssertCreated(UUID.randomUUID().toString(),
                new FileInputStream(f), f.length());
        putPlanAndAssertCreated(UUID.randomUUID().toString(),
                new FileInputStream(f), f.length());

        HttpGet get =
                new HttpGet(SCAPE_URL +
                        "/plan/sru?version=1&operation=searchRetrieve&query=*");
        HttpResponse resp = this.client.execute(get);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        String xml = EntityUtils.toString(resp.getEntity(), "UTF-8");
        get.releaseConnection();
        assertTrue(0 < xml.length());
        assertEquals(0, xml
                .indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
        assertEquals(-1, xml.indexOf(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", 10));
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

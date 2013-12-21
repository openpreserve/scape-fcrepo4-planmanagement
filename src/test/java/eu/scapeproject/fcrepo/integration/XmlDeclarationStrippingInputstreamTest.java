/**
 *
 */
package eu.scapeproject.fcrepo.integration;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.scape_project.resource.XmlDeclarationStrippingInputstream;


/**
 * @author frank asseg
 *
 */
public class XmlDeclarationStrippingInputstreamTest {

    @Test
    public void testStripXmlDeclaration1() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><srw:searchRetrieveResponse xmlns:srw=\"http://scapeproject.eu/srw/\"><srw:numberOfRecords>0</srw:numberOfRecords><srw:records></srw:records></srw:searchRetrieveResponse>";
        XmlDeclarationStrippingInputstream src = new XmlDeclarationStrippingInputstream(new ByteArrayInputStream(xml.getBytes()));
        String stripped = IOUtils.toString(src);
        assertEquals(-1,stripped.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
        assertEquals("<srw:searchRetrieveResponse xmlns:srw=\"http://scapeproject.eu/srw/\"><srw:numberOfRecords>0</srw:numberOfRecords><srw:records></srw:records></srw:searchRetrieveResponse>",stripped);
    }

    @Test
    public void testStripXmlDeclaration2() throws Exception {
        String xml = "<srw:searchRetrieveResponse xmlns:srw=\"http://scapeproject.eu/srw/\"><srw:numberOfRecords>0</srw:numberOfRecords><srw:records></srw:records></srw:searchRetrieveResponse>";
        XmlDeclarationStrippingInputstream src = new XmlDeclarationStrippingInputstream(new ByteArrayInputStream(xml.getBytes()));
        String stripped = IOUtils.toString(src);
        assertEquals("<srw:searchRetrieveResponse xmlns:srw=\"http://scapeproject.eu/srw/\"><srw:numberOfRecords>0</srw:numberOfRecords><srw:records></srw:records></srw:searchRetrieveResponse>",stripped);
    }

    @Test
    public void testStripXmlDeclaration3() throws Exception {
        String xml = "  \n <?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<srw:searchRetrieveResponse xmlns:srw=\"http://scapeproject.eu/srw/\"><srw:numberOfRecords>0</srw:numberOfRecords><srw:records></srw:records></srw:searchRetrieveResponse>";
        XmlDeclarationStrippingInputstream src = new XmlDeclarationStrippingInputstream(new ByteArrayInputStream(xml.getBytes()));
        String stripped = IOUtils.toString(src);
        assertEquals(-1,stripped.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
        assertEquals("<srw:searchRetrieveResponse xmlns:srw=\"http://scapeproject.eu/srw/\"><srw:numberOfRecords>0</srw:numberOfRecords><srw:records></srw:records></srw:searchRetrieveResponse>",stripped);
    }

    @Test
    public void testStripXmlDeclaration4() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><plans xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://ifs.tuwien.ac.at/dp/plato\" xmlns:fits=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\" xsi:schemaLocation=\"http://ifs.tuwien.ac.at/dp/plato plato-V4.xsd\" version=\"4.0.1\"><plan><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:04:18\"/><properties author=\"frank asseg\" organization=\"fasseg\" name=\"plato-test-plan-scape-1\" privateProject=\"true\" reportPublic=\"false\" planType=\"FULL\"><state value=\"11\"/><description>n/a</description><owner>fasseg</owner><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:12:03\" changedBy=\"fasseg\"/></properties><basis identificationCode=\"\"><documentTypes>pdf</documentTypes><applyingPolicies/><designatedCommunity/><mandate/><organisationalProcedures/><planningPurpose/><planRelations/><preservationRights/><referenceToAgreements/><triggers><trigger type=\"NEW_COLLECTION\" active=\"false\" description=\"\"/><trigger type=\"PERIODIC_REVIEW\" active=\"false\" description=\"\"/><trigger type=\"CHANGED_ENVIRONMENT\" active=\"false\" description=\"\"/><trigger type=\"CHANGED_OBJECTIVE\" active=\"false\" description=\"\"/><trigger type=\"CHANGED_COLLECTION_PROFILE\" active=\"false\" description=\"\"/></triggers><policyTree/><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:05:04\" changedBy=\"fasseg\"/></basis><sampleRecords><samplesDescription>n/a</samplesDescription><collectionProfile><collectionID/><description/><expectedGrowthRate/><numberOfObjects/><typeOfObjects/><retentionPeriod/></collectionProfile><record fullname=\"alterrrrr\" contentType=\"\" shortName=\"stereess\"><data hasData=\"false\"/><formatInfo puid=\"\" name=\"\" version=\"\" mimeType=\"\" defaultExtension=\"\"><changelog created=\"2013-07-18T13:05:29\" createdBy=\"fasseg\" changed=\"2013-07-18T13:05:29\"/></formatInfo><changelog created=\"2013-07-18T13:05:29\" createdBy=\"fasseg\" changed=\"2013-07-18T13:05:39\" changedBy=\"fasseg\"/><description/><originalTechnicalEnvironment/></record><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:05:29\" changedBy=\"fasseg\"/></sampleRecords><requirementsDefinition><description/><uploads/><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:04:18\"/></requirementsDefinition><alternatives><description>adsadsada</description><alternative discarded=\"false\" name=\"nomanananana\"><description>adsadasd</description><resourceDescription><necessaryResources>adsdadasd</necessaryResources><configSettings>asdsadad</configSettings><reasonForConsidering>sadsad</reasonForConsidering><changelog created=\"2013-07-18T13:06:15\" createdBy=\"fasseg\" changed=\"2013-07-18T13:06:15\"/></resourceDescription><experiment><description/><settings/><results/><detailedInfos><detailedInfo key=\"stereess\" successful=\"false\"><programOutput/><measurements/></detailedInfo></detailedInfos><changelog created=\"2013-07-18T13:06:15\" createdBy=\"fasseg\" changed=\"2013-07-18T13:06:15\"/></experiment><changelog created=\"2013-07-18T13:06:15\" createdBy=\"fasseg\" changed=\"2013-07-18T13:06:37\" changedBy=\"fasseg\"/></alternative><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:06:37\" changedBy=\"fasseg\"/></alternatives><decision><reason>saddsad</reason><actionNeeded>sadsadsad</actionNeeded><goDecision value=\"GO\"/><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:07:35\" changedBy=\"fasseg\"/></decision><evaluation><comment/><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:04:18\"/></evaluation><importanceWeighting><comment/><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:04:18\"/></importanceWeighting><recommendation><reasoning>sadsada</reasoning><effects>ddadasd</effects><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:12:03\" changedBy=\"fasseg\"/></recommendation><transformation><comment/><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:04:18\"/></transformation><tree weightsInitialized=\"true\"><node name=\"Root\" weight=\"1.0\" lock=\"false\"><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:06:01\" changedBy=\"fasseg\"/><leaf name=\"blah\" weight=\"1.0\" single=\"false\" lock=\"false\"><changelog created=\"2013-07-18T13:06:01\" createdBy=\"fasseg\" changed=\"2013-07-18T13:08:22\" changedBy=\"fasseg\"/><aggregationMode value=\"AVERAGE\"/><booleanScale unit=\"\" restriction=\"Yes/No\"><changelog created=\"2013-07-18T13:06:09\" createdBy=\"fasseg\" changed=\"2013-07-18T13:06:09\" changedBy=\"fasseg\"/></booleanScale><ordinalTransformer><mappings><mapping ordinal=\"Yes\" target=\"0.0\"/><mapping ordinal=\"No\" target=\"1.0\"/></mappings><changelog created=\"2013-07-18T13:06:15\" createdBy=\"fasseg\" changed=\"2013-07-18T13:08:22\" changedBy=\"fasseg\"/></ordinalTransformer><evaluation><alternative key=\"nomanananana\"><booleanResult><value>Yes</value><comment/><changelog created=\"2013-07-18T13:07:41\" changed=\"2013-07-18T13:07:54\"/></booleanResult></alternative></evaluation></leaf></node></tree><executablePlan><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:04:18\"/></executablePlan><planDefinition currency=\"EUR\"><triggers><trigger type=\"NEW_COLLECTION\" active=\"false\"/><trigger type=\"PERIODIC_REVIEW\" active=\"false\"/><trigger type=\"CHANGED_ENVIRONMENT\" active=\"false\"/><trigger type=\"CHANGED_OBJECTIVE\" active=\"false\"/><trigger type=\"CHANGED_COLLECTION_PROFILE\" active=\"false\"/></triggers><changelog created=\"2013-07-18T13:04:18\" createdBy=\"fasseg\" changed=\"2013-07-18T13:04:18\"/></planDefinition></plan></plans>";
        XmlDeclarationStrippingInputstream src = new XmlDeclarationStrippingInputstream(new ByteArrayInputStream(xml.getBytes()));
        String stripped = IOUtils.toString(src);
        assertEquals(-1,stripped.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertEquals(0,stripped.indexOf("<plans xmlns:xsi"));
    }
}

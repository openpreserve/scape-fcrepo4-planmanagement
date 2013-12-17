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
}

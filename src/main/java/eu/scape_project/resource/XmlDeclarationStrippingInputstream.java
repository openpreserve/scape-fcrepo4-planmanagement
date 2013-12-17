/**
 *
 */
package eu.scape_project.resource;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author frank asseg
 *
 */
public class XmlDeclarationStrippingInputstream extends InputStream {

    final InputStream src;

    String firstElement;
    boolean checked = false;
    boolean hasDeclaration = false;
    int elementIndex;

    public XmlDeclarationStrippingInputstream(InputStream src) {
        super();
        this.src = src;
    }

    @Override
    public int read() throws IOException {
        if (!checked) {
            checked = true;
            StringBuffer name = new StringBuffer();
            int b = src.read();
            if (b == -1) {
                return -1;
            }
            while (Character.isWhitespace(b) || Character.isISOControl(b)) {
                b = src.read();
            }
            if ((char) b == '<') {
                name.append((char) b);
                while ((b=src.read()) != -1 && (char) b != '>') {
                    name.append((char) b);
                }
                name.append((char) b);
                firstElement = name.toString();
                if (firstElement.toLowerCase().startsWith("<?xml ")) {
                    hasDeclaration = true;
                    b = src.read();
                    while (Character.isWhitespace(b) || Character.isISOControl(b)) {
                        b = src.read();
                    }
                    return b;
                }
            }
        }
        if (!hasDeclaration && elementIndex < firstElement.length()) {
            return firstElement.charAt(elementIndex++);
        }
        return src.read();
    }

}

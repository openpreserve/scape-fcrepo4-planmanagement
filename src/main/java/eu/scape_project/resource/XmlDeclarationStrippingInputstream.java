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

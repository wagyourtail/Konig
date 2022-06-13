package xyz.wagyourtail.konig.structure.code;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.Konig;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class KonigProgramTest {

    @Test
    void toXML() throws ParserConfigurationException, IOException, SAXException {
        String programText = """
            <code name="hello world example" version="1.0">
                <headers></headers>
                <main>
                    <wires>
                        <wire wireid="0">
                            <end x="1.0" y="0.5" block="0" port="out"/>
                            <end x="2.0" y="0.5" block="1" port="in"/>
                        </wire>
                    </wires>
                    <blocks>
                        <const blockid="0" x="0.0" y="0.0" scaleX="1.0" scaleY="1.0" rotate="0" flipH="false" flipV="false">
                            <io>
                                <output name="out" wireid="0"/>
                            </io>
                            <value>Hello World!</value>
                        </const>
                        <print blockid="1" x="2.0" y="0.0" scaleX="1.0" scaleY="1.0" rotate="0" flipH="false" flipV="false">
                            <io>
                                <input name="in" wireid="0"/>
                            </io>
                        </print>
                    </blocks>
                </main>
            </code>""";
        KonigProgram program = (KonigProgram) Konig.deserializeString(programText, null);
        assertEquals(programText, program.toXML().toString());
    }

}
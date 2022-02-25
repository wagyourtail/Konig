package xyz.wagyourtail.konig;

import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.structure.KonigFile;
import xyz.wagyourtail.konig.structure.code.KonigProgram;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        Path hello_world = Path.of("software-enginnering-bs/language-spec-revisions/1.0/example/helloworld.konig");
        KonigFile f = Konig.deserialize(hello_world);
        ((KonigProgram) f).compile().apply(Collections.emptyMap()).join();

    }

}

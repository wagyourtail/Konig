package xyz.wagyourtail.konig;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        Path hello_world = Path.of("software-enginnering-bs/language-spec-revisions/1.0/example/helloworld.konig");
        Konig.deserialize(hello_world);
    }

}
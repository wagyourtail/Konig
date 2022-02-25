package xyz.wagyourtail.konig;

import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.structure.KonigFile;
import xyz.wagyourtail.konig.structure.code.KonigProgram;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        Path hello_world = Path.of("software-enginnering-bs/language-spec-revisions/1.0/example/helloworld.konig");
        long start = System.nanoTime();
        KonigFile f = Konig.deserialize(hello_world);
        long end = System.nanoTime();
        System.out.println("Deserialized in " + (end - start) / 1000000 + "ms");
        start = System.nanoTime();
        Function<Map<String, Object>, CompletableFuture<Map<String, Object>>> compiled = ((KonigProgram) f).compile();
        end = System.nanoTime();
        System.out.println("Compiled in " + (end - start) / 1000000 + "ms");
        start = System.nanoTime();
        compiled.apply(Collections.emptyMap()).join();
        end = System.nanoTime();
        System.out.println("Executed in " + (end - start) / 1000000 + "ms");
        start = System.nanoTime();
        compiled.apply(Collections.emptyMap()).join();
        end = System.nanoTime();
        System.out.println("Executed in " + (end - start) / 1000000 + "ms");
    }

}

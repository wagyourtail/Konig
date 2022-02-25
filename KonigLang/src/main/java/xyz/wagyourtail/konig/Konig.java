package xyz.wagyourtail.konig;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import xyz.wagyourtail.konig.lib.InternHeaders;
import xyz.wagyourtail.konig.lib.stdlib.Generic;
import xyz.wagyourtail.konig.lib.stdlib.Math;
import xyz.wagyourtail.konig.structure.KonigFile;
import xyz.wagyourtail.konig.structure.code.KonigProgram;
import xyz.wagyourtail.konig.structure.headers.BlockIO;
import xyz.wagyourtail.konig.structure.headers.KonigHeaders;
import xyz.wagyourtail.konig.structure.headers.blocks.ConstBlock;
import xyz.wagyourtail.konig.structure.headers.blocks.GlobalInput;
import xyz.wagyourtail.konig.structure.headers.blocks.GlobalOutput;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Konig {
    public static final Map<String, KonigHeaders> intern = new HashMap<>();
    static {
        intern.put("stdlib", stdlib());
    }

    public static KonigFile deserialize(Path file) throws ParserConfigurationException, IOException, SAXException {
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("File is not a regular file");
        }
        if (!Files.isReadable(file)) {
            throw new IllegalArgumentException("File is not readable");
        }
        return deserialize(Files.newInputStream(file), file);
    }

    public static KonigFile deserialize(InputStream stream, Path path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(stream);
        doc.getDocumentElement().normalize();

        Node root = doc.getDocumentElement();
        if (root.getNodeName().equals("code")) {
            KonigProgram program = new KonigProgram(path);
            program.parseXML(root);
            return program;
        } else if (root.getNodeName().equals("headers")) {
            KonigHeaders header = new KonigHeaders(path);
            header.parseXML(root);
            return header;
        }
        throw new IllegalArgumentException("File is not a valid Konig file");
    }

    public static KonigFile deserializeString(String code, Path path) throws ParserConfigurationException, IOException, SAXException {
        // string to input stream
        InputStream stream = new ByteArrayInputStream(code.getBytes());
        return deserialize(stream, path);
    }

    public static KonigHeaders getInternHeaders(String name) {
        return intern.get(name);
    }

    protected static KonigHeaders stdlib() {
        InternHeaders headers = new InternHeaders();

        // define 3 "special" blocks
        GlobalInput gi = new GlobalInput();
        gi.generics.put("T", new BlockIO.Generic("T", null, null));
        BlockIO.Output out = new BlockIO.Output(BlockIO.Side.RIGHT, BlockIO.Justify.CENTER, "out", "T");
        gi.io.byName.put("out", out);
        gi.io.outputs.add(out);
        gi.io.elements.computeIfAbsent(out.side, (side) -> new HashMap<>()).computeIfAbsent(out.justify, (justify) -> new ArrayList<>()).add(out);
        headers.getBlocks().computeIfAbsent("generic", (s) -> new HashMap<>()).put("globalinput", gi);

        GlobalOutput go = new GlobalOutput();
        go.generics.put("T", new BlockIO.Generic("T", null, null));
        BlockIO.Input in = new BlockIO.Input(BlockIO.Side.LEFT, BlockIO.Justify.CENTER, "in", "T", false);
        go.io.byName.put("in", in);
        go.io.inputs.add(in);
        go.io.elements.computeIfAbsent(in.side, (side) -> new HashMap<>()).computeIfAbsent(in.justify, (justify) -> new ArrayList<>()).add(in);
        headers.getBlocks().computeIfAbsent("generic", (s) -> new HashMap<>()).put("globaloutput", go);

        ConstBlock cb = new ConstBlock();
        cb.generics.put("T", new BlockIO.Generic("T", null, null));
        BlockIO.Output out2 = new BlockIO.Output(BlockIO.Side.RIGHT, BlockIO.Justify.CENTER, "out", "T");
        cb.io.byName.put("out", out2);
        cb.io.outputs.add(out2);
        cb.io.elements.computeIfAbsent(out2.side, (side) -> new HashMap<>()).computeIfAbsent(out2.justify, (justify) -> new ArrayList<>()).add(out2);
        headers.getBlocks().computeIfAbsent("generic", (s) -> new HashMap<>()).put("const", cb);


        // parse classes
        headers.parseClass(Math.class, "math");
        headers.parseClass(Generic.class, "generic");
        //TODO: finish

        return headers;
    }

}

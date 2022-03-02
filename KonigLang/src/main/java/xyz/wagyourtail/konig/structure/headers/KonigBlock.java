package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class KonigBlock {
    public final BlockIO io = new BlockIO();
    public final Map<String, Map<String, Hollow>> hollowsByGroupName = new HashMap<>();
    public final Map<String, Hollow> hollowsByName = new HashMap<>();
    public final Map<String, BlockIO.Generic> generics = new HashMap<>();
    public String name;
    public String group;
    public Path image;

    public void parseXML(Node node) throws IOException {
        name = node.getAttributes().getNamedItem("name").getNodeValue();
        group = node.getAttributes().getNamedItem("group").getNodeValue();

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (!parseChild(child)) {
                    throw new IOException("Unknown child node: " + child.getNodeName());
                }
            }
        }
    }

    protected boolean parseChild(Node child) throws IOException {
        if (child.getNodeName().equals("io")) {
            io.parseXML(child);
            return true;
        }
        if (child.getNodeName().equals("image")) {
            image = Path.of(child.getAttributes().getNamedItem("src").getNodeValue());
            return true;
        }
        if (child.getNodeName().equals("hollow")) {
            Hollow io = new Hollow();
            io.parseXML(child);

            hollowsByGroupName.computeIfAbsent(io.group, k -> new HashMap<>()).put(io.name, io);
            hollowsByName.put(io.name, io);
            return true;
        }
        if (child.getNodeName().equals("generics")) {
            parseGenerics(child);
            return true;
        }
        return false;
    }

    protected void parseGenerics(Node node) throws IOException {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals("generic")) {
                    NamedNodeMap attrs = child.getAttributes();
                    String name = attrs.getNamedItem("name").getNodeValue();
                    String extend = Optional.ofNullable(attrs.getNamedItem("extend")).map(Node::getNodeValue).orElse(null);
                    String supers = Optional.ofNullable(attrs.getNamedItem("supers")).map(Node::getNodeValue).orElse(null);
                    BlockIO.Generic generic = new BlockIO.Generic(name, extend, supers);
                    generics.put(name, generic);
                } else {
                    throw new IOException("Unknown child node: " + child.getNodeName());
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, group, image, io, hollowsByGroupName, hollowsByName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KonigBlock)) {
            return false;
        }
        KonigBlock that = (KonigBlock) o;
        return Objects.equals(name, that.name) && Objects.equals(group, that.group) &&
            Objects.equals(image, that.image) && Objects.equals(io, that.io) &&
            Objects.equals(hollowsByGroupName, that.hollowsByGroupName) && Objects.equals(
            hollowsByName,
            that.hollowsByName
        );
    }

    public abstract BiFunction<ForkJoinPool, Map<String, CompletableFuture<Object>>, Map<String, CompletableFuture<Object>>> jitCompile(KonigBlockReference self);


}

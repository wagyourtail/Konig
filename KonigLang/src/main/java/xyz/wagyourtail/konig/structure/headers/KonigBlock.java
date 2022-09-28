package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.wagyourtail.XMLBuilder;
import xyz.wagyourtail.konig.structure.code.KonigBlockReference;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class KonigBlock {
    public final BlockIO io = new BlockIO();
    public final Map<String, Map<String, Hollow>> hollowsByGroupName = new LinkedHashMap<>();
    public final Map<String, Hollow> hollowsByName = new LinkedHashMap<>();
    public final Map<String, BlockIO.Generic> generics = new HashMap<>();
    public String name;
    public String group;
    public Path image;

    public boolean hollow() {
        return !hollowsByName.isEmpty();
    }

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


    public XMLBuilder toXML() {
        XMLBuilder builder = new XMLBuilder("block");
        builder.addStringOption("name", name);
        builder.addStringOption("group", group);
        builder.append(io.toXML());
        if (image != null) {
            builder.append(new XMLBuilder("image").addStringOption("src", image.toString()));
        }
        for (Hollow hollow : hollowsByName.values()) {
            builder.append(hollow.toXML());
        }
        for (Map<String, Hollow> value : hollowsByGroupName.values()) {
            for (Hollow hollow : value.values()) {
                builder.append(hollow.toXML());
            }
        }
        for (BlockIO.Generic generic : generics.values()) {
            builder.append(generic.toXML());
        }
        return builder;
    }

    public float getMinWidth() {
        float top = 0;
        float bottom = 0;
        for (BlockIO.Justify justify : BlockIO.Justify.values()) {
            int sT = io.elements.getOrDefault(BlockIO.Side.TOP, Map.of()).getOrDefault(justify, List.of()).size();
            int sB = io.elements.getOrDefault(BlockIO.Side.BOTTOM, Map.of()).getOrDefault(justify, List.of()).size();
            if (sT > 0) {
                top += .2 * sT;
            }
            if (sB > 0) {
                bottom += .2 * sB;
            }
        }
        return Math.max(1, Math.max(top, bottom));
    }

    public float getMinHeight() {
        float left = 0;
        float right = 0;
        for (BlockIO.Justify justify : BlockIO.Justify.values()) {
            int sL = io.elements.getOrDefault(BlockIO.Side.LEFT, Map.of()).getOrDefault(justify, List.of()).size();
            int sR = io.elements.getOrDefault(BlockIO.Side.RIGHT, Map.of()).getOrDefault(justify, List.of()).size();
            if (sL > 0) {
                left += .2 * sL;
            }
            if (sR > 0) {
                right += .2 * sR;
            }
        }
        return Math.max(1, Math.max(left, right));
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

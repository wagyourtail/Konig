package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class Hollow extends BlockIO {
    public double paddingTop, paddingLeft, paddingBottom, paddingRight;
    public String name;
    public String group;

    @Override
    public void parseXML(Node child) throws IOException {
        paddingBottom = Double.parseDouble(child.getAttributes().getNamedItem("paddingBottom").getNodeValue());
        paddingLeft = Double.parseDouble(child.getAttributes().getNamedItem("paddingLeft").getNodeValue());
        paddingRight = Double.parseDouble(child.getAttributes().getNamedItem("paddingRight").getNodeValue());
        paddingTop = Double.parseDouble(child.getAttributes().getNamedItem("paddingTop").getNodeValue());
        name = child.getAttributes().getNamedItem("name").getNodeValue();
        group = Optional.ofNullable(child.getAttributes().getNamedItem("group")).map(Node::getNodeValue).orElseGet(() ->
            "$ungrouped$" + name);
        super.parseXML(child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paddingTop, paddingLeft, paddingBottom, paddingRight, name, group);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof xyz.wagyourtail.konig.structure.headers.Hollow)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        xyz.wagyourtail.konig.structure.headers.Hollow hollow = (xyz.wagyourtail.konig.structure.headers.Hollow) o;
        return Double.compare(hollow.paddingTop, paddingTop) == 0 && Double.compare(
            hollow.paddingLeft,
            paddingLeft
        ) == 0 && Double.compare(hollow.paddingBottom, paddingBottom) == 0 &&
            Double.compare(hollow.paddingRight, paddingRight) == 0 && Objects.equals(
            name,
            hollow.name
        ) && Objects.equals(group, hollow.group);
    }

}

package xyz.wagyourtail.konig.structure.code;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Wire {
    private final List<WireEndpoint> endpoints = new ArrayList<>();
    private final List<WireSegment> segments = new ArrayList<>();
    public int id;

    public List<WireEndpoint> getEndpoints() {
        return endpoints;
    }

    public List<WireSegment> getSegments() {
        return segments;
    }

    public void parseXML(Node node) throws IOException {
        id = Integer.parseInt(node.getAttributes().getNamedItem("wireid").getNodeValue());

        NodeList children = node.getChildNodes();
        int endpointsCount = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (endpointsCount == 2) {
                    throw new IOException("Wire " + id + " has more than 2 endpoints!");
                }
                double x = Double.parseDouble(child.getAttributes().getNamedItem("x").getNodeValue());
                double y = Double.parseDouble(child.getAttributes().getNamedItem("y").getNodeValue());
                if (child.getNodeName().equals("end")) {
                    int block = Integer.parseInt(child.getAttributes().getNamedItem("block").getNodeValue());
                    String port = child.getAttributes().getNamedItem("port").getNodeValue();
                    WireEndpoint endpoint = new WireEndpoint(block, x, y, port);
                    endpoints.add(endpoint);
                    segments.add(endpoint);
                    ++endpointsCount;
                } else if (child.getNodeName().equals("segment")) {
                    WireSegment segment = new WireSegment(x, y);
                    segments.add(segment);
                } else if (child.getNodeName().equals("branch")) {
                    WireBranch branch = new WireBranch(x, y);
                    branch.parseXML(child);
                    segments.add(branch);
                    if (branch.endpoint != null) {
                        endpoints.add(branch.endpoint);
                    }
                } else {
                    throw new IOException("Unknown node: " + child.getNodeName());
                }
            }
        }
    }

    public boolean removeSegment(WireSegment segment) {
        if (endpoints.contains(segment)) {
            endpoints.remove(segment);
        }
        if (segments.remove(segment)) {
            return true;
        }
        for (WireSegment s : segments) {
            if (s instanceof WireBranch) {
                if (((WireBranch) s).removeSegment(segment)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void insertSegment(Wire.WireSegment after, Wire.WireSegment newSeg) {
        int index = segments.indexOf(after);
        if (index != -1) {
            segments.add(index + 1, newSeg);
        } else {
            for (WireSegment s : segments) {
                if (s instanceof WireBranch) {
                    if (((WireBranch) s).insertSegment(after, newSeg)) {
                        break;
                    }
                }
            }
        }
        if (newSeg instanceof WireEndpoint) {
            endpoints.add((WireEndpoint) newSeg);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Wire)) {
            return false;
        }
        Wire wire = (Wire) o;
        return id == wire.id && Objects.equals(endpoints, wire.endpoints) && Objects.equals(
            segments,
            wire.segments
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoints, segments, id);
    }

    public static class WireSegment {
        public double x, y;

        public WireSegment(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class WireEndpoint extends WireSegment {
        public final int blockid;
        public String port;

        public WireEndpoint(int blockid, double x, double y, String port) {
            super(x, y);
            this.blockid = blockid;
            this.port = port;
        }

        public WireEndpoint(int blockid, double x, double y) {
            super(x, y);
            this.blockid = blockid;
        }
    }

    public static class WireBranch extends WireSegment {
        public final List<WireSegment> subSegments = new ArrayList<>();
        public WireEndpoint endpoint;

        public WireBranch(double x, double y) {
            super(x, y);
        }

        void parseXML(Node node) throws IOException {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    if (endpoint != null) {
                        throw new RuntimeException("WireBranch already has an endpoint");
                    }
                    double x = Double.parseDouble(child.getAttributes().getNamedItem("x").getNodeValue());
                    double y = Double.parseDouble(child.getAttributes().getNamedItem("y").getNodeValue());
                    if (child.getNodeName().equals("end")) {
                        int block = Integer.parseInt(child.getAttributes().getNamedItem("block").getNodeValue());
                        String port = child.getAttributes().getNamedItem("port").getNodeValue();
                        endpoint = new WireEndpoint(block, x, y, port);
                        subSegments.add(endpoint);
                    } else if (child.getNodeName().equals("segment")) {
                        subSegments.add(new WireSegment(x, y));
                    } else {
                        throw new IOException("Unknown child node " + child.getNodeName());
                    }
                }
            }
        }

        public boolean removeSegment(WireSegment segment) {
            if (segment == endpoint) {
                endpoint = null;
            }
            for (WireSegment s : subSegments) {
                if (s instanceof WireBranch) {
                    ((WireBranch) s).removeSegment(segment);
                }
            }
            return subSegments.remove(segment);
        }

        public boolean insertSegment(WireSegment after, WireSegment newSeg) {
            if (after == null) {
                subSegments.add(newSeg);
                return true;
            }
            int index = subSegments.indexOf(after);
            if (index != -1) {
                subSegments.add(index + 1, newSeg);
                return true;
            } else {
                for (WireSegment s : subSegments) {
                    if (s instanceof WireBranch) {
                        return ((WireBranch) s).insertSegment(after, newSeg);
                    }
                }
            }
            return false;
        }
    }

}

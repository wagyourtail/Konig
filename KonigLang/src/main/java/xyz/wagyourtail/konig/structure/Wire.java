package xyz.wagyourtail.konig.structure;

import java.util.ArrayList;
import java.util.List;

public class Wire {
    private final List<WireEndpoint> endpoints = new ArrayList<>();

    public List<WireEndpoint> getEndpoints() {
        return endpoints;
    }

    private final List<WireSegment> segments = new ArrayList<>();

    public List<WireSegment> getSegments() {
        return segments;
    }

    public static class WireSegment {
        public final int x, y;

        public WireSegment(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class WireEndpoint extends WireSegment {
        public final int blockid;

        public WireEndpoint(int blockid, int x, int y) {
            super(x, y);
            this.blockid = blockid;
        }
    }

    public static class WireBranch extends WireSegment {
        public final List<WireSegment> subSegments = new ArrayList<>();

        public WireBranch(int x, int y) {
            super(x, y);
        }
    }
}

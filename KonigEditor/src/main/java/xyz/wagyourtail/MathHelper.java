package xyz.wagyourtail;

import java.util.concurrent.atomic.AtomicReference;

public class MathHelper {

    private static final int INSIDE = 0;
    private static final int LEFT   = 1;
    private static final int RIGHT  = 2;
    private static final int BOTTOM = 4;
    private static final int TOP    = 8;

    private final static float MINIMUM_DELTA = 0.01f;

    //clips the given segments against the rectangle, returns false if it is entirely outside
    public static boolean clipLine(AtomicReference<LineSegment> segment, Rectangle rect) {
        // Cohen-Sutherland algorithm
        // https://en.wikipedia.org/wiki/Cohen%E2%80%93Sutherland_algorithm
        Point point1 = segment.get().getPoint1();
        Point point2 = segment.get().getPoint2();
        Point outsidePoint = new Point(0, 0);

        int point1Region = rect.getRegion(point1);
        int point2Region = rect.getRegion(point2);
        int outsidepointRegion;

        boolean lineIsVertical = point1.x == point2.x;
        float lineSlope = lineIsVertical ? 0 : (point2.y - point1.y) / (point2.x - point1.x);

        while (point1Region != INSIDE || point2Region != INSIDE) {
            if ((point1Region & point2Region) != 0) return false;

            outsidepointRegion = point1Region != INSIDE ? point1Region : point2Region;

            if ((outsidepointRegion & LEFT) != 0) {
                outsidePoint = new Point(rect.x1, delta(outsidePoint.x, point1.x)*lineSlope + point1.y);
            } else if ((outsidepointRegion & RIGHT) != 0) {
                outsidePoint = new Point(rect.x2, delta(outsidePoint.x, point1.x)*lineSlope + point1.y);
            } else if ((outsidepointRegion & BOTTOM) != 0) {
                outsidePoint = new Point(lineIsVertical ? point1.x : delta(outsidePoint.y, point1.y)/lineSlope + point1.x, rect.y1);
            } else if ((outsidepointRegion & TOP) != 0) {
                outsidePoint = new Point(lineIsVertical ? point1.x : delta(outsidePoint.y, point1.y)/lineSlope + point1.x, rect.y2);
            }

            if (outsidepointRegion == point1Region) {
                point1 = outsidePoint;
                point1Region = rect.getRegion(point1);
            } else {
                point2 = outsidePoint;
                point2Region = rect.getRegion(point2);
            }
        }

        segment.set(new LineSegment(point1.x, point1.y, point2.x, point2.y));
        return true;
    }

    public static boolean clipRect(AtomicReference<Rectangle> rect, Rectangle clipRect) {
        Rectangle rect1 = rect.get();
        if (rect1.x2 < clipRect.x1 || rect1.x1 > clipRect.x2 || rect1.y2 < clipRect.y1 || rect1.y1 > clipRect.y2) {
            return false;
        }

        if (rect1.x1 < clipRect.x1) {
            rect1 = new Rectangle(clipRect.x1, rect1.y1, rect1.x2, rect1.y2);
        }

        if (rect1.x2 > clipRect.x2) {
            rect1 = new Rectangle(rect1.x1, rect1.y1, clipRect.x2, rect1.y2);
        }

        if (rect1.y1 < clipRect.y1) {
            rect1 = new Rectangle(rect1.x1, clipRect.y1, rect1.x2, rect1.y2);
        }

        if (rect1.y2 > clipRect.y2) {
            rect1 = new Rectangle(rect1.x1, rect1.y1, rect1.x2, clipRect.y2);
        }

        rect.set(rect1);
        return true;
    }

    private static float delta(float value1, float value2) {
        return (Math.abs(value1 - value2) < MINIMUM_DELTA) ? 0 : (value1 - value2);
    }

    public static record Rectangle(float x1, float y1, float x2, float y2) {
        public boolean isInside(Point p) {
            return p.x() >= x1 && p.x() <= x2 && p.y() >= y1 && p.y() <= y2;
        }

        public int getRegion(Point p) {
            int region = (p.x < x1) ? LEFT : (p.x > x2) ? RIGHT : INSIDE;
            if (p.y < y1)
                region |= BOTTOM;
            else if (p.y > y2)
                region |= TOP;
            return region;
        }
    }

    public static record LineSegment(float x1, float y1, float x2, float y2) {

        public float getLength() {
            return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        public Point getPoint1() {
            return new Point(x1, y1);
        }

        public Point getPoint2() {
            return new Point(x2, y2);
        }
    }

    public static record Point(float x, float y) {

    }
}

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
                outsidePoint = new Point(rect.topLeftX, delta(outsidePoint.x, point1.x)*lineSlope + point1.y);
            } else if ((outsidepointRegion & RIGHT) != 0) {
                outsidePoint = new Point(rect.bottomRightX, delta(outsidePoint.x, point1.x)*lineSlope + point1.y);
            } else if ((outsidepointRegion & BOTTOM) != 0) {
                outsidePoint = new Point(lineIsVertical ? point1.x : delta(outsidePoint.y, point1.y)/lineSlope + point1.x, rect.topLeftY);
            } else if ((outsidepointRegion & TOP) != 0) {
                outsidePoint = new Point(lineIsVertical ? point1.x : delta(outsidePoint.y, point1.y)/lineSlope + point1.x, rect.bottomRightY);
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
        if (rect1.bottomRightX < clipRect.topLeftX || rect1.topLeftX > clipRect.bottomRightX || rect1.bottomRightY < clipRect.topLeftY || rect1.topLeftY > clipRect.bottomRightY) {
            return false;
        }

        if (rect1.topLeftX < clipRect.topLeftX) {
            rect1 = new Rectangle(clipRect.topLeftX, rect1.topLeftY, rect1.bottomRightX, rect1.bottomRightY);
        }

        if (rect1.bottomRightX > clipRect.bottomRightX) {
            rect1 = new Rectangle(rect1.topLeftX, rect1.topLeftY, clipRect.bottomRightX, rect1.bottomRightY);
        }

        if (rect1.topLeftY < clipRect.topLeftY) {
            rect1 = new Rectangle(rect1.topLeftX, clipRect.topLeftY, rect1.bottomRightX, rect1.bottomRightY);
        }

        if (rect1.bottomRightY > clipRect.bottomRightY) {
            rect1 = new Rectangle(rect1.topLeftX, rect1.topLeftY, rect1.bottomRightX, clipRect.bottomRightY);
        }

        rect.set(rect1);
        return true;
    }

    private static float delta(float value1, float value2) {
        return (Math.abs(value1 - value2) < MINIMUM_DELTA) ? 0 : (value1 - value2);
    }

    public static record Rectangle(float topLeftX, float topLeftY, float bottomRightX, float bottomRightY) {
        public boolean isInside(Point p) {
            return p.x() >= topLeftX && p.x() <= bottomRightX && p.y() >= topLeftY && p.y() <= bottomRightY;
        }

        public int getRegion(Point p) {
            int region = (p.x < topLeftX) ? LEFT : (p.x > bottomRightX) ? RIGHT : INSIDE;
            if (p.y < topLeftY)
                region |= BOTTOM;
            else if (p.y > bottomRightY)
                region |= TOP;
            return region;
        }
    }

    public static record LineSegment(float startX, float startY, float endX, float endY) {

        public float getLength() {
            return (float) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        }

        public Point getPoint1() {
            return new Point(startX, startY);
        }

        public Point getPoint2() {
            return new Point(endX, endY);
        }
    }

    public static record Point(float x, float y) {

    }
}

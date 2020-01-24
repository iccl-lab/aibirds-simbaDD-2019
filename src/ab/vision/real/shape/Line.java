package ab.vision.real.shape;

import java.awt.Point;
import java.util.Locale;

import ab.simulation.utils.Constants;
import ab.vision.ABType;

public class Line extends Body {
    private Point beginPoint;
    private Point endPoint;

    public Line(Point globalBeginPoint, Point gloabalEndPoint, ABType type) {
        centerX = (double) (globalBeginPoint.x + gloabalEndPoint.x) / 2.0;
        centerY = (double) (globalBeginPoint.y + gloabalEndPoint.y) / 2.0;
        this.beginPoint = new Point(globalBeginPoint.x - (int) centerX, globalBeginPoint.y - (int) centerY);
        this.endPoint = new Point(gloabalEndPoint.x - (int) centerX, gloabalEndPoint.y - (int) centerY);
        this.type = type;
    }

    public Line(Point globalCenterPoint, Point localBeginPoint, Point localEndPoint, ABType type) {
        centerX = globalCenterPoint.getX();
        centerY = globalCenterPoint.getY();
        this.beginPoint = localBeginPoint;
        this.endPoint = localEndPoint;
        this.type = type;
    }

    public Line(int y, ABType type) {
        centerX = (double) Constants.WINDOW_WIDTH / 2.0;
        centerY = y;
        beginPoint = new Point(-Constants.WINDOW_WIDTH / 2, 0);
        endPoint = new Point(Constants.WINDOW_WIDTH / 2, 0);
        this.type = type;
    }

    public Point getBeginPoint() {
        return beginPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public String toString() {
        return String.format(Locale.US, "Line(%f, ABType.%s) with id:%d", centerY, type, id);
    }

}

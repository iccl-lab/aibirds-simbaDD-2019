package ab.simulation.factory.shape;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

import ab.simulation.factory.ShapeFactory;
import ab.simulation.utils.SimUtil;
import ab.vision.ABObject;
import ab.vision.real.shape.Line;

public class EdgeShapeFactory extends ShapeFactory {

    public static List<Shape> createShapes(ABObject object, float PPM) {
        List<Shape> shapes = new LinkedList<Shape>();
        Line line = (Line) object;
        EdgeShape shape = new EdgeShape();
        Point centerPoint = line.getCenter();
        Point beginPoint = new Point(line.getBeginPoint().x + centerPoint.x, line.getBeginPoint().y + centerPoint.y);
        Point endPoint = new Point(line.getEndPoint().x + centerPoint.x, line.getEndPoint().y + centerPoint.y);
        Vec2 centerVec2 = SimUtil.convertViewToModel(centerPoint, PPM);
        Vec2 beginVec2 = SimUtil.convertViewToModel(beginPoint, PPM);
        Vec2 endVec2 = SimUtil.convertViewToModel(endPoint, PPM);
        shape.set(beginVec2.sub(centerVec2), endVec2.sub(centerVec2));
        shapes.add(shape);
        return shapes;
    }

}

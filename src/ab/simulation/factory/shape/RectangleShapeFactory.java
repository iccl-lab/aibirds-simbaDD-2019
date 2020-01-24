package ab.simulation.factory.shape;

import java.awt.Polygon;
import java.util.LinkedList;
import java.util.List;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

import ab.simulation.factory.ShapeFactory;
import ab.simulation.utils.SimUtil;
import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.real.shape.Rect;

public class RectangleShapeFactory extends ShapeFactory {

    public static List<Shape> createShapes(ABObject object, float PPM) {
        List<Shape> shapes = new LinkedList<Shape>();
        Rect obj = (Rect) object;
        if (object.shape != ABShape.Rect) {
            System.out.println("but this is a Rectangle!");
        }
        Vec2 center = SimUtil.convertViewToModel(new Vec2(object.getCenter().x, object.getCenter().y), PPM);
        Polygon p = obj.p;
        Vec2[] vertices = new Vec2[p.npoints];
        for (int i = 0; i < p.npoints; i++) {
            Vec2 point = SimUtil.convertViewToModel(new Vec2(p.xpoints[i], p.ypoints[i]), PPM);
            vertices[i] = point.subLocal(center);
        }
        PolygonShape shape = new PolygonShape();
        shape.set(vertices, p.npoints);
        shapes.add(shape);
        return shapes;
    }
}

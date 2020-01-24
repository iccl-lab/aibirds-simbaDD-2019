package ab.simulation.factory;

import java.util.LinkedList;
import java.util.List;

import org.jbox2d.collision.shapes.Shape;

import ab.simulation.factory.shape.CircleShapeFactory;
import ab.simulation.factory.shape.EdgeShapeFactory;
import ab.simulation.factory.shape.PolygonShapeFactory;
import ab.simulation.factory.shape.RectangleShapeFactory;
import ab.vision.ABObject;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Line;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;

public class ShapeFactory {

    /**
     * creates the shape of the given object
     *
     * @param object either Circle, Rect or Poly
     * @return CircleShape or PolygonShape
     */
    public static List<Shape> createShapes(ABObject object, float PPM) {
        List<Shape> shapes = new LinkedList<Shape>();
        if (object instanceof Circle) {
            return CircleShapeFactory.createShapes(object, PPM);
        } else if (object instanceof Rect) {
            return RectangleShapeFactory.createShapes(object, PPM);
        } else if (object instanceof Poly) {
            return PolygonShapeFactory.createShapes(object, PPM);
        } else if (object instanceof Line) {
            return EdgeShapeFactory.createShapes(object, PPM);
        }
        return shapes;
    }

}

package ab.simulation.factory.shape;

import java.util.LinkedList;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;

import ab.simulation.factory.ShapeFactory;
import ab.vision.ABObject;
import ab.vision.real.shape.Circle;

public class CircleShapeFactory extends ShapeFactory {

    public static List<Shape> createShapes(ABObject object, float PPM) {
        List<Shape> shapes = new LinkedList<Shape>();
        Circle circle = (Circle) object;
        CircleShape shape = new CircleShape();
        float radius;
        switch (object.getType()) {
        case RED_BIRD:
            radius = 0.5f;
            break;
        case BLACK_BIRD:
            radius = 0.75f;
            break;
        case BLUE_BIRD:
            radius = 0.33f;
            break;
        case WHITE_BIRD:
            radius = 0.825f;
            break;
        case YELLOW_BIRD:
            radius = 0.4f;
            break;
        default:
            radius = (float) (circle.r / PPM);
            break;
        }
        shape.setRadius(radius);
        shapes.add(shape);
        return shapes;
    }
}

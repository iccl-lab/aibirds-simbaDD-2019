package ab.simulation.factory;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ab.simulation.model.SimulationUserData;
import ab.simulation.utils.SimUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.Circle;

public class BodyFactory {

    public static Body createBody(ABObject object, World world, float PPM) {
        Vec2 centerVec2 = SimUtil.convertViewToModel(object.getCenter(), PPM);
        BodyDef bodyDef = BodyDefFactory.createBodyDef(object.type, centerVec2, -object.angle);
        List<Shape> shapes = ShapeFactory.createShapes(object, PPM);
        Body body = world.createBody(bodyDef);

        if (object instanceof Circle) {
            for (Shape shape : shapes) {
                FixtureDef fixtureDef = CircleFixtureDefFactory.createFixtureDef(object.type, shape);
                body.createFixture(fixtureDef);
            }
        } else {
            for (Shape shape : shapes) {
                FixtureDef fixtureDef = FixtureDefFactory.createFixtureDef(object.type, shape);
                body.createFixture(fixtureDef);
            }
        }
        // create references between ABObject and simulated body
        body.setUserData(new SimulationUserData(object, PPM));
        body.setSleepingAllowed(true);
        // body.setAwake(false);
        return body;
    }

    /**
     *
     * @param objects
     * @param bodies  Collection which will store the created Body objects
     * @return
     */
    public static List<Body> createBodies(List<ABObject> objects, World world, float PPM) {
        List<Body> bodies = new ArrayList<Body>();
        for (ABObject object : objects) {
            Body body = createBody(object, world, PPM);
            bodies.add(body);
        }
        return bodies;
    }

    private static ABObject createCurrentBird(Rectangle sling) {
        ABObject abObject = new Circle(sling.getCenterX(), sling.getY() + sling.getWidth() / 2.0,
                sling.getWidth() / 2.0, ABType.RED_BIRD);
        return abObject;
    }
}

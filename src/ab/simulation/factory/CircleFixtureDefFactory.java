package ab.simulation.factory;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.FixtureDef;

import ab.simulation.utils.Constants;
import ab.vision.ABType;

public class CircleFixtureDefFactory extends FixtureDefFactory {

    /**
     *
     * @param type
     * @return
     */
    public static FixtureDef createFixtureDef(ABType type, Shape shape) {
        FixtureDef fixtureDef = FixtureDefFactory.createFixtureDef(type, shape);
        /**
         * densities of the birds are calculated by mass/area where area is calculated
         * from the radius in CircleShapeFactory and mass is taken from
         * http://scienceofangrybirds.weebly.com/mass-and-weight.html
         */
        // is the code block below actually needed?!?
//        switch (type) {
//        case ICE:
//        	fixtureDef.friction = 0.7f;
//            fixtureDef.restitution = 0.2f;
//            fixtureDef.density = Constants.ICE_DENSITY;
//            break;
//        case WOOD:
//        	fixtureDef.friction = 4.0f;
//            fixtureDef.restitution = 0.0f;
//            fixtureDef.density = Constants.WOOD_DENSITY;
//            break;
//        case STONE:
//        	fixtureDef.friction = 4.0f;
//            fixtureDef.restitution = 0.1f;
//            fixtureDef.density = Constants.STONE_DENSITY;
//            break;
//        default:
//            break;
//        }
        return fixtureDef;
    }

}

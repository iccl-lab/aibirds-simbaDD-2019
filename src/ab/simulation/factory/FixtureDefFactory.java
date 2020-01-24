package ab.simulation.factory;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.FixtureDef;

import ab.simulation.utils.Constants;
import ab.vision.ABType;

public class FixtureDefFactory {

    /**
     *
     * @param type
     * @return
     */
    public static FixtureDef createFixtureDef(ABType type, Shape shape) {
        FixtureDef fixtureDef = new FixtureDef();
        /**
         * densities of the birds are calculated by mass/area where area is calculated
         * from the radius in CircleShapeFactory and mass is taken from
         * http://scienceofangrybirds.weebly.com/mass-and-weight.html
         */
        /* some densities from ihsev are multiplied by 10 / 6 */
        switch (type) {
        case GROUND:
            fixtureDef.friction = 0.8f;
            fixtureDef.restitution = 0.0f;
            fixtureDef.density = 0.5f;
            break;
        case SUPPORT:
            // fall through, currently treated as a hill
        case HILL:
            fixtureDef.friction = 0.8f;
            fixtureDef.restitution = 0.0f;
            fixtureDef.density = 0.5f;
            break;
        case SLING:
        case UNKNOWN:
            // no collision with anything
            fixtureDef.filter.maskBits = 0x0;
            break;
        case RED_BIRD:
            fixtureDef.friction = 0.3f;
            fixtureDef.restitution = 0.43f;
            fixtureDef.density = 10.0f; // 6.0f
            break;
        case YELLOW_BIRD:
            fixtureDef.friction = 0.3f;
            fixtureDef.restitution = 0.23f;
            fixtureDef.density = 10.0f; // 6.0f
            break;
        case BLUE_BIRD:
            fixtureDef.friction = 0.3f;
            fixtureDef.restitution = 0.25f;
            fixtureDef.density = 7.5f; // 4.5f
            break;
        case BLACK_BIRD:
            fixtureDef.friction = 0.4f; // 0.3f
            fixtureDef.restitution = 0.01f; // 0.03f
            fixtureDef.density = 10.0f; // 6.0f
            break;
        case WHITE_BIRD:
            fixtureDef.friction = 0.3f;
            fixtureDef.restitution = 0.23f;
            fixtureDef.density = 6.6f; // 4.0f
            fixtureDef.filter.categoryBits = 0x0002;
            break;
        case PIG:
            fixtureDef.friction = 0.7f;// small pig //others have 0.3f
            fixtureDef.restitution = 0.00f; // 0.05f
            // TODO find density
            fixtureDef.density = 4.0f; // 2.0f
            break;
        case ICE:
            fixtureDef.friction = 0.5f; // 0.7f
            fixtureDef.restitution = 0.2f;
            fixtureDef.density = Constants.ICE_DENSITY;
            break;
        case WOOD:
            fixtureDef.friction = 2.0f;
            fixtureDef.restitution = 0.0f;
            fixtureDef.density = Constants.WOOD_DENSITY;
            break;
        case STONE:
            fixtureDef.friction = 8.0f; // 4.0f
            fixtureDef.restitution = 0.1f;
            fixtureDef.density = Constants.STONE_DENSITY;
            break;
        case TNT:
            fixtureDef.friction = 0.7f;
            fixtureDef.restitution = 0.4f;
            fixtureDef.density = 0.75f;
            break;
        case EGG:
            fixtureDef.friction = 1.0f;
            fixtureDef.restitution = 0.0f;
            fixtureDef.density = 5.0f;
            // collide with anything but the white bird
            // prevents collisions when the egg is spawned on top of the bird
            fixtureDef.filter.maskBits = ~0x0002;
            break;
        }
        fixtureDef.shape = shape;
        return fixtureDef;
    }

}

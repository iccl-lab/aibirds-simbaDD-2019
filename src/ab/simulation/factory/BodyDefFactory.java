package ab.simulation.factory;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

import ab.vision.ABType;

public class BodyDefFactory {

    /**
     * 
     * @param type
     * @param position The world position within the model
     * @param angle    The angle the object is rotated with in the model
     * @return
     */
    public static BodyDef createBodyDef(ABType type, Vec2 position, double angle) {
        BodyDef bodyDef = new BodyDef();
        switch (type) {
        case GROUND:
        case HILL:
        case SUPPORT:
        case SLING:
        case UNKNOWN:
            bodyDef.type = BodyType.STATIC;
            bodyDef.fixedRotation = true;
            break;
        case RED_BIRD:
        case YELLOW_BIRD:
        case BLUE_BIRD:
        case BLACK_BIRD:
        case WHITE_BIRD:
        case PIG:
            // >1 means more <1 means less dampening
            bodyDef.angularDamping = 2.0f;
        case ICE:
        case WOOD:
        case STONE:
        case TNT:
            bodyDef.type = BodyType.DYNAMIC;
            bodyDef.fixedRotation = false;
            break;
        case EGG:
        	bodyDef.type = BodyType.DYNAMIC;
        	bodyDef.fixedRotation = true;
        	break;
        }
        bodyDef.position.set(position);
        // bodyDef.angle = (float) angle;
        return bodyDef;
    }

}

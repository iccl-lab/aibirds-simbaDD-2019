package ab.simulation.model;

import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.Circle;

public class SimulationUserData {

    private double health = 100.0;

    private enum ABOBJECT_TYPE {
        unknown_shape(0x0), shape_2X1(0x021), shape_4X1(0x041), shape_8X1(0x081), shape_10X1(0x0a1), shape_2X2(0x022),
        shape_4X2(0x042), shape_4X4_h(0x044), shape_4X4_c(0x144);
        private ABOBJECT_TYPE(int i) {
            // TODO Auto-generated constructor stub
        }

        public static ABOBJECT_TYPE shapeOf(int value) {
            switch (value) {
            case 0x021:
                return shape_2X1;
            case 0x041:
                return shape_4X1;
            case 0x081:
                return shape_8X1;
            case 0x0a1:
                return shape_10X1;
            case 0x022:
                return shape_2X2;
            case 0x042:
                return shape_4X2;
            case 0x044:
                return shape_4X4_h;
            case 0x144:
                return shape_4X4_c;
            default:
                return unknown_shape;
            }
        }
    }

    private final ABObject abObject;

    private final ABOBJECT_TYPE shapeType;

    // tells shot computation from which side the object is accessible
    private boolean access_side[];

    private double shift(double x, double w) {
        return Math.abs((x - w * 5.0 / 9.0) * 9.0 / 5.0);
    }

    public SimulationUserData(ABObject abObject, float ppm) {
        this.abObject = abObject;
        // 0 left side, 1 upper side, 2 right side (future work)
        access_side = new boolean[3];
        for (int i = 0; i < access_side.length; i++) {
            access_side[i] = false;
        }

        int type = 0x0;
        double x, y;
        // is circle?
        if (abObject instanceof Circle) {
            x = 2.0 * ((Circle) abObject).r / ppm;
            y = x;
            type |= 0x100;
        } else {
            x = abObject.getBbox().width / ppm;
            y = abObject.getBbox().height / ppm;
            if (x < y) {
                double z = y;
                y = x;
                x = z;
            }
        }

        int[] xs = {
                2,
                4,
                8,
                10 };
        int min_indx = 0;
        double min_val = Double.POSITIVE_INFINITY;
        for (int i = 0; i < xs.length; i++) {
            double val = shift(x, xs[i]);
            if (val < min_val) {
                min_val = val;
                min_indx = i;
            }
        }
        switch (xs[min_indx]) {
        case 2:
            type |= 0x020;
            break;
        case 4:
            type |= 0x040;
            break;
        case 8:
            type |= 0x080;
            break;
        case 10:
            type |= 0x0a0;
            break;
        default:
            break;
        }
        int[] ys = {
                1,
                2,
                4 };
        min_indx = 0;
        min_val = Double.POSITIVE_INFINITY;
        for (int i = 0; i < ys.length; i++) {
            double val = shift(y, ys[i]);
            if (val < min_val) {
                min_val = val;
                min_indx = i;
            }
        }
        switch (ys[min_indx]) {
        case 1:
            type |= 0x001;
            break;
        case 2:
            type |= 0x002;
            break;
        case 4:
            type |= 0x004;
            break;
        default:
            break;
        }

        shapeType = ABOBJECT_TYPE.shapeOf(type);
        // base hp based on type
        switch (abObject.type) {
        case ICE:
            health = 80;
            break;
        case WOOD:
            health = 120;
            break;
        case STONE:
            health = 300;
            break;
        case PIG:
        	health = 35;
        	break;
        case TNT:
        	health = 20;
        	break;
        case EGG:
        	// use very low health here, egg should explode on first contact
        	health = 1;
        	break;
        default:
            break;
        }
        
        // hp multiplier based on shape
        switch (shapeType) {
        case shape_2X1:
            health *= 1.0;
            break;
        case shape_4X1:
            health *= 1.25;
            break;
        case shape_8X1:
            health *= 1.75;
            break;
        case shape_10X1:
            health *= 2.5;
            break;
        case shape_2X2:
            health *= 1.5;
            break;
        case shape_4X2:
            health *= 2.0;
            break;
        case shape_4X4_h:
            health *= 3.0;
            break;
        case shape_4X4_c:
            health *= 4.5;
            break;
        case unknown_shape:
        	// fall through
        default:
            health *= 1.0;
            break;
        }
    }

    public ABObject getABObject() {
        return abObject;
    }

    public void setLeftSide(boolean status) {
        access_side[0] = status;
    }

    public void setUpperSide(boolean status) {
        access_side[1] = status;
    }

    public boolean isReachableLeft() {
        return (access_side[0]);
    }

    public boolean isReachableAbove() {
        return (access_side[1]);
    }

    /**
     * 
     * @param collidingType
     * @param impulseStrength
     * @return the remaining health of this object
     */
    public double inflictDamage(ABType collidingType, float impulseStrength) {
        // don't kill a dead object twice
        if (health <= 0.0)
            return 100.0;

        // TODO refinement
        // ideas:
        // * damage resistance (hills, etc. never receive damage),
        // * damage amplification (red vs wood, black vs stone, ...),
        // (maybe implement it with a 2d array of floats)
        // * impulse threshold (at least x amount of force needs to be applied to deal
        // damage)
        double damage_multiplier = 1.0;
        // double immunity = 1.0;
        switch (abObject.getType()) {
//        	case PIG:
//        		if (impulseStrength > 10.0f) {
//        			health = 0.0;
//        		}
//        		break;
        	case STONE:
        		switch (collidingType) {
    				case BLACK_BIRD:
    					damage_multiplier = 5.0f;    // original value: 7.0f
    					break;
    				case WHITE_BIRD:
    				case RED_BIRD:
    				case BLUE_BIRD:
    					damage_multiplier = 1.0f;
    					break;
    				case YELLOW_BIRD:
    					damage_multiplier = 0.5f;
    					break;
    				default:
    					damage_multiplier = 0.5f;
    					break;
        		}
        		break;
        	case ICE:
        		switch (collidingType) {
    				case BLACK_BIRD:
    					damage_multiplier = 1.25f;
    					break;
    				case WHITE_BIRD:
    					damage_multiplier = 0.8f;
    					break;
    				case RED_BIRD:
    					damage_multiplier = 1.25f;
    					break;
    				case BLUE_BIRD:
    					damage_multiplier = 10.0f;
    					break;
    				case YELLOW_BIRD:
    					damage_multiplier = 0.5f;
    					break;
    				default:
    					damage_multiplier = 0.5f;
    					break;
        		}
        		break;
        	case WOOD:
        		switch (collidingType) {
        			case YELLOW_BIRD:
        				damage_multiplier = 7.0f;
        				break;
    				case WHITE_BIRD:
    					damage_multiplier = 0.8f;
    					break;
    				case BLACK_BIRD:
    					// fall through
    				case RED_BIRD:
    					damage_multiplier = 1.25f;
    					break;
    				case BLUE_BIRD:    				
    					damage_multiplier = 1.0f;
    					break;
    				default:
    					damage_multiplier = 0.5f;
    					break;
        		}
        		break;
        	case TNT:
        		// this function is not responsible for detonating, this should be done upon
        		// destruction of this object
        		break;
        	case PIG:
        		damage_multiplier = 1.0f;
        		break;
        	case EGG:
        		// make sure this one blows up...
        		damage_multiplier = 100.0f;
        		break;
        	default:
        		// all birds, hills, supports, slings, ... receive no damage
        		damage_multiplier = 0.0;
        		break;
        }
        
        // double damage = impulseStrength / 13.57 * 100;
        double damage = impulseStrength * damage_multiplier;
        
        // damage *= damage_multiplier * immunity;
        // if (shapeType == ABOBJECT_TYPE.shape_8X1) {
        // System.out.println("SimulationUserData.inflictDamage() hp: " + health + "
        // damage: " + damage);
        // }
        if (damage > health / 2) {
            // health -= damage * damage_multiplier * immunity;
        	health -= damage;
        }
        return health;
    }

}
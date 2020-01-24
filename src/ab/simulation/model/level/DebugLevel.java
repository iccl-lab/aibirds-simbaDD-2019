package ab.simulation.model.level;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jbox2d.common.Vec2;

import ab.demo.other.Shot;
import ab.simulation.utils.SimUtil;
import ab.vision.ABObject;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Line;
import ab.vision.real.shape.Rect;

public class DebugLevel extends GenericLevel {

    private final Vec2 referencePoint_model = new Vec2(0.0f, 0.0f);
    private final Vec2 releasePoint_model = new Vec2(0.0f, 0.0f);

    public DebugLevel(Rect sling, Line ground, 
    		Map<String, List<ABObject>> world_objects, Queue<Circle> birds) {
        super(sling, ground, world_objects, birds);
    }

    public DebugLevel(GenericLevel level) {
        super(level);
    }

    @Override
    public String getTestName() {
        return "DebugLevel";
    }

    @Override
    public void mouseUp(Vec2 p, int button) {
        // TODO reimplement this
//        if (button == MOUSE_JOINT_BUTTON || button == BOMB_SPAWN_BUTTON) {
//            releasePoint_model.set(p);
//            launchBird();
//        } else {
//            super.mouseUp(p, button);
//        }
    }

    @Override
    public void mouseDown(Vec2 p, int button) {
        // TODO reimplement this
//        if (button == MOUSE_JOINT_BUTTON) {
//            referencePoint_model.set(getReferencePoint());
//        } else if (button == BOMB_SPAWN_BUTTON) {
//            referencePoint_model.set(p);
//        } else {
//            super.mouseDown(p, button);
//        }
    }

    @Override
    public void mouseDrag(Vec2 p, int button) {
        // TODO reimplement this
//        if (button == MOUSE_JOINT_BUTTON || button == BOMB_SPAWN_BUTTON) {
//            releasePoint_model.set(p);
//            // TODO update quadratic parameters for drawing the parabola
//            Vec2 deltaVector_model = releasePoint_model.sub(referencePoint_model);
//            Vec2 launchPoint_model = computeLaunchPoint(referencePoint_model, deltaVector_model);
//            Vec2 velocity_model = computerVelocity(deltaVector_model);
//            computeQuadraticParameters(launchPoint_model, velocity_model);
//        } else {
//            super.mouseDrag(p, button);
//        }
    }

    private void launchBird() {
        if (!(super.birdsLeft() > 0))
            return;
        Vec2 referencePoint_view = SimUtil.convertModelToView(referencePoint_model, super.getPPM());
        Vec2 releasePoint_view = SimUtil.convertModelToView(releasePoint_model, super.getPPM());
        Vec2 deltaVec2 = releasePoint_view.sub(referencePoint_view);
        Shot shot = new Shot((int) referencePoint_view.x, (int) referencePoint_view.y, (int) deltaVec2.x,
                (int) deltaVec2.y, getStepCount(), 0);
        super.addShots(shot);
    }

}

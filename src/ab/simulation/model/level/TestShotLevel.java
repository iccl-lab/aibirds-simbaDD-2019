package ab.simulation.model.level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ab.demo.other.Shot;

/**
 * stores the shots separately in order to restore them on reset
 */
public class TestShotLevel extends GenericLevel {

    private final List<Shot> shots = new ArrayList<Shot>();
    private boolean isInitialized = false;

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    public TestShotLevel(GenericLevel genericLevel, Collection<Shot> shots) {
        super(genericLevel);
        this.shots.addAll(shots);
    }

    public TestShotLevel(GenericLevel genericLevel, Shot... shots) {
        super(genericLevel);
        for (Shot shot : shots) {
            this.shots.add(shot);
        }
    }

    @Override
    public String getTestName() {
        return "TestShotLevel";
    }

    @Override
    public void initTest(boolean deserialized) {
        super.initTest(deserialized);
        super.addShots(shots);
        isInitialized = true;
    }

    public List<Shot> getShots() {
        return shots;
    }
}

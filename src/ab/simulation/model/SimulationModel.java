package ab.simulation.model;

import org.jbox2d.testbed.framework.TestbedSetting;
import org.jbox2d.testbed.framework.TestbedSetting.SettingType;
import org.jbox2d.testbed.framework.TestbedSettings;

import ab.simulation.model.level.GenericLevel;

public class SimulationModel<LevelType extends GenericLevel> {

    private LevelType genericLevel;
    private final TestbedSettings settings = new TestbedSettings();

    public SimulationModel() {
        TestbedSetting putToSleep = new TestbedSetting("PUT_TO_SLEEP", SettingType.ENGINE, true);
        settings.addSetting(putToSleep);
    }

    public SimulationModel(LevelType genericLevel) {
        this();
        this.genericLevel = genericLevel;
    }

    @Deprecated
    public void setCalculatedFps(float frameRate) {
        // TODO Auto-generated method stub

    }

    public void setRunningTest(GenericLevel nextTest) {
        // TODO Auto-generated method stub

    }

    public LevelType getCurrTest() {
        // TODO Auto-generated method stub
        return genericLevel;
    }

    public TestbedSettings getSettings() {
        return settings;
    }
}

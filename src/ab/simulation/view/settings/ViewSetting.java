package ab.simulation.view.settings;

public class ViewSetting {

    /**
     * The type of value this setting pertains to
     */
    public static enum ConstraintType {
        BOOLEAN, RANGE
    }

    public final String name;
    public final ConstraintType constraintType;
    public boolean enabled;
    public int value;
    public final int min;
    public final int max;

    public ViewSetting(String argName, boolean argValue) {
        name = argName;
        enabled = argValue;
        constraintType = ConstraintType.BOOLEAN;
        min = max = value = 0;
    }

    public ViewSetting(String argName, int argValue, int argMinimum, int argMaximum) {
        name = argName;
        value = argValue;
        min = argMinimum;
        max = argMaximum;
        constraintType = ConstraintType.RANGE;
        enabled = false;
    }
}
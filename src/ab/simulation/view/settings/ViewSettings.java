package ab.simulation.view.settings;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ViewSettings {
    public static final String DrawShapes = "Shapes";
    public static final String DrawJoints = "Joints";
    public static final String DrawAABBs = "AABBs";
    public static final String DrawContactPoints = "Contact Points";
    public static final String DrawContactNormals = "Contact Normals";
    public static final String DrawContactImpulses = "Contact Impulses";
    public static final String DrawFrictionImpulses = "Friction Impulses";
    public static final String DrawCOMs = "Center of Mass";
    public static final String DrawStats = "Stats";
    public static final String DrawHelp = "Help";
    public static final String DrawTree = "Dynamic Tree";
    public static final String DrawWireframe = "Wireframe Mode";
    public static final String DrawPairs = "Pairs";
    public static final String DrawABTypes = "ABTypes";

    private List<ViewSetting> settings;
    private final Map<String, ViewSetting> settingsMap;

    public ViewSettings() {
        settings = Lists.newArrayList();
        settingsMap = Maps.newHashMap();
        populateDefaultSettings();
    }

    private void populateDefaultSettings() {
        addSetting(new ViewSetting(DrawShapes, true));
        addSetting(new ViewSetting(DrawJoints, true));
        addSetting(new ViewSetting(DrawAABBs, false));
        addSetting(new ViewSetting(DrawContactPoints, false));
        addSetting(new ViewSetting(DrawContactNormals, false));
        addSetting(new ViewSetting(DrawContactImpulses, false));
        addSetting(new ViewSetting(DrawFrictionImpulses, false));
        addSetting(new ViewSetting(DrawCOMs, false));
        addSetting(new ViewSetting(DrawStats, true));
        addSetting(new ViewSetting(DrawHelp, false));
        addSetting(new ViewSetting(DrawTree, false));
        addSetting(new ViewSetting(DrawWireframe, false));
        addSetting(new ViewSetting(DrawPairs, false));
        addSetting(new ViewSetting(DrawABTypes, true));
    }

    /**
     * Adds a settings to the settings list
     * 
     * @param drawingSetting
     */
    public void addSetting(ViewSetting drawingSetting) {
        if (settingsMap.containsKey(drawingSetting.name)) {
            throw new IllegalArgumentException("Settings already contain a setting with name: " + drawingSetting.name);
        }
        settings.add(drawingSetting);
        settingsMap.put(drawingSetting.name, drawingSetting);
    }

    /**
     * Returns an unmodifiable list of settings
     * 
     * @return
     */
    public List<ViewSetting> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    /**
     * Gets a setting by name.
     * 
     * @param argName
     * @return
     */
    public ViewSetting getSetting(String argName) {
        return settingsMap.get(argName);
    }
}
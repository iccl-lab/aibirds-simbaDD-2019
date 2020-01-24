package ab.simulation.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ab.simulation.controller.SimulationInputListeners;
import ab.simulation.model.level.GenericLevel;
import ab.simulation.view.panel.SimulationPanel;
import ab.simulation.view.panel.SimulationSidePanel;
import ab.simulation.view.settings.ViewSettings;

@SuppressWarnings("serial")
public class DefaultSimulationView extends AbstractSimulationView {

    private final SimulationPanel simulationPanel;
    private final SimulationSidePanel simulationSidePanel;
    private final ViewSettings drawingSettings = new ViewSettings();

    public DefaultSimulationView(float initialScale) {
        this.setTitle("Angry Birds Simulation");
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        simulationPanel = new SimulationPanel(drawingSettings, initialScale);
        simulationSidePanel = new SimulationSidePanel(drawingSettings);

        this.add(simulationPanel, "Center");
        this.add(new JScrollPane(simulationSidePanel), "East");

        this.pack();
        this.setVisible(true);
    }

    private long frameCount;
    private int targetFrameRate = 60;
    private float frameRate = 0;
    protected long startTime, beforeTime, afterTime, updateTime, timeDiff, sleepTime, timeSpent;

    @Override
    public boolean render() {
        float timeInSecs;
        timeSpent = beforeTime - updateTime;
        if (timeSpent > 0) {
            timeInSecs = timeSpent * 1.0f / 1000000000.0f;
            updateTime = System.nanoTime();
            frameRate = (frameRate * 0.9f) + (1.0f / timeInSecs) * 0.1f;
        } else {
            updateTime = System.nanoTime();
        }
        frameCount++;

        afterTime = System.nanoTime();

        timeDiff = afterTime - beforeTime;
        sleepTime = (1000000000 / targetFrameRate - timeDiff) / 1000000;
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
            }
        }
        beforeTime = System.nanoTime();
        return simulationPanel.render();
    }

    @Override
    public void paintScreen() {
        simulationPanel.paintScreen();
    }

    @Override
    public void drawLevel(GenericLevel level) {
        simulationPanel.drawLevel(level);
    }

    @Override
    public void initListeners(SimulationInputListeners simulationInputListeners) {
        // TODO Auto-generated method stub
        simulationSidePanel.initListeners(simulationInputListeners);
    }

}

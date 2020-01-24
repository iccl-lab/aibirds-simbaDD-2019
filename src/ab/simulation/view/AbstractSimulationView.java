package ab.simulation.view;

import javax.swing.JFrame;

import ab.simulation.controller.SimulationInputListeners;
import ab.simulation.model.level.GenericLevel;

@SuppressWarnings("serial")
public abstract class AbstractSimulationView extends JFrame {
    public abstract boolean render();

    public abstract void paintScreen();

    public abstract void initListeners(SimulationInputListeners simulationInputListeners);

    public abstract void drawLevel(GenericLevel level);

}

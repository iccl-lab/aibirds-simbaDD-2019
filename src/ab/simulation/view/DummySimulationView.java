package ab.simulation.view;

import ab.simulation.controller.SimulationInputListeners;
import ab.simulation.model.level.GenericLevel;

/**
 * does absolutely nothing. Intended to be used when the SimulationController
 * should not display anything and run completely in the background
 * 
 * @author Sineme
 *
 */
@SuppressWarnings("serial")
public class DummySimulationView extends AbstractSimulationView {

    @Override
    public boolean render() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void paintScreen() {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawLevel(GenericLevel level) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initListeners(SimulationInputListeners simulationInputListeners) {
        // TODO Auto-generated method stub

    }

}

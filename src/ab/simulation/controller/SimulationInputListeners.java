package ab.simulation.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimulationInputListeners {

    private SimulationController<?, ?, ?> simulationController;

    public SimulationInputListeners(SimulationController<?, ?, ?> simulationController) {
        this.simulationController = simulationController;
    }

    public synchronized SimulationController<?, ?, ?> getSimulationController() {
        return simulationController;
    }

    public synchronized void setSimulationController(SimulationController<?, ?, ?> simulationController) {
        this.simulationController = simulationController;
    }

    public ActionListener unPauseListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSimulationController().unPause();
            }
        };
    }

    /**
     * 
     * @return an action listener that stops the simulation and triggers a single
     *         time step
     */
    public ActionListener singleStepListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSimulationController().singleStep();
            }
        };
    }

    public ActionListener resetListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSimulationController().reset();
            }
        };
    }

    public ActionListener exitListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSimulationController().stop();
            }
        };
    }

    public ActionListener saveListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSimulationController().save();
            }
        };
    }

    public ActionListener loadListener() {

        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSimulationController().load();
            }
        };
    }
}

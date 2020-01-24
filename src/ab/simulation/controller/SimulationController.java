package ab.simulation.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.jbox2d.testbed.framework.AbstractTestbedController.UpdateBehavior;

import ab.simulation.model.SimulationModel;
import ab.simulation.model.level.GenericLevel;
import ab.simulation.utils.Constants;
import ab.simulation.view.AbstractSimulationView;

public class SimulationController<LevelType extends GenericLevel, ModelType extends SimulationModel<LevelType>, ViewType extends AbstractSimulationView>
        implements Callable<LevelType>, Runnable {
    private static AtomicInteger numberOfControllers = new AtomicInteger(0);

    // Supplier for when this Controller is run in a Thread
    private final Supplier<ModelType> modelSupplier;
    private final Supplier<ViewType> viewSupplier;

    private final AtomicBoolean continueSimulating = new AtomicBoolean(true);
    private final boolean closeOnFinish;

    private SimulationModel<LevelType> simulationModel;
    private static final ThreadLocal<AbstractSimulationView> simulationView = new ThreadLocal<AbstractSimulationView>() {
        @Override
        protected AbstractSimulationView initialValue() {
            return null;
        }
    };

    private boolean savePending, loadPending, resetPending = false;
    private static final ThreadLocal<SimulationInputListeners> simulationListeners = new ThreadLocal<SimulationInputListeners>() {
        @Override
        protected SimulationInputListeners initialValue() {
            return null;
        }
    };

    public SimulationController(Supplier<ModelType> modelSupplier, Supplier<ViewType> viewSupplier) {
        this(modelSupplier, viewSupplier, true);
    }

    public SimulationController(Supplier<ModelType> modelSupplier, Supplier<ViewType> viewSupplier,
            boolean closeOnFinish) {
        this.modelSupplier = modelSupplier;
        this.viewSupplier = viewSupplier;
        this.closeOnFinish = closeOnFinish;
    }

    @Override
    public void run() {
        init();
        while (!(!continueSimulating.get() || Thread.currentThread().isInterrupted())) {
            stepAndRender();
            if (simulationModel.getCurrTest().isFinished() && closeOnFinish)
                continueSimulating.set(false);
        }
        // cleanup thread
        if (numberOfControllers.decrementAndGet() < Constants.NUMBER_OF_THREADS) {
            simulationView.get().dispose();
            simulationView.set(null);
            simulationListeners.set(null);
        }
    }

    @Override
    public LevelType call() throws Exception {
        run();
        return simulationModel.getCurrTest();
    }

    private void init() {
        simulationModel = modelSupplier.get();
        if (simulationView.get() == null) {
            simulationView.set(viewSupplier.get());
        }
        simulationModel.getCurrTest().init();
        // ensure that the listeners for the view are connected to the right controller
        if (simulationListeners.get() == null) {
            simulationListeners.set(new SimulationInputListeners(this));
            simulationView.get().initListeners(simulationListeners.get());
        } else {
            simulationListeners.get().setSimulationController(this);
        }
    }

    private GenericLevel nextTest;
    private GenericLevel currTest;

    protected void stepAndRender() {
        if (nextTest != null) {
            initTest(nextTest);
            simulationModel.setRunningTest(nextTest);
            if (currTest != null) {
                currTest.exit();
            }
            currTest = nextTest;
            nextTest = null;
        }
        if (simulationView.get().render()) {
            updateTest();
            simulationView.get().paintScreen();
        }
    }

    /**
     * Called by the main run loop. If the update behavior is set to
     * {@link UpdateBehavior#UPDATE_IGNORED}, then this needs to be called manually
     * to update the input and test.
     */
    public void updateTest() {
        if (resetPending) {
            if (currTest != null) {
                currTest.init();
            }
            simulationModel.getCurrTest().init();
            resetPending = false;
        }
        if (savePending) {
            if (currTest != null) {
                // _save();
            }
            savePending = false;
        }
        if (loadPending) {
            if (currTest != null) {
                // _load();
            }
            loadPending = false;
        }

        // simulate step
        simulationModel.getCurrTest().step(simulationModel.getSettings());

        // draw world
        simulationView.get().drawLevel(simulationModel.getCurrTest());
    }

    private void initTest(GenericLevel nextTest2) {
        // TODO Auto-generated method stub

    }

    /************ LISTENER INPUT ************/

    void stop() {
        continueSimulating.set(false);
    }

    void reset() {
        resetPending = true;
    }

    void save() {
        savePending = true;
    }

    void load() {
        loadPending = true;
    }

    void unPause() {
        if (simulationModel.getSettings().pause) {
            simulationModel.getSettings().pause = false;
        } else {
            simulationModel.getSettings().pause = true;
        }
    }

    void singleStep() {
        simulationModel.getSettings().singleStep = true;
        if (!simulationModel.getSettings().pause) {
            simulationModel.getSettings().pause = true;
        }
    }

}

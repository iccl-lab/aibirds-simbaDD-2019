package ab.simulation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import ab.demo.other.Shot;
import ab.simulation.controller.SimulationController;
import ab.simulation.model.SimulationModel;
import ab.simulation.model.level.GenericLevel;
import ab.simulation.model.level.TestShotLevel;
import ab.simulation.utils.Constants;
import ab.simulation.view.AbstractSimulationView;
import ab.simulation.view.DefaultSimulationView;
import ab.simulation.view.DummySimulationView;

public class SimulationManager {

    private static long timeout = 15000;

    public static <LevelType extends GenericLevel> LevelType simulate(Supplier<LevelType> levelSupplier,
            boolean showSimulation, boolean closeOnFinish) {
        final LevelType level = levelSupplier.get();
        Supplier<AbstractSimulationView> viewSupplier;
        if (showSimulation) {
            viewSupplier = () -> new DefaultSimulationView(level.getPPM());
        } else {
            viewSupplier = DummySimulationView::new;
        }
        Supplier<SimulationModel<LevelType>> modelSupplier = () -> new SimulationModel<LevelType>(level);
        SimulationController<LevelType, SimulationModel<LevelType>, AbstractSimulationView> simulationController = new SimulationController<LevelType, SimulationModel<LevelType>, AbstractSimulationView>(
                modelSupplier, viewSupplier, showSimulation ? closeOnFinish : true);
        simulationController.run();
        return level;
    }

    public static Collection<SimulationResult> simulateShots(GenericLevel genericLevel, List<Shot> shots,
            boolean showSimulations) {
        return simulateShots(genericLevel, shots, showSimulations, true);
    }

    public static Collection<SimulationResult> simulateShots(GenericLevel genericLevel, List<Shot> shots,
            boolean showSimulations, boolean closeOnFinish) {
        List<List<Shot>> shotsList = new ArrayList<List<Shot>>(shots.size());
        for (Shot shot : shots) {
            shotsList.add(new ArrayList<Shot>(Arrays.asList(shot)));
        }
        return simulateShotsList(genericLevel, shotsList, showSimulations, closeOnFinish);
    }

    public static Collection<SimulationResult> simulateShotsList(GenericLevel genericLevel, List<List<Shot>> shotsList,
            boolean showSimulations) {

        return simulateShotsList(genericLevel, shotsList, showSimulations, true);
    }

    public static Collection<SimulationResult> simulateShotsList(GenericLevel genericLevel, List<List<Shot>> shotsList,
            boolean showSimulations, boolean closeOnFinish) {

        List<SimulationResult> resultList = new LinkedList<SimulationResult>();

        Supplier<AbstractSimulationView> viewSupplier = DummySimulationView::new;
        if (showSimulations) {
            viewSupplier = () -> new DefaultSimulationView(genericLevel.getPPM());
        } else {
            viewSupplier = DummySimulationView::new;
        }
        Supplier<SimulationModel<TestShotLevel>> modelSupplier;

        ExecutorService executorService = Executors.newFixedThreadPool(Constants.NUMBER_OF_THREADS);
        ExecutorCompletionService<TestShotLevel> executorCompletionService = new ExecutorCompletionService<TestShotLevel>(
                executorService);
        // add callables to be run on the threads
        AbstractList<Future<TestShotLevel>> futures = new ArrayList<Future<TestShotLevel>>(shotsList.size());
        for (SimulationController<TestShotLevel, ?, ?> simulationController : createControllers(
                genericLevel,
                viewSupplier,
                shotsList,
                closeOnFinish)) {
            futures.add(executorCompletionService.submit(simulationController));
        }
        // wait for the result of the simulation and put it into the resultList
        try {
            TestShotLevel level;
            long t1, t0;
            t0 = System.currentTimeMillis();
            t1 = t0;
            int number_of_shots = shotsList.size();
            while (t1 - t0 < timeout) {
                if (number_of_shots <= 0)
                    break; // no more shots left to wait for
                Future<TestShotLevel> future = executorCompletionService.poll();
                if (future != null) {
                    level = future.get();
                    int score = level.getScore();
                    SimulationResult result = new SimulationResult(score, level.getShots());
                    System.out.println("score of shot " + (shotsList.size() - number_of_shots) + ": " + score);
                    resultList.add(result);
                    --number_of_shots;
                }
                t1 = System.currentTimeMillis();
            }
            for (Future future : futures) {
                future.cancel(true);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return resultList;
    }

    private static List<SimulationController<TestShotLevel, SimulationModel<TestShotLevel>, AbstractSimulationView>> createControllers(
            GenericLevel genericLevel, Supplier<AbstractSimulationView> viewSupplier, List<List<Shot>> shotsList,
            boolean closeOnFinish) {
        List<SimulationController<TestShotLevel, SimulationModel<TestShotLevel>, AbstractSimulationView>> simulationControllers = new ArrayList<SimulationController<TestShotLevel, SimulationModel<TestShotLevel>, AbstractSimulationView>>(
                shotsList.size());
        for (List<Shot> shots : shotsList) {
            Supplier<SimulationModel<TestShotLevel>> modelSupplier = () -> new SimulationModel<TestShotLevel>(
                    new TestShotLevel(genericLevel, shots));
            SimulationController<TestShotLevel, SimulationModel<TestShotLevel>, AbstractSimulationView> simulationController = new SimulationController<TestShotLevel, SimulationModel<TestShotLevel>, AbstractSimulationView>(
                    modelSupplier, viewSupplier, closeOnFinish);
            simulationControllers.add(simulationController);
        }
        return simulationControllers;
    }

}

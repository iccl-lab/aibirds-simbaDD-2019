/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;

import static java.lang.Thread.sleep;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import ab.demo.other.ActionRobot;
import ab.demo.other.ActionRobotWrapper;
import ab.demo.other.Shot;
import ab.planner.BasicLevelSelectionStrategy;
import ab.planner.LevelSelectionStrategy;
import ab.planner.TrajectoryPlanner;
import ab.simulation.SimulationManager;
import ab.simulation.SimulationResult;
import ab.simulation.model.SimulationUserData;
import ab.simulation.model.level.GenericLevel;
import ab.simulation.model.level.StaticAnalysisLevel;
import ab.simulation.model.level.TestShotLevel;
import ab.simulation.utils.SimUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.real.ImageSegmenter;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Line;
import ab.vision.real.shape.Rect;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class SimulationAgent implements Runnable {

    private ActionRobotWrapper actionRobotWrapper;
    private Random randomGenerator;
    /*
     * interesting levels on first page of poached eggs:
     * 1 - 2  - backward edge level
     * 1 - 10 - first with blue birds
     * 1 - 16 - first with yellow birds
     * 2 - 5  - first with black birds
     * 2 - 14 - first with white birds
     */
    public int currentLevel = 1;
    public static int time_limit = 12;

    // the vision object that is currently used for analysis. Due to some
    // indeterminism inside the vision module, it is important that the vision
    // is not changed (i.e. no new screenshots are used) during the 
    // computation of a single shot.
    Vision current_vision;
    TrajectoryPlanner tp;
    private Point prevTarget;

    private LevelSelectionStrategy levelStrategy;
    
    private boolean showSimulation = false;
    private boolean closeAfterSim = false;

    public SimulationAgent(boolean showSim, boolean closeAfterSim) {
    	this.showSimulation = showSim;
    	this.closeAfterSim = closeAfterSim;
    	
        actionRobotWrapper = new ActionRobotWrapper(false);
        // scores = new LinkedHashMap<Integer, Integer>();
        tp = new TrajectoryPlanner();
        prevTarget = null;
        randomGenerator = new Random();

        levelStrategy = new BasicLevelSelectionStrategy(false);

        actionRobotWrapper.GoFromMainMenuToLevelSelection();
    }

    public SimulationAgent(String serverIP, int teamId, boolean showSim, boolean closeAfterSim) {
    	this.showSimulation = showSim;
    	this.closeAfterSim = closeAfterSim;
    	
        actionRobotWrapper = new ActionRobotWrapper(serverIP, teamId);
        tp = new TrajectoryPlanner();
        prevTarget = null;
        randomGenerator = new Random();
        levelStrategy = new BasicLevelSelectionStrategy(true);
    }
    
    /**
     * TODO: re-enable use of level selection class after testing!!!
     * 
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
    	// init competition parameters
    	levelStrategy.setCompetitionParameters(actionRobotWrapper.getCompetitionParameters());
    	
    	// When client mode, this call is blocking until the competition starts!
    	// I.e. the "Start" button in the Server GUI is pressed.
        actionRobotWrapper.loadLevel(levelStrategy.getCurrentLevel()+1);
        
        while (true) {
            GameState state = solve();
            if (state == GameState.WON) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int score = actionRobotWrapper.getScore(levelStrategy.getCurrentLevel());
                
                // overall best scores, if in competition mode, otherwise null
                int[] bestScores = actionRobotWrapper.checkScore();
                levelStrategy.updateLevelInfo(levelStrategy.getCurrentLevel(), state, score, bestScores);
                
                // +1 since we start counting with 0, but load level counts 1...n
                actionRobotWrapper.loadLevel(levelStrategy.nextLevel()+1);
                
                // make a new trajectory planner whenever a new level is entered
                tp = new TrajectoryPlanner();
            } else if (state == GameState.LOST) {
                int[] bestScores = actionRobotWrapper.checkScore();
                // also give this info to the level strategy
                levelStrategy.updateLevelInfo(levelStrategy.getCurrentLevel(), state, 0, bestScores);
                //
                System.out.println("SimulationAgent.run(): " + "Restart");
                
                 if (levelStrategy.restartLevel())
                	 actionRobotWrapper.restartLevel();
                 else
                	 actionRobotWrapper.loadLevel(levelStrategy.nextLevel()+1);
                 
            } else if (state == GameState.LEVEL_SELECTION) {
                System.out.println(
                        "SimulationAgent.run(): " + "Unexpected level selection page, go to the last current level : "
                                + levelStrategy.getCurrentLevel());
                // actionRobotWrapper.loadLevel(currentLevel);
                actionRobotWrapper.loadLevel(levelStrategy.getCurrentLevel());
            } else if (state == GameState.MAIN_MENU) {
                System.out.println(
                        "SimulationAgent.run(): " + "Unexpected main menu page, go to the last current level : "
                                + levelStrategy.getCurrentLevel());
                // ActionRobot.GoFromMainMenuToLevelSelection();
                actionRobotWrapper.loadLevel(levelStrategy.getCurrentLevel());
            } else if (state == GameState.EPISODE_MENU) {
                System.out.println(
                        "SimulationAgent.run(): " + "Unexpected episode menu page, go to the last current level : "
                                + levelStrategy.getCurrentLevel());
                // ActionRobot.GoFromMainMenuToLevelSelection();
                actionRobotWrapper.loadLevel(levelStrategy.getCurrentLevel());
            }

        }

    }

    private double distance(Point p1, Point p2) {
        return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
    }

    /**
     * 
     */
    private GenericLevel createLevel(BufferedImage screenshot) {
        // process image
        current_vision = new Vision(screenshot);

        // find the slingshot
        Rectangle tmpsling = current_vision.findSlingshotMBR();

        // confirm the slingshot
        while (tmpsling == null && actionRobotWrapper.getState() == GameState.PLAYING) {
            System.out.println(
                    "SimulationAgent.createLevel(): " + "CNA: no slingshot detected. Please remove pop up or zoom out");
            ActionRobot.fullyZoomOut();
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            screenshot = ActionRobot.doScreenShot();
            current_vision = new Vision(screenshot);
            tmpsling = current_vision.findSlingshotMBR();
        }

        ImageSegmenter seg = new ImageSegmenter(screenshot);
        Line ground = new Line(seg.findGroundLevel(), ABType.GROUND);
        Map<String, List<ABObject>> world_objects = new HashMap<String, List<ABObject>>();

        world_objects.put("hills", current_vision.findHills());
        world_objects.put("supps", current_vision.findSupport());
        world_objects.put("objects", current_vision.findBlocksRealShape());
        world_objects.put("TNT", current_vision.findTNTs());
        world_objects.put("pigs", current_vision.findPigsRealShape());
        Queue<Circle> birds = current_vision.findBirdsRealShape();

        System.out.println("number of pigs left: " + world_objects.get("pigs").size());
        if (birds.size() == 0) {
        	// vision has failed once again...
        	System.out.println("WARN: no birds detected, using fallback.");
        	double radius;
        	ABType bird_type = current_vision.detectShootingBirdSpecies();
        	// values taken from ihsev
        	switch (bird_type) {
        		case YELLOW_BIRD:
        			radius = tmpsling.getHeight() * 0.5 * 0.23;
        			break;
        		case BLUE_BIRD:
        			radius = tmpsling.getHeight() * 0.5 * 0.14;
        			break;
        		case BLACK_BIRD:
        			radius = tmpsling.getHeight() * 0.5 * 0.27;
        			break;
        		case WHITE_BIRD:
        			radius = tmpsling.getHeight() * 0.5 * 0.30;
        			break;
        		case RED_BIRD:
        			// fall through
        		default:
        			radius = tmpsling.getHeight() * 0.5 * 0.21;
        			break;
        	}
        	
        	birds.add(new Circle(0, 0, radius, bird_type));
        }

        // useful functions
        // aRobot.getBirdTypeOnSling();
        Rect sling = new Rect(tmpsling.getCenterX(), tmpsling.getCenterY(), tmpsling.getHeight(), tmpsling.getWidth(),
                0, ABType.SLING);

        return new GenericLevel(sling, ground, world_objects, birds);
    }

    /**
     * Body is also returned for later analysis.
     * 
     * @param genericLevel will be copied and used to find potential targets
     * @return Map of Points to shoot at and the target body
     */
    private Map<Point, Body> findTargets(final GenericLevel genericLevel) {
        System.out.println("Starting static analysis");
        StaticAnalysisLevel staticAnalysisLevel = SimulationManager
                .simulate(() -> new StaticAnalysisLevel(genericLevel),
                		              false, false);

        Map<Point, Body> targets = new HashMap<Point, Body>();
        List<Body> tbodies = staticAnalysisLevel.getReachableTargets();

        HashMap<Body, Float> bodyForces = staticAnalysisLevel.getBodyForces(tbodies);
        // System.out.println("find targets: body set size is " +
        // bodyForces.keySet().size());
        float PPM = staticAnalysisLevel.getPPM();
        for (Map.Entry<Body, Float> entry : bodyForces.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList())) {
            // System.out.println(
            // "SimulationAgent.findTargets(): " + entry.getKey().getUserData() + " force: "
            // + entry.getValue());
        	// TODO: insert shots at every edge of the body, not only the center
            Vec2 center = entry.getKey().getWorldCenter();
            Vec2 target = SimUtil.convertModelToView(center, PPM);
            Point p = new Point((int) target.x, (int) target.y);
            targets.put(p, entry.getKey());
            
            // try points at the edges of a body too
            AABB bbox  = entry.getKey().m_fixtureList.getAABB(0);
            int width  = (int) (bbox.upperBound.x - bbox.lowerBound.x);
            int height = (int) (bbox.upperBound.y - bbox.lowerBound.y);
            // if the body is higher than wide, shoot at the upper and lower rim
            if (height > width) {
            	Vec2 v_upper = new Vec2(center.x, center.y + height / 2 * 0.9f);
            	Vec2 v_lower = new Vec2(center.x, center.y - height / 2 * 0.9f);
            	v_upper = SimUtil.convertModelToView(v_upper, PPM);
            	v_lower = SimUtil.convertModelToView(v_lower, PPM);
            	Point p_upper = new Point((int) v_upper.x, (int) v_upper.y);
            	Point p_lower = new Point((int) v_lower.x, (int) v_lower.y);
            	targets.put(p_upper, entry.getKey());
            	targets.put(p_lower, entry.getKey());
            }
            // otherwise try spots at its left or right border
            else {
            	Vec2 v_left  = new Vec2(center.x - width / 2 * 0.9f, center.y);
            	Vec2 v_right = new Vec2(center.x + width / 2 * 0.9f, center.y);
            	v_left  = SimUtil.convertModelToView(v_left, PPM);
            	v_right = SimUtil.convertModelToView(v_right, PPM);
            	Point p_left  = new Point((int) v_left.x, (int) v_left.y);
            	Point p_right = new Point((int) v_right.x, (int) v_right.y);
            	targets.put(p_left, entry.getKey());
            	targets.put(p_right, entry.getKey());
            }
        }

        System.out.println("Chose " + targets.size() + " targets to evaluate.");
        
        // find targets residing on backward edges (i.e. hills that are angled
        // towards pigs)
        Map<Point, Body> bwe_ts = staticAnalysisLevel.findBackwardEdgeTargets();
        targets.putAll(bwe_ts);
        System.out.println("Added " + bwe_ts.size() + " bwe targets.");        

        return targets;
    }

    /**
     * This function is responsible for creating the shots that should be tried by
     * 
     * @param targets a list of Points that should be shot at
     * @param sling   to estimate the release Point of the shot
     * @return a list of shots at each target
     */
    private List<Shot> computeShots(Map<Point, Body> targets, Rectangle sling) {
        List<Shot> shots = new ArrayList<Shot>();
        Point refPoint   = tp.getReferencePoint(sling);
        ABType birdType  = current_vision.detectShootingBirdSpecies();
        for (Point target : targets.keySet()) {
            // estimate the trajectory
            ArrayList<Point> points = tp.estimateLaunchPoint(sling, target);
            
            Point pts[] = points.toArray(new Point[points.size()]);
            for (int i = 0; i < pts.length; i++) {
                // do not consider high shot for objects only accessible from
                // left and vice versa
            	SimulationUserData su = (SimulationUserData) targets.get(target).getUserData();
                if (pts.length == 2) {

                    if ((i == 0 && (!su.isReachableLeft())) || (i == 1 && (!su.isReachableAbove()))) {
                    	System.out.println("Skipping shot " + i + "...");
                        continue;
                    }
                }
                
                int dx = pts[i].x - refPoint.x;
                int dy = pts[i].y - refPoint.y;
                double releaseAngle = tp.getReleaseAngle(sling, pts[i]);
                int tapInterval;
                int tapTime;
                Shot shot;
                
                System.out.println("bird type: " + birdType);
                switch (birdType) {
                // Intervals are taken from the NaiveAgent
                case RED_BIRD:
                	tapTime = tp.getTapTime(sling, pts[i], target, 0);
            		shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
            		shots.add(shot);
                    break; // start of trajectory
                case YELLOW_BIRD:
                	// angles have to be greater than 90 degrees!
                	if (pts.length == 2) {
                		tapTime = tp.getTapTimeYellowBird(sling, pts[i], target, 
                									  (i == 0) ? 170 : 110);
                		System.out.println("Tap time for yellow bird: " + tapTime);
                		shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
                        shots.add(shot);
                	}
                	else {
                		// if there is only one option, simply try both impact 
                		// angles
                		shots.add(new Shot(refPoint.x, refPoint.y, dx, dy, 0,
                				tp.getTapTimeYellowBird(sling, pts[i], target, 
                			    su.isReachableLeft() ? 110 : 170)));
                	}
                    break;
                case WHITE_BIRD:
                	// as long as we do not have special computations for the
                	// tap time of each bird class, simply simulate tapping
                	// every 10% of the trajectory, starting at the half
                	for (tapInterval = 50; tapInterval <= 90; tapInterval += 10) {
                		tapTime = tp.getTapTime(sling, pts[i], target, tapInterval);
                		shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
                		shots.add(shot);
                	}
                    break;
                case BLUE_BIRD:
                	// as long as we do not have special computations for the
                	// tap time of each bird class, simply simulate tapping
                	// every 10% of the trajectory, starting at the half
                	for (tapInterval = 45; tapInterval < 100; tapInterval += 10) {
                		tapTime = tp.getTapTime(sling, pts[i], target, tapInterval);
                		shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
                		shots.add(shot);
                	}
                    break;
                case BLACK_BIRD:
                	// black birds will most likely be most 
                	// effective when using an inertia fuse
                	tapTime = tp.getTapTime(sling, pts[i], target, 200);
            		shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
            		shots.add(shot);
                    break; // end of trajectory
                default:
                	// if the birds type is unknown, do not trigger the ability
                	tapTime = tp.getTapTime(sling, pts[i], target, 200);
            		shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
            		shots.add(shot);
                    break; // beyond end of trajectory
                }
            }
        }
        
        System.out.println("Chose " + shots.size() + " shots to evaluate.");
        return shots;
    }

    /**
     * @param level the shots will be tested in
     * @param shots that will be tested
     * @return a list of shots in order which achieved the best result
     */
    private List<Shot> findBestConsecutiveShots(final GenericLevel genericLevel, List<List<Shot>> shotsList) {

        SimulationManager.simulateShotsList(genericLevel, shotsList, true);
        List<Shot> bestCascadingShots = new LinkedList<Shot>();
//        GenericLevel bestLevel = genericLevel;
//        for (List<Shot> shots : shotsList) {
//            TestShotLevel level = new TestShotLevel(genericLevel, shots);
//            simulate(level, true);
//            if (level.getScore() > bestLevel.getScore()) {
//                bestCascadingShots = shots;
//                bestLevel = level;
//            }
//        }
//        if (!shotsList.isEmpty() && bestCascadingShots.isEmpty()) {
//            bestCascadingShots = shotsList.get(randomGenerator.nextInt(shotsList.size()));
//        } else {
//            simulate(bestLevel, false);
//        }
        return bestCascadingShots;
    }

    /**
     * @param level the shots will be tested in
     * @param shots that will be tested
     * @return a list of shots in order which achieved the best result
     */
    private Optional<Shot> findBestShot(final GenericLevel genericLevel, List<Shot> shots) {
        Optional<Shot> bestShot = Optional.empty();
        PriorityQueue<SimulationResult> rankedShots = new PriorityQueue<SimulationResult>(shots.size());
        // set last parameter to false to only show the best shot
        Collection<SimulationResult> resultsCollection = SimulationManager.simulateShots(genericLevel, shots, false);
        rankedShots.addAll(resultsCollection);
        
        SimulationResult result = levelStrategy.selectBestShot(rankedShots);
        //SimulationResult result = rankedShots.poll();
        // TODO: enble score requirement again when there is a heuristic for
        // points yielded by object destruction etc.
        if (result != null /* && result.getScore() > 0 */) {
            bestShot = Optional.of(result.getShot().get(0));
            System.out.println("Best shot score: " + result.getScore());
        }
        return bestShot;
    }

    private boolean performShot(Shot shot, Rectangle sling) {
        // check image if no pigs
        BufferedImage newScreenshot = actionRobotWrapper.doScreenShot();
        Vision newVision = new Vision(newScreenshot);
        // somehow, findPigsMBR yielded wrong pig counts in level 16...
        List<ABObject> newPigs = newVision.findPigsRealShape();
        
        if (newPigs.size() == 0) {
        	System.out.println("No pigs detected! Skipping shot...");
            return false;
        }
        // make the shot
        actionRobotWrapper.shoot(shot);

        // check the state after the shot
        GameState state = actionRobotWrapper.getState();

        // update parameters after a shot is made
        if (state == GameState.PLAYING) {
            BufferedImage screenshot = actionRobotWrapper.doScreenShot();
            Vision vision = new Vision(screenshot);
            // TODO what is this for?
            List<Point> traj = vision.findTrajPoints();
            Point releasePoint = new Point(shot.getDx() + shot.getX(), shot.getDy() + shot.getY());
            tp.adjustTrajectory(traj, sling, releasePoint);
        }
        // TODO wait for shot to end
        return true;
    }

    /**
     * @param shots that will be performed
     * @param sling used to update trajectoryPlanner tp
     * @return returns true if all shots have been successful
     */
    @SuppressWarnings("unused")
    private boolean performShots(Collection<Shot> shots, Rectangle sling) {
        boolean allShotsSuccessful = true;
        for (Shot shot : shots) {
            allShotsSuccessful = allShotsSuccessful && performShot(shot, sling);
        }
        return true;
    }

    public GameState solve() {
    	// click in the center of the scene (or at 100,100 when not running in
    	// client mode) to close the side menu, then wait some time for menu to
    	// close
    	// double click to avoid confusion of the vision component (single click
    	// shifts focus)
    	actionRobotWrapper.click();
    	actionRobotWrapper.click();
    	try {
    		Thread.sleep(1000);
    	}
    	catch (InterruptedException ie) {
    		// do nothing...
    	}

        // capture Image
        BufferedImage screenshot = actionRobotWrapper.doScreenShot();

        // process image
        Vision vision = new Vision(screenshot);

        // find the slingshot
        Rectangle sling        = vision.findSlingshotMBR();
        List<ABObject> newPigs = vision.findPigsRealShape();

        // confirm the slingshot
        while (sling == null && actionRobotWrapper.getState() == GameState.PLAYING) {
            System.out.println("SimulationAgent.solve(): No slingshot detected. Please remove pop up or zoom out");
            actionRobotWrapper.fullyZoomOut();
            screenshot = actionRobotWrapper.doScreenShot();
            vision = new Vision(screenshot);
            sling = vision.findSlingshotMBR();
        }

        // if there is a sling, then play, otherwise just skip.
        // due to the while statement this shouldn't be necessary
        if (sling != null && newPigs.size() > 0) {
            GenericLevel level = createLevel(screenshot);
            Map<Point, Body> targets = findTargets(level);
            List<Shot> shots = computeShots(targets, sling);
            Optional<Shot> bestShot = findBestShot(level, shots);
            if (bestShot.isPresent()) {
                if (performShot(bestShot.get(), sling)) {
                	System.out.println("Showing best shot: " + bestShot.get());
                	// set last parameter to false to not close on finish and hence
                	// freeze execution after finding the first best shot
                	SimulationManager.simulate(() -> new TestShotLevel(level, bestShot.get()), showSimulation, closeAfterSim);
                }
                else {
                	System.out.println("Pigs disappeared while computing a new shot...");
                	return(actionRobotWrapper.getState());
                }
            }
        }
        return actionRobotWrapper.getState();
    }

    public static void main(String args[]) {
        Getopt go = new Getopt("SimulationAgent", args, "i:t:sc");

        int c;
        String ip = null;
        // we will get a teamId when participating in the comp
        String teamId = "4711";
       
        boolean showSim = false;
        boolean closeAfterSim = true;

        while ((c = go.getopt()) != -1) {
            switch (c) {
            case 'i':
                ip = go.getOptarg();
                break;

            case 't':
                teamId = go.getOptarg();
                break;
                
            case 's':
            	showSim = true;
            	break;
            	
            case 'c':
            	closeAfterSim = false;
            	break;
            	
            case '?':
                // System.out.println("Unknonwn option " + c);
                break;

            }
        }

        SimulationAgent na;
        if (null != ip)
            na = new SimulationAgent(ip, Integer.valueOf(teamId), showSim, closeAfterSim);
        else
            na = new SimulationAgent(showSim, closeAfterSim);

        na.run();

    }

}
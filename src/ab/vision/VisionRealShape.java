/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ab.demo.other.ActionRobot;
import ab.vision.real.ConnectedComponent;
import ab.vision.real.ImageSegmenter;
import ab.vision.real.shape.Body;
import ab.vision.real.shape.Circle;

public class VisionRealShape {
    // offset constants for calculating reference point
    private static double X_OFFSET = 0.188;
    private static double Y_OFFSET = 0.156;
    private final static int unassigned = -1;
    // image segmenter
    private ImageSegmenter _seg;

    // all connected components in the scene
    private ArrayList<ConnectedComponent> _components = null;

    // detected game objects
    private Rectangle _sling = null;
    private Queue<Circle> _birds = null;

    // connected component and shapes for drawing purposes
    private ArrayList<ConnectedComponent> _draw = null;
    private ArrayList<Body> _drawShape = null;

    // the reference point (point birds are launched from)
    private Point _ref = null;

    // size of the screen
    private int _width = 0;
    private int _height = 0;

    // ground level
    private int _ground = 0;

    public VisionRealShape(BufferedImage screenshot) {
        // initialise screen size
        _width = screenshot.getWidth();
        _height = screenshot.getHeight();

        // Reset object counter
        ABObject.resetCounter();

        // find ground level and all connected components in scene
        _seg = new ImageSegmenter(screenshot);
        _ground = _seg.findGroundLevel();
        _components = _seg.findComponents();

        // initialise drawing objects
        _draw = new ArrayList<ConnectedComponent>();
        _drawShape = new ArrayList<Body>();

        // find the slingshot and reference point
        findSling();

    }

    // find the slingshot
    public Rectangle findSling() {
        if (_sling != null)
            return _sling;

        // use the highest sling typed component: e.g. frame
        int minY = 999999;
        ConnectedComponent sling = null;
        for (ConnectedComponent c : _components) {
            int top = c.boundingBox()[1];
            int left = c.boundingBox()[0];
            if (c.getType() == ImageSegmenter.SLING && top < minY && left < 300) {
                minY = top;
                sling = c;
            }
        }
        if (sling == null)
            return null;

        _draw.add(sling);
        _drawShape.add(sling.getBody());

        // find bounding box of the slingshot and reference point
        int bound[] = sling.boundingBox();
        _sling = new Rectangle(bound[0], bound[1], bound[2] - bound[0], bound[3] - bound[1]);
        _ref = new Point();
        _ref.x = (int) (_sling.x + _sling.height * X_OFFSET);
        _ref.y = (int) (_sling.y + _sling.height * Y_OFFSET);

        return _sling;
    }

    // find all birds in the scene, listed from left to right
    public Queue<Circle> findBirds() {
        if (_birds != null)
            return _birds;
        if (_sling == null) {
            System.out.println("findBirds(): no sling detected.");
        	return null;
        }

        _birds = new LinkedList<Circle>();

        // scan the birds from left to right
        int xMax = -2;
        final int BIRD_DISTANCE = 150;
        for (ConnectedComponent c : _components) {
            if (c.getType() > ImageSegmenter.SLING && c.getType() < ImageSegmenter.PIG) {
                int bound[] = c.boundingBox();

                // exit if next component is far from the last bird detected
                if (bound[2] > _width / 2 || (xMax != -2 && bound[0] - xMax > BIRD_DISTANCE))
                    break;

                // add if not overlapping with previous bird
                if ((bound[0] + bound[2]) / 2 > xMax + 1) {
                    Circle b = (Circle) c.getBody();
                    _birds.add(b);
                    _draw.add(c);
                    _drawShape.add(b);
                    xMax = bound[2];
                }
            }
        }
        // System.out.println(_birds.size() + " birds found");
        return _birds;
    }

    public List<ABObject> findPigs() {
        int xMin = 0;
        if (_sling != null)
            xMin = _sling.x + 100;
        List<ABObject> pigs = new LinkedList<ABObject>();
        for (ConnectedComponent c : _components) {
            if (c.getType() == ImageSegmenter.PIG) {
                Body b = c.getBody();
                if (b == null || (b.centerX < xMin))
                    continue;
                pigs.add(b);
                _draw.add(c);
                _drawShape.add(b);
            }

        }
        return pigs;
    }

    public List<ABObject> findHills() {
        int xMin = 0;
        if (_sling != null)
            xMin = _sling.x + 100;
        List<ABObject> hills = new LinkedList<ABObject>();
        for (ConnectedComponent c : _components) {
            if (c.getType() == ImageSegmenter.HILLS) {
                Body b = c.getBody();
                if (b == null || (b.centerX < xMin))
                    continue;
                hills.add(b);
                _draw.add(c);
                _drawShape.add(b);
            }

        }
        return hills;
    }

    // find all objects in the scene beside slingshot, birds, pigs. and hills.
    public List<ABObject> findObjects() {
        int xMin = 0;
        if (_sling != null)
            xMin = _sling.x + 100;

        List<ABObject> blocks = new LinkedList<ABObject>();
        for (ConnectedComponent c : _components) {
            if ((c.getType() > ImageSegmenter.PIG && c.getType() <= ImageSegmenter.DUCK)) {
                Body b = c.getBody();
                if (b == null || (b.centerX < xMin))
                    continue;
                blocks.add(b);
                _draw.add(c);
                _drawShape.add(b);

            }

        }
        return blocks;
    }

    // find the trajectory points
    public ArrayList<Point> findTrajectory() {
        if (_sling == null)
            return null;

        ArrayList<ConnectedComponent> traj = _seg.findTrajectory();
        ArrayList<Point> pts = new ArrayList<Point>();

        // use distance from previous point to remove noise
        final int THRESHOLD = 30;
        final int TAP_SIZE = 20;
        final int MAX_ERROR = 3;
        Point prev = _ref;
        for (ConnectedComponent c : traj) {
            int bound[] = c.boundingBox();

            // validate bounding box is roughly a square
            if (Math.abs((bound[2] - bound[0]) - (bound[3] - bound[1])) > MAX_ERROR)
                continue;

            // add the point if it is close to the previous trajectory point
            Point np = new Point((bound[0] + bound[2]) / 2, (bound[1] + bound[3]) / 2);
            if (np.x > _sling.x && distance(prev, np) < THRESHOLD) {
                pts.add(np);
                prev = np;
                _draw.add(c);
                _drawShape.add(c.getBody());

                // break if the tap point is found (special ability is used)
                if (c.getArea() > TAP_SIZE)
                    break;
            }
        }
        return pts;
    }

    // return the reference point
    public Point getReferencePoint() {
        return _ref;
    }

    // return the scene scale
    public int getSceneScale() {
        return _sling.height;
    }

    // calculate Euclidean distance between two points
    public static double distance(Point p1, Point p2) {
        int x = p1.x - p2.x;
        int y = p1.y - p2.y;
        return Math.sqrt(x * x + y * y);
    }

    public static void main(String args[]) {
        new ActionRobot();
        BufferedImage screenshot = ActionRobot.doScreenShot();
        Vision vision = new Vision(screenshot);
        // List<ABObject> objs = vision.findBlocksRealShape();
        // List<ABObject> objs = vision.findHills();
        Rectangle obj = vision.findSlingshotMBR();
        System.out.println(obj);
//		for (ABObject obj : objs)
//			System.out.println(obj);
    }
}

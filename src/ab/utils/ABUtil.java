package ab.utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.Vision;

public class ABUtil {

    // vision tolerance for recognizing neighbors
    public static int gap = 5;

    private static TrajectoryPlanner tp = new TrajectoryPlanner();

    // If o1 supports o2, return true
    public static boolean isSupport(ABObject o2, ABObject o1) {
        Rectangle mbr_o1 = o1.getBbox();
        Rectangle mbr_o2 = o2.getBbox();

        // identical objects
        if (mbr_o2.x == mbr_o1.x && mbr_o2.y == mbr_o1.y && mbr_o2.width == mbr_o1.width
                && mbr_o2.height == mbr_o1.height) {
            return false;
        }

        // compute left bounds of object
        int ex_o1 = mbr_o1.x + mbr_o1.width;
        int ex_o2 = mbr_o2.x + mbr_o2.width;

        int ey_o2 = mbr_o2.y + mbr_o2.height;

        // for a sketch of the supporting criteria see the doc folder
        // +/- 5 px around the lower vertical bound of o2
        if ((Math.abs(ey_o2 - mbr_o1.y) < gap) && !(mbr_o2.x - ex_o1 > gap || mbr_o1.x - ex_o2 > gap)) {
            return true;
        }

        return false;
    }

    // Return a link list of ABObjects that support o1 (test by isSupport function).
    // objs refers to a list of potential supporters.
    // Empty list will be returned if there are no supporters.
    public static List<ABObject> getSupporters(ABObject o2, List<ABObject> objs) {
        List<ABObject> result = new LinkedList<ABObject>();
        // Loop through the potential supporters
        for (ABObject o1 : objs) {
            if (isSupport(o2, o1))
                result.add(o1);
        }

        return result;
    }

    // Return true if the target can be hit by releasing the bird at the specified
    // release point
    public static boolean isReachable(Vision vision, Point target, Shot shot) {
        // test whether the trajectory can pass the target without considering
        // obstructions
        Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy());
        int traY = tp.getYCoordinate(vision.findSlingshotMBR(), releasePoint, target.x);
        if (Math.abs(traY - target.y) > 100) {
            // System.out.println(Math.abs(traY - target.y));
            return false;
        }

        boolean result = true;
        List<Point> points = tp.predictTrajectory(vision.findSlingshotMBR(), releasePoint);

        for (Point point : points) {
            if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400)
                for (ABObject ab : vision.findBlocksMBR()) {
                    Rectangle mbr = ab.getBbox();
                    if (((mbr.contains(point) && !mbr.contains(target))
                            || Math.abs(vision.getMBRVision()._scene[point.y][point.x] - 72) < 10)
                            && point.x < target.x) {
                        return false;
                    }
                }

        }
        return result;
    }
}

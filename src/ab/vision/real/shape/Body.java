/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision.real.shape;

import java.awt.Point;

import ab.vision.ABObject;

public abstract class Body extends ABObject {
    // position (x, y) as center of the object
    public double centerX = 0;
    public double centerY = 0;

    public Body() {

    }

    /**
     * Rounds a double and returns an integer (which differs from Math.round() which
     * returns a double)
     * 
     * @param i double value to round to integer values
     * @return rounded value as primitive integer
     */
    public static int round(double i) {
        return (int) (i + 0.5);
    }

    /**
     * Converts an angle given as radian into degrees.
     * 
     * @param rad
     * @return
     */
    public static double deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public Point getCenter() {
        Point point = new Point();
        point.setLocation(centerX, centerY);
        return point;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }
}

/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision.real.shape;

import java.awt.Rectangle;
import java.util.Locale;

import ab.vision.ABShape;
import ab.vision.ABType;

public class Circle extends Body {
    // radius of the circle
    public double r;

    /*
     * Create a new circle
     * 
     * @param xs, ys - coordinate of the circle centre radius - circle radius t -
     * type of the object
     */
    public Circle(double xs, double ys, double radius, ABType type) {
        centerX = xs;
        centerY = ys;
        r = radius;
        shape = ABShape.Circle;
        Rectangle bounds = new Rectangle((int) (xs - r * Math.sin(Math.PI / 4)), (int) (ys - r * Math.sin(Math.PI / 4)),
                (int) (2 * r * Math.sin(Math.PI / 4)), (int) (2 * r * Math.sin(Math.PI / 4)));
        this.type = type;
        angle = 0;
        area = (int) (Math.PI * r * r);
        this.bbox.setBounds(bounds);

    }

    public Circle(int box[], ABType type) {
        centerX = (box[0] + box[2]) / 2.0;
        centerY = (box[1] + box[3]) / 2.0;
        r = (box[2] - box[0] + box[3] - box[1]) / 4.0;
        area = (int) (Math.PI * r * r);
        Rectangle bounds = new Rectangle((int) (centerX - r * Math.sin(Math.PI / 4)),
                (int) (centerY - r * Math.sin(Math.PI / 4)), (int) (2 * r * Math.sin(Math.PI / 4)),
                (int) (2 * r * Math.sin(Math.PI / 4)));
        angle = 0;
        this.type = type;
        this.bbox.setBounds(bounds);
    }

    public String toString() {
        return String.format(Locale.US, "Circle(%f, %f, %f, ABType.%s) with id:%d", centerX, centerY, r, type, id);
    }
}

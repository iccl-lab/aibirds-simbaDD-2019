/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision.real.shape;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Locale;

import org.jbox2d.common.Vec2;

import ab.simulation.utils.Constants;
import ab.vision.ABType;

public class Rect extends Body {
    // polygon that describes the exact solid if angled
    public Polygon p;

    // width and height of the rectangle (real, not bounding box)
    protected double pwidth = -1, plength = -1;

    public Rect(double xs, double ys, double w, double h, double theta, ABType type) {
//        if (h >= w) {
        angle = theta;
        pwidth = w;
        plength = h;
//        }
//        else {
//            angle   = theta + Math.PI / 2;
//            pwidth  = h;
//            plength = w;
//        }

        centerY = ys;
        centerX = xs;

        area = (int) (pwidth * plength);
        this.type = type;

        createPolygon();
        this.bbox.setBounds(p.getBounds());
        this.bbox.width = p.getBounds().width;
        this.bbox.height = p.getBounds().height;
    }

    public Rect(int box[], ABType type) {
        centerX = (box[0] + box[2]) / 2.0;
        centerY = (box[3] + box[1]) / 2.0;
        pwidth = box[2] - box[0];
        plength = box[3] - box[1];
        angle = Math.PI / 2;

        if (plength < pwidth) {
            pwidth = plength;
            plength = box[2] - box[0];
            angle = 0;
        }

        this.bbox.width = (int) pwidth;
        this.bbox.height = (int) plength;

        this.type = type;

        area = this.bbox.width * this.bbox.height;
        createPolygon();
    }

    private void createPolygon() {
        double angle1 = angle;
        double angle2 = perpendicular(angle1);

        // starting point for drawing
        double _xs, _ys;
        _ys = centerY + Math.sin(angle) * plength / 2 + Math.sin(Math.abs(Math.PI / 2 - angle)) * pwidth / 2;
        if (angle < Math.PI / 2)
            _xs = centerX + Math.cos(angle) * plength / 2 - Math.sin(angle) * pwidth / 2;
        else if (angle > Math.PI / 2)
            _xs = centerX + Math.cos(angle) * plength / 2 + Math.sin(angle) * pwidth / 2;
        else
            _xs = centerX - pwidth / 2;

        p = new Polygon();
        p.addPoint(round(_xs), round(_ys));

        _xs -= Math.cos(angle1) * plength;
        _ys -= Math.sin(angle1) * plength;
        p.addPoint(round(_xs), round(_ys));

        _xs -= Math.cos(angle2) * pwidth;
        _ys -= Math.sin(angle2) * pwidth;
        p.addPoint(round(_xs), round(_ys));

        _xs += Math.cos(angle1) * plength;
        _ys += Math.sin(angle1) * plength;
        p.addPoint(round(_xs), round(_ys));
    }

    /**
     * Returns the real bound of this object. In this case this is a Rectangle with
     * pwidth and pheight as dimensions.
     */
    @Override
    public Rectangle getBounds() {
        return (new Rectangle((int) pwidth, (int) plength));
    }

    public double getpWidth() {
        if (pwidth != -1)
            return pwidth;
        return this.bbox.width;
    }

    public double getpLength() {
        if (plength != -1)
            return plength;
        return this.bbox.height;
    }

    public static double perpendicular(double angle) {
        return angle > Math.PI / 2 ? angle - Math.PI / 2 : angle + Math.PI / 2;
    }

    public String toString() {

        return String.format(
                Locale.US,
                "Rect(%f, %f, %f, %f, %f, ABType.%s) with id:%d",
                centerX,
                centerY,
                pwidth,
                plength,
                angle,
                type,
                id);
    }

    /**
     * This function returns the more accurate exact polygon instead of the bounding
     * box polygon. As usual, all polygon values are relative to the body's center.
     */
    @Override
    public Vec2[] getVec2Polygon() {
        // it is sure that the polygon has 4 vertices since this is a rectangle
        Vec2[] vertices = new Vec2[p.npoints];

        float center_x = this.bbox.x + this.bbox.width / 2f;
        float center_y = this.bbox.y + this.bbox.height / 2f;

        for (int i = 0; i < p.npoints; i++) {
            // I have no idea why the operands in the 1st equation must be
            // swapped (otherwise the objects are mirrored along an axis
            // which is parallel to the y axis but goes through the object
            // center), but anyway this works out fine.
            float x_i = (center_x - p.xpoints[i]) / Constants.PIXEL_PER_METER;
            float y_i = (p.ypoints[i] - center_y) / Constants.PIXEL_PER_METER;

            vertices[i] = new Vec2(x_i, y_i);
        }

        return (vertices);
    }
}

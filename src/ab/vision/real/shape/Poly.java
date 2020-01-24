/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jbox2d.common.Vec2;

import ab.simulation.utils.Constants;
import ab.vision.ABShape;
import ab.vision.ABType;
import ab.vision.real.ImageSegmenter;
import ab.vision.real.LineSegment;

public class Poly extends Body {
    public Polygon polygon = null;

    public Poly(ArrayList<LineSegment> lines, int left, int top, ABType type, double xs, double ys) {
        polygon = new Polygon();
        shape = ABShape.Poly;
        if (lines != null) {
            for (LineSegment l : lines) {
                Point start = l._start;
                polygon.addPoint(start.x + left, start.y + top);
            }
        }
        centerX = xs;
        centerY = ys;
        angle = 0;
        area = getBounds().height * getBounds().width;
        this.type = type;
        this.bbox.setBounds(polygon.getBounds());
    }

    public void draw(Graphics2D g, boolean fill, Color boxColor, boolean stat) {
        if (fill) {
            g.setColor(ImageSegmenter._colors[type.id]);
            g.fillPolygon(polygon);
        } else {
            g.setColor(boxColor);
            g.drawPolygon(polygon);
        }
    }

    public String toString() {
        List<Point> points = new ArrayList<Point>(polygon.npoints);
        for (int i = 0; i < polygon.npoints; i++) {
            points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
        }
        return String.format(
                Locale.US,
                "Poly(%s, %d, %d, %s, %f, %f): with id:%d",
                points.toString(),
                0,
                0,
                type,
                centerX,
                centerY,
                id);
    }

    /**
     * This function returns the more accurate exact polygon instead of the bounding
     * box polygon. As usual, all polygon values are relative to the body's center.
     * If this polygon has more than eight vertices, the bounding box implementation
     * of the superclass is returned because jbox2d can not handle polygon fixtures
     * with more than eight vertices.
     */
    @Override
    public Vec2[] getVec2Polygon() {
        if (polygon.npoints > 8) {
            return (super.getVec2Polygon());
        }

        // vector array for final solution
        Vec2[] vertices = new Vec2[polygon.npoints];

        float center_x = 0;
        float center_y = 0;

        for (int i = 0; i < polygon.npoints; i++) {
            center_x += (float) polygon.xpoints[i];
            center_y += (float) polygon.ypoints[i];
        }
        center_x /= polygon.npoints;
        center_y /= polygon.npoints;

        for (int i = 0; i < polygon.npoints; i++) {
            // I have no idea why the operands in the 1st equation must be
            // swapped (otherwise the objects are mirrored along an axis
            // which is parallel to the y axis but goes through the object
            // center), but anyway this works out fine.
            float x_i = (center_x - polygon.xpoints[i]) / Constants.PIXEL_PER_METER;
            float y_i = (polygon.ypoints[i] - center_y) / Constants.PIXEL_PER_METER;

            vertices[i] = new Vec2(x_i, y_i);
        }

        return (vertices);
    }
}

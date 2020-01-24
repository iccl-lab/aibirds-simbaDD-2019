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
import java.util.LinkedList;
import java.util.List;

import org.jbox2d.common.Vec2;

import ab.simulation.utils.Constants;

public class ABObject {
    /* vision tolerance used for computing exact intersections. */
    public static final int VSN_TOLERANCE = 2;

    public static final int ABO_LEFT = 0;
    public static final int ABO_RIGHT = 1;
    public static final int ABO_TOP = 2;
    public static final int ABO_BOTTOM = 3;

    // bounding box of object
    protected Rectangle bbox;

    // global counter to assign unique block id to object, incremented whenever
    // a new ABObject instance in created.
    private static int counter = 0;

    // id of this object
    public int id;
    // object type
    public ABType type;

    public int area = 0;

    // shape is Rect by default.
    public ABShape shape = ABShape.Rect;

    // angle is 0 by default.
    public double angle = 0;

    // is hollow or not
    public boolean hollow = false;

    // list of object this thing is based on
    protected List<ABObject> supporters;

    /**
     * Constructor for creating an ABObject without any knowledge about the actual
     * shape. Only the type and the bounding box will be set.
     * 
     * @param mbr  - bounding box of the object.
     * @param type - type of the object (pig, bird, etc.)
     */
    public ABObject(Rectangle mbr, ABType type) {
        this.bbox = new Rectangle(mbr);
        this.type = type;
        this.id = counter++;
        this.supporters = new LinkedList<ABObject>();
    }

    public ABObject() {
        this.bbox = new Rectangle(); // construct default rectangle
        this.id = counter++;
        this.type = ABType.UNKNOWN;
        this.supporters = new LinkedList<ABObject>();
    }

    public ABType getType() {
        return type;
    }

    public Point getCenter() {
        // default implementation: use center point of bounding box
        return new Point((int) bbox.getCenterX(), (int) bbox.getCenterY());
    }

    /**
     * Resets the global ID counter.
     */
    public static void resetCounter() {
        counter = 0;
    }

    public Rectangle getBbox() {
        return (this.bbox);
    }

    public Rectangle getBounds() {
        return (this.bbox.getBounds());
    }

    /**
     * Default method: return vertices of bounding box.
     * 
     * @return
     */
    public Vec2[] getVec2Polygon() {
        Vec2[] vertices = new Vec2[4];
        float real_width = bbox.width / Constants.PIXEL_PER_METER;
        float real_height = bbox.height / Constants.PIXEL_PER_METER;

        vertices[0] = new Vec2(-1 * real_width / 2, -1 * real_height / 2);
        vertices[1] = new Vec2(-1 * real_width / 2, real_height / 2);
        vertices[2] = new Vec2(real_width / 2, real_height / 2);
        vertices[3] = new Vec2(real_width / 2, -1 * real_height / 2);

        return (vertices);
    }
}

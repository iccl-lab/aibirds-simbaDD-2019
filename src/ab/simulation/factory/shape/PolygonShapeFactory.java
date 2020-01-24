package ab.simulation.factory.shape;

import java.awt.Polygon;
import java.util.LinkedList;
import java.util.List;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

import ab.simulation.factory.ShapeFactory;
import ab.simulation.utils.SimUtil;
import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.real.shape.Poly;

public class PolygonShapeFactory extends ShapeFactory {

    public static List<Shape> createShapes(ABObject object, float PPM) {
        List<Shape> shapes = new LinkedList<Shape>();
        Poly obj = (Poly) object;
        if (object.shape != ABShape.Poly) {
            System.out.println("but this is a Polygon!");
        }
        Vec2 center = SimUtil.convertViewToModel(new Vec2(object.getCenter().x, object.getCenter().y), PPM);
        Polygon p = obj.polygon;
        List<Vec2> vertices = new LinkedList<Vec2>();
        for (int i = 0; i < p.npoints; i++) {
            Vec2 point = SimUtil.convertViewToModel(new Vec2(p.xpoints[i], p.ypoints[i]), PPM);
            vertices.add(point.subLocal(center));
        }
        List<List<Vec2>> tesselatedPolygon = new LinkedList<List<Vec2>>();
        tesselate(vertices, tesselatedPolygon);
        for (List<Vec2> points : tesselatedPolygon) {
            PolygonShape shape = new PolygonShape();
            Vec2[] polygonPoints = new Vec2[points.size()];
            points.toArray(polygonPoints);
            shape.set(polygonPoints, points.size());
            shapes.add(shape);
        }
        return shapes;
    }

    private static void tesselate(List<Vec2> vertices, List<List<Vec2>> tesselatedPolygons) {
        int[] split_points = {
                0,
                0 };
        boolean split_found = false;
        for (int i = 0; i < vertices.size() && !split_found; i++) {
            // vector to the previous point
            Vec2 vpp = vertices.get(Math.floorMod(i - 1, vertices.size())).sub(vertices.get(i));
            // vector to the next point
            Vec2 vnp = vertices.get(Math.floorMod(i + 1, vertices.size())).sub(vertices.get(i));
            if (vnp.x * vpp.y - vnp.y * vpp.x < 0) {
                // concave -> split up
                for (int j = 2; j < vertices.size() && !split_found; j++) {
                    // potential splitting vector
                    Vec2 psv = vertices.get(Math.floorMod(i + j, vertices.size())).sub(vertices.get(i));

                    // check if split would create polygon with negative area
                    if (vnp.x * psv.y - vnp.y * psv.x < 0) {
                        // split would create polygon with negative area
                        continue;
                    }
                    boolean is_valid_split = true;

                    // check if split would create two overlapping polygons
                    // check polygon1 between i and i+j
                    for (int k = 2; k < j && is_valid_split; k++) {
                        // potentially self-cutting edge
                        Vec2 sce = vertices.get(Math.floorMod(i + k, vertices.size())).sub(vertices.get(i));

                        float p_sce = (sce.x * psv.x + sce.y * psv.y) / psv.length();
                        if (0 < p_sce && p_sce < psv.length()) {
                            // sce could be a self-cutting edge

                            Vec2 rejection = sce.sub(psv.mul(p_sce / psv.length()));
                            if ((rejection.x * psv.x + rejection.y * psv.y) > 0) {
                                is_valid_split = false;
                            }
                        }
                    }
                    if (!is_valid_split)
                        continue;
                    // check polygon2 between i+j and i
                    for (int k = 1; k < vertices.size() - i - j && is_valid_split; k++) {
                        // potentially self-cutting edge
                        Vec2 sce = vertices.get(Math.floorMod(i + j + k, vertices.size())).sub(vertices.get(i));

                        float p_sce = (sce.x * psv.x + sce.y * psv.y) / psv.length();
                        if (!(p_sce < 0 || psv.length() < p_sce)) {
                            // sce could be a self-cutting edge

                            Vec2 rejection = sce.sub(psv.mul(p_sce / psv.length()));
                            if ((rejection.x * psv.x + rejection.y * psv.y) < 0) {
                                is_valid_split = false;
                            }
                        }
                    }
                    if (is_valid_split) {
                        split_found = true;
                        int p = Math.floorMod(i + j, vertices.size());
                        if (i < p) {
                            split_points[0] = i;
                            split_points[1] = p;
                        } else {
                            split_points[0] = p;
                            split_points[1] = i;
                        }
                    }
                }
            }
        }
        if (split_points[0] == split_points[1])
            tesselatedPolygons.add(vertices);
        else {
            List<Vec2> polygon1 = new LinkedList<Vec2>();
            List<Vec2> polygon2 = new LinkedList<Vec2>();
            for (int i = split_points[0]; i < split_points[1] + 1; i++) {
                polygon1.add(vertices.get(i));
            }
            for (int i = 0; i < split_points[0] + 1; i++) {
                polygon2.add(vertices.get(i));
            }
            for (int i = split_points[1]; i < vertices.size(); i++) {
                polygon2.add(vertices.get(i));
            }
            tesselate(polygon1, tesselatedPolygons);
            tesselate(polygon2, tesselatedPolygons);
        }
    }
}

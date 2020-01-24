package ab.simulation.utils;

import java.awt.Point;

import org.jbox2d.common.Vec2;

public class SimUtil {

    public static Vec2 convertVectorViewToModel(Vec2 v, float PPM) {
        Vec2 vec2 = new Vec2(v.x, -v.y);
        return vec2.mulLocal(1.0f / PPM);
    }

    public static Vec2 convertVectorModelToView(Vec2 v, float PPM) {
        Vec2 vec2 = new Vec2(v.x, -v.y);
        return vec2.mulLocal(PPM);
    }

    /**
     * <b>Model</b>: origin in the <b>bottom</b> left corner, y-axis points
     * <b>upwards</b> <br>
     * <b>View</b>: origin in the <b>top</b> left corner, y-axis points
     * <b>downwards</b>
     * 
     * @param v the vector that gets converted
     * @return the converted vector
     */
    public static Vec2 convertModelToView(Vec2 v, float PPM) {
        Vec2 tmp = new Vec2(0.0f, (float) Constants.WINDOW_HEIGHT).subLocal(v.mul(PPM));
        tmp.x *= -1.0f;
        return tmp;
    }

    /**
     * <b>Model</b>: origin in the <b>bottom</b> left corner, y-axis points
     * <b>upwards</b> <br>
     * <b>View</b>: origin in the <b>top</b> left corner, y-axis points
     * <b>downwards</b>
     * 
     * @param v the vector that gets converted
     * @return the converted vector
     */
    public static Vec2 convertViewToModel(Vec2 v, float PPM) {
        Vec2 tmp = new Vec2(0.0f, (float) Constants.WINDOW_HEIGHT).subLocal(v).mulLocal(1.0f / PPM);
        tmp.x *= -1.0f;
        return tmp;
    }

    /**
     * <b>Model</b>: origin in the <b>bottom</b> left corner, y-axis points
     * <b>upwards</b> <br>
     * <b>View</b>: origin in the <b>top</b> left corner, y-axis points
     * <b>downwards</b>
     * 
     * @param p the point that gets converted
     * @return the converted vector
     */
    public static Vec2 convertViewToModel(Point p, float PPM) {
        Vec2 v = new Vec2(p.x, p.y);
        Vec2 tmp = new Vec2(0.0f, (float) Constants.WINDOW_HEIGHT).subLocal(v).mulLocal(1.0f / PPM);
        tmp.x *= -1.0f;
        return tmp;
    }

}

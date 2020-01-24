package ab.simulation.view.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.IViewportTransform;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.pooling.arrays.IntArray;
import org.jbox2d.pooling.arrays.Vec2Array;

import ab.simulation.view.panel.SimulationPanel;

public class PrimitiveDrawer extends AbstractDrawer {
    public static int circlePoints = 13;
    public static final float edgeWidth = 0.02f;

    private final SimulationPanel panel;

    private final boolean yFlip;
    private final BasicStroke stroke;
    private final Shape circle;

    public PrimitiveDrawer(SimulationPanel argTestPanel, boolean yFlip) {
        panel = argTestPanel;
        this.yFlip = yFlip;
        stroke = new BasicStroke(0);
        circle = new Ellipse2D.Float(-1, -1, 2, 2);
    }

    @Override
    public void setViewportTransform(IViewportTransform viewportTransform) {
        super.setViewportTransform(viewportTransform);
        viewportTransform.setYFlip(yFlip);
    }

    @Override
    public void drawPoint(Vec2 argPoint, float argRadiusOnScreen, Color color) {
        getWorldToScreenToOut(argPoint, sp1);
        Graphics2D g = getGraphics();

        g.setColor(color);
        sp1.x -= argRadiusOnScreen;
        sp1.y -= argRadiusOnScreen;
        g.fillOval((int) sp1.x, (int) sp1.y, (int) argRadiusOnScreen * 2, (int) argRadiusOnScreen * 2);
    }

    private final Vec2 sp1 = new Vec2();
    private final Vec2 sp2 = new Vec2();

    @Override
    public void drawSegment(Vec2 p1, Vec2 p2, Color color) {
        getWorldToScreenToOut(p1, sp1);
        getWorldToScreenToOut(p2, sp2);

        Graphics2D g = getGraphics();
        g.setColor(color);
        g.setStroke(stroke);
        g.drawLine((int) sp1.x, (int) sp1.y, (int) sp2.x, (int) sp2.y);
    }

    private final Vec2Array vec2Array = new Vec2Array();

    public void drawAABB(AABB argAABB, Color color) {
        Vec2 vecs[] = vec2Array.get(4);
        argAABB.getVertices(vecs);
        drawPolygon(vecs, 4, color);
    }

    private final AffineTransform tr = new AffineTransform();
    private AffineTransform oldTrans = new AffineTransform();
    private Stroke oldStroke;

    private void saveState(Graphics2D g) {
        oldTrans = g.getTransform();
        oldStroke = g.getStroke();
    }

    private void restoreState(Graphics2D g) {
        g.setTransform(oldTrans);
        g.setStroke(oldStroke);
    }

    private void transformGraphics(Graphics2D g, Vec2 center) {
        Vec2 e = viewportTransform.getExtents();
        Vec2 vc = viewportTransform.getCenter();
        Mat22 vt = viewportTransform.getMat22Representation();

        int flip = yFlip ? -1 : 1;
        tr.setTransform(vt.ex.x, flip * vt.ex.y, vt.ey.x, flip * vt.ey.y, e.x, e.y);
        tr.translate(-vc.x, -vc.y);
        tr.translate(center.x, center.y);
        g.transform(tr);
    }

    @Override
    public void drawCircle(Vec2 center, float radius, Color color) {
        Graphics2D g = getGraphics();

        saveState(g);
        transformGraphics(g, center);
        g.setStroke(stroke);
        g.scale(radius, radius);
        g.setColor(color);
        g.drawOval(-1, -1, 2, 2);
        restoreState(g);
    }

    @Override
    public void drawCircle(Vec2 center, float radius, Vec2 axis, Color color) {
        Graphics2D g = getGraphics();
        saveState(g);
        transformGraphics(g, center);
        g.setStroke(stroke);

        g.scale(radius, radius);
        g.setColor(color);
        g.draw(circle);
        if (axis != null) {
            g.rotate(MathUtils.atan2(axis.y, axis.x));
            g.drawLine(0, 0, 1, 0);
        }
        restoreState(g);
    }

    @Override
    public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color color) {
        Graphics2D g = getGraphics();
        saveState(g);
        transformGraphics(g, center);
        g.setStroke(stroke);
        g.scale(radius, radius);
        g.setColor(color);
        g.fill(circle);
        Color negativeColor = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
        g.setColor(negativeColor);
        g.draw(circle);
        if (axis != null) {
            g.rotate(MathUtils.atan2(axis.y, axis.x));
            g.drawLine(0, 0, 1, 0);
        }
        restoreState(g);
    }

    private final Vec2 zero = new Vec2();
    private final Color pcolorA = new Color(1f, 1f, 1f, .4f);

    @Override
    public void drawParticles(Vec2[] centers, float radius, ParticleColor[] colors, int count) {
        Graphics2D g = getGraphics();
        saveState(g);
        transformGraphics(g, zero);
        g.setStroke(stroke);
        for (int i = 0; i < count; i++) {
            Vec2 center = centers[i];
            Color color;
            if (colors == null) {
                color = pcolorA;
            } else {
                ParticleColor c = colors[i];
                color = new Color(c.r * 1f / 127, c.g * 1f / 127, c.b * 1f / 127, c.a * 1f / 127);
            }
            AffineTransform old = g.getTransform();
            g.translate(center.x, center.y);
            g.scale(radius, radius);
            g.setColor(color);
            g.fill(circle);
            g.setTransform(old);
        }
        restoreState(g);
    }

    private final Color pcolor = new Color(1f, 1f, 1f, 1f);

    @Override
    public void drawParticlesWireframe(Vec2[] centers, float radius, ParticleColor[] colors, int count) {
        Graphics2D g = getGraphics();
        saveState(g);
        transformGraphics(g, zero);
        g.setStroke(stroke);
        for (int i = 0; i < count; i++) {
            Vec2 center = centers[i];
            Color color;
            // No alpha channel, it slows everything down way too much.
            if (colors == null) {
                color = pcolor;
            } else {
                ParticleColor c = colors[i];
                color = new Color(c.r * 1f / 127, c.g * 1f / 127, c.b * 1f / 127, 1);
            }
            AffineTransform old = g.getTransform();
            g.translate(center.x, center.y);
            g.scale(radius, radius);
            g.setColor(color);
            g.draw(circle);
            g.setTransform(old);
        }
        restoreState(g);
    }

    private final Vec2 temp = new Vec2();
    private final IntArray xIntsPool = new IntArray();
    private final IntArray yIntsPool = new IntArray();

    @Override
    public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color color) {
        Graphics2D g = getGraphics();
        saveState(g);
        int[] xInts = xIntsPool.get(vertexCount);
        int[] yInts = yIntsPool.get(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            getWorldToScreenToOut(vertices[i], temp);
            xInts[i] = (int) temp.x;
            yInts[i] = (int) temp.y;
        }
        g.setStroke(stroke);
        g.setColor(color);
        g.fillPolygon(xInts, yInts, vertexCount);
        // g.setColor(color);
        // g.drawPolygon(xInts, yInts, vertexCount);
        restoreState(g);
    }

    @Override
    public void drawPolygon(Vec2[] vertices, int vertexCount, Color color) {
        Graphics2D g = getGraphics();
        saveState(g);
        int[] xInts = xIntsPool.get(vertexCount);
        int[] yInts = yIntsPool.get(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            getWorldToScreenToOut(vertices[i], temp);
            xInts[i] = (int) temp.x;
            yInts[i] = (int) temp.y;
        }
        g.setStroke(stroke);
        g.setColor(color);
        g.drawPolygon(xInts, yInts, vertexCount);
        restoreState(g);
    }

    @Override
    public void drawString(float x, float y, String s, Color color) {
        Graphics2D g = getGraphics();
        if (g == null) {
            return;
        }
        g.setColor(color);
        g.drawString(s, x, y);
    }

    protected Graphics2D getGraphics() {
        return panel.getDBGraphics();
    }

    private final Vec2 temp2 = new Vec2();

    @Override
    public void drawTransform(Transform xf) {
        Graphics2D g = getGraphics();
        getWorldToScreenToOut(xf.p, temp);
        temp2.setZero();
        float k_axisScale = 0.4f;

        g.setColor(Color.RED);

        temp2.x = xf.p.x + k_axisScale * xf.q.c;
        temp2.y = xf.p.y + k_axisScale * xf.q.s;
        getWorldToScreenToOut(temp2, temp2);
        g.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y);

        g.setColor(Color.GREEN);
        temp2.x = xf.p.x + -k_axisScale * xf.q.s;
        temp2.y = xf.p.y + k_axisScale * xf.q.c;
        getWorldToScreenToOut(temp2, temp2);
        g.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y);
    }
}
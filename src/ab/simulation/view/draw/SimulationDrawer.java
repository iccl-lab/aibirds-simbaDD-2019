package ab.simulation.view.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Collision.PointState;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleSystem;
import org.jbox2d.pooling.IWorldPool;
import org.jbox2d.pooling.arrays.Vec2Array;
import org.jbox2d.testbed.framework.ContactPoint;
import org.jbox2d.testbed.framework.TestbedSettings;

import ab.simulation.model.SimulationUserData;
import ab.simulation.model.level.GenericLevel;
import ab.simulation.view.panel.DefaultSimulationViewColors;
import ab.simulation.view.panel.SimulationPanel;
import ab.simulation.view.settings.ViewSettings;
//import ab.test.simulation.model.TestSimulationModel;
//import ab.test.simulation.model.level.TestSimulationLevel;
import ab.vision.ABType;

public class SimulationDrawer extends PrimitiveDrawer {

    private final ViewSettings drawingSettings;

    public SimulationDrawer(ViewSettings drawingSettings, SimulationPanel panel, boolean yFlip) {
        super(panel, yFlip);
        this.drawingSettings = drawingSettings;
    }

    private Color getColor(Body body) {
        SimulationUserData simulationUserData = (SimulationUserData) body.getUserData();
        Color color;
        ABType type = ABType.UNKNOWN;
        if (this.drawingSettings.getSetting(ViewSettings.DrawABTypes).enabled) {
            if (simulationUserData != null)
            	type = simulationUserData.getABObject().getType();
            // else use unknown color
        }
        switch (type) {
        case SLING:
            color = DefaultSimulationViewColors.SLING_COLOR;
            break;
        case GROUND:
            color = DefaultSimulationViewColors.GROUND_COLOR;
            break;
        case HILL:
            color = DefaultSimulationViewColors.HILL_COLOR;
            break;
        case SUPPORT:
            color = DefaultSimulationViewColors.SUPPORT_COLOR;
            break;
        case ICE:
            color = DefaultSimulationViewColors.ICE_COLOR;
            break;
        case WOOD:
            color = DefaultSimulationViewColors.WOOD_COLOR;
            break;
        case STONE:
            color = DefaultSimulationViewColors.STONE_COLOR;
            break;
        case TNT:
            color = DefaultSimulationViewColors.TNT_COLOR;
            break;
        case RED_BIRD:
            color = DefaultSimulationViewColors.RED_BIRD_COLOR;
            break;
        case BLUE_BIRD:
            color = DefaultSimulationViewColors.BLUE_BIRD_COLOR;
            break;
        case YELLOW_BIRD:
            color = DefaultSimulationViewColors.YELLOW_BIRD_COLOR;
            break;
        case BLACK_BIRD:
            color = DefaultSimulationViewColors.BLACK_BIRD_COLOR;
            break;
        case WHITE_BIRD:
            color = DefaultSimulationViewColors.WHITE_BIRD_COLOR;
            break;
        case PIG:
            color = DefaultSimulationViewColors.PIG_COLOR;
            break;
        case UNKNOWN:
        default:
            if (body.isActive() == false) {
                color = new Color(0.5f, 0.5f, 0.3f);
            } else if (body.getType() == BodyType.STATIC) {
                color = new Color(0.5f, 0.9f, 0.3f);
            } else if (body.getType() == BodyType.KINEMATIC) {
                color = new Color(0.5f, 0.5f, 0.9f);
            } else if (body.isAwake() == false) {
                color = new Color(0.5f, 0.5f, 0.5f);
            } else {
                color = new Color(0.9f, 0.7f, 0.7f);
            }
            break;
        }
        return color;
    }

    public void drawQuadratic(Vec2 startPoint, Vec2 controlPoint, Vec2 endPoint, Color color) {
        Vec2 sP = new Vec2();
        Vec2 cP = new Vec2();
        Vec2 eP = new Vec2();
        getWorldToScreenToOut(startPoint, sP);
        getWorldToScreenToOut(controlPoint, cP);
        getWorldToScreenToOut(endPoint, eP);

        Path2D path2d = new Path2D.Float();
        path2d.moveTo(sP.x, sP.y);
        path2d.quadTo(cP.x, cP.y, eP.x, eP.y);

        Graphics2D g = getGraphics();
        // TODO set color based on parameter
        g.setColor(Color.GREEN);
        g.draw(path2d);
    }

    private final Color color1 = new Color(.3f, .95f, .3f);
    private final Color color2 = new Color(.3f, .3f, .95f);
    private final Color color3 = new Color(.9f, .9f, .9f);
    private final Color color4 = new Color(.6f, .61f, 1);
    private final Color color5 = new Color(.9f, .9f, .3f);
    private final Color mouseColor = new Color(0f, 1f, 0f);
    public static final int TEXT_LINE_SPACE = 13;
    public static final int TEXT_SECTION_SPACE = 3;
    protected int m_textLine;
    private final List<String> statsList = new ArrayList<String>();

//    @Deprecated
//    public void drawStuff(TestSimulationModel simulationModel) {
//        // TODO Auto-generated method stub
//        m_textLine = 20;
//
//        TestSimulationLevel testbedTest = simulationModel.getCurrTest();
//        String title = simulationModel.getCurrTest().getTestName();
//
////        if (title != null) {
////            drawString(testbedCamera.getTransform().getExtents().x, 15, title, Color3f.RED);
////            m_textLine += TEXT_LINE_SPACE;
////        }
//
//        TestbedSettings settings = simulationModel.getSettings();
//        if (settings.pause) {
//            drawString(5, m_textLine, "****PAUSED****", Color3f.WHITE);
//            m_textLine += TEXT_LINE_SPACE;
//        }
//
//        int flags = 0;
//        flags += settings.getSetting(TestbedSettings.DrawShapes).enabled ? DebugDraw.e_shapeBit : 0;
//        flags += settings.getSetting(TestbedSettings.DrawJoints).enabled ? DebugDraw.e_jointBit : 0;
//        flags += settings.getSetting(TestbedSettings.DrawAABBs).enabled ? DebugDraw.e_aabbBit : 0;
//        flags += settings.getSetting(TestbedSettings.DrawCOMs).enabled ? DebugDraw.e_centerOfMassBit : 0;
//        flags += settings.getSetting(TestbedSettings.DrawTree).enabled ? DebugDraw.e_dynamicTreeBit : 0;
//        flags += settings.getSetting(TestbedSettings.DrawWireframe).enabled ? DebugDraw.e_wireframeDrawingBit : 0;
//        setFlags(flags);
//
//        drawString(5, m_textLine, "Engine Info", color4);
//        m_textLine += TEXT_LINE_SPACE;
//        drawString(5, m_textLine, "Framerate: " + (int) simulationModel.getCalculatedFps(), Color3f.WHITE);
//        m_textLine += TEXT_LINE_SPACE;
//
//        World m_world = testbedTest.getWorld();
//        Vec2 mouseWorld = new Vec2(); // testbedTest.getWorldMouse();
//        if (settings.getSetting(TestbedSettings.DrawStats).enabled) {
//            int particleCount = m_world.getParticleCount();
//            int groupCount = m_world.getParticleGroupCount();
//            drawString(
//                    5,
//                    m_textLine,
//                    "bodies/contacts/joints/proxies/particles/groups = " + m_world.getBodyCount() + "/"
//                            + m_world.getContactCount() + "/" + m_world.getJointCount() + "/" + m_world.getProxyCount()
//                            + "/" + particleCount + "/" + groupCount,
//                    Color3f.WHITE);
//            m_textLine += TEXT_LINE_SPACE;
//
//            drawString(5, m_textLine, "World mouse position: " + mouseWorld.toString(), Color3f.WHITE);
//            m_textLine += TEXT_LINE_SPACE;
//
//            statsList.clear();
//            Profile p = m_world.getProfile();
//            p.toDebugStrings(statsList);
//
//            for (String s : statsList) {
//                drawString(5, m_textLine, s, Color3f.WHITE);
//                m_textLine += TEXT_LINE_SPACE;
//            }
//            m_textLine += TEXT_SECTION_SPACE;
//        }
//
//        if (settings.getSetting(TestbedSettings.DrawHelp).enabled) {
//            drawString(5, m_textLine, "Help", color4);
//            m_textLine += TEXT_LINE_SPACE;
//            List<String> help = simulationModel.getImplSpecificHelp();
//            for (String string : help) {
//                drawString(5, m_textLine, string, Color3f.WHITE);
//                m_textLine += TEXT_LINE_SPACE;
//            }
//            m_textLine += TEXT_SECTION_SPACE;
//        }
//
//        List<String> textList = new ArrayList<String>();
//        if (!textList.isEmpty()) {
//            drawString(5, m_textLine, "Test Info", color4);
//            m_textLine += TEXT_LINE_SPACE;
//            for (String s : textList) {
//                drawString(5, m_textLine, s, Color3f.WHITE);
//                m_textLine += TEXT_LINE_SPACE;
//            }
//            textList.clear();
//        }
//
//        drawString(5, m_textLine, "is finished: " + Boolean.toString(testbedTest.isFinished()), Color3f.WHITE);
//        drawString(
//                5,
//                m_textLine + TEXT_LINE_SPACE,
//                "is awake: " + Boolean.toString(testbedTest.isAwake()),
//                Color3f.WHITE);
//        drawString(5, m_textLine + 2 * TEXT_LINE_SPACE, "score: " + testbedTest.getScore(), Color3f.WHITE);
//
//    }

    private Color color;
    private final Transform xf = new Transform();
    private final Vec2 cA = new Vec2();
    private final Vec2 cB = new Vec2();
    private final Vec2Array avs = new Vec2Array();

    /**
     * Call this to draw shapes and other debug draw data.
     */
    public void drawLevel(GenericLevel level) {

        World world = level.getWorld();

        // Draw Shapes
        if (drawingSettings.getSetting(ViewSettings.DrawShapes).enabled) {
            drawShapes(world.getBodyList());

            // does this work?
            drawParticleSystem(new ParticleSystem(world));
        }

        // Draw Joints
        if (drawingSettings.getSetting(ViewSettings.DrawJoints).enabled) {
            IWorldPool pool = world.getPool();
            for (Joint j = world.getJointList(); j != null; j = j.getNext()) {
                drawJoint(j, pool);
            }
        }

        // Draw Pairs
        if (drawingSettings.getSetting(ViewSettings.DrawPairs).enabled) {
            color = new Color(0.3f, 0.9f, 0.9f);
            for (Contact c = world.getContactList(); c != null; c = c.getNext()) {
                Fixture fixtureA = c.getFixtureA();
                Fixture fixtureB = c.getFixtureB();
                fixtureA.getAABB(c.getChildIndexA()).getCenterToOut(cA);
                fixtureB.getAABB(c.getChildIndexB()).getCenterToOut(cB);
                drawSegment(cA, cB, color);
            }
        }

        // Draw AABBs
        if (drawingSettings.getSetting(ViewSettings.DrawAABBs).enabled) {
            color = new Color(0.9f, 0.3f, 0.9f);

            for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
                if (b.isActive() == false) {
                    continue;
                }

                for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
                    for (int i = 0; i < f.m_proxyCount; ++i) {
                        AABB aabb = f.getAABB(i);
                        if (aabb != null) {
                            Vec2[] vs = avs.get(4);
                            vs[0].set(aabb.lowerBound.x, aabb.lowerBound.y);
                            vs[1].set(aabb.upperBound.x, aabb.lowerBound.y);
                            vs[2].set(aabb.upperBound.x, aabb.upperBound.y);
                            vs[3].set(aabb.lowerBound.x, aabb.upperBound.y);
                            drawPolygon(vs, 4, color);
                        }
                    }
                }
            }
        }

        if (drawingSettings.getSetting(ViewSettings.DrawCOMs).enabled) {
            for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
                xf.set(b.getTransform());
                xf.p.set(b.getWorldCenter());
                drawTransform(xf);
            }
        }

        if (drawingSettings.getSetting(ViewSettings.DrawTree).enabled) {
            // world.getContactManager().m_broadPhase.drawTree(simulationDebugDraw);
        }

        if (drawingSettings.getSetting(TestbedSettings.DrawContactPoints).enabled) {
            drawContactPoints(level.getContactPoints());
        }

        flush();
    }

    private void drawShapes(Body bodyList) {
        boolean wireframe = drawingSettings.getSetting(ViewSettings.DrawWireframe).enabled;
        for (Body b = bodyList; b != null; b = b.getNext()) {
            Color color = getColor(b);
            xf.set(b.getTransform());
            for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
                drawShape(f, xf, color, wireframe);
            }
        }
    }

    private void drawContactPoints(ContactPoint[] points) {
        final float k_impulseScale = 0.1f;
        final float axisScale = 0.3f;

        for (int i = 0; i < points.length; i++) {

            ContactPoint point = points[i];

            if (point.state == PointState.ADD_STATE) {
                drawPoint(point.position, 10f, color1);
            } else if (point.state == PointState.PERSIST_STATE) {
                drawPoint(point.position, 5f, color2);
            }

            if (drawingSettings.getSetting(TestbedSettings.DrawContactNormals).enabled) {
                p1.set(point.position);
                p2.set(point.normal).mulLocal(axisScale).addLocal(p1);
                drawSegment(p1, p2, color3);

            } else if (drawingSettings.getSetting(TestbedSettings.DrawContactImpulses).enabled) {
                p1.set(point.position);
                p2.set(point.normal).mulLocal(k_impulseScale).mulLocal(point.normalImpulse).addLocal(p1);
                drawSegment(p1, p2, color5);
            }

            if (drawingSettings.getSetting(TestbedSettings.DrawFrictionImpulses).enabled) {
                Vec2.crossToOutUnsafe(point.normal, 1, tangent);
                p1.set(point.position);
                p2.set(tangent).mulLocal(k_impulseScale).mulLocal(point.tangentImpulse).addLocal(p1);
                drawSegment(p1, p2, color5);
            }
        }
    }

    private final Vec2 center = new Vec2();
    private final Vec2 axis = new Vec2();
    private final Vec2 v1 = new Vec2();
    private final Vec2 v2 = new Vec2();
    private final Vec2Array tlvertices = new Vec2Array();

    private void drawShape(Fixture fixture, Transform xf, Color color, boolean wireframe) {
        switch (fixture.getType()) {
        case CIRCLE: {
            CircleShape circle = (CircleShape) fixture.getShape();

            // Vec2 center = Mul(xf, circle.m_p);
            Transform.mulToOutUnsafe(xf, circle.m_p, center);
            float radius = circle.m_radius;
            xf.q.getXAxis(axis);

            if (wireframe) {
                drawCircle(center, radius, axis, color);
            } else {
                drawSolidCircle(center, radius, axis, color);
            }
        }
            break;

        case POLYGON: {
            PolygonShape poly = (PolygonShape) fixture.getShape();
            int vertexCount = poly.m_count;
            assert (vertexCount <= Settings.maxPolygonVertices);
            Vec2[] vertices = tlvertices.get(Settings.maxPolygonVertices);

            for (int i = 0; i < vertexCount; ++i) {
                // vertices[i] = Mul(xf, poly.m_vertices[i]);
                Transform.mulToOutUnsafe(xf, poly.m_vertices[i], vertices[i]);
            }
            if (wireframe) {
                drawPolygon(vertices, vertexCount, color);
            } else {
                drawSolidPolygon(vertices, vertexCount, color);
            }
        }
            break;
        case EDGE: {
            EdgeShape edge = (EdgeShape) fixture.getShape();
            Transform.mulToOutUnsafe(xf, edge.m_vertex1, v1);
            Transform.mulToOutUnsafe(xf, edge.m_vertex2, v2);
            drawSegment(v1, v2, color);
        }
            break;
        case CHAIN: {
            ChainShape chain = (ChainShape) fixture.getShape();
            int count = chain.m_count;
            Vec2[] vertices = chain.m_vertices;

            Transform.mulToOutUnsafe(xf, vertices[0], v1);
            for (int i = 1; i < count; ++i) {
                Transform.mulToOutUnsafe(xf, vertices[i], v2);
                drawSegment(v1, v2, color);
                drawCircle(v1, 0.05f, color);
                v1.set(v2);
            }
        }
            break;
        default:
            break;
        }
    }

    private void drawParticleSystem(ParticleSystem system) {
        boolean wireframe = (getFlags() & DebugDraw.e_wireframeDrawingBit) != 0;
        int particleCount = system.getParticleCount();
        if (particleCount != 0) {
            float particleRadius = system.getParticleRadius();
            Vec2[] positionBuffer = system.getParticlePositionBuffer();
            ParticleColor[] colorBuffer = null;
            if (system.m_colorBuffer.data != null) {
                colorBuffer = system.getParticleColorBuffer();
            }
            if (wireframe) {
                drawParticlesWireframe(positionBuffer, particleRadius, colorBuffer, particleCount);
            } else {
                drawParticles(positionBuffer, particleRadius, colorBuffer, particleCount);
            }
        }
    }

    private void drawJoint(Joint joint, IWorldPool pool) {
        Body bodyA = joint.getBodyA();
        Body bodyB = joint.getBodyB();
        Transform xf1 = bodyA.getTransform();
        Transform xf2 = bodyB.getTransform();
        Vec2 x1 = xf1.p;
        Vec2 x2 = xf2.p;
        Vec2 p1 = pool.popVec2();
        Vec2 p2 = pool.popVec2();
        joint.getAnchorA(p1);
        joint.getAnchorB(p2);

        color = new Color(0.5f, 0.8f, 0.8f);

        switch (joint.getType()) {
        // TODO djm write after writing joints
        case DISTANCE:
            drawSegment(p1, p2, color);
            break;

        case PULLEY: {
            PulleyJoint pulley = (PulleyJoint) joint;
            Vec2 s1 = pulley.getGroundAnchorA();
            Vec2 s2 = pulley.getGroundAnchorB();
            drawSegment(s1, p1, color);
            drawSegment(s2, p2, color);
            drawSegment(s1, s2, color);
        }
            break;
        case CONSTANT_VOLUME:
        case MOUSE:
            // don't draw this
            break;
        default:
            drawSegment(x1, p1, color);
            drawSegment(p1, p2, color);
            drawSegment(x2, p2, color);
        }
        pool.pushVec2(2);
    }

    private final Vec2 p1 = new Vec2();
    private final Vec2 p2 = new Vec2();
    private final Vec2 tangent = new Vec2();

    public void draw(MouseJoint mouseJoint) {
        if (mouseJoint != null) {
            mouseJoint.getAnchorB(p1);
            Vec2 p2 = mouseJoint.getTarget();

            drawSegment(p1, p2, mouseColor);
        }
    }

    public void draw(List<String> textList) {

        if (!textList.isEmpty()) {
            drawString(5, m_textLine, "Test Info", color4);
            m_textLine += TEXT_LINE_SPACE;
            for (String s : textList) {
                drawString(5, m_textLine, s, Color.WHITE);
                m_textLine += TEXT_LINE_SPACE;
            }
            textList.clear();
        }

    }

}

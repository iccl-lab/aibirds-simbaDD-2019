package ab.simulation.model.level;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DestructionListener;
import org.jbox2d.callbacks.ParticleDestructionListener;
import org.jbox2d.callbacks.ParticleQueryCallback;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Collision.PointState;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.serialization.JbDeserializer;
import org.jbox2d.serialization.JbDeserializer.ObjectListener;
import org.jbox2d.serialization.JbSerializer;
import org.jbox2d.serialization.JbSerializer.ObjectSigner;
import org.jbox2d.serialization.UnsupportedListener;
import org.jbox2d.serialization.UnsupportedObjectException;
import org.jbox2d.serialization.pb.PbDeserializer;
import org.jbox2d.serialization.pb.PbSerializer;
import org.jbox2d.testbed.framework.ContactPoint;
import org.jbox2d.testbed.framework.TestbedSettings;

public abstract class SimulationLevel implements ContactListener, ObjectListener, ObjectSigner, UnsupportedListener {
    public static final int MAX_CONTACT_POINTS = 4048;

    public final ContactPoint[] points = new ContactPoint[MAX_CONTACT_POINTS];

    private World m_world;

    private int pointCount;
    private int stepCount;

    protected DestructionListener destructionListener;
    protected ParticleDestructionListener particleDestructionListener;

    private JbSerializer serializer;
    private JbDeserializer deserializer;

    private final Transform identity = new Transform();

    public SimulationLevel() {
        identity.setIdentity();
        for (int i = 0; i < MAX_CONTACT_POINTS; i++) {
            points[i] = new ContactPoint();
        }
        serializer = new PbSerializer(this, new SignerAdapter(this) {
            @Override
            public Long getTag(Body argBody) {
                return super.getTag(argBody);
            }

            @Override
            public Long getTag(Joint argJoint) {
                return super.getTag(argJoint);
            }
        });
        deserializer = new PbDeserializer(this, new ListenerAdapter(this) {
            @Override
            public void processBody(Body argBody, Long argTag) {
                super.processBody(argBody, argTag);
            }

            @Override
            public void processJoint(Joint argJoint, Long argTag) {
                super.processJoint(argJoint, argTag);
            }
        });
        destructionListener = new DestructionListener() {
            public void sayGoodbye(Fixture fixture) {
                fixtureDestroyed(fixture);
            }

            public void sayGoodbye(Joint joint) {
                // if (mouseJoint == joint) {
                // mouseJoint = null;
                // } else {
                jointDestroyed(joint);
                // }
            }
        };

        particleDestructionListener = new ParticleDestructionListener() {
            @Override
            public void sayGoodbye(int index) {
                particleDestroyed(index);
            }

            @Override
            public void sayGoodbye(ParticleGroup group) {
                particleGroupDestroyed(group);
            }
        };

    }

    public void init() {
        isAwake.set(true);
        Vec2 gravity = new Vec2(0, -9.81f);
        m_world = new World(gravity);
        m_world.setParticleGravityScale(0.4f);
        m_world.setParticleDensity(1.2f);

        init(m_world, false);
    }

    public void init(World world, boolean deserialized) {
        m_world = world;
        pointCount = 0;
        stepCount = 0;

        world.setDestructionListener(destructionListener);
        world.setParticleDestructionListener(particleDestructionListener);
        world.setContactListener(this);
        initTest(deserialized);
    }

    protected JbSerializer getSerializer() {
        return serializer;
    }

    protected JbDeserializer getDeserializer() {
        return deserializer;
    }

    /**
     * Gets the current world
     */
    public World getWorld() {
        return m_world;
    }

    /**
     * Gets the contact points for the current test
     */
    public ContactPoint[] getContactPoints() {
        return points;
    }

    public int getStepCount() {
        return stepCount;
    }

    /**
     * The number of contact points we're storing
     */
    public int getPointCount() {
        return pointCount;
    }

    /**
     * Gets the filename of the current test. Default implementation uses the test
     * name with no spaces".
     */
    public String getFilename() {
        return getTestName().toLowerCase().replaceAll(" ", "_") + ".box2d";
    }

    /**
     * Initializes the current test.
     * 
     * @param deserialized if the test was deserialized from a file. If so, all
     *                     physics objects are already added.
     */
    public abstract void initTest(boolean deserialized);

    /**
     * The name of the test
     */
    public abstract String getTestName();

    /**
     * called when the tests exits
     */
    public void exit() {
    }

    protected AtomicBoolean isAwake = new AtomicBoolean(true);

    /**
     * Performs a step in the world if isFinished() is false
     * 
     * @param settings
     */
    public void step(TestbedSettings settings) {
        if (isFinished()) {
            return;
        }

        float hz = settings.getSetting(TestbedSettings.Hz).value;
        float timeStep = hz > 0f ? 1f / hz : 0;
        if (settings.singleStep && !settings.pause) {
            settings.pause = true;
        }

        if (settings.pause) {
            if (settings.singleStep) {
                settings.singleStep = false;
            } else {
                timeStep = 0;
            }
        }

        m_world.setAllowSleep(settings.getSetting(TestbedSettings.AllowSleep).enabled);
        m_world.setWarmStarting(settings.getSetting(TestbedSettings.WarmStarting).enabled);
        m_world.setSubStepping(settings.getSetting(TestbedSettings.SubStepping).enabled);
        m_world.setContinuousPhysics(settings.getSetting(TestbedSettings.ContinuousCollision).enabled);

        pointCount = 0;

        m_world.step(
                timeStep,
                settings.getSetting(TestbedSettings.VelocityIterations).value,
                settings.getSetting(TestbedSettings.PositionIterations).value);

        boolean tmp_isAwake = false;
        World tmpWorld = getWorld();
        Body body = tmpWorld.getBodyList();
        while (body != null) {
            tmp_isAwake = tmp_isAwake || (body.isAwake() && body.getType() == BodyType.DYNAMIC);
            body = body.getNext();
        }
        isAwake.set(tmp_isAwake);

        if (timeStep > 0f) {
            ++stepCount;
        }

    }

    public boolean isAwake() {
        return isAwake.get();
    }

    public boolean isFinished() {
        return !isAwake.get();
    }

    public synchronized int getScore() {
        // every dead pig yields 5.000 points
        // destroyed wood yields 500 points
        // every bird left over yields 10.000 points
        return 0;
    }

    /************ INPUT ************/

    public void mouseUp(Vec2 p, int button) {

    }

    public void keyPressed(char keyChar, int keyCode) {
    }

    public void keyReleased(char keyChar, int keyCode) {
    }

    public void mouseDown(Vec2 p, int button) {

    }

    public void mouseMove(Vec2 p) {

    }

    public void mouseDrag(Vec2 p, int button) {

    }

    /************ SERIALIZATION *************/

    /**
     * Override to enable saving and loading. Remember to also override the
     * {@link ObjectListener} and {@link ObjectSigner} methods if you need to
     * 
     * @return
     */
    public boolean isSaveLoadEnabled() {
        return false;
    }

    @Override
    public Long getTag(Body body) {
        return null;
    }

    @Override
    public Long getTag(Fixture fixture) {
        return null;
    }

    @Override
    public Long getTag(Joint joint) {
        return null;
    }

    @Override
    public Long getTag(Shape shape) {
        return null;
    }

    @Override
    public Long getTag(World world) {
        return null;
    }

    @Override
    public void processBody(Body body, Long tag) {
    }

    @Override
    public void processFixture(Fixture fixture, Long tag) {
    }

    @Override
    public void processJoint(Joint joint, Long tag) {
    }

    @Override
    public void processShape(Shape shape, Long tag) {
    }

    @Override
    public void processWorld(World world, Long tag) {
    }

    @Override
    public boolean isUnsupported(UnsupportedObjectException exception) {
        return true;
    }

    public void fixtureDestroyed(Fixture fixture) {
    }

    public void jointDestroyed(Joint joint) {
    }

    public void beginContact(Contact contact) {
    }

    public void endContact(Contact contact) {
    }

    public void particleDestroyed(int particle) {
    }

    public void particleGroupDestroyed(ParticleGroup group) {
    }

    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    private final PointState[] state1 = new PointState[Settings.maxManifoldPoints];
    private final PointState[] state2 = new PointState[Settings.maxManifoldPoints];
    private final WorldManifold worldManifold = new WorldManifold();

    public void preSolve(Contact contact, Manifold oldManifold) {
        Manifold manifold = contact.getManifold();

        if (manifold.pointCount == 0) {
            return;
        }

        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Collision.getPointStates(state1, state2, oldManifold, manifold);

        contact.getWorldManifold(worldManifold);

        for (int i = 0; i < manifold.pointCount && pointCount < MAX_CONTACT_POINTS; i++) {
            ContactPoint cp = points[pointCount];
            cp.fixtureA = fixtureA;
            cp.fixtureB = fixtureB;
            cp.position.set(worldManifold.points[i]);
            cp.normal.set(worldManifold.normal);
            cp.state = state2[i];
            cp.normalImpulse = manifold.points[i].normalImpulse;
            cp.tangentImpulse = manifold.points[i].tangentImpulse;
            cp.separation = worldManifold.separations[i];
            ++pointCount;
        }
    }
}

class TestQueryCallback implements QueryCallback {

    public final Vec2 point;
    public Fixture fixture;

    public TestQueryCallback() {
        point = new Vec2();
        fixture = null;
    }

    public boolean reportFixture(Fixture argFixture) {
        Body body = argFixture.getBody();
        if (body.getType() == BodyType.DYNAMIC) {
            boolean inside = argFixture.testPoint(point);
            if (inside) {
                fixture = argFixture;

                return false;
            }
        }

        return true;
    }
}

class ParticleVelocityQueryCallback implements ParticleQueryCallback {
    World world;
    Shape shape;
    Vec2 velocity;
    final Transform xf = new Transform();

    public ParticleVelocityQueryCallback() {
        xf.setIdentity();
    }

    public void init(World world, Shape shape, Vec2 velocity) {
        this.world = world;
        this.shape = shape;
        this.velocity = velocity;
    }

    @Override
    public boolean reportParticle(int index) {
        Vec2 p = world.getParticlePositionBuffer()[index];
        if (shape.testPoint(xf, p)) {
            Vec2 v = world.getParticleVelocityBuffer()[index];
            v.set(velocity);
        }
        return true;
    }
}

class SignerAdapter implements ObjectSigner {
    private final ObjectSigner delegate;

    public SignerAdapter(ObjectSigner argDelegate) {
        delegate = argDelegate;
    }

    public Long getTag(World argWorld) {
        return delegate.getTag(argWorld);
    }

    public Long getTag(Body argBody) {
        return delegate.getTag(argBody);
    }

    public Long getTag(Shape argShape) {
        return delegate.getTag(argShape);
    }

    public Long getTag(Fixture argFixture) {
        return delegate.getTag(argFixture);
    }

    public Long getTag(Joint argJoint) {
        return delegate.getTag(argJoint);
    }
}

class ListenerAdapter implements ObjectListener {
    private final ObjectListener listener;

    public ListenerAdapter(ObjectListener argListener) {
        listener = argListener;
    }

    public void processWorld(World argWorld, Long argTag) {
        listener.processWorld(argWorld, argTag);
    }

    public void processBody(Body argBody, Long argTag) {
        listener.processBody(argBody, argTag);
    }

    public void processFixture(Fixture argFixture, Long argTag) {
        listener.processFixture(argFixture, argTag);
    }

    public void processShape(Shape argShape, Long argTag) {
        listener.processShape(argShape, argTag);
    }

    public void processJoint(Joint argJoint, Long argTag) {
        listener.processJoint(argJoint, argTag);
    }
}
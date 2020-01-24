package ab.simulation.model.level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.testbed.framework.TestbedSettings;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.simulation.factory.BodyFactory;
import ab.simulation.model.SimulationUserData;
import ab.simulation.utils.Constants;
import ab.simulation.utils.SimUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Line;
import ab.vision.real.shape.Rect;

public class GenericLevel extends SimulationLevel {

    private boolean isInitialized = false;
    // Pixel per meter in this simulation
    private float PPM;
    // Objects from the Vision component
    private final Rect sling;
    private final Line ground;

    // contains the lists of following object types: hills, objects (ice, wood,
    // stone), TNT, supports and pigs
    private final Map<String, List<ABObject>> world_objects;
    private final List<Circle> birds;
    private final List<Circle> shootingBirds;
    // reference to body of bird that is currently shot for quick access
    private Body birdBody = null;
    // shot that is currently executed
    private Shot cur_shot = null;
    // true if the current bird's talent was already triggered
    private boolean talent_triggered = false;

    private final Vec2 referencePoint_model_fixed = new Vec2();
    /**
     * these variables are used to draw the parabola of the shot(s)
     */
    private Vec2 startPoint = new Vec2();
    private Vec2 controlPoint = new Vec2();
    private Vec2 endPoint = new Vec2();

    // the .thenComparing part might become obsolete depending on how the trigger
    // for activating the special ability will be handled
    private final PriorityQueue<Shot> shots = new PriorityQueue<Shot>(1,
            Comparator.comparing((Shot shot) -> shot.getT_shot()).thenComparing((Shot shot) -> shot.getT_tap()));

    /**
     * add Body objects to this list when they should not collide/move yet
     */
    private final Queue<Body> bodiesToHold = new LinkedList<Body>();
    /**
     * add Body objects to this list when they should be removed (collision)
     */
    private final Queue<Body> scheduledForRemovalList = new LinkedList<Body>();

    private int score = 0;
    private boolean first_hit = true;
    private int fuse_timer = 0;

    public GenericLevel(Rect sling, Line ground, Map<String, List<ABObject>> world_objects, Queue<Circle> birds) {
        super();
        this.sling = sling;
        this.ground = ground;
        this.world_objects = world_objects;
        this.birds = new LinkedList<Circle>(birds);
        shootingBirds = new LinkedList<Circle>(birds);
        // sort birds to shoot them in the right order in the simulation, too!
        Collections.sort(shootingBirds, new Comparator<Circle>() {
            public int compare(Circle c1, Circle c2) {
                int c1_center_x = c1.getCenter().x;
                int c2_center_x = c2.getCenter().x;
                if (c1_center_x > c2_center_x)
                    return (-1);
                else if (c1_center_x < c2_center_x)
                    return (1);
                else
                    return (0);
            }
        });
        Collections.sort(this.birds, new Comparator<Circle>() {
            public int compare(Circle c1, Circle c2) {
                int c1_center_x = c1.getCenter().x;
                int c2_center_x = c2.getCenter().x;
                if (c1_center_x > c2_center_x)
                    return (-1);
                else if (c1_center_x < c2_center_x)
                    return (1);
                else
                    return (0);
            }
        });
        this.PPM = (float) sling.getBbox().getHeight() / Constants.SLING_HEIGHT_IN_METER;

        // reference point from which the bird will be shot at
        // given the current vision it's about
        Vec2 referencePoint_view = new Vec2((float) (sling.getCenterX() + 0.05 * sling.getpWidth()),
                (float) (sling.getCenterY() - 0.3 * sling.getpLength()));
        referencePoint_model_fixed.set(SimUtil.convertViewToModel(referencePoint_view.clone(), PPM));
    }

    /**
     * Copy Constructor
     * 
     * @param genericLevel
     */
    public GenericLevel(GenericLevel genericLevel) {
        this(genericLevel.sling, genericLevel.ground, genericLevel.world_objects,
                new LinkedList<Circle>(genericLevel.birds));
    }

    @Override
    public String getTestName() {
        return "generic Level";
    }

    public synchronized boolean isInitialized() {
        return isInitialized;
    }

    public synchronized boolean isAwake() {
        boolean isAwake = false;
        World tmpWorld = getWorld();
        Body body = tmpWorld.getBodyList();
        while (body != null) {
            isAwake = isAwake || (body.isAwake() && body.getType() == BodyType.DYNAMIC);
            body = body.getNext();
        }
        // System.out.println("GenericLevel.isAwake() = " + isAwake);
        return isAwake;
    }

    public synchronized int getScore() {
        // every dead pig yields 5.000 points
        // destroyed block yields 500 points
        // every bird left over yields 10.000 points
        return score;
    }

    /**
     * @return a list with ABObjects representing pigs in the current level
     */
    protected List<ABObject> getPigs() {
        return (world_objects.getOrDefault("pigs", new ArrayList<ABObject>()));
    }

    /**
     * Access is protected to only allow extending classes access.
     * 
     * @return a list with ABObjects representing the hills in the current level
     */
    protected List<ABObject> getHills() {
        return (world_objects.getOrDefault("hills", new ArrayList<ABObject>()));
    }

    public float getPPM() {
        return PPM;
    }

    public int birdsLeft() {
        return shootingBirds.size();
    }

    /**
     * if this function is finished the level is considered to be initialized
     */
    @Override
    public void initTest(boolean deserialized) {
        getWorld().setGravity(new Vec2(0.0f, -Constants.GRAVITY));
        BodyFactory.createBody(sling, getWorld(), PPM);
        BodyFactory.createBody(ground, getWorld(), PPM);

        for (String key : world_objects.keySet()) {
            BodyFactory.createBodies(world_objects.getOrDefault(key, new ArrayList<ABObject>()), getWorld(), PPM);
        }

        shots.clear();
        shootingBirds.clear();
        shootingBirds.addAll(birds);
        scheduledForRemovalList.clear();
        bodiesToHold.clear();
        Body bodyList = getWorld().getBodyList();
        while (bodyList != null) {
            bodiesToHold.add(bodyList);
            bodyList = bodyList.getNext();
        }

        isInitialized = true;
    }

    @Override
    public void step(TestbedSettings settings) {
        if (!settings.singleStep && settings.pause)
            return;
        super.step(settings);
        performShot2();
        ABType bird_type = (birdBody != null) ? ((SimulationUserData) birdBody.getUserData()).getABObject().getType()
                : ABType.UNKNOWN;

        // if the tap time has passed, activate the bird's talent
        float time = (float) getStepCount() / settings.getSetting(TestbedSettings.Hz).value;
        if (cur_shot != null && (time * 1000) >= cur_shot.getT_tap()
                || (bird_type == ABType.BLACK_BIRD && fuse_timer > 0)) {
            activateTalent();
        }

        // add all bodies below the origin to the removal list to prevent ever falling
        // and ever awake bodies
        Body body = getWorld().getBodyList();
        while (body != null) {
            // discard body if it is outside of the world or if it is an explosion
            // particle without speed.
            if (body.getWorldCenter().y < 0)
                scheduledForRemovalList.add(body);
            else if (body.getUserData() == null && body.getLinearVelocity().length() < 1.0f)
                scheduledForRemovalList.add(body);
            body = body.getNext();
        }

        Body doomedBody = scheduledForRemovalList.poll();
        while (doomedBody != null) {
            SimulationUserData sud = (SimulationUserData) doomedBody.getUserData();
            Vec2 body_center = doomedBody.getWorldCenter();
            if (sud != null) {
                switch (sud.getABObject().getType()) {
                case PIG:
                    score += 5000;
                    break;
                case TNT:
                    // fall through, but don't forget to trigger the explosion!
                    if (body_center.y > 0)
                        triggerExplosion(body_center);
                case WOOD:
                    // fall through
                case STONE:
                    // fall through
                case ICE:
                    score += 500;
                    break;
                case EGG:
                    // explode, but do not grant a reward in terms of points
                    if (body_center.y > 0)
                        triggerExplosion(body_center);
                default:
                    break;
                }
            }
            getWorld().destroyBody(doomedBody);
            doomedBody = scheduledForRemovalList.poll();
        }
        if (settings.getSetting(TestbedSettings.AllowSleep).enabled && settings.getSetting("PUT_TO_SLEEP").enabled) {
            Body body2;
            while ((body2 = bodiesToHold.poll()) != null) {
                body2.setAwake(false);
            }
        }

        if (bird_type == ABType.BLACK_BIRD && !first_hit) {
            fuse_timer++;
        }
        /*
         * TODO move the drawing of the throwing parabola to the panel if
         * (getDebugDraw() != null) { try { SimulationDebugDraw simulationDebugDraw =
         * (SimulationDebugDraw) getDebugDraw();
         * simulationDebugDraw.drawQuadratic(startPoint, controlPoint, endPoint, new
         * Color3f()); } catch (Exception e) { System.err
         * .println("DebugDraw of this test has to extend SimulationDebugDraw in order to draw the shot"
         * ); System.err.println("couldn't draw shot"); } }
         */
    }

    /**
     * Activates special abilities of a bird.
     */
    private void activateTalent() {
        // only activate bird effects once!
        if (talent_triggered == true)
            return;

        SimulationUserData sud = (SimulationUserData) birdBody.getUserData();
        Vec2 bird_pos; // position of shooting bird in simulation
        Vec2 model_pos; // position of shooting bird in real game
        Vec2 bird_vel; // velocity of shooting bird
        Circle bird_circle = (Circle) sud.getABObject();
        switch (sud.getABObject().getType()) {
        case YELLOW_BIRD:
            // System.out.println("Activating yellow bird.");
            birdBody.setLinearVelocity(birdBody.getLinearVelocity().mul(Constants.YELLOW_BIRD_ACCELERATION));
            break;
        case BLUE_BIRD:
            // this is not perfect, but for now, this should suffice for the
            // simulation.
            // System.out.println("Activating blue bird.");
            bird_pos = birdBody.getWorldCenter();
            model_pos = SimUtil.convertModelToView(bird_pos, getPPM());
            bird_vel = birdBody.getLinearVelocity();
            // new velocity components
            float bird_vel_x;
            float bird_vel_y;

            // bird that is flying higher
            bird_vel_x = (float) (bird_vel.x * Math.cos(Constants.BLUE_BIRD_SPREAD)
                    - bird_vel.y * Math.sin(Constants.BLUE_BIRD_SPREAD));
            bird_vel_y = (float) (bird_vel.x * Math.sin(Constants.BLUE_BIRD_SPREAD)
                    + bird_vel.y * Math.cos(Constants.BLUE_BIRD_SPREAD));
            Circle new_bird_up = new Circle(model_pos.x, model_pos.y, bird_circle.r, bird_circle.getType());
            Body up_bird = BodyFactory.createBody(new_bird_up, getWorld(), getPPM());
            up_bird.setLinearVelocity(new Vec2(bird_vel_x, bird_vel_y));

            // bird that is flying lower
            bird_vel_x = (float) (bird_vel.x * Math.cos(-Constants.BLUE_BIRD_SPREAD)
                    - bird_vel.y * Math.sin(-Constants.BLUE_BIRD_SPREAD));
            bird_vel_y = (float) (bird_vel.x * Math.sin(-Constants.BLUE_BIRD_SPREAD)
                    + bird_vel.y * Math.cos(-Constants.BLUE_BIRD_SPREAD));
            Circle new_bird_dn = new Circle(model_pos.x, model_pos.y, bird_circle.r, bird_circle.getType());
            Body dn_bird = BodyFactory.createBody(new_bird_dn, getWorld(), getPPM());
            dn_bird.setLinearVelocity(new Vec2(bird_vel_x, bird_vel_y));
            break;
        case BLACK_BIRD:
            // explosion has some inertia!
            float fuse_time = (float) fuse_timer / Constants.TESTBED_HZ;
            // System.err.println("fuse timer: " + fuse_timer);
            if (fuse_time < Constants.FUSE_INERTIA) {
                return; // talent is still not activated
            } else {
                triggerExplosion(birdBody.getWorldCenter());
                // black bird must disappear after explosion!
                scheduledForRemovalList.add(birdBody);
            }
            break;
        case WHITE_BIRD:
            bird_pos = birdBody.getWorldCenter();
            bird_vel = birdBody.getLinearVelocity();

            // spawn the explosive egg
            Vec2 egg_pos = bird_pos;
            egg_pos = SimUtil.convertModelToView(egg_pos, getPPM());
            Circle egg = new Circle(egg_pos.x, egg_pos.y, bird_circle.r / 2, ABType.EGG);
            Body egg_body = BodyFactory.createBody(egg, getWorld(), getPPM());
            egg_body.setBullet(true);
            egg_body.setLinearVelocity(new Vec2(0.0f, -20.0f));

            // kick away the residuals of the white bird
            double angle = Math.PI / 3.0;
            Vec2 acc = new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
            acc.normalize();
            birdBody.setLinearVelocity(acc.mul(bird_vel.length() * 2.0f));
            break;
        default:
            // this bird has no special ability to trigger
            // System.out.println("Activated talent of type: "+
            // sud.getABObject().getType());
            break;
        }
        talent_triggered = true;
    }

    /**
     * Triggers an explosion starting at the spot specified by center. Here, the
     * particle method for modeling blasts is used. See also:
     * https://www.iforce2d.net/b2dtut/explosions
     * 
     * @param center - point from which the explosion particles are launched.
     */
    private void triggerExplosion(Vec2 center) {
        for (int i = 0; i < Constants.EXPLOSION_PARTICLE_COUNT; i++) {
            float angle = (float) (i / Constants.EXPLOSION_PARTICLE_COUNT * Math.PI * 2);
            Vec2 blast_dir = new Vec2((float) Math.sin(angle), (float) Math.cos(angle));

            BodyDef bdef = new BodyDef();
            bdef.type = BodyType.DYNAMIC;
            bdef.fixedRotation = true;
            bdef.bullet = true; // prevent tunneling at high speed
            bdef.linearDamping = 10; // drag due to moving through air
            bdef.gravityScale = 0; // ignore gravity
            bdef.position = center;
            bdef.linearVelocity = blast_dir.mulLocal(Constants.BLAST_POWER);
            Body b = getWorld().createBody(bdef);

            CircleShape particle = new CircleShape();
            particle.m_radius = 0.05f; // very small

            FixtureDef fdef = new FixtureDef();
            fdef.shape = particle;
            fdef.density = 45f;
            fdef.friction = 0;
            fdef.restitution = 0.99f; // high restitution to reflect off obstacles
            fdef.filter.groupIndex = -1; // particles should not collide with each other
            b.createFixture(fdef);
        }
    }

    /**
     * 
     * @param shots to be added
     * @return <code>true</code> if shots have been successfully added
     */
    protected boolean addShots(Collection<Shot> shots) {
        return this.shots.addAll(shots);
    }

    /**
     * 
     * @param shots to be added
     * @return <code>true</code> if shots have been successfully added
     */
    protected boolean addShots(Shot... shots) {
        for (Shot shot : shots) {
            if (!this.shots.add(shot))
                return false;
        }
        return true;
    }

    protected Vec2 getReferencePoint() {
        return referencePoint_model_fixed;
    }

    /**
     * computes the launch point and clips the delta vector to the maximum stretch
     * 
     * @param referencePoint_model the point in the slingshot the bird is centered
     *                             at in model coordinates
     * @param deltaVector_model    the vector from the reference point to the
     *                             release point of the mouse in model coordinates
     * @return the point in model coordinates the bird will be released at
     */
    protected Vec2 computeLaunchPoint(Vec2 referencePoint_model, Vec2 deltaVector_model) {
        Vec2 launchPoint = new Vec2(referencePoint_model);
        float stretch = deltaVector_model.normalize();
        // clip delta vector within the maximum stretch
        stretch = Math.min(stretch, Constants.MAX_STRETCH);
        deltaVector_model.mulLocal(stretch);
        launchPoint.addLocal(deltaVector_model);
        return launchPoint;
    }

    protected Vec2 computeVelocity(Vec2 deltaVector_model) {
        // Vec2 deltaVec2_model = SimUtil.convertVectorViewToModel(deltaVec2_view,
        // getPPM());
        Vec2 velocity = new Vec2();
        // scale velocity based on the stretch
        velocity = deltaVector_model.mul(Constants.MAXIMUM_VELOCITY / Constants.MAX_STRETCH);
        // flip vector as it points from the reference point to the bird
        velocity.mulLocal(-1.0f);
        return velocity;
    }

    protected void computeQuadraticParameters(Vec2 launchPoint_model, Vec2 velocity_model) {
        startPoint.set(launchPoint_model);
        Vec2 groundCenter_model = SimUtil
                .convertViewToModel(new Vec2((float) ground.getCenterX(), (float) ground.getCenterY()), PPM);
        // Calculate intersection between parabola and ground as end point of the bezier
        float p = velocity_model.y / Constants.GRAVITY;
        float dy = startPoint.y - groundCenter_model.y;
        float q = 2.0f * dy / Constants.GRAVITY;
        float t = p + (float) Math.sqrt(p * p + q);

        endPoint.x = startPoint.x + velocity_model.x * t;
        endPoint.y = groundCenter_model.y;

        // control point of bezier curve for quadratic equations
        controlPoint.x = (startPoint.x + endPoint.x) / 2.0f;
        controlPoint.y = startPoint.y + velocity_model.y / velocity_model.x * (endPoint.x - startPoint.x) / 2.0f;
    }

    @SuppressWarnings("unused")
    private void performShot2() {
        if (shots.isEmpty() || shootingBirds.isEmpty()) {
            // System.out.println("Not shooting bird: shots or birds are empty!");
            return;
        }
        // TODO shoot at the right moment given by t_time and trigger special ability
        // afterwards given by t_tap
        // maybe create a thread that calls this function at the right time? Or have a
        // timer in step()?
        // is this.getStepCount() a viable method given the frequency of this
        // simulation?
        // what about special abilities?
        if (shots.peek().getT_shot() > getStepCount()) {
            // System.out.println("Not shooting bird: step count");
            return;
        }
        Shot shot = shots.poll();
        cur_shot = shot;

        TrajectoryPlanner trajectoryPlanner = new TrajectoryPlanner();

        // get the scaling that was used to compute the Shot
        double scale = trajectoryPlanner.getSceneScale(sling.getBbox());
        // calculate the angle
        // since Dx and Dy are from the reference point to the release point they need
        // to be reversed i.e. times -1 for atan2 and since the y-coordinate is
        // upside down in the image we need to turn in the opposite direction hence the
        // negative angle which leads to the following equation
        // double theta = -Math.atan2(-shot.getDy(), -shot.getDx());
        // due to symmetry this can be rewritten, in order to save time (I guess?), as:
        double theta = Math.atan2(shot.getDy(), -shot.getDx());
        theta = trajectoryPlanner.launchToActual(theta);

        // work out initial velocities and coefficients of the parabola
        double _velocity = trajectoryPlanner.getVelocity(theta);
        double _ux = _velocity * Math.cos(theta);
        double _uy = _velocity * Math.sin(theta);
        double a = -0.5 / (_ux * _ux);
        double b = _uy / _ux;

        // calculate the coefficient to convert from the vision component to the
        // simulation
        double f = Math.sqrt(scale * Constants.GRAVITY / getPPM());
        // TODO check calculations if abs is correct (if so shots below the slingshot
        // need special treatment)
        double v_x = f * _ux;
        double v_y = f * _uy;

        Vec2 velocity = new Vec2((float) v_x, (float) v_y);
        Vec2 launchPoint = new Vec2(shot.getX(), shot.getY());

        // create the bird at the launch point
        Circle bird = shootingBirds.remove(0);
        ABType bird_type = bird.getType();
        // System.out.println("shooting bird of type: " + bird_type);
        // System.out.println("bird radius: " + bird.r);
        // if vision fails to detect the bird properly and it is therefore
        // reported as unknown, assume a red bird for simulation to get at least
        // some result (unknown objects are unable to collide with anything)
        Circle shootingBird = new Circle(launchPoint.x, launchPoint.y, bird.r, bird_type);
        birdBody = BodyFactory.createBody(shootingBird, getWorld(), getPPM());

        /*
         * Impulse = m*a*dt = m*v
         */
        birdBody.applyLinearImpulse(velocity.mul(birdBody.getMass()), birdBody.getWorldCenter(), true);

        computeQuadraticParameters(birdBody.getWorldCenter(), velocity);
    }

    /**
     * will be removed once the correctness of performShot2 has been verified or is
     * way better than this one
     */
    @SuppressWarnings("unused")
    @Deprecated
    private void performShot() {
        if (shots.isEmpty() || shootingBirds.isEmpty())
            return;
        // TODO shoot at the right moment given by t_time and trigger special ability
        // afterwards given by t_tap
        // maybe create a thread that calls this function at the right time? Or have a
        // timer in step()?
        // is this.getStepCount() a viable method given the frequency of this
        // simulation?
        if (shots.peek().getT_shot() > getStepCount())
            return;
        Shot shot = shots.poll();

        // compute launch point and launch velocity
        Vec2 deltaVector_view = new Vec2(shot.getDx(), shot.getDy());
        Vec2 deltaVector_model = SimUtil.convertVectorViewToModel(deltaVector_view, getPPM());
        Vec2 referencePoint_view = new Vec2(shot.getX(), shot.getY());
        Vec2 referencePoint_model = SimUtil.convertViewToModel(referencePoint_view, getPPM());
        Vec2 launchPoint_model = computeLaunchPoint(referencePoint_model, deltaVector_model);
        Vec2 launchPoint_view = SimUtil.convertModelToView(launchPoint_model, getPPM());
        Vec2 velocityVec2 = computeVelocity(deltaVector_model);

        // create the bird at the launch point
        Circle bird = shootingBirds.remove(0);
        Circle shootingBird = new Circle(launchPoint_view.x, launchPoint_view.y, bird.r, bird.getType());
        Body birdBody = BodyFactory.createBody(shootingBird, getWorld(), getPPM());

        /*
         * Impulse = m*a*dt = m*v
         */
        birdBody.applyLinearImpulse(velocityVec2.mul(birdBody.getMass()), birdBody.getWorldCenter(), true);

        computeQuadraticParameters(birdBody.getWorldCenter(), velocityVec2);
    }

    @Override
    public void beginContact(Contact contact) {
        super.beginContact(contact);

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // TODO Auto-generated method stub
        super.postSolve(contact, impulse);
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        SimulationUserData sudA = (SimulationUserData) fixtureA.getBody().getUserData();
        SimulationUserData sudB = (SimulationUserData) fixtureB.getBody().getUserData();

        /*
         * the method was caused by a collision of an object with an explosion*
         * particle. Add special behavior of particles in if statement...
         */
        if (sudA == null || sudB == null)
            return;

        ABObject abObjectA = sudA.getABObject();
        ABObject abObjectB = sudB.getABObject();

        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);
        // always points from fixture A to fixture B the order of the fixtures is random
        // so either A hits B or B hits A
        // points in the direction that's shortest to seperate both fixtures
        // does not contain the strength
        // TODO find the strength of the impact
        // Vec2 normal = worldManifold.normal;
        // strength of each impulse
        // always contains 2 values?
        // float[] normalImpulses = impulse.normalImpulses;
        // System.out.println("#normal_impulses: " + normalImpulses.length);
        float strength = 0.0f;
        for (int i = 0; i < impulse.count; i++) {
            // strength += impulse.normalImpulses[i];
            // instead of adding up impulses, only consider the maximum
            strength = Math.max(strength, impulse.normalImpulses[i]);
        }
        if (abObjectA == null || abObjectB == null)
            return;

        if (sudA.inflictDamage(sudB.getABObject().getType(), strength) <= 0.0) {
            // explosion must be triggered outside the callback function
            // because it attempts to spawn new objects!
            scheduledForRemovalList.add(fixtureA.getBody());
        }
        if (sudB.inflictDamage(sudA.getABObject().getType(), strength) <= 0.0) {
            scheduledForRemovalList.add(fixtureB.getBody());
        }

        // adjust speed of bird after collision
        if ((ABType.isBird(sudA.getABObject().getType()) || ABType.isBird(sudB.getABObject().getType())) && first_hit) {
            Body birdBody;
            SimulationUserData otherData;
            SimulationUserData birdData;
            if (ABType.isBird(sudA.getABObject().getType())) {
                birdBody = fixtureA.getBody();
                otherData = sudB;
                birdData = sudA;
            } else {
                birdBody = fixtureB.getBody();
                otherData = sudA;
                birdData = sudB;
            }

            switch (otherData.getABObject().getType()) {
            case STONE:
                if (birdData.getABObject().getType() != ABType.BLACK_BIRD) {
                    birdBody.m_linearVelocity.mulLocal(0.15f);
                    birdBody.m_angularVelocity *= 0.15f;
                }
                break;
            case ICE:
                if (birdData.getABObject().getType() != ABType.BLUE_BIRD) {
                    birdBody.m_linearVelocity.mulLocal(0.15f);
                    birdBody.m_angularVelocity *= 0.15f;
                }
                break;
            case WOOD:
                if (birdData.getABObject().getType() != ABType.YELLOW_BIRD) {
                    birdBody.m_linearVelocity.mulLocal(0.15f);
                    birdBody.m_angularVelocity *= 0.15f;
                }
                break;
            default:
                break;
            }

            // once the bird hit the ground, its talent is wasted, except if it
            // is a black bird.
            if (birdData.getABObject().getType() != ABType.BLACK_BIRD) {
                talent_triggered = true;
            }
            first_hit = false;
        }
    }
}

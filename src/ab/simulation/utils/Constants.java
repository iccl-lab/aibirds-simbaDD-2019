package ab.simulation.utils;

public class Constants {
    public static final int NUMBER_OF_THREADS = 2;
    public static final float GRAVITY = 9.81f;
    public static final int TESTBED_HZ = 60;
    /**
     * maximum velocity the slingshot can shoot the birds with in m/s
     */
    public static final float MAXIMUM_VELOCITY = 22.5f;// in m/s
    public static final int WINDOW_WIDTH = 840;
    public static final int WINDOW_HEIGHT = 480;
    // Pixel per meter
    public static final float PIXEL_PER_METER = 32;
    public static final float SLING_HEIGHT_IN_METER = 5.0f;
    public static final float MAX_STRETCH = SLING_HEIGHT_IN_METER * 0.5f;

    public static final float ICE_DENSITY   = 0.75f * 4.0f;
    public static final float WOOD_DENSITY  = 1.5f * 4.0f;    // 1.5 *...
    public static final float STONE_DENSITY = 6.0f * 4.0f;
    
    public static final float YELLOW_BIRD_ACCELERATION = 1.9f;
    public static final double BLUE_BIRD_SPREAD = Math.PI / 11;
    // time it takes for a black bird to detonate after first impact
    public static final float FUSE_INERTIA = 2.0f;
    
    public static final float EXPLOSION_PARTICLE_COUNT = 120f;
    public static final float BLAST_POWER = 100f;
}

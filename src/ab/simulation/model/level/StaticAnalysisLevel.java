package ab.simulation.model.level;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;
import org.jbox2d.testbed.framework.TestbedSettings;

import ab.simulation.model.SimulationUserData;
import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.ABType;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Line;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;

/**
 * this class is responsible for doing the static analysis which will be used to
 * find targets
 */
public class StaticAnalysisLevel extends GenericLevel {

	/***     Constants for configuration of the static analysis process     ***/
	public static final float MAX_HIDDEN_PCT        = 0.6f;
	public static final int   PTS_PER_BACKWARD_EDGE = 10;
	
    private final Map<Set<Body>, Float> contactMap = new HashMap<Set<Body>, Float>();
    
    public StaticAnalysisLevel(Rect sling, Line ground, 
    		Map<String, List<ABObject>> world_objects, Queue<Circle> birds) {
        super(sling, ground, world_objects, birds);
    }

    /**
     * Copy Constructor
     * 
     * @param staticAnalysisLevel
     */
    public StaticAnalysisLevel(StaticAnalysisLevel staticAnalysisLevel) {
        super(staticAnalysisLevel);
    }

    /**
     * pseudo copy constructor
     * 
     * @param genericLevel
     */
    public StaticAnalysisLevel(GenericLevel genericLevel) {
        super(genericLevel);
    }

    @Override
    public String getTestName() {
        return "StaticAnalysisLevel";
    }

    @Override
    public void step(TestbedSettings settings) {
        updateContactMap();
        settings.getSetting("PUT_TO_SLEEP").enabled = false;
        super.step(settings);
    }

    @Override
    /**
     * do nothing, no damage nothing gets destroyed
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    private void updateContactMap() {
        Contact currentContact = getWorld().getContactList();
        while (currentContact != null) {
            Body A = currentContact.getFixtureA().getBody();
            Body B = currentContact.getFixtureB().getBody();
            Set<Body> contactBodies = new HashSet<Body>();
            contactBodies.add(A);
            contactBodies.add(B);
            // get impulse manifold
            Manifold manifold = currentContact.getManifold();
            int count = manifold.pointCount;
            float summedForces = 0;
            for (int i = 0; i < count; ++i) {
                summedForces += manifold.points[i].normalImpulse;
            }
            contactMap.put(contactBodies, summedForces);
            currentContact = currentContact.getNext();
        }
    }

    public HashMap<Body, Float> getBodyForces() {
        HashMap<Body, Float> forces = new HashMap<>();
        for (Set<Body> contactBodies : contactMap.keySet()) {
            float force = contactMap.getOrDefault(contactBodies, 0.0f);
            Object[] bodies = contactBodies.toArray();
            Body bodyA = (Body) bodies[0];
            Body bodyB = (Body) bodies[1];
            if (bodyA.getWorldCenter().y > bodyB.getWorldCenter().y) {
                float currentForce = forces.getOrDefault(bodyB, 0.0f);
                forces.put(bodyB, currentForce + force);
            } else {
                float currentForce = forces.getOrDefault(bodyA, 0.0f);
                forces.put(bodyA, currentForce + force);
            }
        }
        return forces;
    }

    /**
     * Computes the forces that are applied to all bodies. The key set of the
     * returned map is restricted to elements that are present in targets, a body
     * list that may be computed using functions like getReachableTargets.
     * 
     * @param targets
     * @return
     */
    public HashMap<Body, Float> getBodyForces(List<Body> targets) {
        HashMap<Body, Float> forces = new HashMap<>();
        for (Set<Body> contactBodies : contactMap.keySet()) {
            float force = contactMap.get(contactBodies);
            Object[] bodies = contactBodies.toArray();
            Body bodyA = (Body) bodies[0];
            Body bodyB = (Body) bodies[1];
            if (bodyA.getWorldCenter().y > bodyB.getWorldCenter().y) {
                float currentForce = forces.getOrDefault(bodyB, 0.0f);
                forces.put(bodyB, currentForce + force);
                // keep track of bodies without a load
                if (! forces.keySet().contains(bodyA)) {
                	forces.put(bodyA, 0.0f);
                }
            } else {
                float currentForce = forces.getOrDefault(bodyA, 0.0f);
                forces.put(bodyA, currentForce + force);
                // keep track of bodies without a load
                if (! forces.keySet().contains(bodyB)) {
                	forces.put(bodyB, 0.0f);
                }
            }
        }
        
    	int rightmost_pig_x = 0;
    	
    	// find rightmost pig in the scene
    	for (ABObject pig : getPigs()) {
    		int pig_x = pig.getCenter().x;
    		if (pig_x > rightmost_pig_x) {
    			rightmost_pig_x = pig_x;
    		}
    	}
        
        // filter objects without any statical stress on them, excluding pigs,
        // TNT boxes and any circular shapes. The latter are excluded because
        // shooting spheres often results in chain reactions which might be 
        // worth trying.
        Iterator<Map.Entry<Body, Float>> iter = forces.entrySet().iterator();
        while (iter.hasNext()) {
        	Map.Entry<Body, Float> ent = iter.next();
        	SimulationUserData sud = (SimulationUserData) ent.getKey().getUserData();
    		ABType type = sud.getABObject().type;
    		
        	boolean filter = false;
        	if (ent.getValue() == 0.0f) {
        		if (! ((type == ABType.PIG) || (type == ABType.TNT) ||
        			   (sud.getABObject().shape == ABShape.Circle))) {
        			filter = true;
        		}
        	}
        	else if (sud.getABObject().getCenter().x > rightmost_pig_x &&
        			 ! (type == ABType.TNT || sud.getABObject() instanceof Circle)) {
        		filter = true;
        	}

        	if (! targets.contains(ent.getKey()) || filter) {
        		if (targets.contains(ent.getKey()) && filter) {
        		    System.out.println("Filtered object of type: " + type);
        		    // System.out.println("ABO id: " + sud.getABObject().id);
        		    // System.out.println("weight: " + ent.getValue());
        		}
        		iter.remove();
        	}
        }

//        System.out.println("forces size after deletion: " + forces.size());
//        for (Body bdy : forces.keySet()) {
//			SimulationUserData sud = (SimulationUserData) bdy.getUserData();
//    		ABType type = sud.getABObject().type;
//    		System.out.println(type);
//		}

        return forces;
    }

    /**
     * Finds reachable targets by computing the hull directly reachable with
     * high and straight shots.
     * 
     * @return a list of objects that can be shot directly
     */
    public List<Body> getReachableTargets() {
    	List<Body> targets = new LinkedList<Body>();
    	Body b             = getWorld().getBodyList();
    	
    	// transform JBox2D's representation into something reasonable
    	// prepare a body list for partitioning into components, leave out 
    	// leave out static objects here.
    	while (b != null) {
    		SimulationUserData sud = (SimulationUserData) b.getUserData();
    		ABType type = sud.getABObject().type;
    		if (type == ABType.HILL || type == ABType.GROUND ||
    			type == ABType.SLING || type == ABType.SUPPORT) {
    			b = b.getNext();
    			continue;
    		}
    			
    		targets.add(b);
    		b = b.getNext();
    	}
    	
    	System.out.println("no. of scene blocks: " +  targets.size());
//    	for (Body target : targets) {
//    		SimulationUserData sud = (SimulationUserData) target.getUserData();
//        	ABType type = sud.getABObject().type;
//        	System.out.println(type);
//    	}
    	
    	// targets will be empty after this call!!!
    	List<List<Body>> components = partition(targets);
    	
    	System.out.println("no of components: " + components.size());
    	// for (List<Body> blist : components) {
    		// System.out.println("component size: " + blist.size());
    		// for (Body bdy : blist) {
    		// 	SimulationUserData sud = (SimulationUserData) bdy.getUserData();
        	//	ABType type = sud.getABObject().type;
        	//	System.out.println(type);
    		// }
    	// }
    	
    	// convert vec2 to point for better precision !!!
    	// now, do a projection for upper and lefthand side.
    	
    	// upper projection
    	for (List<Body> blist : components) {
    		List<Point> sectors = new LinkedList<Point>();
    		
    		blist.sort(new Comparator<Body>() {
    			public int compare(Body b1, Body b2) {
    				Vec2 p1 = b1.getWorldCenter();
    				Vec2 p2 = b2.getWorldCenter();
    				
    				if (p1.y > p2.y) {
    					return(-1);
    				}
    				else if (p1.y < p2.y) {
    					return(1);
    				}
    				else {
    					return(0);
    				}
    			}
    		});
    		
    		for (Body body : blist) {
    			boolean insert      = true;
    			boolean shoot_upper = true;
    			AABB  bbox  = body.m_fixtureList.getAABB(0);
    			Point width = new Point((int) (bbox.lowerBound.x * 1000), 
    					                (int) (bbox.upperBound.x * 1000));
    			float hidden_pct = 0.0f;
    			
    			// do shadowing of objects: the idea is to not shoot objects
    			// with a high trajectory if a significant amount of their
    			// surface reachable by a high shot is covered by other objects
    			// lying on top of body.
    			for (Point sector : sectors) {
    				int   w          = width.y - width.x;
    				
    				// make sure that sector and body overlap
    				if (! (sector.x >= width.y || sector.y <= width.x ||
    						(sector.x <= width.x && sector.y >= width.y))) {
    					int overlap_x = Math.max(sector.x, width.x);
    					int overlap_y = Math.min(sector.y, width.y);
    					
    					hidden_pct += ((float) (overlap_y - overlap_x) / w);
    				}
    			}
    			
    			// disable high shots on object if threshold is exceeded
    			if (hidden_pct >= MAX_HIDDEN_PCT) {
    				shoot_upper = false;
    				SimulationUserData sud = (SimulationUserData) body.getUserData();
    	        	ABType type = sud.getABObject().type;
    				System.out.println("Disabled high shot for object of " +
    	        	                   "type " + type + " (hidden_pct: " + 
    						            hidden_pct + ")");
    			}
    				
    			for (Point sector : sectors) {
    				if (sector.x <= width.x && sector.y >= width.y) {
    					insert = false;
    					break;
    				}
    				else if (sector.x <= width.x && sector.y < width.y) {
    					sector.y = width.y;
    					insert = false;
    					targets.add(body);
    					((SimulationUserData) body.getUserData()).setUpperSide(shoot_upper);
    					break;
    				}
    				else if (sector.y >= width.y && sector.x > width.x) {
    					sector.x = width.x;
    					insert = false;
    					targets.add(body);
    					((SimulationUserData) body.getUserData()).setUpperSide(shoot_upper);
    					break;
    				}
    				else if (sector.x > width.x && sector.y < width.y) {
    					sector.x = width.x;
    					sector.y = width.y;
    					targets.add(body);
    					((SimulationUserData) body.getUserData()).setUpperSide(shoot_upper);
    					insert = false;
    					break;
    				}
    			}
    			
    			if (insert == true) {
    				sectors.add(width);
    				targets.add(body);
    				((SimulationUserData) body.getUserData()).setUpperSide(shoot_upper);
    			}
    			
    			coalesce(sectors);
    		}
    	}
    	
    	// System.out.println("no. of targets after upper projection: " +  targets.size());
    	// for (Body bdy : targets) {
		// 	SimulationUserData sud = (SimulationUserData) bdy.getUserData();
    	//	ABType type = sud.getABObject().type;
    	//	System.out.println(type);
		// }
    	
    	// lefthand side projection
    	for (List<Body> blist : components) {
    		List<Point> sectors = new LinkedList<Point>();
    		
    		blist.sort(new Comparator<Body>() {
    			public int compare(Body b1, Body b2) {
    				Vec2 p1 = b1.getWorldCenter();
    				Vec2 p2 = b2.getWorldCenter();
   				
    				if (p1.x < p2.x) {
    					return(-1);
    				}
    				else if (p1.x > p2.x) {
    					return(1);
    				}
    				else {
    					return(0);
    				}
    			}
    		});
    		
    		for (Body body : blist) {
    			boolean insert = true;
    			AABB  bbox   = body.m_fixtureList.getAABB(0);
    			Point height = new Point((int) (bbox.lowerBound.y * 1000), 
    					                 (int) (bbox.upperBound.y * 1000));
    			
    			for (Point sector : sectors) {
    				if (sector.x <= height.x && sector.y >= height.y) {
    					insert = false;
    					break;
    				}
    				else if (sector.x <= height.x && height.x <= sector.y &&
    						 sector.y < height.y) {
    					sector.y = height.y;
    					insert = false;
    					if (! targets.contains(body)) {
    						targets.add(body);
    					}
    					((SimulationUserData) body.getUserData()).setLeftSide(true);
    					break;
    				}
    				else if (sector.y >= height.y && height.y >= sector.x && 
    						 sector.x > height.x) {
    					sector.x = height.x;
    					insert = false;
    					if (! targets.contains(body)) {
    						targets.add(body);
    					}
    					((SimulationUserData) body.getUserData()).setLeftSide(true);
    					break;
    				}
    				else if (sector.x > height.x && sector.y < height.y) {
    					sector.x = height.x;
    					sector.y = height.y;
    					if (! targets.contains(body)) {
    						targets.add(body);
    					}
    					((SimulationUserData) body.getUserData()).setLeftSide(true);
    					insert = false;
    					break;
    				}
    			}
    			
    			if (insert == true) {
    				sectors.add(height);
    				if (! targets.contains(body)) {
    					targets.add(body);
    				}
    				((SimulationUserData) body.getUserData()).setLeftSide(true);
    			}
                coalesce(sectors);
    		}
    		
    	}
  	
//    	System.out.println("no. of targets after both projections: " +  targets.size());
//    	for (Body bdy : targets) {
//    		SimulationUserData sud = (SimulationUserData) bdy.getUserData();
//    		ABType type = sud.getABObject().type;
//    		System.out.println(type);
//		}
    	
    	return(targets);
    }

    /**
     * Finds targets on backward edges. Backward edges are parts of a hill that
     * are angled towards pigs. In some levels, such as the second one of
     * poached eggs, it may be advantageous to shoot over the scene, let the 
     * bird rebound from a hill and attack the scene "from behind".
     * 
     * @return a set of backward edge targets and their associated box2d bodies
     */
    public Map<Point, Body> findBackwardEdgeTargets() {
    	Map<Point, Body> bwe_targets = new HashMap<Point, Body>();
    	List<Body> box2d_hills       = new LinkedList<Body>();
    	Body iter_body               = getWorld().getBodyList();
    	int leftmost_pig_x = Integer.MAX_VALUE;
    	
    	// find leftmost pig in the scene
    	for (ABObject pig : getPigs()) {
    		int pig_x = pig.getCenter().x;
    		if (pig_x < leftmost_pig_x) {
    			leftmost_pig_x = pig_x;
    		}
    	}
    	
    	// find all box2d object that are hills (extra effort because ABObjects
    	// do not contain a reference to the body backing them
    	while (iter_body != null) {
    		SimulationUserData sud = 
    				(SimulationUserData) iter_body.getUserData();
    		
    		ABType type = sud.getABObject().type;
    		if (type == ABType.HILL) {
    			box2d_hills.add(iter_body);
    		}
    			
    		iter_body = iter_body.getNext();
    	}
    	
    	for (ABObject hill : getHills()) {
    		if (! (hill.getType() == ABType.HILL && hill.shape == ABShape.Poly)) 
    			continue;
    		
    		Poly    poly = (Poly) hill;
    		Polygon p    = poly.polygon;
    		
    		// we shall only have linear segments in our polygons!!!
    		for (int i = 0; i < p.npoints - 1; i++) {
    			// if the segment is left of the leftmost pig, we don't care
    			// anyways
    			if (Math.min(p.xpoints[i], p.xpoints[i + 1]) < leftmost_pig_x) {
    				continue;
    			}
    			
    			// compute gradient for next segment
    			// make sure that gradient can be computed at all!
    			if (p.xpoints[i] == p.xpoints[i + 1]) {
    				// gradient computation would result in devision by zero
    				continue;
    			}
    			
    			float dy       = (float) (p.ypoints[i + 1] - p.ypoints[i]);
    			float dx       = (float) (p.xpoints[i + 1] - p.xpoints[i]);
    			float gradient = dy / dx;
    			
    			// mind that we are in the strange coordinate system of Java
    			// here, a gradient smaller than zero indicates a monotonically
    			// increasing line from "lower left to upper right" of the 
    			// screen.
    			if (gradient >= 0.0f) {
    				// this is not a backward edge
    				continue;
    			}
    			
    			// find box2d body that is backing the current hill structure
    			Body cur_bdy = null;
    			for (Body b : box2d_hills) {
    				SimulationUserData sud = (SimulationUserData) b.getUserData();
    				if (sud.getABObject() == hill) {
    					cur_bdy = b;
    					break;
    				}
    			}
    			
    			if (cur_bdy == null) {
    				continue;
    			}
    			else {
    				// only try high shots on backward edges.
    				// disabling straight shots might be a disadvantage, but
    				// with the current state of simulation, the solution
    				// below works better. TODO: re-evaluate later on!!!
    				SimulationUserData sud = 
    						(SimulationUserData) cur_bdy.getUserData();
    				sud.setLeftSide(false);
    				sud.setUpperSide(true);
    			}
    			
    			// equally distribute a constant number of target points on the
    			// edge
    			for (int j = 0; j < PTS_PER_BACKWARD_EDGE; j++) {
    				float offs_y = (dy / (PTS_PER_BACKWARD_EDGE - 1) * j);
    				float offs_x = (dx / (PTS_PER_BACKWARD_EDGE - 1) * j);
    				
    				bwe_targets.put(new Point((int) (p.xpoints[i] + offs_x),
    										  (int) (p.ypoints[i] + offs_y)),
    						        cur_bdy);
    			}
    		}
    	}
    	
    	return(bwe_targets);
    }
    
    /**
     * Coalesces lists of sectors used for reachability analysis.
     * 
     * @param sectors
     */
    private void coalesce(List<Point> sectors) {
        if (sectors.size() < 2) {
            return;
        }

        sectors.sort(new Comparator<Point>() {
            public int compare(Point p1, Point p2) {
                if (p1.x < p2.x) {
                    return (-1);
                } else if (p1.x > p2.x) {
                    return (1);
                } else {
                    return (0);
                }
            }
        });

        int i = 0;
        while (i < sectors.size() - 1) {
            Point p1 = sectors.get(i);
            Point p2 = sectors.get(i + 1);

            if (p1.x <= p2.x && p1.y >= p2.y) {
                sectors.remove(p2);
                continue;
            } else if (p1.y >= p2.x) {
                // v1.y can not be greater than v2.y here
                p1.y = p2.y;
                sectors.remove(p2);
                continue;
            } else {
                i++;
            }
        }
    }

    /**
     * Partitions a list of bodies into connected components. Here, a set of bodies
     * is considered a graph with bodies representing vertices and contact points as
     * edges. Each components is represented by an own list in the returned
     * collection. The input list <code>body</code> will be empty after the
     * invocation of this method.
     * 
     * @param bodies
     * @return
     */
    private List<List<Body>> partition(List<Body> bodies) {
        List<List<Body>> components = new LinkedList<List<Body>>();

        while (!bodies.isEmpty()) {
            Body b = bodies.get(0);
            boolean seen = false;
            int idx = -1;

            bodies.remove(b);

            for (List<Body> blist : components) {
                if (blist.contains(b)) {
                    seen = true;
                    idx = components.lastIndexOf(blist);
                    break;
                }
            }

            if (!seen) {
                List<Body> nlist = new LinkedList<Body>();
                nlist.add(b);
                components.add(nlist);
                idx = components.lastIndexOf(nlist);
            }

            List<Body> clist = components.get(idx);
            List<Body> nbodies = new LinkedList<Body>();
            nbodies.add(b);
            while (!nbodies.isEmpty()) {
                b = nbodies.get(0);
                nbodies.remove(b);
                ContactEdge ce = b.getContactList();
                while (ce != null) {
                    // again, do not consider static objects here!
                    SimulationUserData sud = (SimulationUserData) ce.other.getUserData();
                    ABType type = sud.getABObject().type;
                    if (type == ABType.HILL || type == ABType.GROUND || type == ABType.SLING
                            || type == ABType.SUPPORT) {
                        ce = ce.next;
                        continue;
                    }

                    if (!clist.contains(ce.other)) {
                        clist.add(ce.other);
                        if (!nbodies.contains(ce.other)) {
                            nbodies.add(ce.other);
                        }
                    }
                    ce = ce.next;
                }
            }
        }

        return (components);
    }

}

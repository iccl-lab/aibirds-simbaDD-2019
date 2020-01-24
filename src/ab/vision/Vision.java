/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Rect;

public class Vision {
    private BufferedImage image;
    private VisionMBR visionMBR = null;
    
    private VisionRealShape visionRealShape = null;
    
    public Vision(BufferedImage image) {
        this.image = image;
    }

    public List<ABObject> findBirdsMBR() {
        if (visionMBR == null) {
            visionMBR = new VisionMBR(image);
        }
        return visionMBR.findBirds();

    }

    /**
     * @return a list of MBRs of the blocks in the screenshot. Blocks: Stone, Wood,
     *         Ice
     */
    public List<ABObject> findBlocksMBR() {
        if (visionMBR == null) {
            visionMBR = new VisionMBR(image);
        }
        return visionMBR.findBlocks();
    }

    public List<ABObject> findTNTs() {
        if (visionMBR == null) {
            visionMBR = new VisionMBR(image);
        }
        return visionMBR.findTNTs();
    }

    public List<ABObject> findPigsMBR() {
        if (visionMBR == null) {
            visionMBR = new VisionMBR(image);
        }
        return visionMBR.findPigs();
    }

    public List<ABObject> findPigsRealShape() {
        if (visionRealShape == null) {
            visionRealShape = new VisionRealShape(image);
        }

        return visionRealShape.findPigs();
    }

    public Queue<Circle> findBirdsRealShape() {
        if (visionRealShape == null) {
            visionRealShape = new VisionRealShape(image);
        }

        return(visionRealShape.findBirds());
    }

    public List<ABObject> findHills() {
        if (visionRealShape == null) {
            visionRealShape = new VisionRealShape(image);
        }

        return visionRealShape.findHills();
    }

    public Rectangle findSlingshotMBR() {
        if (visionMBR == null) {
            visionMBR = new VisionMBR(image);
        }
        return visionMBR.findSlingshotMBR();
    }

    public List<Point> findTrajPoints() {
        if (visionMBR == null) {
            visionMBR = new VisionMBR(image);
        }
        return visionMBR.findTrajPoints();
    }

    /**
     * @return a list of real shapes (represented by Body.java) of the blocks in the
     *         screenshot. Blocks: Stone, Wood, Ice
     */
    public List<ABObject> findBlocksRealShape() {
        if (visionRealShape == null) {
            visionRealShape = new VisionRealShape(image);
        }
        List<ABObject> allBlocks = visionRealShape.findObjects();

        return allBlocks;
    }

    public VisionMBR getMBRVision() {
        if (visionMBR == null)
            visionMBR = new VisionMBR(image);
        return visionMBR;
    }
    
    /**
     * Finds indestructible support platforms inside the levels (see level 1 for
     * an example)
     * 
     * @return
     */
    public List<ABObject> findSupport() {
        int nHeight = this.image.getHeight();
        int nWidth  = this.image.getWidth();
        
        List<Point> candidatePixels = new ArrayList<Point>();
        for (int x = 0; x < nWidth; x++) {
            for (int y = 0; y < nHeight; y++) {
            	Color col = new Color(this.image.getRGB(x,y));
                final int r = col.getRed();
                final int g = col.getGreen();
                final int b = col.getBlue();

                if ((r == 184 && g == 113 && b == 56) ||
                    (r == 230 && g == 157 && b == 99) ||
                    (r == 147 && g == 90 && b == 44) ||
                    (r == 140 && g == 86 && b == 42) ||
                    (r == 215 && g == 143 && b == 86) ||
                    (r == 180 && g == 123 && b == 79) ||
                    (r == 192 && g == 131 && b == 84)) {
                    	candidatePixels.add(new Point(x, y));
                }
            }
        }

        List<Rectangle> sups = new ArrayList<Rectangle>();

        for (int i=0; i < candidatePixels.size(); i++) {
            Rectangle sup = new Rectangle(candidatePixels.get(i).x, 
            		                      candidatePixels.get(i).y, 1, 1);
            sups.add(sup);
            candidatePixels.remove(i); 
            i--;

            boolean grew = true;
            while (grew) {
                grew = false;
                for (int j=0; j<candidatePixels.size(); j++) {
                    Point cp = candidatePixels.get(j);
                    if (sup.contains(cp)) {
                        candidatePixels.remove(j); j--;
                        continue;
                    }

                    //left
                    if ((sup.x - cp.x == 1) && (cp.y >= sup.y) && 
                        (cp.y <= sup.y+sup.height)) {
                        sup.x -= 1;
                        sup.width += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }

                    //right
                    if ((cp.x - (sup.x+sup.width) == 1) && (cp.y >= sup.y) && 
                        (cp.y <= sup.y+sup.height)) {
                        sup.width += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }

                    //top
                    if ((sup.y - cp.y == 1) && (cp.x >= sup.x) && 
                    	(cp.x <= sup.x+sup.width)) {
                        sup.y -= 1;
                        sup.height += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }
                    
                    //bottom
                    if ((cp.y - (sup.y+sup.height) == 1) && (cp.x >= sup.x) && 
                    	(cp.x <= sup.x+sup.width)) {
                        sup.height += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }
                }
            }
        }

        List<ABObject> ab_supps = new ArrayList<ABObject>();
        for (Rectangle sup : sups) {
        	Rect r = new Rect(sup.x + sup.width / 2d, 
        			          sup.y + sup.height / 2d,
        			          (double) sup.width, 
        			          (double) sup.height, 
        			          Math.toRadians(90.0), 
        			          ABType.SUPPORT);
        	ab_supps.add(r);
        }
        return ab_supps;
    }
    
    /**
     * @return the type of the bird that is the next one to be shot.
     */
    public ABType getBirdTypeOnSling() {
    	List<ABObject> birds = new LinkedList<ABObject>(findBirdsRealShape());
        if (birds.isEmpty())
            return ABType.UNKNOWN;
        
        Collections.sort(birds, new Comparator<ABObject>() {
        	public int compare(ABObject o1, ABObject o2) {
        		int o1_center_x = o1.getCenter().x;
        		int o2_center_x = o2.getCenter().x;
        		if (o1_center_x > o2_center_x)
        			return(-1);
        		else if (o1_center_x < o2_center_x)
        			return(1);
        		else
        			return(0);
        	}
        });
        System.out.println("bird types:");
        for (ABObject abo : birds)
        	System.out.println(abo.getType());
        return birds.get(0).getType();
    }
    
    /**
     * Alternative method for getting the species of the bird currently shot,
     * taken from ihsev.
     * 
     * @return
     */
    public ABType detectShootingBirdSpecies() {
        Rectangle sling = findSlingshotMBR();

        if (sling == null) 
        	return ABType.UNKNOWN;

        final int[][] redBirds = {{165, 19, 51}, {136, 1, 29}, {214, 0, 45}, 
        		                  {211, 0, 44}, {208, 0, 44}, {204, 0, 42}, {200, 0, 41}, 
        		                  {195, 0, 41}, {193, 0, 40}, {187, 0, 39}, {185, 0, 38},
        		                  {168, 0, 35}, {163, 0, 34}, {156, 2, 31}, {154, 0, 32},
        		                  {141, 4, 28}, {131, 0, 27}, {104, 0, 21}, {100, 0, 21},
        		                  {92, 0, 19}, {87, 0, 18}};
        final int[][] blueBirds = {{97, 167, 194}, {94, 163, 189}, {89, 153, 177},
        		                   {88, 103, 83}, {86, 143, 162}, {64, 107, 124},
        		                   {50, 83, 96}, {99, 170, 197}, {96, 108, 113},
        		                   {95, 164, 190}, {78, 134, 154}, {68, 98, 109}};
        final int[][] yellowBirds = {{243, 223, 54}, {242, 221, 41}, {241, 219, 32},
        		                     {238, 217, 31}, {219, 199, 29}, {218, 201, 48}, 
        		                     {200, 181, 26}, {154, 135, 20}, {140, 131, 53},
        		                     {120, 109, 16}, {245, 232, 111}, {243, 230, 115},
        		                     {241, 220, 37}, {238, 212, 30}, {234, 200, 28},
        		                     {216, 200, 57}, {202, 184, 26}, {201, 182, 26},
        		                     {150, 136, 20}, {219, 197, 28}, {118, 110, 38}};
        final int[][] whiteBirds = {{253, 251, 235}, {245, 237, 157}, {243, 241, 223},
        		                    {236, 233, 203}, {232, 229, 199}, {226, 223, 194},
        		                    {216, 215, 210}, {205, 199, 132}, {165, 163, 142},
        		                    {225, 222, 193}, {248, 242, 183}, {247, 240, 170},
        		                    {246, 238, 158}, {241, 234, 154}, {237, 234, 206},
        		                    {229, 226, 197}, {225, 218, 144}};
        final int[][] blackBirds = {{67, 67, 67}, {63, 63, 63}, {61, 61, 61}, 
        		                    {23, 23, 23}, {17, 17, 17}, {13, 13, 13},
        		                    {10, 5, 2}, {8, 3, 1}, {1, 1, 1}, {13, 9, 0},
        		                    {66, 66, 66}, {62, 62, 62}, {22, 22, 22},
        		                    {18, 18, 18}, {4, 4, 4}, {3, 2, 1}};

        int[] probably = {0, 0, 0, 0, 0};

        for (int x = sling.x; x < (sling.x + sling.width); ++x)
        {
            for (int y = sling.y; y < (sling.y + sling.height / 2); ++y)
            {
                Color col = new Color(image.getRGB(x, y));
                final int r = col.getRed();
                final int g = col.getGreen();
                final int b = col.getBlue();

                for (int i = 0; i < redBirds.length; ++i)
                {
                    if ((r == redBirds[i][0]) && (g == redBirds[i][1]) && (b == redBirds[i][2]))
                    {
                        //return "RED_BIRD";
                        probably[0]++;
                        break;
                    }
                }

                for (int i=0; i<blueBirds.length; ++i)
                {
                    if ((r == blueBirds[i][0]) && (g == blueBirds[i][1]) && (b == blueBirds[i][2]))
                    {
                        //return "BLUE_BIRD";
                        probably[1]++;
                        break;
                    }
                }

                for (int i=0; i<yellowBirds.length; ++i)
                {
                    if ((r == yellowBirds[i][0]) && (g == yellowBirds[i][1]) && (b == yellowBirds[i][2]))
                    {
                        //return "YELLOW_BIRD";
                        probably[2]++;
                        break;
                    }
                }

                for (int i=0; i<whiteBirds.length; ++i)
                {
                    if ((r == whiteBirds[i][0]) && (g == whiteBirds[i][1]) && (b == whiteBirds[i][2]))
                    {
                        //return "WHITE_BIRD";
                        probably[3]++;
                        break;
                    }
                }

                for (int i=0; i<blackBirds.length; ++i)
                {
                    if ((r == blackBirds[i][0]) && (g == blackBirds[i][1]) && (b == blackBirds[i][2]))
                    {
                        //return "BLACK_BIRD";
                        probably[4]++;
                        break;
                    }
                }
            }
        }
        int maxProb = 0;
        int maxI = 0;
        for (int i=0; i<probably.length; ++i)
        {
            if (probably[i] > maxProb)
            {
                maxProb = probably[i];
                maxI = i;
            }
        }
        
        if (maxProb > 0) {
            switch (maxI) {
                case 0: return ABType.RED_BIRD;
                case 1: return ABType.BLUE_BIRD;
                case 2: return ABType.YELLOW_BIRD;
                case 3: return ABType.WHITE_BIRD;
                case 4: return ABType.BLACK_BIRD;
            }
        }

        return ABType.UNKNOWN;
    }
}

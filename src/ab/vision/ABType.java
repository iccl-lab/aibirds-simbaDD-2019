/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision;

public enum ABType {

    GROUND(1), HILL(2), SLING(3), RED_BIRD(4), YELLOW_BIRD(5), BLUE_BIRD(6), BLACK_BIRD(7), WHITE_BIRD(8), PIG(9),
    ICE(10), WOOD(11), STONE(12), TNT(18), SUPPORT(19), EGG(30), UNKNOWN(0);
    public int id;

    private ABType(int id) {
        this.id = id;
    }
    
    public static boolean isBird(ABType id) {
    	return(id == RED_BIRD || id == YELLOW_BIRD || id == BLUE_BIRD ||
    		   id == BLACK_BIRD || id == WHITE_BIRD);
    }
}

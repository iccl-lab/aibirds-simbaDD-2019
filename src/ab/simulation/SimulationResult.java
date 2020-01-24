package ab.simulation;

import java.util.ArrayList;
import java.util.List;

import ab.demo.other.Shot;

public class SimulationResult implements Comparable<SimulationResult> {

    private Integer score = 0;
    private List<Shot> shots = new ArrayList<Shot>();

    public SimulationResult(int score, List<Shot> shots) {
        this.score = score;
        this.shots = shots;
    }

    @Override
    public int compareTo(SimulationResult otherResult) {
        // if this score is higher it should come first when sorting
        if (this.score > otherResult.score) {
            return -1;
        } else if (this.score < otherResult.score) {
            return 1;
        }
        return 0;
    }

    public int getScore() {
        return score.intValue();
    }

    public List<Shot> getShot() {
        return shots;
    }

    @Override
    public String toString() {
    	StringBuffer str = new StringBuffer();
    	str.append("SimulationResult: ");
    	str.append(score);
    	str.append("\n");
    	
    	for (Shot shot : shots) {
    		str.append(shot);
    		str.append("\n");
    	}
    	
    	return str.toString();
    }
}

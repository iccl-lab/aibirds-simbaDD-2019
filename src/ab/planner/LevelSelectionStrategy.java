package ab.planner;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import ab.demo.other.CompetitionParameters;
import ab.simulation.SimulationResult;
import ab.vision.GameStateExtractor.GameState;

/**
 * This class acts as basic 
 * @author lschweizer
 *
 */
public abstract class LevelSelectionStrategy {
	
	protected int currentLevel = 0;
	protected int prevUpdatedLevel = 0;
	protected int timesCurrentLevelPlayed = 0;
	
	protected CompetitionParameters compParams;
	
	protected int totalScore = 0;
	// record the last state of the level
	protected Map<Integer, GameState> levelStates = new HashMap<Integer, GameState>();
	// record the highscore achieved in the levels
	protected Map<Integer, Integer> levelHighScores = new HashMap<Integer, Integer>();
	// record how often a level has been played
	protected Map<Integer, Integer> levelsPlayed = new HashMap<Integer, Integer>();
	
	// these are always the last known best scores retrieved from the server
	protected int[] bestScores = null;
	
	public LevelSelectionStrategy() {
	}

	public LevelSelectionStrategy(int startWithLevel) {
		this.currentLevel = startWithLevel;
	}
	
	public int getCurrentLevel() {
		return currentLevel;
	}
	
	public void setCompetitionParameters(CompetitionParameters params) {
		this.compParams = params;
	}
	
	public abstract SimulationResult selectBestShot(PriorityQueue<SimulationResult> rankedShots);
	
	private void updateTotalScore() {
		totalScore = 0;
		for (Integer level : levelHighScores.keySet()) {
			totalScore += levelHighScores.get(level);
		}
	}
	
	/**
	 * Decide, whether or not to play a (lost) level again.
	 * If <i>false</i>, use @link {@link LevelSelectionStrategy#nextLevel()} to decide on
	 * whic level to play next.
	 * @return false, in case the current level should not be tried again. true, otherwise.
	 */
	public abstract boolean restartLevel();
	
	/**
	 * 
	 * @param level 
	 * @param state Should be either WON or LOST, see {@link GameState}.
	 * @param score The score for the current level. If LOST a score of 0 is assumed.
	 */
	public void updateLevelInfo(int level, GameState state, int score, int[] bestScores) {
		levelStates.put(level, state);
		
		int nPlayed = 1;
		if (levelsPlayed.containsKey(level))
			nPlayed = levelsPlayed.get(level)+1;
		levelsPlayed.put(level, nPlayed);
		
		if (GameState.WON == state) {
			
			if (levelHighScores.containsKey(level)) {
				if (levelHighScores.get(level) < score)
					levelHighScores.put(level, score);
			} else {
				levelHighScores.put(level, score);
			}
		}	
		
		this.bestScores = bestScores;
		
		if (level == prevUpdatedLevel)
			timesCurrentLevelPlayed++;
		else {
			prevUpdatedLevel = level;
			timesCurrentLevelPlayed = 1;
		}
		
		updateTotalScore();
	}	
	
	/**
	 * @return The next level the strategy suggests to play.
	 */
	public abstract int nextLevel();
}

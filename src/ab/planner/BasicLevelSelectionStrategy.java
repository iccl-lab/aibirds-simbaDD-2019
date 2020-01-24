package ab.planner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import ab.simulation.SimulationResult;

public class BasicLevelSelectionStrategy extends LevelSelectionStrategy {
	
	private boolean clientMode = false;
	
	private final static int LEVEL_MAX_RETRIES = 2;
	
	public BasicLevelSelectionStrategy(boolean clientMode) {
		super();
		this.clientMode = clientMode;
	}

	/**
	 * In this impl. we play each level once, and then decide which level to play
	 * again:
	 */
	@Override
	public int nextLevel() {
		System.out.println("Own Best Scores: " + this.levelHighScores);
		System.out.println("Overall Best Scores: " + Arrays.toString(bestScores));

		if (!clientMode) {
			return currentLevel++;
		}
		
		if (compParams.getNumLeves() > currentLevel + 1)
			return currentLevel += 1;
		else {
			// go to the level where our own score differs most to the leaderboard
			
			if (null == this.bestScores)
				return this.currentLevel = 0;

			int maxDiff = 0;
			int maxDiffLevel = 0;
			for (int level = 0; level < compParams.getNumLeves(); level++) {
				int bestScore = this.bestScores[level];
				int ownBestScore = this.levelHighScores.get(level);

				if (bestScore > ownBestScore) {
					int diff = bestScore - ownBestScore;
					maxDiff = diff > maxDiff ? diff : maxDiff;
					maxDiffLevel = level;
				}
			}

			return currentLevel = maxDiffLevel;
		}
	}
	
	@Override
	public boolean restartLevel() {		
		return timesCurrentLevelPlayed <= LEVEL_MAX_RETRIES;
	}
	
	/**
	 * Get the top ranked, equal score, shots.
	 * @param rankedShots
	 * @return
	 */
	private List<SimulationResult> getNBestShots(PriorityQueue<SimulationResult> rankedShots) {
		ArrayList<SimulationResult> nbest = new ArrayList<SimulationResult>();
		
		boolean isFirst = true;
		SimulationResult best = null;
		while (!rankedShots.isEmpty()) {
			SimulationResult current = rankedShots.poll();
			
			if (isFirst) {
				best = current;
				nbest.add(current);
				isFirst = false;
			} else {
				if (current.getScore() != best.getScore()) 
					return nbest;
				else 
					nbest.add(current);
			}
		}
		
		return nbest;
	}

	@Override
	public SimulationResult selectBestShot(PriorityQueue<SimulationResult> rankedShots) {
		int nPlayed=0;
		if (levelsPlayed.containsKey(currentLevel))
			nPlayed = levelsPlayed.get(currentLevel);

		System.out.println(String.format("Level %d has been played %d times", currentLevel+1, nPlayed));
		
		List<SimulationResult> nbest = getNBestShots(rankedShots);
		
		System.out.println(nbest);
		
		Random rnd = new Random();
		return nbest.get(rnd.nextInt(nbest.size()));
	}
}

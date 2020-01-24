package ab.demo.other;

import java.awt.image.BufferedImage;

import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;

public class ActionRobotWrapper {
    private ActionRobot actionRobot;
    private ClientActionRobotJava clientActionRobotJava;
    private final boolean isClient;
    private int teamId;
    private int[] scores = new int[63];

    public ActionRobotWrapper(boolean isClient) {
        // the default ip is the localhost
        if (isClient) {
            clientActionRobotJava = new ClientActionRobotJava("127.0.0.1");
        } else {
            actionRobot = new ActionRobot();
            actionRobot.loadLevel(1);
        }
        this.isClient = isClient;
    }

    public ActionRobotWrapper(String ip, int teamId) {
        this.teamId = teamId;
        clientActionRobotJava = new ClientActionRobotJava(ip);
        isClient = true;
    }

    public CompetitionParameters getCompetitionParameters() {
        CompetitionParameters cp = new CompetitionParameters();

        if (isClient) {
            byte[] serverInfo = clientActionRobotJava.configure(ClientActionRobot.intToByteArray(teamId));
            cp.setRoundType(serverInfo[0]);
            cp.setTimeLimitMinutes(serverInfo[1]);
            cp.setNumLeves(serverInfo[2]);
        }

        return cp;
    }

    public void loadLevel(int level) {
        if (isClient) {
            clientActionRobotJava.loadLevel((byte) level);
        } else {
            actionRobot.loadLevel(level);
        }
    }

    public int getScore(int level) {
        if (isClient) {
            int[] myScores = clientActionRobotJava.checkMyScore();
            return myScores[level];
        } else {
            int score = actionRobot.getScore();
            try {
                this.scores[level] = score;
            } catch (Exception e) {
                // TODO: handle exception
                // out of bounds exception
                int[] prev_scores = this.scores;
                this.scores = new int[level];
                // copy scores from previous
                for (int i = 0; i < prev_scores.length; ++i) {
                    this.scores[i] = prev_scores[i];
                }
                this.scores[level] = score;
            }
            return actionRobot.getScore();
        }
    }

    public void restartLevel() {
        if (isClient) {
            clientActionRobotJava.restartLevel();
        } else {
            actionRobot.restartLevel();
        }
    }

    public GameState getState() {
        GameState gameState;
        if (isClient) {
            gameState = clientActionRobotJava.checkState();
        } else {
            gameState = actionRobot.getState();
        }
        return gameState;
    }

    public ABType getBirdTypeOnSling() {
        ABType birdType = null;
        if (isClient) {
            birdType = clientActionRobotJava.getBirdTypeOnSling();
        } else {
            birdType = actionRobot.getBirdTypeOnSling();
        }
        return birdType;
    }

    public int[] checkMyScore() {
        if (isClient) {
            return clientActionRobotJava.checkMyScore();
        } else {
            return this.scores;
        }
    }

    public byte[] configure(int team_id) {
        if (isClient) {
            return clientActionRobotJava.configure(ClientActionRobotJava.intToByteArray(team_id));
        } else {
            // TODO local version?
            throw new UnsupportedOperationException(
                    "This function is not supported for local communication with the game");
        }
    }

    public int[] checkScore() {
        if (isClient) {
            return clientActionRobotJava.checkScore();
        } else {
            return this.scores;
        }
    }

    public BufferedImage doScreenShot() {
        if (isClient) {
            return clientActionRobotJava.doScreenShot();
        } else {
            return ActionRobot.doScreenShot();
        }
    }

    public GameState checkState() {
        if (isClient) {
            return clientActionRobotJava.checkState();
        } else {
            return actionRobot.getState();
        }
    }

    public void fullyZoomOut() {
        if (isClient) {
            clientActionRobotJava.fullyZoomOut();
        } else {
            ActionRobot.fullyZoomOut();
        }
    }

    public void fullyZoomIn() {
        if (isClient) {
            clientActionRobotJava.fullyZoomIn();
        } else {
            ActionRobot.fullyZoomIn();
        }
    }

    public void click() {
        if (isClient) {
            clientActionRobotJava.clickInCenter();
        } else {
            actionRobot.click();
        }
    }

    public void GoFromMainMenuToLevelSelection() {
        if (isClient) {

        } else {
            ActionRobot.GoFromMainMenuToLevelSelection();
        }
    }

    public void shoot(int x, int y, int dx, int dy, int t_shot, int t_tap, boolean polar) {
        if (isClient) {
            clientActionRobotJava.shoot(x, y, dx, dy, t_shot, t_tap, polar);
        } else {
            Shot shot = new Shot(x, y, dx, dy, t_shot, t_tap);
            actionRobot.cshoot(shot);
        }
    }

    public void shootFast(int x, int y, int dx, int dy, int t_shot, int t_tap, boolean polar) {
        if (isClient) {
            clientActionRobotJava.shootFast(
                    ClientActionRobotJava.intToByteArray(x),
                    ClientActionRobotJava.intToByteArray(y),
                    ClientActionRobotJava.intToByteArray(dx),
                    ClientActionRobotJava.intToByteArray(dy),
                    ClientActionRobotJava.intToByteArray(t_shot),
                    ClientActionRobotJava.intToByteArray(t_tap),
                    polar);
        } else {
            Shot shot = new Shot(x, y, dx, dy, t_shot, t_tap);
            actionRobot.cFastshoot(shot);
        }
    }

    public void shoot(Shot shot) {
        shoot(shot.getX(), shot.getY(), shot.getDx(), shot.getDy(), shot.getT_shot(), shot.getT_tap(), false);
    }

}

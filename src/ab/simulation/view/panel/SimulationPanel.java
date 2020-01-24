package ab.simulation.view.panel;

import java.awt.AWTError;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import org.jbox2d.common.Vec2;
import org.jbox2d.testbed.framework.TestbedCamera;
import org.jbox2d.testbed.framework.TestbedCamera.ZoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ab.simulation.model.level.GenericLevel;
import ab.simulation.utils.Constants;
import ab.simulation.utils.SimUtil;
import ab.simulation.view.draw.SimulationDrawer;
import ab.simulation.view.settings.ViewSettings;

@SuppressWarnings("serial")
public class SimulationPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(SimulationPanel.class);

    private static final int INIT_WIDTH = Constants.WINDOW_WIDTH;
    private static final int INIT_HEIGHT = Constants.WINDOW_HEIGHT;

    private Graphics2D dbg = null;
    private Image image = null;

    private int panelWidth;
    private int panelHeight;
    private SimulationDrawer simulationDrawer;

    // right mouse button
    public static final int SCREEN_DRAG_BUTTON = MouseEvent.BUTTON3;
    public static final float ZOOM_SCALE_DIFF = .05f;
    private TestbedCamera testbedCamera;

    public SimulationPanel(ViewSettings drawingSettings, float initialScale) {
        simulationDrawer = new SimulationDrawer(drawingSettings, this, true);
        testbedCamera = new TestbedCamera(
                SimUtil.convertViewToModel(new Vec2(INIT_WIDTH / 2, INIT_HEIGHT / 2), initialScale), initialScale,
                .05f);
        simulationDrawer.setViewportTransform(testbedCamera.getTransform());
        setBackground(DefaultSimulationViewColors.BACKGROUND_COLOR);
        setPreferredSize(new Dimension(INIT_WIDTH, INIT_HEIGHT));
        updateSize(INIT_WIDTH, INIT_HEIGHT);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize(getWidth(), getHeight());
                image = null;
            }
        });
        initListeners();
    }

    private final Vec2 mouse = new Vec2();
    private final Vec2 oldDragMouse = new Vec2();
    private boolean screenDragButtonDown = false;

    /**
     * initializes private Listeners that change the drawing state of the buttons
     * for example switching the text between "Pause" and "Resume"
     */
    private void initListeners() {
        this.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                ZoomType zoom = notches < 0 ? ZoomType.ZOOM_IN : ZoomType.ZOOM_OUT;
                testbedCamera.zoomToPoint(new Vec2(e.getX(), e.getY()), zoom);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                if (arg0.getButton() == SCREEN_DRAG_BUTTON) {
                    screenDragButtonDown = false;
                }
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (arg0.getButton() == SCREEN_DRAG_BUTTON) {
                    screenDragButtonDown = true;
                    oldDragMouse.set(arg0.getX(), arg0.getY());
                    return;
                }

            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent arg0) {
                mouse.set(arg0.getX(), arg0.getY());
            }

            @Override
            public void mouseDragged(MouseEvent arg0) {
                mouse.set(arg0.getX(), arg0.getY());
                if (screenDragButtonDown) {
                    Vec2 diff = oldDragMouse.sub(mouse);
                    testbedCamera.moveWorld(diff);
                    oldDragMouse.set(mouse);
                }
            }

        });
    }

    private void updateSize(int width, int height) {
        panelWidth = width;
        panelHeight = height;
        updateExtents(width / 2, height / 2);
    }

    public void updateExtents(float halfWidth, float halfHeight) {
        testbedCamera.getTransform().setExtents(halfWidth, halfHeight);
    }

    public void paintScreen() {
        try {
            Graphics g = this.getGraphics();
            if ((g != null) && image != null) {
                g.drawImage(image, 0, 0, null);
                Toolkit.getDefaultToolkit().sync();
                g.dispose();
            }
        } catch (AWTError e) {
            log.error("Graphics context error", e);
        }
    }

    public boolean render() {
        if (image == null) {
            log.debug("image is null, creating a new one");
            if (panelWidth <= 0 || panelHeight <= 0) {
                return false;
            }
            image = createImage(panelWidth, panelHeight);
            if (image == null) {
                log.error("dbImage is still null, ignoring render call");
                return false;
            }
            dbg = (Graphics2D) image.getGraphics();
            dbg.setFont(new Font("Courier New", Font.PLAIN, 12));
        }
        dbg.setColor(DefaultSimulationViewColors.BACKGROUND_COLOR);
        dbg.fillRect(0, 0, panelWidth, panelHeight);
        return true;
    }

    public Graphics2D getDBGraphics() {
        // TODO Auto-generated method stub
        return dbg;
    }

    public void drawLevel(GenericLevel level) {
        // TODO Auto-generated method stub
        simulationDrawer.drawLevel(level);
    }

}

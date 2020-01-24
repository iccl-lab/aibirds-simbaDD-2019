package ab.simulation.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedModel.ListItem;
import org.jbox2d.testbed.framework.TestbedSetting;
import org.jbox2d.testbed.framework.TestbedSetting.SettingType;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

import ab.simulation.controller.SimulationInputListeners;
import ab.simulation.view.settings.ViewSetting;
import ab.simulation.view.settings.ViewSettings;

@SuppressWarnings("serial")
public class SimulationSidePanel extends JPanel implements ActionListener {

    private static final String SETTING_TAG = "settings";
    private static final String LABEL_TAG = "label";

    public JComboBox<ListItem> tests;

    private JButton pauseButton = new JButton("Pause");
    private JButton stepButton = new JButton("Step");
    private JButton resetButton = new JButton("Reset");
    private JButton quitButton = new JButton("Quit");

    public JButton saveButton = new JButton("Save");
    public JButton loadButton = new JButton("Load");

    // TODO remove model from view
    private TestbedModel model = new TestbedModel();

    public SimulationSidePanel(ViewSettings drawingSettings) {
        createComponents(drawingSettings);
        initListeners();
    }

    public void initTop(TestbedSettings settings) {
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(0, 1));
        top.setBorder(
                BorderFactory.createCompoundBorder(
                        new EtchedBorder(EtchedBorder.LOWERED),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        tests = new JComboBox<ListItem>(model.getComboModel());
        tests.setMaximumRowCount(30);
        tests.setMaximumSize(new Dimension(250, 20));
        tests.addActionListener(this);
        tests.setRenderer(new ListCellRenderer<Object>() {
            JLabel categoryLabel = null;
            JLabel testLabel = null;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object ovalue, int index, boolean isSelected,
                    boolean cellHasFocus) {
                ListItem value = (ListItem) ovalue;
                if (value.isCategory()) {
                    if (categoryLabel == null) {
                        categoryLabel = new JLabel();
                        categoryLabel.setOpaque(true);
                        categoryLabel.setBackground(new Color(.5f, .5f, .6f));
                        categoryLabel.setForeground(Color.white);
                        categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        categoryLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                    }
                    categoryLabel.setText(value.category);
                    return categoryLabel;
                } else {
                    if (testLabel == null) {
                        testLabel = new JLabel();
                        testLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 1, 0));
                    }

                    testLabel.setText(value.test.getTestName());

                    if (isSelected) {
                        testLabel.setBackground(list.getSelectionBackground());
                        testLabel.setForeground(list.getSelectionForeground());
                    } else {
                        testLabel.setBackground(list.getBackground());
                        testLabel.setForeground(list.getForeground());
                    }
                    return testLabel;
                }
            }
        });

        top.add(new JLabel("Choose a test:"));
        top.add(tests);

        addSettings(top, settings, SettingType.ENGINE);

        add(top, "North");
    }

    private void createMiddleComponents(ViewSettings drawingSettings) {
        JPanel middle = new JPanel();
        middle.setLayout(new GridLayout(0, 1));
        middle.setBorder(
                BorderFactory.createCompoundBorder(
                        new EtchedBorder(EtchedBorder.LOWERED),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        addSettings(middle, drawingSettings);

        add(middle, "Center");
    }

    /**
     * creates the JButtons on the bottom
     */
    private void createBottomComponents() {
        pauseButton.setAlignmentX(CENTER_ALIGNMENT);
        stepButton.setAlignmentX(CENTER_ALIGNMENT);
        resetButton.setAlignmentX(CENTER_ALIGNMENT);
        saveButton.setAlignmentX(CENTER_ALIGNMENT);
        loadButton.setAlignmentX(CENTER_ALIGNMENT);
        quitButton.setAlignmentX(CENTER_ALIGNMENT);

        Box buttonGroups = Box.createHorizontalBox();
        JPanel buttons1 = new JPanel();
        buttons1.setLayout(new GridLayout(0, 1));
        buttons1.add(resetButton);

        JPanel buttons2 = new JPanel();
        buttons2.setLayout(new GridLayout(0, 1));
        buttons2.add(pauseButton);
        buttons2.add(stepButton);

        JPanel buttons3 = new JPanel();
        buttons3.setLayout(new GridLayout(0, 1));
        buttons3.add(saveButton);
        buttons3.add(loadButton);
        buttons3.add(quitButton);

        buttonGroups.add(buttons1);
        buttonGroups.add(buttons2);
        buttonGroups.add(buttons3);

        add(buttonGroups, "South");
    }

    public void createComponents(ViewSettings drawingSettings) {
        // TODO remove TestbedModel and replace it with SimulationModel or remove the
        // dropdown menu

        model.addTest(new TestbedTest() {

            @Override
            public void initTest(boolean arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public String getTestName() {
                // TODO Auto-generated method stub
                return "????";
            }
        });
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        TestbedSettings settings = model.getSettings();
        initTop(settings);
        createMiddleComponents(drawingSettings);
        createBottomComponents();
    }

    /**
     * initializes private Listeners that change the drawing state of the buttons
     * for example switching the text between "Pause" and "Resume"
     */
    private void initListeners() {
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (pauseButton.getText() == "Resume") {
                    pauseButton.setText("Pause");
                } else {
                    pauseButton.setText("Resume");
                }
            }
        });
        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (pauseButton.getText() != "Pause") {
                    pauseButton.setText("Pause");
                }
            }
        });
        /*
         * quitButton.addActionListener(new ActionListener() {
         * 
         * @Override public void actionPerformed(ActionEvent e) { System.exit(0); } });
         */
    }

    private void addSettings(JPanel argPanel, TestbedSettings argSettings, SettingType settingType) {
        for (TestbedSetting setting : argSettings.getSettings()) {
            if (setting.settingsType != settingType) {
                continue;
            }
            switch (setting.constraintType) {
            case RANGE:
                JLabel text = new JLabel(setting.name + ": " + setting.value);
                JSlider slider = new JSlider(setting.min, setting.max, setting.value);
                slider.setMaximumSize(new Dimension(200, 20));
                slider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        // TODO Auto-generated method stub
                        // change the value of the slider accordingly
                    }
                });
                slider.setName(setting.name);
                slider.putClientProperty(SETTING_TAG, setting);
                slider.putClientProperty(LABEL_TAG, text);
                argPanel.add(text);
                argPanel.add(slider);
                break;
            case BOOLEAN:
                JCheckBox checkbox = new JCheckBox(setting.name);
                checkbox.setSelected(setting.enabled);
                checkbox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        // TODO check if this really works once the simulation is drawn
                        setting.enabled = checkbox.isSelected();
                    }
                });
                checkbox.putClientProperty(SETTING_TAG, setting);
                argPanel.add(checkbox);
                break;
            }
        }
    }

    private void addSettings(JPanel middle, ViewSettings drawingSettings) {
        for (ViewSetting setting : drawingSettings.getSettings()) {
            switch (setting.constraintType) {
            case RANGE:
                JLabel text = new JLabel(setting.name + ": " + setting.value);
                JSlider slider = new JSlider(setting.min, setting.max, setting.value);
                slider.setMaximumSize(new Dimension(200, 20));
                slider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        // TODO Auto-generated method stub
                        // change the value of the slider accordingly
                    }
                });
                slider.setName(setting.name);
                slider.putClientProperty(SETTING_TAG, setting);
                slider.putClientProperty(LABEL_TAG, text);
                middle.add(text);
                middle.add(slider);
                break;
            case BOOLEAN:
                JCheckBox checkbox = new JCheckBox(setting.name);
                checkbox.setSelected(setting.enabled);
                checkbox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        // TODO check if this really works once the simulation is drawn
                        setting.enabled = checkbox.isSelected();
                    }
                });
                checkbox.putClientProperty(SETTING_TAG, setting);
                middle.add(checkbox);
                break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

    public void initListeners(SimulationInputListeners simulationInputListeners) {
        pauseButton.addActionListener(simulationInputListeners.unPauseListener());
        stepButton.addActionListener(simulationInputListeners.singleStepListener());
        resetButton.addActionListener(simulationInputListeners.resetListener());
        quitButton.addActionListener(simulationInputListeners.exitListener());
        saveButton.addActionListener(simulationInputListeners.saveListener());
        loadButton.addActionListener(simulationInputListeners.loadListener());
    }
}

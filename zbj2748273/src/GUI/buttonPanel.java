package GUI;

import MazeExploration.*;
import TSPAlgo.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class buttonPanel extends JPanel implements ActionListener {
    // ================== declaring of objects
    // =============================================================================
    // setting of the size of the button panels panel

    private mainGUI main;
    private mazePanel mazeInButton;
    private mazePointCollection collection;
    // Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); //get the
    // entire screnesize
    private int buttonPanelWidth = (int) (1200 * 0.3);
    private int buttonPanelHeight = (int) (700 * 0.1);
    // Dimension buttonPanelDim = new
    // Dimension(buttonPanelWidth,buttonPanelHeight);
    private Dimension buttonPanelDim = new Dimension(buttonPanelWidth, buttonPanelHeight);
    // Buttons
    private JButton ExploreBtn = new JButton("Maze Explore");
    private JButton RefreshBtn = new JButton("Pause");
    private JButton configureBtn = new JButton("Configure Port");
    private JButton SaveBtn = new JButton("Save Maze");
    private JButton LoadBtn = new JButton("Load Maze");
    private JButton ForwardButton = new JButton("Forward");
    private JButton LeftButton = new JButton("Left");
//    private JButton BackwardButton = new JButton("Backward");
    private JButton RightButton = new JButton("Right");
    public JButton SCIlight = new JButton("SCI ACTIVE");
    private JToggleButton fastBtn = new JToggleButton("Fastest");
    private JToggleButton shortBtn = new JToggleButton("Shortest");
    private boolean blfast = false;
    private boolean blshort = false;
    private JTextArea logBox = new JTextArea(2, 30);
    private JLabel timeLabel = new JLabel("0m 0s");
    //Fastest - Shortest Data
    private LinkedList chosenList;
    //TSP data
    private JButton enableButton = new JButton("Enable");
    private JButton disableButton = new JButton("Disable");
    private JButton TSPExecuteButton = new JButton("Execute");
    private JLabel cityLabel = new JLabel("Number of Cities: ");
    private JLabel displayNumberCity = new JLabel();
    private TSPControl travelling;

    // SCI-Tree Connection
    // ================== Button panel coding
    // =============================================================================
    public buttonPanel(mainGUI main) {
        //Variable Initialization
        this.main = main;
        LoadBtn.setVisible(false);
        collection = main.getMazePointCollection();

        this.setLayout(new FlowLayout());
        this.setPreferredSize(buttonPanelDim);
        JTabbedPane tabPane = new JTabbedPane();

        // =================== configure panel
        // =============================================================================
        JPanel mainBtnPanel = new JPanel();
        mainBtnPanel.add(configureBtn);
        mainBtnPanel.setPreferredSize(new Dimension(buttonPanelDim));
        mainBtnPanel.setBorder(BorderFactory.createTitledBorder("Configure COM Port"));
        SCIlight.setBackground(Color.RED);
        SCIlight.setForeground(Color.YELLOW);
        SCIlight.setEnabled(false);
        mainBtnPanel.add(SCIlight);

        // =================== status logbox panel
        // =============================================================================
        JPanel displayBtnPanel = new JPanel();
        logBox.setPreferredSize(new Dimension((int) (buttonPanelWidth * 0.95),buttonPanelHeight * 50));

        logBox.setEditable(false);
        JScrollPane scroll = new JScrollPane(logBox);
        scroll.setAutoscrolls(true);
        scroll.setPreferredSize(new Dimension((int) (buttonPanelWidth * 0.95),buttonPanelHeight * 2));
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        displayBtnPanel.add(scroll);
        displayBtnPanel.setPreferredSize(new Dimension(buttonPanelDim));
        displayBtnPanel.setBorder(BorderFactory.createTitledBorder("Status"));

        // =================== tab panel
        // =============================================================================
        // ==================== algo
        // page========================================================================

        JPanel EPalgo = new JPanel();
        JPanel EPsave = new JPanel();
        JPanel EPtime = new JPanel();
        JPanel explorePane = new JPanel();

        EPalgo.add(ExploreBtn);
        EPalgo.add(RefreshBtn);
        EPalgo.setPreferredSize(new Dimension(buttonPanelDim));
        EPalgo.setBorder(BorderFactory.createTitledBorder("Algorithams"));

        EPsave.add(SaveBtn);
        EPsave.add(LoadBtn);
        EPsave.setPreferredSize(new Dimension(buttonPanelDim));
        EPsave.setBorder(BorderFactory.createTitledBorder("Save/Load"));

        // EPtime.add(fastshortBtn);
/*
        EPtime.add(timeLabel);
        EPtime.setPreferredSize(new Dimension(buttonPanelDim));
        EPtime.setBorder(BorderFactory.createTitledBorder("Time Elasped"));
         */
        JPanel TInfo = new JPanel();
        ForwardButton.setPreferredSize(new Dimension(60, 60));
        LeftButton.setPreferredSize(new Dimension(60, 60));
//        BackwardButton.setPreferredSize(new Dimension(60, 60));
        RightButton.setPreferredSize(new Dimension(60, 60));
         GridBagLayout gridbag = new GridBagLayout();
       TInfo.setLayout(gridbag);
       GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        gridbag.setConstraints(ForwardButton, c);
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(LeftButton, c);
        c.gridx = 1;
        c.gridy = 2;
//        gridbag.setConstraints(BackwardButton, c);
        c.gridx = 2;
        c.gridy = 1;
        gridbag.setConstraints(RightButton, c);
        TInfo.add(ForwardButton);
        TInfo.add(LeftButton);
//        TInfo.add(BackwardButton);
        TInfo.add(RightButton);

        TInfo.setPreferredSize(new Dimension(buttonPanelWidth, buttonPanelHeight +10));
        EPtime.setPreferredSize(new Dimension(buttonPanelWidth, buttonPanelHeight*2));
        EPtime.setBorder(BorderFactory.createTitledBorder("Manual Exploration"));

         EPtime.setLayout(new BorderLayout());
        EPtime.add(TInfo,BorderLayout.NORTH);
        // EPtime.add(TInfo);
        explorePane.add(EPalgo);
        explorePane.add(EPsave);
        explorePane.add(EPtime);

        tabPane.addTab("Maze Explorer", explorePane);


        // =================== Fastest + Shortest
        // =============================================================================
        JPanel TSP = new JPanel();
        JPanel layer1 = new JPanel();
        layer1.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        layer1.add(enableButton);
        enableButton.setActionCommand("tspEnable");
        enableButton.addActionListener(this);
        layer1.add(disableButton);
        disableButton.setActionCommand("tspDisable");
        disableButton.addActionListener(this);
        layer1.setPreferredSize(new Dimension(buttonPanelWidth, 80));
        layer1.setBorder(BorderFactory.createTitledBorder("TSP City Selection"));

        JPanel layer2 = new JPanel();
        layer2.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        layer2.setPreferredSize(new Dimension(buttonPanelWidth, buttonPanelHeight));
        layer2.add(cityLabel);
        layer2.add(displayNumberCity);
        layer2.setBorder(BorderFactory.createTitledBorder("Total Number Of Cities"));

        JPanel layer3 = new JPanel();
        layer3.setPreferredSize(new Dimension(buttonPanelWidth, buttonPanelHeight));
        layer3.add(TSPExecuteButton);
        TSPExecuteButton.setActionCommand("TSPExecuteButton");
        TSPExecuteButton.addActionListener(this);

        TSP.setLayout(new FlowLayout());
        TSP.add(layer1);
        TSP.add(layer2);
        TSP.add(layer3);

        tabPane.addTab("TSP", TSP);

        // Add the tabbed pane to this panel.
        //tabPane.setPreferredSize(new Dimension(buttonPanelWidth, (int) 800 / 3));
        tabPane.setPreferredSize(new Dimension(buttonPanelWidth, 300));
        this.add(tabPane);
        this.add(mainBtnPanel);
        displayBtnPanel.setPreferredSize(new Dimension(buttonPanelWidth,
                (int) 1200 / 2));
        this.add(displayBtnPanel);

        // The following line enables to use scrolling tabs.
        tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addListener();
    }

    public void setMazeInButton(mazePanel passedMazeObject) {
        mazeInButton = passedMazeObject;
    }

    public TSPControl getTSPControl() {
        return travelling;
    }

    public void setDisplayNumberCity(int value) {
        displayNumberCity.setText(Integer.toString(value));
    }

    // ================== intialisation
    // =============================================================================
    // ================== listener
    // =============================================================================
    public void addListener() { // add all the button listeners
        ExploreBtn.addActionListener(this);
        RefreshBtn.addActionListener(this);
        configureBtn.addActionListener(this);
        SaveBtn.addActionListener(this);
        LoadBtn.addActionListener(this);
        fastBtn.addActionListener(this);
        shortBtn.addActionListener(this);
        LeftButton.addActionListener(this);
        RightButton.addActionListener(this);
        ForwardButton.addActionListener(this);
//        BackwardButton.addActionListener(this);
    }

    // ==================action buttons
    // =============================================================================
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(ExploreBtn)) {
            logBox.append("EXPLORE\n");
            main.getAlgo().setExplorationMode();
            main.getAlgo().consult();
        } else if (e.getSource().equals(RefreshBtn)) {
            if(RefreshBtn.getText().equals("Refresh"))
            {
                RefreshBtn.setText("Pause");
                 main.getSCIPort().setExploreFlag(0);
                 main.getAlgo().consult();
                 logBox.append("Resume\n");

            }else
            {
                RefreshBtn.setText("Refresh");
                main.getSCIPort().setExploreFlag(4);
                logBox.append("Pause\n");
            }
        } else if (e.getSource().equals(configureBtn)) {
            //temp is just a temporary variable, the SCIPort of main is set inside the SCISetting class.
            SCISettings temp = new SCISettings(main);
            logBox.append("configure\n");
        } else if (e.getSource().equals(SaveBtn)) {
            logBox.append("Save\n");
            saveMazeData();
        } else if (e.getSource().equals(LoadBtn)) {
            logBox.append("Load\n");
            clearMaze();
            loadMazeData();
            mazeInButton.refreshMaze();
        } else if (e.getSource().equals(fastBtn)) {
            blfast = true;
            blshort = false;
            if (blshort == false) {
                shortBtn.setSelected(false);
            } else {
                shortBtn.setSelected(true);
            }
            logBox.append("fast\n");
        } else if (e.getSource().equals(shortBtn)) {
            blshort = true;
            blfast = false;
            if (blfast == false) {
                fastBtn.setSelected(false);
            } else {
                fastBtn.setSelected(true);
            }
            logBox.append("short\n");
        } else if (e.getSource().equals(LeftButton)) {
            setDirectionDisable();
            main.getSCIPort().setExploreFlag(1);
            ManualLeft();
            main.getSCIPort().sendCommand("d");
            setDirectionEnable();
        } else if (e.getSource().equals(RightButton)) {
            setDirectionDisable();
            main.getSCIPort().setExploreFlag(1);
            ManualRight();
            main.getSCIPort().sendCommand("c");
            setDirectionEnable();
        } else if (e.getSource().equals(ForwardButton)) {
            setDirectionDisable();
            main.getSCIPort().setExploreFlag(1);
            ManualForward();
            main.getSCIPort().sendCommand("a");
            setDirectionEnable();
        } /*else if (e.getSource().equals(BackwardButton)) {
            setDirectionDisable();
            main.getSCIPort().setExploreFlag(1);
            ManualBackward();
            main.getSCIPort().sendCommand("ea");
            setDirectionEnable();
        } */else if (e.getActionCommand().equals("tspEnable")) {
            mazeInButton.TSPButtonOn();
            main.getTSPButtonClickHandler().clearCityList();
            setDisplayNumberCity(0);
        } else if (e.getActionCommand().equals("tspDisable")) {
            mazeInButton.TSPButtonOff();
        } else if (e.getActionCommand().equals("executeButton")) {
            //start a function to make the robot move from starting node to ending node
            mazeInButton.TSPButtonOff();
            shortestMovement move = new shortestMovement(main, chosenList);
            main.getSCIPort().setExploreFlag(2);
            main.getSCIPort().setMoveObject(move);
            move.control();
        } else if (e.getActionCommand().equals("fastestExecuteButton")) {
            System.out.println("fastest");
        } else if (e.getActionCommand().equals("TSPExecuteButton")) {
            //Get the list of the cities
            LinkedList<node> cityList = main.getTSPButtonClickHandler().getCityList();

            travelling = new TSPControl(main, cityList);
            travelling.control();
        }
    }

    public void setDirectionDisable() {
        ForwardButton.setEnabled(false);
//        BackwardButton.setEnabled(false);
        LeftButton.setEnabled(false);
        RightButton.setEnabled(false);
    }

    public void setDirectionEnable() {
        ForwardButton.setEnabled(true);
//        BackwardButton.setEnabled(true);
        LeftButton.setEnabled(true);
        RightButton.setEnabled(true);
    }

    void ManualLeft() {
        mazeInButton.setUpManualTurnLeft();
    }

    void ManualRight() {
        mazeInButton.setUpManualTurnRight();
    }

    void ManualForward() {
        mazeInButton.setUpManualForward();
    }

    void ManualBackward() {
        mazeInButton.setUpManualBackward();
    }

    private void saveMazeData() {
        JFileChooser jfc = new JFileChooser();
        int result = jfc.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File file = jfc.getSelectedFile();
        try {
            mazeInButton.saveMazeToFile(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMazeData() {
        JFileChooser jfc = new JFileChooser();
        int result = jfc.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        try {
            File file = jfc.getSelectedFile();
            mazeInButton.loadMazeFromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearMaze() {
        mazeInButton.clearObstacleOnMaze();
    }
}
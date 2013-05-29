//version 1.0
package GUI;
import TSPAlgo.*;
import MazeExploration.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class mainGUI extends JFrame
{
    private buttonPanel button;
    private mazePanel maze;
    private SCISettings SCIPort;
    private mazePointCollection collection;
    private node rootNode;
    private ExploreAlgo brain;
    private nodeClickTSP tspButtonClickHandler;
    
    public mainGUI()
    {
        //All initialization
        tspButtonClickHandler = new nodeClickTSP(this);
        collection = new mazePointCollection(tspButtonClickHandler);
        tspButtonClickHandler.setCollection(collection);
        rootNode = collection.nodeAtRowCol(5, 6);
        
        button = new buttonPanel(this);
        maze = new mazePanel(this);
        button.setMazeInButton(maze);
        
        //SCISetting should be instantiated under the Config Button Press - not here
        //treeObject is instantiated at the MazeExploration Button Click - not here

        getContentPane().setLayout(new BorderLayout());
        this.add(button, BorderLayout.EAST);
        //maze.setPreferredSize(new Dimension(726, 726));
        this.add(maze, BorderLayout.CENTER);
    //    Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED); //set border
      //  button.setBorder(border); //add border
    }

    public static void main(String[] args)
    {
        mainGUI main = new mainGUI();
        main.setTitle("Control Panel");

        main.setSize(new Dimension(1200, 700));

        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }
    
/*========================================= Accessor and Mutator =================================================================*/    
    public buttonPanel getButtonPanel()
    {
        return button;
    }
    
    public mazePanel getMazePanel()
    {
        return maze;
    }
    
    public SCISettings getSCIPort()
    {
        return SCIPort;
    }
    
    public void setSCIPort(SCISettings settingObject)
    {
        SCIPort = settingObject;
    }
    
    public mazePointCollection getMazePointCollection()
    {
        return collection;
    }
    
    public node getRootNode()
    {
        return rootNode;
    }
    
    public ExploreAlgo getAlgo()
    {
        return brain;
    }
    
    public void createAlgo()
    {
        brain = new ExploreAlgo(this);
    }
    
    public nodeClickTSP getTSPButtonClickHandler()
    {
        return tspButtonClickHandler;
    }
}

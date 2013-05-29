package GUI;
import TSPAlgo.*;
import java.awt.Color;
import java.awt.Point;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class node
{
    private int id;
    private boolean visited;
    private boolean obstacle;
    private boolean mouse;
    private int pointX;
    private int pointY;
    private boolean inMaze = true; //true - the node locate inside the maze
    private int grade = -1;
    private JButton cityButton = new JButton();
    
    public node(int ID, boolean visit, boolean obs, int x, int y, nodeClickTSP handler)
    {
        id = ID;
        visited = visit;
        obstacle = obs;
        mouse = false;
        pointX = x;
        pointY = y;
        cityButton.setBackground(Color.YELLOW);
        cityButton.setVisible(false);
        cityButton.setPreferredSize(new Dimension(17, 17));
        cityButton.addActionListener(handler);
        cityButton.setActionCommand(Integer.toString(id));
    }
    
    public int getID()
    {
        return id;
    }
    
    public void setID(int ID)
    {
        this.id = ID;
    }
   

    public int getCoordinateX()
    {
        return pointX;
    }

    public int getCoordinateY()
    {
        return pointY;
    }

    public boolean getObstacle()
    {
        return obstacle;
    }

    public void setObstacle(Boolean status)
    {
        obstacle = status;
    }

    public boolean getVisited()
    {
        return visited;
    }

    public void setVisited(boolean visit)
    {
        visited = visit;
    }

    public void setMousePlace(boolean moused)
    {
        mouse = moused;
    }

    public boolean getMouse()
    {
        return mouse;
    }
    
    public boolean getInMaze()
    {
        return inMaze;
    }
    
    public void setInMaze(boolean value)
    {
        inMaze = value;
    }
    
    public void setGrade(int value)
    {
        grade = value;
    }
    
    public int getGrade()
    {
        return grade;
    }
    
    public JButton getCityButton()
    {
        return cityButton;
    }
}

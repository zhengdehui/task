package GUI;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import MazeExploration.*;
import java.util.LinkedList;

public class mazePanel extends JPanel
{

    private mainGUI main;
    private node currentNode;
    private boolean eastBoundaryFound = false, westBoundaryFound=false;
     private int eastColBoundary,westColBoundary;
     private boolean northBoundaryFound = false, southBoundaryFound=false;
     private int northRowBoundary,southRowBoundary;
    private buttonPanel buttonInMaze;
    private int mouseDirection;
    private SCISettings sciInAlgo;
    mazeWrite write;
    private Image mouseImage, obsImage;
    private BufferedImage floorImage, startImage, tickImage, outBound, shortestNode;
    private boolean obstacleDetect; //true - obstacle detect.
    private int direction = 0;
    private int oldDirection, tempDirection;
    private int x;
    private int y;
    private boolean init;
    //These variables for manual mode control
    private int NewX;
    private int NewY;
    private int nextX, nextY;
    //These variable for auto mode control (SCI)
    private boolean blForward;
    private int m_interval = 1;  // Milliseconds between updates.
    private Timer autoAnimationSCIForwardBackward;
    private Timer autoAnimationSCILeftRight;
    private Timer manualLeftRightControlTimer;
    private Timer manualForwardBackwardControlTimer;
    private mazePointCollection collectionInMazePanel;
    private node[][] arrayNode;
    private node Origin;
    private boolean bound, bump;
    
    //Shortest Path list
    private LinkedList<node> shortestList;
    private boolean displayShortestPath = false;
    
    private JPanel cityButtonPanel = new JPanel(new GridLayout(11, 12, 49, 49));

    public mazePanel(mainGUI main)
    {
        //variable initialization
        this.main = main;
        buttonInMaze = main.getButtonPanel();
        obstacleDetect = false;
        init = false;
        collectionInMazePanel = main.getMazePointCollection();
        arrayNode = collectionInMazePanel.getCollection();
        Origin = arrayNode[5][6];
        write = new mazeWrite(collectionInMazePanel);
        cityButtonPanel.setVisible(true);
        cityButtonPanel.setOpaque(false);
        
        //add all the buttons into the cityButtonPanel
        for(int row = 0; row < arrayNode.length; row++)
        {
            for(int col = 0; col < arrayNode[0].length; col++)
            {
                cityButtonPanel.add(arrayNode[10 - row][col].getCityButton());
            }
        }
        
        this.setBackground(Color.WHITE);
        init = false;

        mouseImage = new ImageIcon("src/Images/robot0.png").getImage();
        obsImage = new ImageIcon("bin/Images/obstacle_red.gif").getImage();

        autoAnimationSCIForwardBackward = new Timer(m_interval, new TimerForSCIAnimation());
        autoAnimationSCILeftRight = new Timer(20, new TimerForSCILeftRight());
        manualLeftRightControlTimer = new Timer(20, new manualLeftRightControlTimerAction());
        manualForwardBackwardControlTimer = new Timer(m_interval, new manualForwardBackwardControlTimerAction());
        try
        {
            floorImage = ImageIO.read(new File("bin/Images/junction.gif"));
            startImage = ImageIO.read(new File("bin/Images/home.gif"));
            tickImage = ImageIO.read(new File("bin/Images/tick1.gif"));
            outBound = ImageIO.read(new File("bin/Images/whiteSquare.jpg"));
            shortestNode = ImageIO.read(new File("bin/Images/smile.gif"));
        } catch (IOException e)
        {
            System.out.println(e);
        }
    }
    /*=========================================== Accessor and Mutator =======================================================================*/

    public boolean getObstacleDetect()
    {
        return obstacleDetect;
    }

    public void setObstacleDetect(boolean value)
    {
        this.obstacleDetect = value;
    }

    public int getDirection()
    {
        return direction;
    }

    public void setDirection(int value)
    {
        direction = value;
    }

    public int getOldDirection()
    {
        return oldDirection;
    }

    public void setOldDirection(int value)
    {
        oldDirection = value;
    }

    public int getTempDirection()
    {
        return tempDirection;
    }

    public void setTempDirection(int value)
    {
        tempDirection = value;
    }

    public boolean getInitVar()
    {
        return init;
    }

    public void setInit(boolean value)
    {
        init = value;
    }

    public int getNewX()
    {
        return NewX;
    }

    public void setNewX(int value)
    {
        NewX = value;
    }

    public int getNewY()
    {
        return NewY;
    }

    public void setNewY(int value)
    {
        NewY = value;
    }

    public boolean getBlForward()
    {
        return blForward;
    }

    public void setBlForward(boolean value)
    {
        blForward = value;
    }

    public boolean getBound()
    {
        return bound;
    }

    public void setBoundVariable(boolean value)
    {
        bound = value;
    }

    public boolean getBump()
    {
        return bump;
    }

    public void setBumpVariable(boolean value)
    {
        bump = value;
    }

    public int getNextX()
    {
        return nextX;
    }

    public void setNextX(int value)
    {
        nextX = value;
    }

    public int getNextY()
    {
        return nextY;
    }

    public void setNextY(int value)
    {
        nextY = value;
    }

    public void setx(int value)
    {
        x = value;
    }
    
    public void sety(int value)
    {
        y = value;
    }
    
    public JPanel getCityButtonPanel()
    {
        return cityButtonPanel;
    }
    /*===================================== Painting Functions ===========================================================================*/

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        drawBG(g);
        drawObs(g);
        drawCar(g, tempDirection);
        obstacle();
    }

    public void drawCar(Graphics g, int direction)
    {
        mouseImage = new ImageIcon("bin/Images/robot" + direction + ".gif").getImage();
        if (init == true)
        {
            mouseImage = new ImageIcon("bin/Images/robot" + direction + ".gif").getImage();
            g.drawImage(mouseImage, x, y, null);
        } else if (init == false)
        {
            mouseImage = new ImageIcon("bin/Images/robot0.gif").getImage();
            Origin.setMousePlace(true);
           
                  x = Origin.getCoordinateX() - 66;
            y = Origin.getCoordinateY() - 66;
            
          
            g.drawImage(mouseImage, x, y, null);
            init = true;

        }
    }

    void drawBG(Graphics g)
    {
        g.setColor(Color.BLUE);
        
        //Draw Maze Panel by using mazePoint
        for (int row = 0; row < arrayNode.length; row++)
        {
            for (int col = 0; col < arrayNode[0].length; col++)
            {
                if (arrayNode[row][col].getInMaze() == true)
                {
                    g.drawImage(floorImage, (int) (arrayNode[row][col].getCoordinateX() - 33),
                            (int) (arrayNode[row][col].getCoordinateY() - 33), null);
                } else
                {
                    g.drawImage(outBound, (int) (arrayNode[row][col].getCoordinateX() - 33),
                            (int) (arrayNode[row][col].getCoordinateY() - 33), null);
                }
            }
        }

        //draw the Maze coordinates
        for (int row = 0; row < arrayNode.length; row++)
        {
            for (int col = 0; col < arrayNode[0].length; col++)
            {
                if (arrayNode[row][col].getInMaze() == true)
                {
                    g.drawString("(" + col + "," + row + ")", (int) (arrayNode[row][col].getCoordinateX() + 8),
                            (int) (arrayNode[row][col].getCoordinateY() - 8));
                }

                if (arrayNode[row][col].getVisited() == true && arrayNode[row][col].getInMaze() == true)
                {
                    g.drawImage(tickImage, (int) (arrayNode[row][col].getCoordinateX() - 9),
                            (int) (arrayNode[row][col].getCoordinateY() - 9), null);
                }
                /*else   if (arrayNode[row][col].getVisited() == false && arrayNode[row][col].getInMaze() == true)
                {
                    g.drawImage(floorImage, (int) (arrayNode[row][col].getCoordinateX() - 9),
                            (int) (arrayNode[row][col].getCoordinateY() - 9), null);
                }*/
            }
        }
        
        //draw the shortestPath (if exist)
        if(displayShortestPath == true)
        {
            Graphics2D g2 = (Graphics2D)g;
            Stroke stroke = new BasicStroke(8, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 3, 1 }, 0);
            g2.setStroke(stroke);
            g2.setColor(Color.red);
            
            //g.setColor(Color.red);
            for(int i = 0; i < shortestList.size() - 1; i++)
            {
                node point1 = shortestList.get(i);
                node point2 = shortestList.get(i + 1);
                
                g2.drawLine(point1.getCoordinateX(), point1.getCoordinateY(),
                        point2.getCoordinateX(), point2.getCoordinateY());
            }
           
        }

        //draw the starting position

        g.drawImage(startImage, (int) (Origin.getCoordinateX() - 18),
                (int) (Origin.getCoordinateY() - 18), null);
        
        add(cityButtonPanel);
        cityButtonPanel.setLocation(25, 25);
      
    }

    public void drawObs(Graphics g)
    {

        for (int row = 0; row < arrayNode.length; row++)
        {
            for (int col = 0; col < arrayNode[0].length; col++)
            {
                if (arrayNode[row][col].getObstacle() == true)
                {
                    g.drawImage(obsImage, (int) (arrayNode[row][col].getCoordinateX() - 9),
                            (int) (arrayNode[row][col].getCoordinateY() - 9), null);
                }
            }
        }
    }

    public void refreshMaze()
    {
        this.repaint();
    }
    public void resetExplore()
    {
       for (int row = 0; row < arrayNode.length; row++)
        {
            for (int col = 0; col < arrayNode[0].length; col++)
            {
                if(arrayNode[row][col].getVisited())
                {
                    arrayNode[row][col].setVisited(false);

                }
                if (arrayNode[row][col].getObstacle())
                {
                  arrayNode[row][col].setVisited(false);
                  arrayNode[row][col].setObstacle(false);
                }
            }
        }
        init=false;
       repaint();
    }
    /*====================================== Set up for Manual Control (Left, Right)=================================================================================*/
    public void setUpManualTurnLeft()
    {
        oldDirection = direction;
        if (direction == 270)
        {
            direction = 0;
        } else
        {
            direction += 90;
        }
        repaintForManualControlMode();
    }

    public void setUpManualTurnRight()
    {
        oldDirection = direction;
        if (direction == 0)
        {
            direction = 270;
        } else
        {
            direction -= 90;
        }
        repaintForManualControlMode();
    }

    public void repaintForManualControlMode()
    {
        if (oldDirection == 0 && direction == 270)
        {
            //spectial turn right case
            tempDirection = 360;
        } else if (oldDirection - direction == 90)
        {
            //normal turn right case
            tempDirection = oldDirection;
        } else if (direction - oldDirection == 90)
        {
            //normal turn left case
            tempDirection = oldDirection;
        } else if (oldDirection == 270 && direction == 0)
        {
            //special turn left case
            tempDirection = oldDirection;
        }
        manualLeftRightControlTimer.start();
    }

    class manualLeftRightControlTimerAction implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (oldDirection == 0 && direction == 270)
            {
                //turn right case
                if (tempDirection != direction) //tempDirection already set to 360 above
                {
                    tempDirection -= 15;
                    repaint();
                } else
                {
                    oldDirection = direction;
                    manualLeftRightControlTimer.stop();
                }
            } else if (oldDirection - direction == 90)
            {
                //turn right case
                if (tempDirection != direction)
                {
                    tempDirection -= 15;
                    repaint();
                } else
                {
                    oldDirection = direction;
                    manualLeftRightControlTimer.stop();
                }
            } else if (direction - oldDirection == 90)
            {
                //turn left case
                if (tempDirection != direction)
                {
                    tempDirection += 15;
                    repaint();
                } else
                {
                    oldDirection = direction;
                    manualLeftRightControlTimer.stop();
                }
            } else if (oldDirection == 270 && direction == 0)
            {
                //turn left case
                if (tempDirection != 360) //initially tempDirection = 270
                {
                    tempDirection += 15;
                    repaint();
                } else
                {
                    tempDirection = 0;
                    oldDirection = direction;
                    manualLeftRightControlTimer.stop();
                }
            }
        }
    }
    /*====================================== Set up for Manual Control (Forward, Backward)=================================================================================*/

    public void setUpManualForward()
    {
        blForward = true;
        if (tempDirection == 0)
        { 
            NewX = x + 66;
            nextX = getMouseLocationX() + 1;
            nextY = getMouseLocationY();

            arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
            try
            {
                arrayNode[nextY][nextX].setMousePlace(true);
            } catch (ArrayIndexOutOfBoundsException e)
            {
                arrayNode[nextY][nextX - 1].setMousePlace(true);
                bound = true;
            }
            
        } else if (tempDirection == 180)
        {
            NewX = x - 66;

            nextX = getMouseLocationX() - 1;
            nextY = getMouseLocationY();
            arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
            try
            {
                arrayNode[nextY][nextX].setMousePlace(true);
            } catch (ArrayIndexOutOfBoundsException e)
            {
                arrayNode[nextY][nextX + 1].setMousePlace(true);
                bound = true;
            }

        } else if (tempDirection == 90)
        {
            NewY = y - 66;

            nextX = getMouseLocationX();
            nextY = getMouseLocationY() + 1;
            arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
            try
            {
                arrayNode[nextY][nextX].setMousePlace(true);
            } catch (ArrayIndexOutOfBoundsException e)
            {
                arrayNode[nextY - 1][nextX].setMousePlace(true);
                bound = true;
            }
        } else if (tempDirection == 270)
        {
            NewY = y + 66;

            nextX = getMouseLocationX();
            nextY = getMouseLocationY() - 1;
            arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
            try
            {
                arrayNode[nextY][nextX].setMousePlace(true);
            } catch (ArrayIndexOutOfBoundsException e)
            {
                arrayNode[nextY + 1][nextX].setMousePlace(true);
                bound = true;
            }
        }
        manualForwardBackwardControlTimer.start();
    }

    public void setUpManualBackward()
    {
        blForward = false;
        if (tempDirection == 0)
        {
            NewX = x - 66;
            nextX = (getMouseLocationX() - 1);
            nextY = getMouseLocationY();

            if (bound == false)
            {
                arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
                arrayNode[nextY][nextX].setMousePlace(true);
            }
        } else if (tempDirection == 180)
        {
            NewX = x + 66;

            nextX = (getMouseLocationX() + 1);
            nextY = getMouseLocationY();
            if (bound == false)
            {
                arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
                arrayNode[nextY][nextX].setMousePlace(true);
            }

        } else if (tempDirection == 90)
        {
            NewY = y + 66;

            nextX = (getMouseLocationX());
            nextY = getMouseLocationY() - 1;
            if (bound == false)
            {
                arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
                arrayNode[nextY][nextX].setMousePlace(true);
            }

        } else if (tempDirection == 270)
        {
            NewY = y - 66;

            nextX = (getMouseLocationX());
            nextY = getMouseLocationY() + 1;
            if (bound == false)
            {
                arrayNode[getMouseLocationY()][getMouseLocationX()].setMousePlace(false);
                arrayNode[nextY][nextX].setMousePlace(true);
            }
        }
        manualForwardBackwardControlTimer.start();
    }

    class manualForwardBackwardControlTimerAction implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (blForward == true)
            {
                manualAnimateForward();
            } //back direction
            else if (blForward == false)
            {
                manualAnimateBackward();
            }
            // Repaint indirectly calls paintComponent.
            repaint();
        }
    }

    private void manualAnimateForward()
    {
        if (tempDirection == 0)
        {
            if (NewX > x)
            {
                x = x + 1;
            } else
            {
                manualForwardBackwardControlTimer.stop();
                retreat();
                boundary();
            }
        } else if (tempDirection == 270)
        {
            if (NewY > y)
            {
                y = y + 1;

            } else
            {
                manualForwardBackwardControlTimer.stop();
                retreat();
                boundary();
            }
        } else if (tempDirection == 180)
        {
            if (NewX < x)
            {
                x = x - 1;
            } else
            {
                manualForwardBackwardControlTimer.stop();
                retreat();
                boundary();
            }
        } else if (tempDirection == 90)
        {
            if (NewY < y)
            {
                y = y - 1;
            } else
            {
                manualForwardBackwardControlTimer.stop();
                retreat();
                boundary();
            }
        }
    }

    private void manualAnimateBackward()
    {
        if (tempDirection == 0)
        {
            if (NewX < x)
            {
                x = x - 1;
            } else
            {
                manualForwardBackwardControlTimer.stop();
            }
        } else if (tempDirection == 270)
        {
            if (NewY < y)
            {
                y = y - 1;
            } else
            {
                manualForwardBackwardControlTimer.stop();
            }
        } else if (tempDirection == 180)
        {
            if (NewX > x)
            {
                x = x + 1;
            } else
            {
                manualForwardBackwardControlTimer.stop();
            }
        } else if (tempDirection == 90)
        {
            if (NewY > y)
            {
                y = y + 1;
            } else
            {
                manualForwardBackwardControlTimer.stop();
            }
        }
    }

    /*======================================== Set up for auto animation - Forward + Backward ==========================================================================*/
    public void setUpAutoForward()
    {
        //Just set up the coordinate for the mouse to move
        //Updating the mouse Location is done under SCISettings once the 
        //ack is received.
        blForward = true;

        if (tempDirection == 0)
        {
            NewX = x + 66;
        } else if (tempDirection == 180)
        {
            NewX = x - 66;
        } else if (tempDirection == 90)
        {
            NewY = y - 66;
        } else if (tempDirection == 270)
        {
            NewY = y + 66;
        }
        autoAnimationSCIForwardBackward.start();
    }

    public void setUpAutoBackward()
    {
        blForward = false;

        if (tempDirection == 0)
        {
            NewX = x - 66;
        } else if (tempDirection == 180)
        {
            NewX = x + 66;

        } else if (tempDirection == 90)
        {
            NewY = y + 66;
        } else if (tempDirection == 270)
        {
            NewY = y - 66;
        }
        autoAnimationSCIForwardBackward.start();
    }

    class TimerForSCIAnimation implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (blForward == true)
            {
                autoAnimateForward();
            } else if (blForward == false)
            {
                autoAnimateBackward();
            }
            // Repaint indirectly calls paintComponent.
            repaint();
        }
    }

    private void autoAnimateForward()
    {
        if (tempDirection == 0)
        {
            if (x < NewX)
            {
                x = x + 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        } else if (tempDirection == 180)
        {
            if (x > NewX)
            {
                x = x - 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        } else if (tempDirection == 90)
        {
            if (y > NewY)
            {
                y = y - 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        } else if (tempDirection == 270)
        {
            if (y < NewY)
            {
                y = y + 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        }
    }

    private void autoAnimateBackward()
    {
        if (tempDirection == 0)
        {
            if (x > NewX)
            {
                x = x - 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        } else if (tempDirection == 180)
        {
            if (x < NewX)
            {
                x = x + 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        } else if (tempDirection == 90)
        {
            if (y < NewY)
            {
                y = y + 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        } else if (tempDirection == 270)
        {
            if (y > NewY)
            {
                y = y - 1;
            } else
            {
                autoAnimationSCIForwardBackward.stop();
            }
        }
    }

    /*======================================== Set up for auto animation - Left + Right ==========================================================================*/
    public void setUpAutoTurnLeft()
    {
        //direction = new direction
        oldDirection = direction;
        if (direction == 270)
        {
            direction = 0;
        } else
        {
            direction += 90;
        }
        repaintForAutoControlMode();
    }

    public void setUpAutoTurnRight()
    {
        //direction = new direction
        oldDirection = direction;
        if (direction == 0)
        {
            direction = 270;
        } else
        {
            direction -= 90;
        }
        repaintForAutoControlMode();
    }

    public void repaintForAutoControlMode()
    {
        if (oldDirection == 0 && direction == 270)
        {
            //spectial turn right case
            tempDirection = 360;
        } else if (oldDirection - direction == 90)
        {
            //normal turn right case
            tempDirection = oldDirection;
        } else if (direction - oldDirection == 90)
        {
            //normal turn left case
            tempDirection = oldDirection;
        } else if (oldDirection == 270 && direction == 0)
        {
            //special turn left case
            tempDirection = oldDirection;
        }
        autoAnimationSCILeftRight.start();
    }

    class TimerForSCILeftRight implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (oldDirection == 0 && direction == 270)
            {
                //turn right case
                if (tempDirection != direction) //tempDirection already set to 360 above
                {
                    tempDirection -= 15;
                    repaint();
                } else
                {
                    oldDirection = direction;
                    autoAnimationSCILeftRight.stop();
                }
            } else if (oldDirection - direction == 90)
            {
                //turn right case
                if (tempDirection != direction)
                {
                    tempDirection -= 15;
                    repaint();
                } else
                {
                    oldDirection = direction;
                    autoAnimationSCILeftRight.stop();
                }
            } else if (direction - oldDirection == 90)
            {
                //turn left case
                if (tempDirection != direction)
                {
                    tempDirection += 15;
                    repaint();
                } else
                {
                    oldDirection = direction;
                    autoAnimationSCILeftRight.stop();
                }
            } else if (oldDirection == 270 && direction == 0)
            {
                //turn left case
                if (tempDirection != 360) //initially tempDirection = 270
                {
                    tempDirection += 15;
                    repaint();
                } else
                {
                    tempDirection = 0;
                    oldDirection = direction;
                    autoAnimationSCILeftRight.stop();
                }
            }
        }
    }

    public void saveMazeToFile(File file)
    {
        write.saveFile(file);
    }

    public void loadMazeFromFile(File file)
    {
        write.readFile(file);
    }

//=====================obstacle========================
    void retreat()
    {
        if (bump == true)
        {
            if (blForward == true)
            {
                System.out.println("Ob hit: "+bump);
                //animate a backward for manual control mode
               // setUpManualBackward();
            }
            bump = false;
        } else
        {
            //if it's not an obstacle, mark the node as visited
            for (int row = 0; row < arrayNode.length; row++)
            {
                for (int col = 0; col < arrayNode[0].length; col++)
                {
                    if (arrayNode[row][col].getMouse() == true)
                    {
                        arrayNode[row][col].setVisited(true);
                        return;
                    }
                }
            }
        }
    }

public void manualOb()
    {
                currentNode = collectionInMazePanel.MouseNode();
                currentNode.setObstacle(true);
                currentNode.setVisited(true);
                buttonInMaze.ManualBackward();
                bump=true;
                retreat();
               main.getSCIPort().sendCommand("eae");
}

    public void manualBoundary()
    {
                        mouseDirection = tempDirection;
                if (mouseDirection == 0)
                {
                    //east boundary found
                    eastBoundaryFound = true;
                    eastColBoundary = collectionInMazePanel.MouseAtCol();

                    System.out.println("eastColBoundary = " + eastColBoundary);
                    for (int row = 0; row < arrayNode.length; row++)
                    {
                        for (int col = eastColBoundary; col < arrayNode[0].length; col++)
                        {
                             
                            arrayNode[row][col].setInMaze(false);
                            arrayNode[row][col].setVisited(true);
                            
                        }
                    }

                }
                        else if (mouseDirection == 180)
                {
                    //west boundary found
                    westBoundaryFound = true;
                    westColBoundary = collectionInMazePanel.MouseAtCol();

                    System.out.println("westColBoundary = " + westColBoundary);
                    for (int row = 0; row < arrayNode.length; row++)
                    {
                        for (int col = 0; col <= westColBoundary; col++)
                        {
                            arrayNode[row][col].setInMaze(false);
                            arrayNode[row][col].setVisited(true);
                        }
                    }

                }
                        else if (mouseDirection == 90)
                {
                    //north boundary found
                    northBoundaryFound = true;
                    northRowBoundary = collectionInMazePanel.MouseAtRow();

                    System.out.println("northRowBoundary = " + northRowBoundary);
                    for (int row = northRowBoundary; row < arrayNode.length; row++)
                    {
                        for (int col = 0; col < arrayNode[0].length; col++)
                        {
                            arrayNode[row][col].setInMaze(false);
                            arrayNode[row][col].setVisited(true);
                        }
                    }

                }
                        else if (mouseDirection == 270)
                {
                    //south boundary found
                    southBoundaryFound = true;
                    southRowBoundary = collectionInMazePanel.MouseAtRow();

                    System.out.println("southRowBoundary = " + southRowBoundary);
                    for (int row = 0; row <= southRowBoundary; row++)
                    {
                        for (int col = 0; col < arrayNode[0].length; col++)
                        {
                            arrayNode[row][col].setInMaze(false);
                            arrayNode[row][col].setVisited(true);
                        }
                    }
                }
                     buttonInMaze.ManualBackward();
                    //manualAnimateBackward();
                       main.getSCIPort().sendCommand("eae");

    }
//=======================boundary=============================
    void boundary()
    {
        if (bound == true)
        {
            if (blForward == true)
            {
                //animate a back for boundary
                setUpManualBackward();
                bound = false;
            }
        }
    }

    //=========================== Get location x, y of the mouse, set the mouse location ================================
    public int getMouseLocationX()
    {
        //return the collumn that the mouse is
        int x = 0;
        for (int row = 0; row < arrayNode.length; row++)
        {
            for (int col = 0; col < arrayNode[0].length; col++)
            {
                if (arrayNode[row][col].getMouse() == true)
                {
                    x = col;
                }
            }
        }
        return x;
    }

    public int getMouseLocationY()
    {
        //return the row that the mouse is
        int y = 0;

        for (int row = 0; row < arrayNode.length; row++)
        {
            for (int col = 0; col < arrayNode[0].length; col++)
            {
                if (arrayNode[row][col].getMouse() == true)
                {
                    y = row;
                }
            }
        }
        return y;
    }

    public void setMouseLocation()
    {
        int tempX = getMouseLocationX(); //return the collumn
        int tempY = getMouseLocationY(); //return the row

        //clear the mouse position for the old node
        arrayNode[tempY][tempX].setMousePlace(false);

        switch (tempDirection)
        {
            case 0:
            case 360:
                tempX++;
                break;

            case 90:
                tempY++;
                break;

            case 180:
                tempX--;
                break;

            case 270:
                tempY--;
                break;
        }
        arrayNode[tempY][tempX].setMousePlace(true);
    }

    public void setMouseLocationForBackwardCase()
    {
        int tempX = getMouseLocationX(); //return the collumn
        int tempY = getMouseLocationY(); //return the row

        //clear the mouse position for the old node
        arrayNode[tempY][tempX].setMousePlace(false);

        switch (tempDirection)
        {
            case 0:
            case 360:
                tempX--;
                break;

            case 90:
                tempY--;
                break;

            case 180:
                tempX++;
                break;

            case 270:
                tempY++;
                break;
        }
        arrayNode[tempY][tempX].setMousePlace(true);
    }
//===========================obstacle detections===============================    

    void obstacle()
    {
        //this function is called under the paintComponent
        for (int row = 0; row < arrayNode.length; row++)
        {
            for (int col = 0; col < arrayNode[0].length; col++)
            {
                if ((arrayNode[row][col].getMouse() == true) && (arrayNode[row][col].getObstacle() == true))
                {
                    bump = true;
                }
            }
        }
    }

    /*========================================== Clear the whole maze ============================================================*/
    public void clearObstacleOnMaze()
    {
        collectionInMazePanel.clearAllObstacle();
        repaint();
    }
    
    /*========================================== Show shortest path ===================================*/
    
    public void showShortestPath(LinkedList<node> passedShortestList)
    {
        shortestList = passedShortestList;
        displayShortestPath = true;
        refreshMaze();
    }
    
    public void TSPButtonOn()
    {
        for(int row = 0 ; row < arrayNode.length; row++)
        {
            for(int col = 0; col < arrayNode[0].length; col++)
            {
                if(arrayNode[row][col].getInMaze() == true && arrayNode[row][col].getObstacle() == false)
                {
                    arrayNode[row][col].getCityButton().setVisible(true);
                    arrayNode[row][col].getCityButton().setBackground(Color.YELLOW);
                }
            }
        }
    }
    
    public void TSPButtonOff()
    {
        for(int row = 0 ; row < arrayNode.length; row++)
        {
            for(int col = 0; col < arrayNode[0].length; col++)
            {
                if(arrayNode[row][col].getInMaze() == true && arrayNode[row][col].getObstacle() == false)
                    arrayNode[row][col].getCityButton().setVisible(false);
            }
        }
    }
 void setEastBound(int east) {
        for (int col = east + 1; col < arrayNode[0].length; col++) {
            for (int count = 0; count < arrayNode.length; count++) {
                arrayNode[count][col].setInMaze(false);
            }
        }
    }

    void setWestBound(int west) {
        for (int col = 0; col < west; col++) {
            for (int count = 0; count < arrayNode.length; count++) {
                arrayNode[count][col].setInMaze(false);
            }
        }
    }

    void setSouthBound(int north) {
        for (int col = 0; col < north; col++) {
            for (int count = 0; count < arrayNode[0].length; count++) {
                arrayNode[col][count].setInMaze(false);
            }
        }
    }

    void setNorthBound(int south) {
        for (int col = south + 1; col < arrayNode.length; col++) {
            for (int count = 0; count < arrayNode[0].length; count++) {
                arrayNode[col][count].setInMaze(false);
            }
        }
    }
}
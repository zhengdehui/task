/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TSPAlgo;

import GUI.*;
import java.util.LinkedList;

/**
 *
 * 
 */
public class shortestMovement
{

    private mainGUI main;
    private mazePanel mazePanelInMovement;
    private mazePointCollection collection;
    private LinkedList chosenList;
    private SCISettings sciPort;
    private int pointer1 = 0, pointer2 = 1;
    private int sizeOfList;

    public shortestMovement(mainGUI passedMain, LinkedList listToMove)
    {
        main = passedMain;
        mazePanelInMovement = main.getMazePanel();
        chosenList = listToMove;
        sciPort = main.getSCIPort();
        sizeOfList = chosenList.size();
        collection = main.getMazePointCollection();
    }

    public void control()
    {
        if (pointer2 < sizeOfList)
        {
            //get the two node at pointer1 and pointer2
            node node1 = (node) (chosenList.get(pointer1));
            node node2 = (node) (chosenList.get(pointer2));

            //get the row1, col1, and row2, col2
            int row1 = collection.searchRow(node1);
            int col1 = collection.searchCol(node1);

            int row2 = collection.searchRow(node2);
            int col2 = collection.searchCol(node2);

            //compare with 4 cases to determine the movement
            //get the robot direction
            int robotDirection = mazePanelInMovement.getTempDirection();
            System.out.println("row1 col1 "+row1+" "+col1+" row2 col2 "+row2+" "+col2+" DIR "+robotDirection);
            if (row1 == row2 && col2 == (col1 + 1))
            {
                //moving to the East
                switch (robotDirection)
                {
                    case 0:
                        mazePanelInMovement.setUpManualForward();
                        sciPort.sendCommand("a");
                        pointer1++;
                        pointer2++;
                        break;

                    case 90:
                        //turn right
                        mazePanelInMovement.setUpManualTurnRight();
                        sciPort.sendCommand("c");
                        break;

                    case 180:
                        //turn left 1 time first - then the 2nd time will be handle by 270 case
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;

                    case 270:
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;
                }
            } else if (col1 == col2 && row2 == (row1 + 1))
            {
                //moving to the North
                switch(robotDirection)
                {
                    case 0:
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;
                        
                    case 90:
                        mazePanelInMovement.setUpManualForward();
                        sciPort.sendCommand("a");
                        pointer1++;
                        pointer2++;
                        break;
                        
                    case 180:
                        mazePanelInMovement.setUpManualTurnRight();
                        sciPort.sendCommand("c");
                        break;
                        
                    case 270:
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;
                }
            } else if (row1 == row2 && col1 == (col2 + 1))
            {
                //moving the the West
                switch(robotDirection)
                {
                    case 0:
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;
                        
                    case 90:
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;
                        
                    case 180:
                        mazePanelInMovement.setUpManualForward();
                        sciPort.sendCommand("a");
                        pointer1++;
                        pointer2++;
                        break;
                        
                    case 270:
                        mazePanelInMovement.setUpManualTurnRight();
                        sciPort.sendCommand("c");
                        break;
                }
            } else if (col1 == col2 && row1 == (row2 + 1))
            {
                //moving to the South
                switch(robotDirection)
                {
                    case 0:
                        mazePanelInMovement.setUpManualTurnRight();
                        sciPort.sendCommand("c");
                        break;
                        
                    case 90:
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;
                        
                    case 180:
                        mazePanelInMovement.setUpManualTurnLeft();
                        sciPort.sendCommand("d");
                        break;
                        
                    case 270:
                        mazePanelInMovement.setUpManualForward();
                        sciPort.sendCommand("a");
                        pointer1++;
                        pointer2++;
                        break;
                }
            }
            mazePanelInMovement.setBumpVariable(false);
        }
        //else - move to the end of the list - accomplished
        //if the SCI mode is 3 - means that it is in the TSP mode
        //should call the function to move to the next city
        else if(main.getSCIPort().getExploreFlag() == 3)
        {
            pointer1 = 0;
            pointer2 = 1;
            main.getButtonPanel().getTSPControl().control();
        }
    }
}

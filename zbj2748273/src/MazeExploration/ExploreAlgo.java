/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MazeExploration;

/**
 *
 * 
 */
import GUI.*;
import java.util.*;

public class ExploreAlgo
{
    private mazePointCollection collectionInAlgo;
    private node[][] arrayNode;
    private SCISettings sciInAlgo;
    private mazePanel mazeInAlgo;
    private int responseFlag;
    private int currentRow;
    private int currentCol;
    private node currentNode;
    private node eastNode, northNode, westNode, southNode;
    private int mouseDirection;
    private boolean eastBoundaryFound = false;
    private boolean northBoundaryFound = false;
    private boolean westBoundaryFound = false;
    private boolean southBoundaryFound = false;
    private int eastColBoundary, westColBoundary;
    private int northRowBoundary, southRowBoundary;
    private Stack visitedList;

    public ExploreAlgo(mainGUI passedMain)
    {
        this.collectionInAlgo = passedMain.getMazePointCollection();
        this.sciInAlgo = passedMain.getSCIPort();
        this.mazeInAlgo = passedMain.getMazePanel();
        this.arrayNode = collectionInAlgo.getCollection();

        visitedList = new Stack();
    }

    public void setResponseFlag(int value)
    {
        this.responseFlag = value;
    }
    
    public void setExplorationMode()
    {
        sciInAlgo.setExploreFlag(0);
    }

    public void consult()
    {
        switch (responseFlag)
        {
            case 0:
                //normal operation
                //1) Get the current mouse position   
                currentRow = collectionInAlgo.MouseAtRow();
                currentCol = collectionInAlgo.MouseAtCol();
                currentNode = collectionInAlgo.MouseNode();

                if (currentNode.getVisited() == false)
                {
                    //mark the node as visited
                    currentNode.setVisited(true);

                    //push the node into visited Stack
                    visitedList.push(currentNode);
                }

                //2) Get the east node of the current node
                // if it is not visited - go to that node

                eastNode = collectionInAlgo.getEast(currentNode);
                northNode = collectionInAlgo.getNorth(currentNode);
                westNode = collectionInAlgo.getWest(currentNode);
                southNode = collectionInAlgo.getSouth(currentNode);

                if ((eastBoundaryFound == false || currentCol < eastColBoundary - 1)
                        && (eastNode == null || eastNode.getVisited() == false))
                {
                    mouseDirection = mazeInAlgo.getTempDirection();

                    switch (mouseDirection)
                    {
                        case 0:
                            //proceed with an U command send

                            sciInAlgo.sendCommand("a");
                            break;

                        case 90:
                            //proceed with a R command send
                            sciInAlgo.sendCommand("c");
                            break;

                        case 180:
                            //proceed with a L command send
                            sciInAlgo.sendCommand("d");
                            break;

                        case 270:
                            //proceed with a L command send
                            sciInAlgo.sendCommand("d");
                            break;
                    }
                } //3) Get the north node of the current node
                // if it is not visited - go to that node
                else if ((northBoundaryFound == false || currentRow < northRowBoundary - 1)
                        && (northNode == null || northNode.getVisited() == false))
                {
                    mouseDirection = mazeInAlgo.getTempDirection();

                    switch (mouseDirection)
                    {
                        case 0:
                            //proceed with an L command send
                            sciInAlgo.sendCommand("d");
                            break;

                        case 90:
                            //proceed with a U command send
                            sciInAlgo.sendCommand("a");
                            break;

                        case 180:
                            //proceed with a R command send
                            sciInAlgo.sendCommand("c");
                            break;

                        case 270:
                            //proceed with a L command send
                            sciInAlgo.sendCommand("d");
                            break;
                    }

                } //4) Get the west node of the current node
                // if it is not visited - go to that node
                else if ((westBoundaryFound == false || currentCol > westColBoundary + 1)
                        && (westNode == null || westNode.getVisited() == false))
                {
                    mouseDirection = mazeInAlgo.getTempDirection();

                    switch (mouseDirection)
                    {
                        case 0:
                            //proceed with an L command send
                            sciInAlgo.sendCommand("d");
                            break;

                        case 90:
                            //proceed with a L command send
                            sciInAlgo.sendCommand("d");
                            break;

                        case 180:
                            //proceed with a U command send
                            sciInAlgo.sendCommand("a");
                            break;

                        case 270:
                            //proceed with a R command send
                            sciInAlgo.sendCommand("c");
                            break;
                    }

                } //5) Get the south node of the current node
                // if it is not visited - go to that node
                else if ((southBoundaryFound == false || currentRow > southRowBoundary + 1)
                        && (southNode == null || southNode.getVisited() == false))
                {
                    mouseDirection = mazeInAlgo.getTempDirection();

                    switch (mouseDirection)
                    {
                        case 0:
                            //proceed with an R command send
                            sciInAlgo.sendCommand("c");
                            break;

                        case 90:
                            //proceed with a L command send
                            sciInAlgo.sendCommand("d");
                            break;

                        case 180:
                            //proceed with a L command send
                            sciInAlgo.sendCommand("d");
                            break;

                        case 270:
                            //proceed with a U command send
                            sciInAlgo.sendCommand("a");
                            break;
                    }

                } //Back track path - to return to un-visited nodes
                else if (collectionInAlgo.allNodeVisited() == false)
                {
                    //get the mouse direction
                    mouseDirection = mazeInAlgo.getTempDirection();

                    //see the top node of the visitedList
                    node topNode = (node) (visitedList.peek());
                    int topNodeRow = collectionInAlgo.searchRow(topNode);
                    int topNodeCol = collectionInAlgo.searchCol(topNode);

                    visitedList.pop(); //pop to see the second last node
                    if (!visitedList.isEmpty())
                    {
                        node previousNode = (node) (visitedList.peek());
                        int previousNodeRow = collectionInAlgo.searchRow(previousNode);
                        int previousNodeCol = collectionInAlgo.searchCol(previousNode);

                        //re-push the topNode to the list
                        visitedList.push(topNode);

                        if (topNodeRow == previousNodeRow)
                        {
                            if (topNodeCol > previousNodeCol)
                            {
                                switch (mouseDirection)
                                {
                                    case 0:
                                        sciInAlgo.sendCommand("d");
                                        break;

                                    case 90:
                                        sciInAlgo.sendCommand("d");
                                        break;

                                    case 180:
                                        sciInAlgo.sendCommand("a");
                                        visitedList.pop();
                                        break;

                                    case 270:
                                        sciInAlgo.sendCommand("c");
                                        break;
                                }
                            } else
                            {
                                //topNodeCol < previouseNodeCol
                                switch (mouseDirection)
                                {
                                    case 0:
                                        sciInAlgo.sendCommand("a");
                                        visitedList.pop();
                                        break;

                                    case 90:
                                        sciInAlgo.sendCommand("c");
                                        break;

                                    case 180:
                                        sciInAlgo.sendCommand("d");
                                        break;

                                    case 270:
                                        sciInAlgo.sendCommand("d");
                                        break;
                                }
                            }
                        } else if (topNodeCol == previousNodeCol)
                        {
                            if (topNodeRow > previousNodeRow)
                            {
                                switch (mouseDirection)
                                {
                                    case 0:
                                        sciInAlgo.sendCommand("c");
                                        break;

                                    case 90:
                                        sciInAlgo.sendCommand("d");
                                        break;

                                    case 180:
                                        sciInAlgo.sendCommand("d");
                                        break;

                                    case 270:
                                        sciInAlgo.sendCommand("a");
                                        visitedList.pop();
                                        break;
                                }
                            } else
                            {
                                switch (mouseDirection)
                                {
                                    case 0:
                                        sciInAlgo.sendCommand("d");
                                        break;

                                    case 90:
                                        sciInAlgo.sendCommand("a");
                                        visitedList.pop();
                                        break;

                                    case 180:
                                        sciInAlgo.sendCommand("c");
                                        break;

                                    case 270:
                                        sciInAlgo.sendCommand("d");
                                        break;
                                }
                            }
                        }
                    }
                }

                break;
            case 1:
                currentNode = collectionInAlgo.MouseNode();
                currentNode.setObstacle(true);
                currentNode.setVisited(true);
                sciInAlgo.sendCommand("eae");
                //obstacle operation
                break;

            case 2:
                //boundary operation
                mouseDirection = mazeInAlgo.getTempDirection();
                if (mouseDirection == 0)
                {
                    //east boundary found
                    eastBoundaryFound = true;
                    eastColBoundary = collectionInAlgo.MouseAtCol();
                    
                    System.out.println("eastColBoundary = " + eastColBoundary);
                    for (int row = 0; row < arrayNode.length; row++)
                    {
                        for (int col = eastColBoundary; col < arrayNode[0].length; col++)
                        {
                            arrayNode[row][col].setInMaze(false);
                            arrayNode[row][col].setVisited(true);
                        }
                    }

                } else if (mouseDirection == 90)
                {
                    //north boundary found
                    northBoundaryFound = true;
                    northRowBoundary = collectionInAlgo.MouseAtRow();
                    
                    System.out.println("northRowBoundary = " + northRowBoundary);
                    for (int row = northRowBoundary; row < arrayNode.length; row++)
                    {
                        for (int col = 0; col < arrayNode[0].length; col++)
                        {
                            arrayNode[row][col].setInMaze(false);
                            arrayNode[row][col].setVisited(true);
                        }
                    }

                } else if (mouseDirection == 180)
                {
                    //west boundary found
                    westBoundaryFound = true;
                    westColBoundary = collectionInAlgo.MouseAtCol();

                    System.out.println("westColBoundary = " + westColBoundary);
                    for (int row = 0; row < arrayNode.length; row++)
                    {
                        for (int col = 0; col <= westColBoundary; col++)
                        {
                            arrayNode[row][col].setInMaze(false);
                            arrayNode[row][col].setVisited(true);
                        }
                    }

                } else if (mouseDirection == 270)
                {
                    //south boundary found
                    southBoundaryFound = true;
                    southRowBoundary = collectionInAlgo.MouseAtRow();
                    
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
                sciInAlgo.sendCommand("eae");
                break;
        }
    }
}

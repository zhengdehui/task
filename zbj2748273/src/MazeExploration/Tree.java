/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * 
 */
package MazeExploration;

import java.util.*;
import GUI.*;

public class Tree
{
    private mainGUI main;
    private node root;
    private mazePointCollection maze;
    private int robotDirection = 0; //0 for 0 degree, 90 for 90 degree, ...
    private Stack visitedList;
    private SCISettings outputStream;
    
    //Boundary Detection
    private boolean eastBoundaryDetect = false;
    private int eastBoundaryCol;
    private boolean westBoundaryDetect = false;
    private int westBoundaryCol;
    private boolean northBoundaryDetect = false;
    private int northBoundaryRow;
    private boolean southBoundaryDetect = false;
    private int southBoundaryRow;
    
    //Receive Command from Receiver
    public static int SCIResponse; //0: DONE, 1: OBS, 2: BOU
    private boolean animationDone; //true: animation finished

    public Tree(node rootNode, mazePointCollection passedMaze, SCISettings output, mainGUI main)
    {
        this.main = main;
        this.root = rootNode;
        this.maze = passedMaze;
        visitedList = new Stack();
        outputStream = output;
    }

    public void startTraversal()
    {
        subtreeTraversal(null, root);
    }

    private void subtreeTraversal(node preNode, node traverseNode)
    {
        //Stop condition: whenever a node is visited or an obstacle - quit
        if (traverseNode == null || traverseNode.getInMaze() == false)
        {
            //Modify here to update all the boundary to the maze

            if (robotDirection == 0)
            {
                //robot is facing the East Direction
                eastBoundaryDetect = true;
                eastBoundaryCol = maze.searchCol(preNode);
                System.out.println("East Boundary Column: " + eastBoundaryCol);
                System.out.println("Hit East Boundary, move backward \n");
            } else if (robotDirection == 180)
            {
                westBoundaryDetect = true;
                westBoundaryCol = maze.searchCol(preNode);
                System.out.println("West Boundary Column: " + westBoundaryCol);
                System.out.println("Hit West Boundary, move backward \n");
            } else if (robotDirection == 90)
            {
                northBoundaryDetect = true;
                northBoundaryRow = maze.searchRow(preNode);
                System.out.println("North Boundary Row: " + northBoundaryRow);
                System.out.println("Hit North Boundary, move backward \n");
            } else if (robotDirection == 270)
            {
                southBoundaryDetect = true;
                southBoundaryRow = maze.searchRow(preNode);
                System.out.println("South Boundary Row: " + southBoundaryRow);
                System.out.println("Hit South Boundary, move backward \n");
            }
            outputStream.sendCommand("eae"); // BACK - reverse from boundary
            return;
        } else if (traverseNode.getVisited() == true)
        {
            return;
        } else if (traverseNode.getObstacle() == true)
        {
            //hit an obstacle node, print out the node then quite
            System.out.println(traverseNode.getID() + " is obstacle");
            System.out.println("Reverse back\n");
            outputStream.sendCommand("eae"); //DOWN - reverse from obstacle
            traverseNode.setVisited(true);
            return;
        }
        System.out.println(traverseNode.getID() + "\n");
        visitedList.push(traverseNode);
        //Call the function to move to this node
        //set the traverse boolean to true - node visited
        traverseNode.setVisited(true);

        if (eastBoundaryDetect == false || maze.searchCol(traverseNode) < eastBoundaryCol)
        {
            if (maze.getEast(traverseNode) == null || maze.getEast(traverseNode).getInMaze() == false || maze.getEast(traverseNode).getVisited() == false)
            {
                if (robotDirection == 90)
                {
                    System.out.println("Turn Right");
                    outputStream.sendCommand("c");
                } else if (robotDirection == 180)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");

                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                } else if (robotDirection == 270)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                }

                robotDirection = 0;
                System.out.println("Move Forward");
                outputStream.sendCommand("a");

                //set the east node correspondingly: normal (DONE) or obstacle (OBS) or boundary (BOU)
                if (SCIResponse == 1)
                {
                    maze.getEast(traverseNode).setObstacle(true);
                } else if (SCIResponse == 2)
                {
                    if (maze.getEast(traverseNode) != null)
                    {
                        //The real size is less than the virtual size
                        maze.getEast(traverseNode).setInMaze(false);
                    }
                }
                subtreeTraversal(traverseNode, maze.getEast(traverseNode)); // 0 1
            }
        }

        if (northBoundaryDetect == false || maze.searchRow(traverseNode) < northBoundaryRow)
        {
            if (maze.getNorth(traverseNode) == null || maze.getNorth(traverseNode).getInMaze() == false || maze.getNorth(traverseNode).getVisited() == false)
            {
                if (robotDirection == 0)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                } else if (robotDirection == 180)
                {
                    System.out.println("Turn Right");
                    outputStream.sendCommand("c");
                } else if (robotDirection == 270)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                }
                robotDirection = 90;
                System.out.println("Move Forward");
                outputStream.sendCommand("a");


                if (SCIResponse == 1)
                {
                    maze.getNorth(traverseNode).setObstacle(true);
                } else if (SCIResponse == 2)
                {
                    if (maze.getNorth(traverseNode) != null)
                    {
                        maze.getNorth(traverseNode).setInMaze(false);
                    }
                }

                subtreeTraversal(traverseNode, maze.getNorth(traverseNode)); // 0 1
            }
        }

        if (westBoundaryDetect == false || maze.searchCol(traverseNode) > westBoundaryCol)
        {
            if (maze.getWest(traverseNode) == null || maze.getWest(traverseNode).getInMaze() == false || maze.getWest(traverseNode).getVisited() == false)
            {
                if (robotDirection == 0)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                } else if (robotDirection == 90)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                } else if (robotDirection == 270)
                {
                    System.out.println("Turn Right");
                    outputStream.sendCommand("c");
                }

                robotDirection = 180;
                System.out.println("Move Forward");
                outputStream.sendCommand("a");
                if (SCIResponse == 1)
                {
                    maze.getWest(traverseNode).setObstacle(true);

                } else if (SCIResponse == 2)
                {
                    if (maze.getWest(traverseNode) != null)
                    {
                        maze.getWest(traverseNode).setInMaze(false);
                    }
                }
                subtreeTraversal(traverseNode, maze.getWest(traverseNode)); // 0 1
            }
        }

        if (southBoundaryDetect == false || maze.searchRow(traverseNode) > southBoundaryRow)
        {
            if (maze.getSouth(traverseNode) == null || maze.getSouth(traverseNode).getInMaze() == false || maze.getSouth(traverseNode).getVisited() == false)
            {
                if (robotDirection == 0)
                {
                    System.out.println("Turn Right");
                    outputStream.sendCommand("c");
                } else if (robotDirection == 90)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                } else if (robotDirection == 180)
                {
                    System.out.println("Turn Left");
                    outputStream.sendCommand("d");
                }
                robotDirection = 270;
                System.out.println("Move Forward");
                outputStream.sendCommand("a");

                if (SCIResponse == 1)
                {
                    maze.getSouth(traverseNode).setObstacle(true);
                } else if (SCIResponse == 2)
                {
                    if (maze.getSouth(traverseNode) != null)
                    {
                        maze.getSouth(traverseNode).setInMaze(false);
                    }
                }

                subtreeTraversal(traverseNode, maze.getSouth(traverseNode)); // 0 1
            }
        }

        //if all node visited, return, don't have to traverse
        if (maze.allNodeVisited() == false)
        {
            //write the back track using stack
            node currentNode = (node) visitedList.pop();
            int currentNodeRow = maze.searchRow(currentNode);
            int currentNodeCol = maze.searchCol(currentNode);

            if (!visitedList.isEmpty())
            {
                node previousNode = (node) visitedList.peek();
                int previousNodeRow = maze.searchRow(previousNode);
                int previousNodeCol = maze.searchCol(previousNode);

                if (currentNodeRow == previousNodeRow)
                {
                    //Two nodes are in the same row
                    if (currentNodeCol > previousNodeCol)
                    {
                        if (robotDirection == 0)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        } else if (robotDirection == 90)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        } else if (robotDirection == 270)
                        {
                            System.out.println("Turn Right");
                            outputStream.sendCommand("c");
                        }
                        System.out.println("Move Forward");
                        outputStream.sendCommand("a");

                        robotDirection = 180;
                    } else
                    {
                        if (robotDirection == 90)
                        {
                            System.out.println("Turn Right");
                            outputStream.sendCommand("c");
                        } else if (robotDirection == 180)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        } else if (robotDirection == 270)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        }
                        System.out.println("Move Forward");
                        outputStream.sendCommand("a");

                        robotDirection = 0;
                    }
                } else if (currentNodeCol == previousNodeCol)
                {
                    //Two nodes are in the same col
                    if (currentNodeRow > previousNodeRow)
                    {
                        if (robotDirection == 0)
                        {
                            System.out.println("Turn Right");
                            outputStream.sendCommand("c");
                        } else if (robotDirection == 90)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        } else if (robotDirection == 180)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        }
                        System.out.println("Move Forward");
                        outputStream.sendCommand("a");

                        robotDirection = 270;
                    } else
                    {
                        if (robotDirection == 0)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        } else if (robotDirection == 180)
                        {
                            System.out.println("Turn Right");
                            outputStream.sendCommand("c");
                        } else if (robotDirection == 270)
                        {
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                            System.out.println("Turn Left");
                            outputStream.sendCommand("d");
                        }
                        System.out.println("Move Forward");
                        outputStream.sendCommand("a");

                        robotDirection = 90;
                    }
                }
            }
        }
    }
}

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
public class TSPAlgorithm
{
    private mazePointCollection collection;
    private node[][] nodeArray;

    //constructor
    public TSPAlgorithm(mazePointCollection passedCollection)
    {
        //passedCollection is the list that contain all the node the mouse has to visit
        collection = passedCollection;
        nodeArray = collection.getCollection();
    }
    /*
    public LinkedList<LinkedList> TSPAllPossible(node startingPoint, node endingPoint)
    {
        //find the direct path first
        LinkedList<LinkedList> directPath = TSPTwoPoints(startingPoint, endingPoint);

        //check for the startingPoint neighbors, if their neighbors have a grade higher by 1
        //then consider this neighbor and the endingPoint
        if (collection.getEast(startingPoint).getGrade() == startingPoint.getGrade() + 1)
        {
            LinkedList<LinkedList> eastCollectionPath = TSPTwoPoints(collection.getEast(startingPoint), endingPoint);

            for (int i = 0; i < eastCollectionPath.size(); i++)
            {
                LinkedList temp = eastCollectionPath.get(i);

                node checkedNode = (node) (temp.get(1));
                if (checkedNode.getID() == startingPoint.getID())
                {
                    continue;
                }

                temp.addFirst(startingPoint);
                directPath.add(temp);
            }
        }

        if (collection.getNorth(startingPoint).getGrade() == startingPoint.getGrade() + 1)
        {
            LinkedList<LinkedList> northCollectionPath = TSPTwoPoints(collection.getNorth(startingPoint), endingPoint);

            for (int i = 0; i < northCollectionPath.size(); i++)
            {
                LinkedList temp = northCollectionPath.get(i);

                node checkedNode = (node) (temp.get(1));
                if (checkedNode.getID() == startingPoint.getID())
                {
                    continue;
                }

                temp.addFirst(startingPoint);
                directPath.add(temp);
            }
        }

        if (collection.getWest(startingPoint).getGrade() == startingPoint.getGrade() + 1)
        {
            LinkedList<LinkedList> westCollectionPath = TSPTwoPoints(collection.getWest(startingPoint), endingPoint);

            for (int i = 0; i < westCollectionPath.size(); i++)
            {
                LinkedList temp = westCollectionPath.get(i);

                node checkedNode = (node) (temp.get(1));
                if (checkedNode.getID() == startingPoint.getID())
                {
                    continue;
                }

                temp.addFirst(startingPoint);
                directPath.add(temp);
            }
        }

        if (collection.getSouth(startingPoint).getGrade() == startingPoint.getGrade() + 1)
        {
            LinkedList<LinkedList> southCollectionPath = TSPTwoPoints(collection.getSouth(startingPoint), endingPoint);

            for (int i = 0; i < southCollectionPath.size(); i++)
            {
                LinkedList temp = southCollectionPath.get(i);

                node checkedNode = (node) (temp.get(1));
                if (checkedNode.getID() == startingPoint.getID())
                {
                    continue;
                }

                temp.addFirst(startingPoint);
                directPath.add(temp);
            }
        }
        return directPath;

    }
    */

    public LinkedList<LinkedList> TSPTwoPoints(node startingPoint, node endingPoint)
    {
        //management LinkedList, each element inside is a possiblePath
        LinkedList<LinkedList> possiblePath = new LinkedList();

        //label all points in the collection to grade -1 (so that 
        //this method can be used more than one time

        for (int row = 0; row < nodeArray.length; row++)
        {
            for (int col = 0; col < nodeArray[0].length; col++)
            {
                if (nodeArray[row][col].getInMaze() == true && nodeArray[row][col].getObstacle() == false)
                {
                    nodeArray[row][col].setGrade(-1);
                }
            }
        }

        //set the ending point Grade to be 0
        //search the node by ID then set the grade to be 0
        collection.searchNodeByID(endingPoint.getID()).setGrade(0);

        //Label all the grade of the Node in maze using recursive method
        labelAllNode(startingPoint);
        
        for (int row = nodeArray.length - 1; row >= 0; row--)
        {
            for (int col = 0; col < nodeArray[0].length; col++)
            {
                if (nodeArray[row][col].getInMaze() == true)
                {
                    System.out.print(nodeArray[row][col].getGrade() + " ");
                }
            }
            System.out.println();
        }

        //After labeling, done the first part - labeling
        //Generating all the possible paths
        int startingPointGrade = startingPoint.getGrade();
        int processingGrade = startingPointGrade - 1;

        LinkedList temp = new LinkedList();
        temp.add(startingPoint);
        possiblePath.add(temp);

        while (processingGrade != -1)
        {
            LinkedList<LinkedList> replaceList = new LinkedList();

            for (int i = 0; i < possiblePath.size(); i++)
            {
                LinkedList processingList = possiblePath.get(i);
                node lastNode = (node) processingList.getLast();

                //count the number of adjacent node of lastNode that has the same grade as processingGrade

                if (collection.getEast(lastNode).getGrade() == processingGrade)
                {
                    LinkedList tempList = new LinkedList();
                    //copy the currentList to another copy
                    for (int j = 0; j < processingList.size(); j++)
                    {
                        tempList.add(processingList.get(j));
                    }
                    tempList.add(collection.getEast(lastNode));

                    //add this tempList into our management list
                    replaceList.add(tempList);
                }

                if (collection.getNorth(lastNode).getGrade() == processingGrade)
                {
                    LinkedList tempList = new LinkedList();
                    //copy the currentList to another copy
                    for (int j = 0; j < processingList.size(); j++)
                    {
                        tempList.add(processingList.get(j));
                    }
                    tempList.add(collection.getNorth(lastNode));

                    //add this tempList into our management list
                    replaceList.add(tempList);
                }

                if (collection.getWest(lastNode).getGrade() == processingGrade)
                {
                    LinkedList tempList = new LinkedList();
                    //copy the currentList to another copy
                    for (int j = 0; j < processingList.size(); j++)
                    {
                        tempList.add(processingList.get(j));
                    }
                    tempList.add(collection.getWest(lastNode));

                    //add this tempList into our management list
                    replaceList.add(tempList);
                }

                if (collection.getSouth(lastNode).getGrade() == processingGrade)
                {
                    LinkedList tempList = new LinkedList();
                    //copy the currentList to another copy
                    for (int j = 0; j < processingList.size(); j++)
                    {
                        tempList.add(processingList.get(j));
                    }
                    tempList.add(collection.getSouth(lastNode));

                    //add this tempList into our management list
                    replaceList.add(tempList);
                }
            }
            //change the possiblePath to replaceList
            possiblePath = replaceList;

            processingGrade--;
        }
        return possiblePath;
    }

    public void labelAllNode(node startingPoint)
    {
        int currentGrade = 0;

        //stopping condition, when the start node is marked, if it is mark then the currentGrade must reach
        //the startNode + 1
        while (startingPoint.getGrade() == -1 || currentGrade <= startingPoint.getGrade() + 1)
        {
            
            for(int row = 0; row < nodeArray.length; row++)
            {
                for(int col = 0; col < nodeArray[0].length; col++)
                {
                    if (nodeArray[row][col]==null)break;
                    
                    node currentNode = nodeArray[row][col];

                    if (currentNode.getInMaze() == true && currentNode.getGrade() == currentGrade)
                    {
                        //mark all the adjacent node
                        node eastNode = collection.getEast(currentNode);
                        node northNode = collection.getNorth(currentNode);
                        node westNode = collection.getWest(currentNode);
                        node southNode = collection.getSouth(currentNode);
                        
                        if (eastNode.getInMaze() == true && eastNode.getObstacle() == false && eastNode.getGrade() == -1)
                        {
                            eastNode.setGrade(currentGrade + 1);
                        }

                        if (northNode.getInMaze() == true && northNode.getObstacle() == false && northNode.getGrade() == -1)
                        {
                            northNode.setGrade(currentGrade + 1);
                        }

                        if (westNode.getInMaze() == true && westNode.getObstacle() == false && westNode.getGrade() == -1)
                        {
                            westNode.setGrade(currentGrade + 1);
                        }

                        if (southNode.getInMaze() == true && southNode.getObstacle() == false && southNode.getGrade() == -1)
                        {
                            southNode.setGrade(currentGrade + 1);
                        }
                    }
                }
            }
            currentGrade++;
        }
    }
}

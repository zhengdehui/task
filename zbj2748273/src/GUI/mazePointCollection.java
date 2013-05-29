/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;
import TSPAlgo.*;
import java.awt.Point;

public class mazePointCollection
{

    private node mazePoints[][] = new node[11][12];
    private int ID = 0;

    public mazePointCollection(nodeClickTSP handler)
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                mazePoints[row][col] = new node(ID++, false, false, col * 66 + 33, 660 - 66 * row + 33, handler);
                mazePoints[row][col].setInMaze(true);
                mazePoints[row][col].setObstacle(false);
            }
        }
    }

    public node getEast(node searchNode)
    {
        int row = searchRow(searchNode);
        int col = searchCol(searchNode);

        if (col == mazePoints[0].length - 1)
        {
            return null;
        }
        return mazePoints[row][col + 1];
    }

    public node getWest(node searchNode)
    {
        int row = searchRow(searchNode);
        int col = searchCol(searchNode);

        if (col == 0)
        {
            return null;
        }
        return mazePoints[row][col - 1];
    }

    public node getNorth(node searchNode)
    {
        int row = searchRow(searchNode);
        int col = searchCol(searchNode);

        if (row == mazePoints.length - 1)
        {
            return null;
        }
        return mazePoints[row + 1][col];
    }

    public node getSouth(node searchNode)
    {
        int row = searchRow(searchNode);
        int col = searchCol(searchNode);

        if (row == 0)
        {
            return null;
        }
        return mazePoints[row - 1][col];
    }

    public int searchRow(node searchNode)
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                if (searchNode.getID() == mazePoints[row][col].getID())
                {
                    return row;
                }
            }
        }
        return 10;
    }

    public int searchCol(node searchNode)
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                if (searchNode.getID() == mazePoints[row][col].getID())
                {
                    return col;
                }
            }
        }
        return 11;
    }

    public node nodeAtRowCol(int row, int col)
    {
        if (row < 0 || row > 10)
        {
            System.out.println("Row out of bound");
            return null;
        }


        if (col < 0 || col > 11)
        {
            System.out.println("Col out of bound");
            return null;
        }
        return mazePoints[row][col];
    }

    public node searchNodeByID(int ID)
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                if (mazePoints[row][col].getID() == ID)
                {
                    return mazePoints[row][col];
                }
            }
        }
        return mazePoints[0][0];
    }
    public node searchNodeByMouse()
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                if (mazePoints[row][col].getMouse())
                {
                    return mazePoints[row][col];
                }
            }
        }
        return null;
    }
    public boolean allNodeVisited()
    {
        boolean result = true;
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                result = result & mazePoints[row][col].getVisited();
            }
        }
        return result;
    }

    public int noRows()
    {
        return mazePoints.length;
    }

    public int noCols()
    {
        return mazePoints[0].length;
    }

    public node[][] getCollection()
    {
        return mazePoints;
    }

    public void resetTheWholeMaze()
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                mazePoints[row][col].setVisited(false);
                mazePoints[row][col].setObstacle(false);
            }
        }
    }

    public void clearAllObstacle()
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                mazePoints[row][col].setObstacle(false);
            }
        }
    }

    public int MouseAtRow()
    {
        //return the row that the mouse is

        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                if (mazePoints[row][col].getMouse() == true)
                {
                    return row;
                }
            }
        }
        return -1;
    }

    public int MouseAtCol()
    {
        //return the collumn that the mouse is
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                if (mazePoints[row][col].getMouse() == true)
                {
                    return col;
                }
            }
        }
        return -1;
    }

    public node MouseNode()
    {
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                if (mazePoints[row][col].getMouse() == true)
                {
                    return mazePoints[row][col];
                }
            }
        }
        return null;
    }

    public void clearMousePosition()
    {
        //this will clear all the MousePlace to be false
        for (int row = 0; row < mazePoints.length; row++)
        {
            for (int col = 0; col < mazePoints[0].length; col++)
            {
                mazePoints[row][col].setMousePlace(false);
            }
        }
    }
}

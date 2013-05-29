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
public class TSPControl
{

    private mainGUI main;
    private LinkedList<node> cityList;
    private node startCity;
    private boolean returnToBase = false;

    public TSPControl(mainGUI passedMain, LinkedList<node> passedCityList)
    {
        main = passedMain;
        cityList = passedCityList;
        startCity = passedCityList.get(0);
    }

    private LinkedList<node> oneShortestPathBetweenCities(node startCity, node endCity)
    {
        LinkedList<LinkedList> allPathCollection;

        TSPAlgorithm brain = new TSPAlgorithm(main.getMazePointCollection());
        allPathCollection = brain.TSPTwoPoints(startCity, endCity);
        
        int smallestNumCity = allPathCollection.get(0).size();
        LinkedList<node> returnPath = allPathCollection.get(0);

        for (int i = 1; i < allPathCollection.size(); i++)
        {
            if (allPathCollection.get(i).size() < smallestNumCity)
            {
                smallestNumCity = allPathCollection.get(i).size();
                returnPath = allPathCollection.get(i);
            }
        }
        return returnPath;
    }

    public void control()
    {
        //the mouse location
        node originCity = cityList.get(0);
        
        //move as normal, as long as cityList contain node
        if (cityList.size() > 1)
        {
            node nearest = cityList.get(1);

            LinkedList<node> ShortestPath = oneShortestPathBetweenCities(originCity, nearest);//(0,1)

            for (int i = 2; i < cityList.size(); i++)
            {
                //compare with each city to find the nearest city
                LinkedList<node> tempShortestPath = oneShortestPathBetweenCities(originCity, cityList.get(i));

                if (tempShortestPath.size() < ShortestPath.size())
                {
                    ShortestPath = tempShortestPath;
                    nearest = cityList.get(i);
                }
            }

            //After doing this, the nearest city to the Origin has been found, now modify the cityList
            LinkedList<node> newCityList = new LinkedList();

            //the new mouse location
            newCityList.add(nearest);

            //add all the remaining cities in this newCityList (except the nearest)
            for (int i = 1; i < cityList.size(); i++)
            {
                if (cityList.get(i).getID() != nearest.getID())
                {
                    newCityList.add(cityList.get(i));
                }
            }

            //update the cityList to the newCityList
            cityList = newCityList;

            //make the mouse moves between originCity and nearest
            shortestMovement move = new shortestMovement(main, ShortestPath);
            main.getSCIPort().setExploreFlag(3);
            main.getSCIPort().setMoveObject(move);
            move.control();
        }
        else if(returnToBase == false)
        {
            //return to base
            returnToBase = true;
            
            //create a path from last visited node to the origin
            LinkedList<node> ShortestPath = oneShortestPathBetweenCities(cityList.get(0), startCity);
            
             //make the mouse moves
            shortestMovement move = new shortestMovement(main, ShortestPath);
            main.getSCIPort().setExploreFlag(3);
            main.getSCIPort().setMoveObject(move);
            move.control();
        }
    }
}

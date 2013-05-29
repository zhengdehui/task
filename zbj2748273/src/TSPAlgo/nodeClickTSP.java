/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TSPAlgo;

import GUI.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
/**
 *
 *
 */
public class nodeClickTSP implements ActionListener
{
    private LinkedList<node> cityList;
    private mazePointCollection collection;
    private mainGUI main;
    
    public nodeClickTSP(mainGUI passedMain)
    {
        //this class is used to create object that will handle the button click for TSP
        cityList = new LinkedList();   
        main = passedMain;
    }
    
    public LinkedList getCityList()
    {
        return cityList;
    }
    
    public void setCollection(mazePointCollection passedCollection)
    {
        collection = passedCollection;
    }
    
    public void actionPerformed(ActionEvent e)
    {

        //get where the mouse currently is and set as starting point
        if(cityList.isEmpty()) {
            cityList.add(collection.searchNodeByMouse());
        }

        int ID = Integer.parseInt(e.getActionCommand());
        node addingNode = collection.searchNodeByID(ID);
        
        if(addingNode.getCityButton().getBackground()==Color.GREEN)
        {
            //Removing a city node
            LinkedList<node> newCityList = new LinkedList();
            newCityList.add(collection.searchNodeByMouse());
            addingNode.getCityButton().setBackground(Color.YELLOW);
            
            for (int i = 1; i < cityList.size(); i++)
            {
                if (cityList.get(i).getID() != addingNode.getID())
                {
                    newCityList.add(cityList.get(i));
                }
            }
            
            cityList = newCityList;
            main.getButtonPanel().setDisplayNumberCity(cityList.size()-1);
        }
        else
        {
            //Adding a city node
            if(addingNode.getVisited())
            {
                cityList.add(addingNode);
                addingNode.getCityButton().setBackground(Color.GREEN);
                main.getButtonPanel().setDisplayNumberCity(cityList.size()-1);
            }
        }

    }
    
    public void clearCityList()
    {
        cityList = new LinkedList();
        main.getButtonPanel().setDisplayNumberCity(cityList.size());
    }
    
    public int getCounter()
    {
        return cityList.size();
    }
}

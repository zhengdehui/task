package GUI;

import java.io.*;
import java.util.Scanner;

class mazeWrite {

    private mazePointCollection collectionOfNode;
    private node[][] arrayNode;

    public mazeWrite(mazePointCollection passedCollectionOfNode) {
        collectionOfNode = passedCollectionOfNode;
        arrayNode = collectionOfNode.getCollection();
    }

     public void saveFile(File file) {
        try {
            PrintWriter writeIn = new PrintWriter(file);

            writeIn.println("mazesize,11,12");
            for (int row = 0; row < arrayNode.length; row++) {
                for (int col = 0; col < arrayNode[0].length; col++) {
                    if (arrayNode[row][col].getObstacle() == true) {
                        writeIn.println("obstacle," + (col) + "," + (row));
                    }
                    //  if (arrayNode[row][col].getInMaze() == true) {
                    //      writeIn.println("boundery," + (col) + "," + (row));
                    //  }
                }
            }
            for (int WEST = 0; WEST < arrayNode[0].length; WEST++) {  
                System.out.println(arrayNode[0][WEST].getInMaze());
                if (arrayNode[5][WEST].getInMaze() == true) {
                    System.out.println("WEST");
                    writeIn.println("WEST," + (WEST));
                    break;
                }
            }
            for (int NORTH = arrayNode.length - 1; NORTH > 0; NORTH--) {
                if (arrayNode[NORTH][6].getInMaze() == true) {
                    writeIn.println("NORTH," + (NORTH));
                    break;
                }
            }
            for (int EAST = arrayNode[0].length - 1; EAST > 0; EAST--) {
                if (arrayNode[5][EAST].getInMaze() == true) {
                    writeIn.println("EAST," + (EAST));
                    break;
                }
            }
            for (int SOUTH = 0; SOUTH < arrayNode.length; SOUTH++) {
                if (arrayNode[SOUTH][6].getInMaze() == true) {
                    writeIn.println("SOUTH," + (SOUTH));
                    break;
                }
            }

            writeIn.println("end");
            writeIn.flush();
            writeIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile(File file) {
        try {
            // File f = new File("MazeGrid.txt");
            Scanner fileInput = new Scanner(file);
            while (fileInput.hasNext()) {
                //input = read the entire line
                String input = (fileInput.nextLine().toString()).trim();
                if (!input.isEmpty() && !input.equalsIgnoreCase("end")) {
                    String temp = input.substring(0, (input.indexOf(",")));
                    //temp = detecting the words before the first comma
                    if (temp.equalsIgnoreCase("mazesize")) {
                        //value = read the string after the first ,
                        String value = input.substring((input.indexOf(",")) + 1);
                        String c = value.substring(0, (value.indexOf(",")));
                        String r = value.substring((value.indexOf(",")) + 1, value.length());
                    }
                    if (temp.equalsIgnoreCase("obstacle")) {
                        String value = input.substring((input.indexOf(",")) + 1);
                        String c = value.substring(0, (value.indexOf(",")));
                        int x = Integer.parseInt(c);
                        String r = value.substring((value.indexOf(",")) + 1, value.length());
                        int y = Integer.parseInt(r);
                        arrayNode[y][x].setObstacle(true);
                    }
                    if (temp.equalsIgnoreCase("boundary")) {
                        String value = input.substring((input.indexOf(",")) + 1);
                        String c = value.substring(0, (value.indexOf(",")));
                        int x = Integer.parseInt(c);
                        String r = value.substring((value.indexOf(",")) + 1, value.length());
                        int y = Integer.parseInt(r);
                        arrayNode[y][x].setInMaze(false);
                    }
                }
            }
            fileInput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
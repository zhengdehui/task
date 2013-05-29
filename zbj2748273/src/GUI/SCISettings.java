/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import TSPAlgo.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.comm.*;
import javax.swing.*;
import javax.swing.Timer;

/**
 *
 *
 *
 * Legend of SCI commands on Control Side
 * Send Message: U - UP, B - BACK, D - DOWN, R - RIGHT, L - LEFT
 * Receive Message: E - DONE, S - OBS, Y - BOU
 */
import MazeExploration.*;

public class SCISettings extends JFrame implements Runnable, SerialPortEventListener, ActionListener {

    private Enumeration portList;
    private CommPortIdentifier portId;
    private mainGUI main;
    private mazePanel mazeInSCI;
    private buttonPanel btnInSCI;
    private SerialPort serialPort;
    String lastReceived;
    private ExploreAlgo brainInSCI;
    //the output stream to send out
    private OutputStream outputStream;
    private InputStream inputStream;
    boolean outputBufferEmptyFlag = false;
    boolean portFound = false;
    private String defaultPort;
    private int baudRate;
    private int dataBit;
    private int stopBit;
    private int parityBit;
    private JFrame frame;
    //JComboBox creation
    private JComboBox comBox;
    private JComboBox baudBox;
    private JComboBox dataBox;
    private JComboBox parityBox;
    private JComboBox stopBox;
    private Thread readThread;
    private String dataReceive="j", previousSendCommand;
    private int explore; //0; exploration mode, 1: manual mode, 2: fastest + shortest mode
    private shortestMovement move;

    public SCISettings(mainGUI main) {
        this.main = main;
        mazeInSCI = main.getMazePanel();
        btnInSCI = main.getButtonPanel();
        frame = new JFrame("Configure SCI");
        JOptionPane dp = new JOptionPane();
        dp.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(320, 300));

        JLabel txtPort = new JLabel("Port");
        String[] comPort = {
            "COM1", "COM2", "COM3", "COM4", "COM5"
        };
        comBox = new JComboBox(comPort);
        comBox.setActionCommand("comBox");
        comBox.addActionListener(this);
        comBox.setPreferredSize(new Dimension(100, 30));
        comBox.setSelectedIndex(4);

        JLabel txtBaud = new JLabel("Baud rate");
        String[] baudRate = {
            "300", "1200", "2400", "4800", "9600", "19200", "38400", "57600"
        };
        baudBox = new JComboBox(baudRate);
        baudBox.setActionCommand("baudBox");
        baudBox.addActionListener(this);
        baudBox.setPreferredSize(new Dimension(100, 30));
        baudBox.setSelectedIndex(4);

        JLabel txtData = new JLabel("Data bits");
        String[] databit = {
            "5", "6", "7", "8"
        };
        dataBox = new JComboBox(databit);
        dataBox.setActionCommand("dataBox");
        dataBox.addActionListener(this);
        dataBox.setPreferredSize(new Dimension(100, 30));
        dataBox.setSelectedIndex(3);

        JLabel txtParity = new JLabel("Parity bit");
        String[] parityBit = {
            "even", "mark", "none", "odd"
        };
        parityBox = new JComboBox(parityBit);
        parityBox.setActionCommand("parityBox");
        parityBox.addActionListener(this);
        parityBox.setPreferredSize(new Dimension(100, 30));
        parityBox.setSelectedIndex(2);


        JLabel txtStop = new JLabel("Stop bits");
        String[] stopBit = {
            "1", "1.5", "2"
        };
        stopBox = new JComboBox(stopBit);
        stopBox.setActionCommand("stopBox");
        stopBox.addActionListener(this);
        stopBox.setPreferredSize(new Dimension(100, 30));
        stopBox.setSelectedIndex(0);

        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new GridLayout(5, 2, 2, 10));
        choicePanel.add(txtPort);
        choicePanel.add(comBox);
        choicePanel.add(txtBaud);
        choicePanel.add(baudBox);
        choicePanel.add(txtData);
        choicePanel.add(dataBox);
        choicePanel.add(txtParity);
        choicePanel.add(parityBox);
        choicePanel.add(txtStop);
        choicePanel.add(stopBox);

        JPanel confirmPane = new JPanel();

        confirmPane.add(new Label());
        JButton okBut = new JButton("OK");
        okBut.setActionCommand("okBut");
        okBut.addActionListener(this);

        JButton cancelBut = new JButton("Cancel");
        cancelBut.setActionCommand("cancelBut");
        cancelBut.addActionListener(this);

        confirmPane.add(okBut);
        confirmPane.add(cancelBut);
        dp.add(choicePanel, "North");
        dp.add(confirmPane, "South");
        frame.setContentPane(dp);

        frame.setVisible(true);
    }

    public void setExploreFlag(int value) {
        explore = value;
    }

    public int getExploreFlag() {
        return explore;
    }

    public void setMoveObject(shortestMovement passMove) {
        move = passMove;
    }

    public void actionPerformed(ActionEvent e) {
        String source = e.getActionCommand();

        //compare to give each event a correct handler
        if (source.equals("comBox")) {
            //Port number setting, e.g COM3
            defaultPort = (String) comBox.getSelectedItem();

        } else if (source.equals("baudBox")) {
            //Baudrate settings, e.g 9600
            baudRate = Integer.parseInt((String) baudBox.getSelectedItem());
        } else if (source.equals("dataBox")) {
            //data-bit setting, e.g 8
            dataBit = Integer.parseInt((String) dataBox.getSelectedItem());
        } else if (source.equals("parityBox")) {
            //parity bit setting, e.g non
            parityBit = parityBox.getSelectedIndex();
        } else if (source.equals("stopBox")) {
            //stop bit setting, e.g 1
            stopBit = stopBox.getSelectedIndex();
        } else if (source.equals("okBut")) {
            //Ok button press, open the comport communication
            //set the port configuration and open the comport
            // btnInSCI.SCIlight.setBackground(Color.GREEN);
            portList = CommPortIdentifier.getPortIdentifiers();
            //portList = CommPortIdentifier.getPortIdentifiers();
            System.out.println("be4 while");
            while (portList.hasMoreElements()) {
                portId = (CommPortIdentifier) portList.nextElement();

                System.out.println("Here4");
                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

                    // serial port
                    System.out.println("Port ID: " + portId.getName());
                    System.out.println("Default Port: " + defaultPort);
                    if (portId.getName().equals(defaultPort)) {
                        portFound = true;
                        btnInSCI.SCIlight.setBackground(Color.GREEN);
                        try {
                            serialPort = (SerialPort) portId.open("SimpleWrite", 2000);
                        } catch (PortInUseException e1) {
                            System.out.println("Port in use.");

                        }

                        try {
                            outputStream = serialPort.getOutputStream();
                        } catch (IOException e2) {
                        }

                        try {
                            serialPort.setSerialPortParams(
                                    baudRate, dataBit,
                                    stopBit, parityBit);
                        } catch (UnsupportedCommOperationException e3) {
                        }

                        try {
                            inputStream = serialPort.getInputStream();
                        } catch (IOException e6) {
                        }
                        try {
                            serialPort.addEventListener(this);
                        } catch (TooManyListenersException e4) {
                        }

                        serialPort.notifyOnDataAvailable(true);

                        try {
                            serialPort.notifyOnOutputEmpty(true);
                            break;
                        } catch (Exception e5) {
                            System.out.println("Error setting event notification");
                            System.out.println(e5.toString());
                            System.exit(-1);
                        }
                    }


                    readThread = new Thread(this);
                    readThread.start();

                }
            }
            if (portFound == false) {
                JOptionPane.showMessageDialog(null, "Com Port not found");
            } else {
                JOptionPane.showMessageDialog(null, "Comport Setup Successfully");

                //set the SCI of the main to this settings
                main.setSCIPort(this);
                main.createAlgo();
                brainInSCI = main.getAlgo();

                frame.dispose();
            }
        } else if (source.equals("cancelBut")) {
            //cancel button press, just close the frame
            // JOptionPane.showMessageDialog(null, "Setting is not finished");
            frame.dispose();
        }
    }

    public synchronized void sendCommand(String cmd) {
        try {
            System.out.println("Command Send: " + cmd);
            previousSendCommand = cmd;
            while (true)
            {
                if (dataReceive.equalsIgnoreCase("j")||dataReceive.equalsIgnoreCase("h")||dataReceive.equalsIgnoreCase("i"))break;
            }
            if (explore == 0) {
                if (cmd.equals("a")) {
                    mazeInSCI.setUpAutoForward(); //timer start to run
                }  else if (cmd.equals("eae")) {
                    mazeInSCI.setUpAutoBackward(); //start animation backward
                } else if (cmd.equals("c")) {
                    mazeInSCI.setUpAutoTurnRight(); //start animation turn right
                } else if (cmd.equals("d")) {
                    mazeInSCI.setUpAutoTurnLeft(); //start animation turn left
                }
            }
            outputStream.write(cmd.getBytes());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot send the message");
        }
    }

    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    public synchronized void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:

            case SerialPortEvent.OE:

            case SerialPortEvent.FE:

            case SerialPortEvent.PE:

            case SerialPortEvent.CD:

            case SerialPortEvent.CTS:

            case SerialPortEvent.DSR:

            case SerialPortEvent.RI:

            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;

            case SerialPortEvent.DATA_AVAILABLE:
                //buffer size is 10 bytes
                byte[] readBuffer = new byte[10];
                int numBytes = 0;
                byte[] convert;
                try {
                    while (inputStream.available() > 0) {
                        numBytes = inputStream.read(readBuffer);
                    }

                    //just obtain the data send ( < 200bytes)
                    convert = new byte[numBytes];
                    for (int i = 0; i < numBytes; i++) {
                        convert[i] = readBuffer[i];
                    }
                    dataReceive = new String(convert);
                    System.out.println(dataReceive);

                    if (explore == 0) {
                        //in exploration mode
                      /*  if(previousSendCommand.equalsIgnoreCase("i"))
                        {

                        }*/
                        if (dataReceive.contains("j")) {
                            //"E" means - DONE command
                            brainInSCI.setResponseFlag(0);
                            if (previousSendCommand.equals("a")) {
                                //next node is not an obstacle or boundary
                                //set the mouse location to the next node
                                mazeInSCI.setMouseLocation();
                                brainInSCI.consult();
                            } else if (previousSendCommand.equals("eae")) {
                                //reverse back from a boundary
                                mazeInSCI.setMouseLocationForBackwardCase();
                                brainInSCI.consult();
                            } else if (previousSendCommand.equals("h")) {
                                //reverse back from an obstacle
                                mazeInSCI.setMouseLocationForBackwardCase();
                                brainInSCI.consult();
                            } else if (previousSendCommand.equals("d")) {
                                brainInSCI.consult();
                            } else if (previousSendCommand.equals("c")) {
                                brainInSCI.consult();
                            }
                        } else if (dataReceive.contains("h")) {
                            brainInSCI.setResponseFlag(1);
                            mazeInSCI.setMouseLocation();
                            System.out.println("Haiz!!!!!!!");
                            //next node is an obstacle,
                            //don't have to set the mouse location
                            brainInSCI.consult();

                        } else if (dataReceive.contains("i")) {
                            brainInSCI.setResponseFlag(2);
                            mazeInSCI.setMouseLocation();
                            brainInSCI.consult();
                        }
                    } else if (explore == 1) {
                        //manual mode
                        if (dataReceive.contains("i")) {

                            mazeInSCI.manualBoundary();

                            System.out.print("Manual Boundary");
                        } else if (dataReceive.contains("h")) {
                         System.out.println("OMG!!!!!!");
                            mazeInSCI.manualOb();
                        }

                        main.getButtonPanel().setDirectionEnable();
                    } else if (explore == 2 || explore == 3) {
                        //fastest + shortest mode

                        move.control();
                    } else if (explore == 4){
                        //Pause
                        if (dataReceive.contains("j")) {
                            //"E" means - DONE command
                            if (previousSendCommand.equals("a")) {
                                //next node is not an obstacle or boundary
                                //set the mouse location to the next node
                                mazeInSCI.setMouseLocation();

                            } else if (previousSendCommand.equals("eae")) {
                                //reverse back from a boundary
                                mazeInSCI.setMouseLocationForBackwardCase();

                            } else if (previousSendCommand.equals("h")) {
                                //reverse back from an obstacle
                                mazeInSCI.setMouseLocationForBackwardCase();

                            } else if (previousSendCommand.equals("d")) {

                            } else if (previousSendCommand.equals("c")) {

                            }
                        } else if (dataReceive.contains("h")) {
                            //brainInSCI.setResponseFlag(1);
                            mazeInSCI.setMouseLocation();
                            //mazeInSCI.setMouseLocationForBackwardCase();
                            System.out.println("Haiz!!!!!!!");
                            //next node is an obstacle,
                            //don't have to set the mouse location


                        } else if (dataReceive.contains("i")) {
                            //brainInSCI.setResponseFlag(2);
                            mazeInSCI.setMouseLocation();
                            //mazeInSCI.setMouseLocationForBackwardCase();

                        }
                    }
                } catch (IOException e) {
                }
                break;
        }
    }
}

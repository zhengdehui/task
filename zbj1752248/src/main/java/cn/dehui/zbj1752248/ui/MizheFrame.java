package cn.dehui.zbj1752248.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import cn.dehui.zbj1752248.EmailChecker;
import cn.dehui.zbj1752248.MizheEmailChecker;

public class MizheFrame extends JFrame {

    private static final long serialVersionUID = 4635519170682763241L;

    private JPanel            contentPane;

    private File              inputFile;

    private File              outputFolder;

    private JCheckBox         chckbxMmmm;

    private JButton           runButton;

    private JButton           openFileButton;

    private JButton           selectFolderButton;

    private JTextField        sleepTextField;

    private String            initFilePath;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    MizheFrame frame = new MizheFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     * @throws IOException
     * @throws NumberFormatException
     */
    public MizheFrame() throws NumberFormatException, IOException {

        initFilePath = System.getProperty("user.home") + "/" + MizheEmailChecker.class.getSimpleName() + ".ini";
        File initFile = new File(initFilePath);
        if (initFile.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(initFile));
            MizheEmailChecker.sleepTime = Integer.parseInt(br.readLine());
            br.close();
        }

        setTitle("MMMM Email Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(300, 100, 500, 400);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        menuBar.add(toolBar);

        sleepTextField = new JTextField(MizheEmailChecker.sleepTime + "");
        sleepTextField.setHorizontalAlignment(SwingConstants.LEFT);
        toolBar.add(sleepTextField);
        sleepTextField.setColumns(10);
        sleepTextField.setMaximumSize(new Dimension(60, 30));

        JButton saveAlipaySleepBtn = new JButton("保存");
        saveAlipaySleepBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedWriter bw = null;
                try {
                    MizheEmailChecker.sleepTime = Integer.parseInt(sleepTextField.getText());
                    File initFile = new File(initFilePath);
                    bw = new BufferedWriter(new FileWriter(initFile));
                    bw.write(sleepTextField.getText());
                    bw.flush();
                } catch (IOException ex) {

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MizheFrame.this, "数字有错");

                } finally {
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

            }
        });
        toolBar.add(saveAlipaySleepBtn);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        chckbxMmmm = new JCheckBox("mmmm");

        final JLabel egouTimeLabel = new JLabel("");

        final JLabel fanliTimeLabel = new JLabel("");

        final JLabel fanhuanTimeLabel = new JLabel("");

        final JLabel bigouTimeLabel = new JLabel("");

        final JLabel alipayTimeLabel = new JLabel("");

        final JLabel statusLabel = new JLabel("未开始", JLabel.CENTER);

        final JProgressBar fanhuanProgressBar = new JProgressBar();
        fanhuanProgressBar.setStringPainted(true);

        runButton = new JButton("运行");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (inputFile == null || outputFolder == null) {
                    return;
                }

                try {
                    String outputFolderPath = outputFolder.getAbsolutePath();
                    //                    File outputFolder = new File(outputFolderPath);
                    //                    if (!outputFolder.exists() || !outputFolder.isDirectory()) {
                    //                        JOptionPane.showMessageDialog(MainFrame.this, "输出目录不存在！\r\n" + outputFolderPath, "Error",
                    //                                JOptionPane.ERROR_MESSAGE);
                    //                        return;
                    //                    }
                    //----------------------------
                    egouTimeLabel.setText("");
                    fanhuanTimeLabel.setText("");
                    fanliTimeLabel.setText("");
                    bigouTimeLabel.setText("");
                    alipayTimeLabel.setText("");

                    //--------------------------------
                    List<String> emailList = new ArrayList<String>();

                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            emailList.add(line);
                        }
                    }
                    br.close();
                    //--------------------------------

                    CountDownLatch startSignal = new CountDownLatch(1);
                    int doneCount = 0;
                    List<EmailChecker> checkerList = new ArrayList<EmailChecker>();
                    if (chckbxMmmm.isSelected()) {
                        checkerList.add(new MizheEmailChecker(emailList, outputFolderPath, startSignal, null,
                                fanhuanTimeLabel, fanhuanProgressBar));
                        doneCount++;
                    }

                    final CountDownLatch doneSignal = new CountDownLatch(doneCount);
                    for (EmailChecker checker : checkerList) {
                        checker.setDoneSignal(doneSignal);
                        checker.start();
                    }

                    startSignal.countDown();

                    setEnabledAll(false);
                    statusLabel.setIcon(new ImageIcon(ClassLoader.getSystemResource("loading.gif")));
                    statusLabel.setText("运行中...");

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                doneSignal.await();
                                statusLabel.setIcon(null);
                                statusLabel.setText("已完成");
                                setEnabledAll(true);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MizheFrame.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        final JLabel inputFilePathLabel = new JLabel("");
        openFileButton = new JButton("打开Email文件");
        openFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle("打开Email文件");

                if (fc.showOpenDialog(MizheFrame.this) == JFileChooser.APPROVE_OPTION) {
                    inputFile = fc.getSelectedFile();
                    inputFilePathLabel.setText(inputFile.getAbsolutePath());
                }
            }
        });

        final JLabel outputFolderPathLabel = new JLabel("");
        selectFolderButton = new JButton("选择输出目录");
        selectFolderButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle("选择输出目录");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);

                if (fc.showOpenDialog(MizheFrame.this) == JFileChooser.APPROVE_OPTION) {
                    outputFolder = fc.getSelectedFile();
                    outputFolderPathLabel.setText(outputFolder.getAbsolutePath());
                }
            }
        });
        contentPane.setLayout(new MigLayout("", "[][80px][grow]",
                "[30px][30px][30px][30px][30px][30px][30px][30px][30px][grow]"));
        contentPane.add(chckbxMmmm, "cell 0 4,grow");

        contentPane.add(fanhuanProgressBar, "cell 2 4,grow");
        contentPane.add(openFileButton, "cell 0 0,grow");
        contentPane.add(selectFolderButton, "cell 0 1,grow");

        contentPane.add(runButton, "cell 0 9,grow");
        contentPane.add(statusLabel, "cell 1 9 2 1,grow");
        contentPane.add(bigouTimeLabel, "cell 1 5,grow");
        contentPane.add(fanhuanTimeLabel, "cell 1 4,grow");
        contentPane.add(fanliTimeLabel, "cell 1 3,grow");
        contentPane.add(egouTimeLabel, "cell 1 2,grow");
        contentPane.add(alipayTimeLabel, "cell 1 6,grow");
        contentPane.add(outputFolderPathLabel, "cell 1 1 2 1,grow");
        contentPane.add(inputFilePathLabel, "cell 1 0 2 1,grow");
    }

    private void setEnabledAll(boolean enabled) {
        this.openFileButton.setEnabled(enabled);
        this.selectFolderButton.setEnabled(enabled);
        this.runButton.setEnabled(enabled);
        this.chckbxMmmm.setEnabled(enabled);
    }
}

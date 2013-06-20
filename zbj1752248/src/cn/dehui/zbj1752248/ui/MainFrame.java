package cn.dehui.zbj1752248.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import cn.dehui.zbj1752248.EgouEmailChecker;
import cn.dehui.zbj1752248.EmailChecker;
import cn.dehui.zbj1752248.FanhuanEmailChecker;
import cn.dehui.zbj1752248.FanliEmailChecker;
import cn.dehui.zbj1752248.FiveOneBiEmailChecker;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 4635519170682763241L;

    private JPanel            contentPane;

    private File              inputFile;

    private File              outputFolder;

    private JCheckBox         egouCheckBox;

    private JCheckBox         fanliCheckBox;

    private JCheckBox         fanhuanCheckBox;

    private JCheckBox         bigouCheckBox;

    private JButton           runButton;

    private JButton           openFileButton;

    private JButton           selectFolderButton;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public MainFrame() {

        setTitle("Email Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 500, 400);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        egouCheckBox = new JCheckBox("易购");

        fanliCheckBox = new JCheckBox("51返利");

        fanhuanCheckBox = new JCheckBox("返还网");

        bigouCheckBox = new JCheckBox("51比购");

        final JLabel egouTimeLabel = new JLabel("");

        final JLabel fanliTimeLabel = new JLabel("");

        final JLabel fanhuanTimeLabel = new JLabel("");

        final JLabel bigouTimeLabel = new JLabel("");

        final JLabel statusLabel = new JLabel("未开始");

        runButton = new JButton("运行");
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (inputFile == null) {
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
                    if (egouCheckBox.isSelected()) {
                        checkerList.add(new EgouEmailChecker(emailList, outputFolderPath, startSignal, null,
                                egouTimeLabel));
                        doneCount++;
                    }
                    if (fanhuanCheckBox.isSelected()) {
                        checkerList.add(new FanhuanEmailChecker(emailList, outputFolderPath, startSignal, null,
                                fanhuanTimeLabel));
                        doneCount++;
                    }
                    if (fanliCheckBox.isSelected()) {
                        checkerList.add(new FanliEmailChecker(emailList, outputFolderPath, startSignal, null,
                                fanliTimeLabel));
                        doneCount++;
                    }
                    if (bigouCheckBox.isSelected()) {
                        checkerList.add(new FiveOneBiEmailChecker(emailList, outputFolderPath, startSignal, null,
                                bigouTimeLabel));
                        doneCount++;
                    }

                    final CountDownLatch doneSignal = new CountDownLatch(doneCount);
                    for (EmailChecker checker : checkerList) {
                        checker.setDoneSignal(doneSignal);
                        checker.start();
                    }

                    startSignal.countDown();

                    setEnabledAll(false);
                    statusLabel.setIcon(new ImageIcon(MainFrame.class.getResource("/cn/dehui/zbj1752248/loading.gif")));
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
                    JOptionPane.showMessageDialog(MainFrame.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        final JLabel inputFilePathLabel = new JLabel("");
        openFileButton = new JButton("打开Email文件");
        openFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle("打开Email文件");

                if (fc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                    inputFile = fc.getSelectedFile();
                    inputFilePathLabel.setText(inputFile.getAbsolutePath());
                }
            }
        });

        final JLabel outputFolderPathLabel = new JLabel("");
        selectFolderButton = new JButton("选择输出目录");
        selectFolderButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle("选择输出目录");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);

                if (fc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                    outputFolder = fc.getSelectedFile();
                    outputFolderPathLabel.setText(outputFolder.getAbsolutePath());
                }
            }
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(
                gl_contentPane
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                gl_contentPane
                                        .createParallelGroup(Alignment.TRAILING)
                                        .addGroup(
                                                gl_contentPane
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(egouCheckBox,
                                                                                GroupLayout.DEFAULT_SIZE, 126,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(fanliCheckBox,
                                                                                Alignment.TRAILING,
                                                                                GroupLayout.DEFAULT_SIZE, 126,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(fanhuanCheckBox,
                                                                                GroupLayout.DEFAULT_SIZE, 126,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(bigouCheckBox,
                                                                                GroupLayout.DEFAULT_SIZE, 126,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(openFileButton,
                                                                                GroupLayout.DEFAULT_SIZE, 126,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(selectFolderButton,
                                                                                GroupLayout.DEFAULT_SIZE, 126,
                                                                                Short.MAX_VALUE))
                                                        .addPreferredGap(ComponentPlacement.RELATED))
                                        .addGroup(
                                                gl_contentPane
                                                        .createSequentialGroup()
                                                        .addComponent(runButton, GroupLayout.PREFERRED_SIZE, 88,
                                                                GroupLayout.PREFERRED_SIZE).addGap(35)))
                        .addGroup(
                                gl_contentPane
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(statusLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 324,
                                                Short.MAX_VALUE)
                                        .addComponent(bigouTimeLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                                324, Short.MAX_VALUE)
                                        .addComponent(fanhuanTimeLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                                324, Short.MAX_VALUE)
                                        .addComponent(fanliTimeLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                                324, Short.MAX_VALUE)
                                        .addComponent(egouTimeLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 324,
                                                Short.MAX_VALUE)
                                        .addGroup(
                                                gl_contentPane
                                                        .createSequentialGroup()
                                                        .addGap(6)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(outputFolderPathLabel,
                                                                                Alignment.TRAILING,
                                                                                GroupLayout.DEFAULT_SIZE, 318,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(inputFilePathLabel,
                                                                                GroupLayout.DEFAULT_SIZE, 318,
                                                                                Short.MAX_VALUE)))).addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
                gl_contentPane
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                gl_contentPane
                                        .createParallelGroup(Alignment.TRAILING)
                                        .addComponent(inputFilePathLabel, GroupLayout.PREFERRED_SIZE, 34,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(openFileButton, GroupLayout.PREFERRED_SIZE, 34,
                                                GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                gl_contentPane
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(selectFolderButton, GroupLayout.PREFERRED_SIZE, 34,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addGroup(
                                                gl_contentPane
                                                        .createSequentialGroup()
                                                        .addComponent(outputFolderPathLabel,
                                                                GroupLayout.PREFERRED_SIZE, 34,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addGap(21)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createParallelGroup(Alignment.TRAILING)
                                                                        .addComponent(egouTimeLabel,
                                                                                GroupLayout.PREFERRED_SIZE, 22,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(egouCheckBox))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createParallelGroup(Alignment.TRAILING)
                                                                        .addComponent(fanliCheckBox)
                                                                        .addComponent(fanliTimeLabel,
                                                                                GroupLayout.PREFERRED_SIZE, 22,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(ComponentPlacement.UNRELATED)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createParallelGroup(Alignment.TRAILING)
                                                                        .addComponent(fanhuanCheckBox)
                                                                        .addComponent(fanhuanTimeLabel,
                                                                                GroupLayout.PREFERRED_SIZE, 22,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(bigouCheckBox)
                                                                        .addComponent(bigouTimeLabel,
                                                                                GroupLayout.PREFERRED_SIZE, 22,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addGap(19)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createParallelGroup(Alignment.LEADING, false)
                                                                        .addComponent(statusLabel,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(runButton,
                                                                                GroupLayout.DEFAULT_SIZE, 85,
                                                                                Short.MAX_VALUE)))).addContainerGap()));
        contentPane.setLayout(gl_contentPane);
    }

    private void setEnabledAll(boolean enabled) {
        this.openFileButton.setEnabled(enabled);
        this.selectFolderButton.setEnabled(enabled);
        this.runButton.setEnabled(enabled);
        this.egouCheckBox.setEnabled(enabled);
        this.fanliCheckBox.setEnabled(enabled);
        this.fanhuanCheckBox.setEnabled(enabled);
        this.bigouCheckBox.setEnabled(enabled);
    }
}

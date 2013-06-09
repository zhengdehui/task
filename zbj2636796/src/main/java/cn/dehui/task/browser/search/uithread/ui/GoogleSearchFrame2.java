package cn.dehui.task.browser.search.uithread.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import cn.dehui.task.browser.search.uithread.controller.ControllerManager;

public class GoogleSearchFrame2 extends JFrame {

    public static final String STATISTIC                     = "统计";

    public static final String GET_URL_WITH_QUOTE            = "获取URL(带引号)";

    public static final String GET_URL                       = "获取URL";

    public static final String SITE_COUNT                    = "site统计";

    private static final int   DEFAULT_URL_COUNT_PER_KEYWORD = 10;

    private static final long  serialVersionUID              = 6144722027050898517L;

    private JPanel             contentPane;

    protected JTextField       maxUrlResultCountTextField;

    protected JTextField       maxUrlCountTextField;

    protected JTextField       waitTimeTextField;

    private JTextField         outputPathTextField;

    private JTextField         inputFileTextField;

    protected JButton          startBtn;

    private JTextField         encodingTextField;

    protected JButton          initUrlBtn;

    protected JButton          fetchUrlBtn;

    private JButton            siteCountBtn;

    private ControllerManager  cm;

    /**
     * Create the frame.
     */
    public GoogleSearchFrame2() {
        setTitle(getBrowserTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                cm.exit();
            }
        });

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);
        panel.setLayout(new MigLayout("", "[][grow][][grow]", "[30px][30px][30px][30px][grow]"));

        JButton inputFileButton = new JButton("关键词文件");
        inputFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle("打开关键词文件");

                if (fc.showOpenDialog(GoogleSearchFrame2.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        readFromText(fc.getSelectedFile());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(GoogleSearchFrame2.this, "文件读取出错！", "警告", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }

        });
        panel.add(inputFileButton, "cell 0 0,grow");

        inputFileTextField = new JTextField();
        inputFileTextField.setEditable(false);
        panel.add(inputFileTextField, "cell 1 0,grow");
        inputFileTextField.setColumns(10);

        JButton outputPathButton = new JButton("输出路径");
        outputPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle("选择输出目录");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);

                if (fc.showOpenDialog(GoogleSearchFrame2.this) == JFileChooser.APPROVE_OPTION) {
                    File outputFolder = fc.getSelectedFile();
                    outputPathTextField.setText(outputFolder.getAbsolutePath());

                    cm.setOutputFolder(outputFolder);
                }
            }
        });
        panel.add(outputPathButton, "cell 2 0,grow");

        outputPathTextField = new JTextField();
        outputPathTextField.setEditable(false);
        panel.add(outputPathTextField, "cell 3 0,grow");
        outputPathTextField.setColumns(10);

        JLabel lblurl = new JLabel("每URL最多统计记录数:");
        lblurl.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(lblurl, "cell 0 2,grow");

        maxUrlResultCountTextField = new JTextField();
        maxUrlResultCountTextField.setText("200");
        maxUrlResultCountTextField.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(maxUrlResultCountTextField, "cell 1 2,grow");
        maxUrlResultCountTextField.setColumns(10);

        JLabel label = new JLabel("页面最长等待时间:");
        label.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(label, "cell 2 1,grow");

        waitTimeTextField = new JTextField();
        waitTimeTextField.setText(getWaitTime());
        panel.add(waitTimeTextField, "cell 3 1,grow");
        waitTimeTextField.setColumns(10);

        JLabel lblNewLabel = new JLabel("编码:");
        lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(lblNewLabel, "cell 0 1,alignx trailing,growy");

        encodingTextField = new JTextField();
        encodingTextField.setText("GBK");
        panel.add(encodingTextField, "cell 1 1,grow");
        encodingTextField.setColumns(10);

        startBtn = new JButton(STATISTIC);
        startBtn.setEnabled(false);
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startBtn.getText().equals(STATISTIC)) {

                    if (inputFileTextField.getText().isEmpty() || outputPathTextField.getText().isEmpty()) {
                        return;
                    }

                    startBtn.setText("暂停" + STATISTIC);

                    cm.setNumberOfResultPerUrl(Integer.parseInt(maxUrlResultCountTextField.getText()));
                    cm.setWaitTime(Integer.parseInt(waitTimeTextField.getText()));
                    cm.startStatisticKeyword(DEFAULT_URL_COUNT_PER_KEYWORD, startBtn);
                } else {
                    startBtn.setText(STATISTIC);
                    cm.stopStatisticKeyword();
                }
            }
        });
        panel.add(startBtn, "cell 2 2,growy");

        siteCountBtn = new JButton(SITE_COUNT);
        siteCountBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (siteCountBtn.getText().equals(SITE_COUNT)) {

                    if (inputFileTextField.getText().isEmpty() || outputPathTextField.getText().isEmpty()) {
                        return;
                    }

                    siteCountBtn.setText("暂停" + SITE_COUNT);
                    cm.startCountResult(siteCountBtn);
                } else {
                    siteCountBtn.setText(SITE_COUNT);
                    cm.stopCountResult();
                }
            }
        });
        siteCountBtn.setEnabled(false);
        panel.add(siteCountBtn, "cell 3 2,growy");

        JLabel label1 = new JLabel("每关键词最多获取URL个数:");
        label1.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(label1, "cell 0 3,grow");

        maxUrlCountTextField = new JTextField();
        maxUrlCountTextField.setColumns(10);
        panel.add(maxUrlCountTextField, "cell 1 3,grow");

        fetchUrlBtn = new JButton(GET_URL_WITH_QUOTE);
        fetchUrlBtn.setEnabled(false);
        fetchUrlBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (fetchUrlBtn.getText().equals(GET_URL_WITH_QUOTE)) {

                    if (inputFileTextField.getText().isEmpty() || maxUrlCountTextField.getText().isEmpty()
                            || outputPathTextField.getText().isEmpty()) {
                        return;
                    }

                    fetchUrlBtn.setText("暂停" + GET_URL_WITH_QUOTE);
                    cm.startFetchUrlWithQuote(Integer.parseInt(maxUrlCountTextField.getText()), fetchUrlBtn);
                } else {
                    fetchUrlBtn.setText(GET_URL_WITH_QUOTE);
                    cm.stopFetchUrlWithQuote();
                }
            }
        });
        panel.add(fetchUrlBtn, "cell 2 3,growy");

        initUrlBtn = new JButton(GET_URL);
        initUrlBtn.setEnabled(false);
        initUrlBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (initUrlBtn.getText().equals(GET_URL)) {

                    if (inputFileTextField.getText().isEmpty() || outputPathTextField.getText().isEmpty()) {
                        return;
                    }

                    initUrlBtn.setText("暂停" + GET_URL);
                    cm.startFetchUrl(DEFAULT_URL_COUNT_PER_KEYWORD, initUrlBtn);
                } else {
                    initUrlBtn.setText(GET_URL);
                    cm.stopFetchUrl();
                }
            }
        });
        panel.add(initUrlBtn, "cell 3 3,growy");

        cm = new ControllerManager(contentPane);
    }

    protected String getWaitTime() {
        return "2000";
    }

    protected String getBrowserTitle() {
        return "Google Search";
    }

    private void readFromText(File selectedFile) throws IOException {
        inputFileTextField.setText(selectedFile.getAbsolutePath());
        List<String> keywordList = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile),
                encodingTextField.getText()));
        String line;
        while ((line = br.readLine()) != null) {
            keywordList.add(line);
        }

        br.close();

        cm.setKeywordList(keywordList);

        startBtn.setEnabled(true);
        initUrlBtn.setEnabled(true);
        fetchUrlBtn.setEnabled(true);
        siteCountBtn.setEnabled(true);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        NativeInterface.open();
        UIUtils.setPreferredLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GoogleSearchFrame2 frame = new GoogleSearchFrame2();
                frame.setSize(1280, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }
}

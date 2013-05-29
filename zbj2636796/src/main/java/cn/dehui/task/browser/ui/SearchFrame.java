package cn.dehui.task.browser.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public abstract class SearchFrame extends JFrame {

    private static final long     serialVersionUID     = 6144722027050898517L;

    protected static final String KEYWORD_URL_FILE_TPL = "关键词的前%d位网址汇总.%d.csv";

    private JPanel                contentPane;

    protected JWebBrowser         webBrowser;

    protected JTextField          maxUrlResultCountTextField;

    protected JTextField          maxUrlCountTextField;

    protected JTabbedPane         tabbedPane;

    protected JTextField          waitTimeTextField;

    private JTextField            outputPathTextField;

    private JTextField            inputFileTextField;

    protected List<String>        keywordList;

    protected BufferedWriter      bw;

    protected JButton             startBtn;

    protected File                outputFolder;

    private JTextField            encodingTextField;

    protected JButton             initUrlBtn;

    protected JButton             fetchUrlBtn;

    protected int                 keywordIndexForUrl   = 0;

    protected int                 keywordIndexForFetch = 0;

    protected JWebBrowser createBrowser() {
        JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setMenuBarVisible(false);
        return webBrowser;
    }

    /**
     * Create the frame.
     */
    public SearchFrame() {
        setTitle(getBrowserTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                webBrowser.stopLoading();
                webBrowser.disposeNativePeer();
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

                if (fc.showOpenDialog(SearchFrame.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        readFromText(fc.getSelectedFile());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(SearchFrame.this, "文件读取出错！", "警告", JOptionPane.WARNING_MESSAGE);
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

                if (fc.showOpenDialog(SearchFrame.this) == JFileChooser.APPROVE_OPTION) {
                    outputFolder = fc.getSelectedFile();
                    outputPathTextField.setText(outputFolder.getAbsolutePath());
                }
            }
        });
        panel.add(outputPathButton, "cell 2 0,grow");

        outputPathTextField = new JTextField();
        outputPathTextField.setEditable(false);
        panel.add(outputPathTextField, "cell 3 0,grow");
        outputPathTextField.setColumns(10);

        JLabel lblurl = new JLabel("每URL最多查询记录数:");
        lblurl.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(lblurl, "cell 0 2,grow");

        maxUrlResultCountTextField = new JTextField();
        maxUrlResultCountTextField.setText("200");
        maxUrlResultCountTextField.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(maxUrlResultCountTextField, "cell 1 2,grow");
        maxUrlResultCountTextField.setColumns(10);

        startBtn = new JButton("开始");
        startBtn.setEnabled(false);
        startBtn.addActionListener(getStartListener());

        JLabel label = new JLabel("页面最长等待时间:");
        label.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(label, "cell 2 1,grow");

        waitTimeTextField = new JTextField();
        waitTimeTextField.setText(getWaitTime());
        panel.add(waitTimeTextField, "cell 3 1,grow");
        waitTimeTextField.setColumns(10);

        initUrlBtn = new JButton("获取URL");
        initUrlBtn.setEnabled(false);
        initUrlBtn.addActionListener(getInitUrlListener());
        panel.add(initUrlBtn, "cell 2 2,growy");
        panel.add(startBtn, "cell 3 2,growy");

        JLabel lblNewLabel = new JLabel("编码:");
        lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(lblNewLabel, "cell 0 1,alignx trailing,growy");

        encodingTextField = new JTextField();
        encodingTextField.setText("GBK");
        panel.add(encodingTextField, "cell 1 1,grow");
        encodingTextField.setColumns(10);

        JLabel label1 = new JLabel("每关键词最多获取URL个数:");
        label1.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(label1, "cell 0 3,grow");

        maxUrlCountTextField = new JTextField();
        maxUrlCountTextField.setColumns(10);
        panel.add(maxUrlCountTextField, "cell 1 3,grow");

        fetchUrlBtn = new JButton("对关键词加引号搜索");
        fetchUrlBtn.setEnabled(false);
        fetchUrlBtn.addActionListener(getFetchUrlListener());
        panel.add(fetchUrlBtn, "cell 2 3,growy");

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        webBrowser = createBrowser();
        tabbedPane.add(getBrowserTitle(), webBrowser);
    }

    protected String getWaitTime() {
        return "5000";
    }

    protected abstract ActionListener getInitUrlListener();

    protected abstract ActionListener getFetchUrlListener();

    protected abstract ActionListener getStartListener();

    protected abstract String getBrowserTitle();

    private void readFromText(File selectedFile) throws IOException {
        inputFileTextField.setText(selectedFile.getAbsolutePath());
        keywordList = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile),
                encodingTextField.getText()));
        String line;
        while ((line = br.readLine()) != null) {
            keywordList.add(line);
        }

        if (bw != null) {
            bw.close();
            bw = new BufferedWriter(new FileWriter(new File(outputFolder, "关键词的前十位网址汇总." + System.currentTimeMillis()
                    + ".csv")));
            bw.write("Keywords,第1,第2,第3,第4,第5,第6,第7,第8,第9,第10\r\n");
        }

        keywordIndexForUrl = 0;

        keywordIndexForFetch = 0;

        startBtn.setEnabled(true);
        initUrlBtn.setEnabled(true);
        fetchUrlBtn.setEnabled(true);
    }
}

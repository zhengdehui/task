package cn.dehui.task.keywordtool.browser.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.keywordtool.browser.controller.Controller;

public abstract class KeywordToolFrameWithBrowser extends JFrame {

    private static final String ENCODING         = "GBK";

    private static final long   serialVersionUID = 6144722027050898517L;

    private JPanel              contentPane;

    private JTextField          outputPathTextField;

    private JTextField          inputFileTextField;

    protected JButton           startBtn;

    private JTextField          encodingTextField;

    private Controller          controller;

    private JWebBrowser         webBrowser;

    private JTextField          usernameField;

    private JPasswordField      passwordField;

    private String              username         = "";

    private String              password         = "";

    private boolean             debug            = false;

    /**
     * Create the frame.
     */
    public KeywordToolFrameWithBrowser() {
        readAccount();
        controller = createController();
        webBrowser = controller.getWebBrowser();

        setTitle(controller.getTitle());
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
        panel.setLayout(new MigLayout("", "[][grow][][grow]", "[30px][30px][30px][grow]"));

        JButton inputFileButton = new JButton("关键词文件");
        inputFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle("打开关键词文件");

                if (fc.showOpenDialog(KeywordToolFrameWithBrowser.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        readFromText(fc.getSelectedFile());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(KeywordToolFrameWithBrowser.this, "文件读取出错！", "警告",
                                JOptionPane.WARNING_MESSAGE);
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

                if (fc.showOpenDialog(KeywordToolFrameWithBrowser.this) == JFileChooser.APPROVE_OPTION) {
                    controller.setOutputFolder(fc.getSelectedFile());
                    outputPathTextField.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        });
        panel.add(outputPathButton, "cell 2 0,grow");

        outputPathTextField = new JTextField();
        outputPathTextField.setEditable(false);
        panel.add(outputPathTextField, "cell 3 0,grow");
        outputPathTextField.setColumns(10);

        JLabel lblNewLabel = new JLabel("编码:");
        lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        panel.add(lblNewLabel, "cell 0 1,alignx trailing,growy");

        encodingTextField = new JTextField();
        encodingTextField.setText(ENCODING);
        panel.add(encodingTextField, "cell 1 1,grow");
        encodingTextField.setColumns(10);

        startBtn = new JButton("开始");
        startBtn.setEnabled(false);
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startBtn.getText().equals("开始")) {

                    if (inputFileTextField.getText().trim().isEmpty() || outputPathTextField.getText().trim().isEmpty()) {
                        return;
                    }

                    startBtn.setText("暂停");

                    controller.setUsername(usernameField.getText().trim());
                    controller.setPassword(new String(passwordField.getPassword()));
                    controller.setDebug(debug);

                    controller.run();

                } else {
                    startBtn.setText("开始");
                    controller.stop();
                }
            }
        });

        JLabel lblNewLabel_1 = new JLabel("账号:");
        panel.add(lblNewLabel_1, "cell 2 1,alignx trailing,growy");

        usernameField = new JTextField();
        usernameField.setText(username);
        panel.add(usernameField, "cell 3 1,grow");
        usernameField.setColumns(10);
        panel.add(startBtn, "cell 0 2,growy");

        JLabel label = new JLabel("密码:");
        panel.add(label, "cell 2 2,alignx trailing,growy");

        passwordField = new JPasswordField();
        passwordField.setText(password);
        panel.add(passwordField, "cell 3 2,grow");

        contentPane.add(webBrowser, BorderLayout.CENTER);
    }

    protected abstract Controller createController();

    private void readAccount() {
        File accountFile = new File(getConfigFile());

        if (accountFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(accountFile), ENCODING));

                username = br.readLine();
                password = br.readLine();

                String level = br.readLine();
                debug = "DEBUG".equals(level);

                br.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected abstract String getConfigFile();

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

        controller.setKeywordList(keywordList);
        startBtn.setEnabled(true);
    }
}

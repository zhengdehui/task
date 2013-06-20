package cn.dehui.zbj2334776;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 8677398841253719729L;

    private JPanel            contentPane;

    private JTextField        clientIdText;

    private JTextField        clientSecretText;

    private JComboBox         lanComboBox;

    private JButton           goonBtn;

    private JButton           pauseBtn;

    private JCheckBox         autoShutdownCheckBox;

    private JLabel            label;

    private JTextArea         runStatusLabel;

    private JComboBox         encodingComboBox;

    private JButton           inputBtn;

    private JTextField        inputFolderLabel;

    private File              folder;

    private JButton           startBtn;

    private ControllerPool    pool;

    private JScrollPane       scrollPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        setTitle("Bing Translator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(400, 100, 420, 450);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout("", "[][grow][][]",
                "[30px][30px][30px][30px][30px][30px][grow][30px][30px][30px]"));

        JLabel lblNewLabel = new JLabel("Client Id: ");
        contentPane.add(lblNewLabel, "cell 0 0,alignx trailing,growy");

        clientIdText = new JTextField();
        clientIdText.setText("mstranslate1");
        contentPane.add(clientIdText, "cell 1 0 3 1,growx");
        clientIdText.setColumns(10);

        JLabel lblNewLabel_1 = new JLabel("Client Secret: ");
        contentPane.add(lblNewLabel_1, "cell 0 1,alignx trailing");

        clientSecretText = new JTextField();
        clientSecretText.setText("Mjx2OuqDvIvE5JeCCc0iTZRK23eUHgbmVQcPrzR7VN8=");
        contentPane.add(clientSecretText, "cell 1 1 3 1,growx");
        clientSecretText.setColumns(10);

        goonBtn = new JButton("继续");
        goonBtn.setEnabled(false);
        goonBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pool.startThreads();

                goonBtn.setEnabled(false);
                pauseBtn.setEnabled(true);
            }
        });

        inputBtn = new JButton("选择目录");
        inputBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                fc.setDialogTitle("选择目录");

                if (fc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                    inputFolderLabel.setText(fc.getSelectedFile().getAbsolutePath());
                    folder = fc.getSelectedFile();
                }
            }
        });
        contentPane.add(inputBtn, "cell 0 2,growx");

        inputFolderLabel = new JTextField();
        inputFolderLabel.setEditable(false);
        contentPane.add(inputFolderLabel, "cell 1 2 3 1,growx");
        inputFolderLabel.setColumns(10);

        JLabel lblNewLabel_2 = new JLabel("目标语言: ");
        contentPane.add(lblNewLabel_2, "cell 0 3,alignx trailing");

        lanComboBox = new JComboBox();
        lanComboBox.setModel(new DefaultComboBoxModel(LanguageTransformEnum.values()));
        contentPane.add(lanComboBox, "cell 1 3 3 1,growx");

        JLabel lblNewLabel_3 = new JLabel("文件编码: ");
        contentPane.add(lblNewLabel_3, "cell 0 4,alignx trailing");

        encodingComboBox = new JComboBox();
        encodingComboBox.setModel(new DefaultComboBoxModel(new String[] { "GBK", "utf-8", "iso8859-1" }));
        contentPane.add(encodingComboBox, "cell 1 4 3 1,growx");

        scrollPane = new JScrollPane();
        contentPane.add(scrollPane, "cell 0 6 4 4,grow");

        runStatusLabel = new JTextArea("未开始");
        scrollPane.setViewportView(runStatusLabel);
        runStatusLabel.setForeground(Color.GREEN);
        runStatusLabel.setBackground(Color.BLACK);
        runStatusLabel.setEditable(false);
        contentPane.add(goonBtn, "flowy,cell 2 5,alignx trailing");

        startBtn = new JButton("开始");
        startBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (clientIdText.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Client Id不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (clientSecretText.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Client Secret不能为空！", "警告",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (folder == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "选择翻译目录！", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                final long timestamp = System.currentTimeMillis();
                runStatusLabel.setText("初始化...");
                Translate.setClientId(clientIdText.getText().trim());
                Translate.setClientSecret(clientSecretText.getText().trim());

                final File[] txtFiles = folder.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isFile() && "txt".equals(FilenameUtils.getExtension(pathname.getName()));
                    }
                });

                LanguageTransformEnum transform = (LanguageTransformEnum) lanComboBox.getSelectedItem();

                pool = new ControllerPool(txtFiles, Language.CHINESE_SIMPLIFIED, transform.to, 5,
                        new Callback<String>() {
                            @Override
                            void execute(String s) {
                                runStatusLabel.setText(runStatusLabel.getText() + "\n" + s + " 翻译开始");
                            };
                        }, new Callback<String>() {
                            @Override
                            void execute(String s) {
                                runStatusLabel.setText(runStatusLabel.getText() + "\n" + s + " 翻译完毕");
                            };
                        }, new Callback<String>() {
                            @Override
                            void execute() {
                                runStatusLabel.setText(runStatusLabel.getText()
                                        + String.format("\n全部翻译完毕, 文件数:%d, 耗时:%.2f", txtFiles.length,
                                                (System.currentTimeMillis() - timestamp) / 1000.0) + "s");
                                lanComboBox.setEnabled(true);
                                startBtn.setEnabled(true);
                                goonBtn.setEnabled(false);
                                pauseBtn.setEnabled(false);
                            };
                        });
                pool.run();

                //                    controller = new TranslateController(pairList, transform.getFrom(), transform.getTo(),
                //                            new Callback() {
                //                                public void execute() {
                //                                    runStatusLabel.setText(String.format("正在翻译第%s行...", controller.getCursor() + 1));
                //                                }
                //                            }, new Callback() {
                //                                public void execute() {
                //                                    runStatusLabel.setText("翻译完毕");
                //                                    lanComboBox.setEnabled(true);
                //
                //                                    exportBtn.setEnabled(true);
                //                                    pauseBtn.setEnabled(false);
                //
                //                                    if (autoExportCheckBox.isSelected()) {
                //                                        export();
                //                                    }
                //                                    if (autoShutdownCheckBox.isSelected()) {
                //                                        shutdown();
                //                                    }
                //                                }
                //                            });

                lanComboBox.setEnabled(false);
                startBtn.setEnabled(false);
                goonBtn.setEnabled(false);
                pauseBtn.setEnabled(true);

            }
        });
        contentPane.add(startBtn, "flowy,cell 1 5,alignx trailing");

        //        label = new JLabel("翻译完后:");
        //        contentPane.add(label, "cell 0 11,alignx trailing");

        //        autoShutdownCheckBox = new JCheckBox("自动关机");
        //        contentPane.add(autoShutdownCheckBox, "cell 1 11");

        pauseBtn = new JButton("暂停");
        pauseBtn.setEnabled(false);
        pauseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pool.parseThreads();

                goonBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
            }
        });
        contentPane.add(pauseBtn, "cell 3 5");
    }

    private void shutdown() {
        try {
            Runtime.getRuntime().exec("shutdown -s");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

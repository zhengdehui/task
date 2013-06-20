package cn.dehui.zbj1984105.v201209;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import cn.dehui.zbj1984105.CategoryTreeNode;
import cn.dehui.zbj1984105.KeyValueEntry;

import com.google.api.adwords.lib.AdWordsUser;
import com.google.api.adwords.v201209.cm.KeywordMatchType;
import com.google.api.adwords.v201209.o.CompetitionSearchParameterLevel;
import com.google.api.adwords.v201209.o.DeviceType;
import com.google.api.adwords.v201209.o.DoubleComparisonOperation;
import com.google.api.adwords.v201209.o.LongComparisonOperation;

public class AdwordsFrame extends JFrame {

    private int                     fetchSize   = 10;

    private int                     threadCount = 5;

    private int                     limit       = 100;

    private Logger                  logger      = Logger.getLogger(getClass());

    private static final DateFormat dateFormat  = new SimpleDateFormat("yyyyMMddHHmm");

    private JPanel                  contentPane;

    private JTextField              keywordTextField;

    private JTextField              categoryTextField;

    private JTextField              locationTextField;

    private JButton                 selectFolderButton;

    private File                    outputFolder;

    private JLabel                  outputFolderPathLabel;

    private File                    inputFile;

    private String                  maxLinePerFile;

    private String                  keywordText;

    private String                  matchTypeText;

    private String                  categoryText;

    private String                  categoryErrorText;

    private String                  languageText;

    private String                  languageErrorText;

    private String                  locationText;

    private String                  locationErrorText;

    private String                  deviceText;

    private String                  selectOutputFolderText;

    private String                  unstartedText;

    private String                  runText;

    private String                  runningText;

    private String                  finishedText;

    private String                  allText;

    private String                  importText;

    private JTextField              adShareFrom;

    private JTextField              adShareTo;

    private JTextField              gmsFrom;

    private JTextField              gmsTo;

    private JTextField              msFrom;

    private JTextField              msTo;

    private JTextField              searchShareFrom;

    private JTextField              searchShareTo;

    private JTextField              excludeTextField;

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
                    AdwordsFrame frame = new AdwordsFrame();
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
    public AdwordsFrame() {
        loadConf();

        setTitle("Get keywords");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 500);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout("", "[100px][grow][100px][][][][][][grow]",
                "[30px][30px][30px][30px][30px][30px][30px][30px][30px][30px][30px][30px][grow][30px][30px][30px]"));

        JLabel lblNewLabel = new JLabel(keywordText);
        contentPane.add(lblNewLabel, "cell 0 0,alignx trailing,growy");

        keywordTextField = new JTextField();
        contentPane.add(keywordTextField, "cell 1 0,grow");
        keywordTextField.setColumns(10);

        final JLabel keywordFileLabel = new JLabel("");
        contentPane.add(keywordFileLabel, "cell 3 0 6 1,grow");

        JButton importKeywordButton = new JButton(importText);
        importKeywordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle(importText);

                if (fc.showOpenDialog(AdwordsFrame.this) == JFileChooser.APPROVE_OPTION) {
                    inputFile = fc.getSelectedFile();
                    keywordFileLabel.setText(inputFile.getAbsolutePath());
                }
            }
        });
        contentPane.add(importKeywordButton, "cell 2 0,grow");

        JLabel lblExclude = new JLabel("Exclude:");
        lblExclude.setHorizontalAlignment(SwingConstants.TRAILING);
        contentPane.add(lblExclude, "cell 0 1,alignx trailing,growy");

        excludeTextField = new JTextField();
        excludeTextField.setToolTipText("以半角分号分隔");
        contentPane.add(excludeTextField, "cell 1 1 8 1,grow");
        excludeTextField.setColumns(100);

        JLabel lblNewLabel_2 = new JLabel(matchTypeText);
        contentPane.add(lblNewLabel_2, "cell 2 2,alignx trailing,growy");

        final JComboBox matchTypeComboBox = new JComboBox();
        matchTypeComboBox.setModel(new DefaultComboBoxModel(new KeywordMatchType[] { KeywordMatchType.EXACT,
                KeywordMatchType.BROAD, KeywordMatchType.PHRASE }));
        contentPane.add(matchTypeComboBox, "cell 3 2 16 1,grow");

        JLabel label = new JLabel(categoryText);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        contentPane.add(label, "cell 0 2,alignx trailing,growy");

        categoryTextField = new JTextField();
        contentPane.add(categoryTextField, "cell 1 2,grow");
        categoryTextField.setColumns(10);

        JLabel lblNewLabel_3 = new JLabel(languageText);
        contentPane.add(lblNewLabel_3, "cell 2 3,alignx trailing,growy");

        // load language.conf
        DefaultComboBoxModel langModel = null;
        try {
            List<KeyValueEntry> entryList = loadKeyValueConf("language.conf.csv");
            langModel = new DefaultComboBoxModel(entryList.toArray(new KeyValueEntry[entryList.size()]));
            //            langModel.setSelectedItem(new KeyValueEntry("1000", "English"));
            langModel.setSelectedItem(null);
        } catch (IOException e2) {
            JOptionPane.showMessageDialog(this, languageErrorText, "Error", JOptionPane.ERROR_MESSAGE);
        }
        final JComboBox languageComboBox = new JComboBox();
        if (langModel != null) {
            languageComboBox.setModel(langModel);
        }
        contentPane.add(languageComboBox, "cell 3 3 16 1,grow");

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, "cell 1 3 1 10,grow");

        DefaultTreeModel categoryModel = null;
        try {
            TreeNode root = loadTreeConf("category.conf.csv");
            categoryModel = new DefaultTreeModel(root);
        } catch (Exception e2) {
            e2.printStackTrace();
            JOptionPane.showMessageDialog(this, categoryErrorText, "Error", JOptionPane.ERROR_MESSAGE);
        }

        final JTree categoryTree = new JTree();
        if (categoryModel != null) {
            categoryTree.setModel(categoryModel);
            categoryTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    if (categoryTree.getSelectionCount() > 0) {
                        CategoryTreeNode selectedNode = (CategoryTreeNode) categoryTree.getSelectionPath()
                                .getLastPathComponent();
                        categoryTextField.setText(selectedNode.id.equals("0") ? "" : selectedNode.id);
                    }
                }
            });
        }
        scrollPane.setViewportView(categoryTree);

        JLabel lblNewLabel_4 = new JLabel(locationText);
        contentPane.add(lblNewLabel_4, "cell 2 4,alignx trailing,growy");

        locationTextField = new JTextField();
        contentPane.add(locationTextField, "cell 3 4 16 1,grow");
        locationTextField.setColumns(10);

        // load location.conf
        DefaultComboBoxModel locationModel = null;
        try {
            List<KeyValueEntry> entryList = loadKeyValueConf("location.conf.csv");
            locationModel = new DefaultComboBoxModel(entryList.toArray(new KeyValueEntry[entryList.size()]));
            locationModel.setSelectedItem(null);
            //            KeyValueEntry selectedItem = new KeyValueEntry("2840", "United States");
            //            locationModel.setSelectedItem(selectedItem);
            //            locationTextField.setText(selectedItem.id);
        } catch (IOException e2) {
            JOptionPane.showMessageDialog(this, locationErrorText, "Error", JOptionPane.ERROR_MESSAGE);
        }
        final JComboBox locationComboBox = new JComboBox();
        if (locationModel != null) {
            locationComboBox.setModel(locationModel);
            locationComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String id = ((KeyValueEntry) locationComboBox.getSelectedItem()).id;
                    locationTextField.setText(id);
                }
            });
        }
        contentPane.add(locationComboBox, "cell 3 5 16 1,grow");

        JLabel lblNewLabel_5 = new JLabel(deviceText);
        contentPane.add(lblNewLabel_5, "cell 2 6,alignx trailing,growy");

        final JComboBox deviceComboBox = new JComboBox();
        deviceComboBox.setModel(new DefaultComboBoxModel(new DeviceType[] { DeviceType.DESKTOPS_AND_LAPTOPS,
                DeviceType.MOBILE_WAP, DeviceType.MOBILE_WITH_FULL_BROWSER }));
        contentPane.add(deviceComboBox, "cell 3 6 16 1,grow");

        selectFolderButton = new JButton(selectOutputFolderText);
        selectFolderButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setCurrentDirectory(new File("."));
                fc.setDialogTitle(selectOutputFolderText);
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);

                if (fc.showOpenDialog(AdwordsFrame.this) == JFileChooser.APPROVE_OPTION) {
                    outputFolder = fc.getSelectedFile();
                    outputFolderPathLabel.setText(outputFolder.getAbsolutePath());
                }
            }
        });

        JLabel competetionLabel = new JLabel("Competition:");
        competetionLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        contentPane.add(competetionLabel, "cell 2 7,grow");

        final JCheckBox highCheckBox = new JCheckBox("High");
        contentPane.add(highCheckBox, "cell 3 7 2 1,grow");

        final JCheckBox mediumCheckBox = new JCheckBox("Medium");
        contentPane.add(mediumCheckBox, "cell 5 7 2 1,grow");

        final JCheckBox lowCheckBox = new JCheckBox("Low");
        contentPane.add(lowCheckBox, "cell 7 7 2 1,alignx leading,growy");

        JLabel gmsLabel = new JLabel("Global Monthly Search:");
        gmsLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        contentPane.add(gmsLabel, "cell 2 8,grow");

        gmsFrom = new JTextField();
        contentPane.add(gmsFrom, "cell 3 8 2 1,grow");
        gmsFrom.setColumns(15);

        JLabel label_1 = new JLabel("-");
        contentPane.add(label_1, "cell 5 8,grow");

        gmsTo = new JTextField();
        contentPane.add(gmsTo, "cell 6 8 2 1,grow");
        gmsTo.setColumns(15);

        JLabel msLabel = new JLabel("Monthly Search:");
        msLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        contentPane.add(msLabel, "cell 2 9,alignx trailing,growy");

        msFrom = new JTextField();
        contentPane.add(msFrom, "cell 3 9 2 1,grow");
        msFrom.setColumns(15);

        JLabel label_5 = new JLabel("-");
        contentPane.add(label_5, "cell 5 9,grow");

        msTo = new JTextField();
        contentPane.add(msTo, "cell 6 9 2 1,grow");
        msTo.setColumns(10);

        JLabel AdShareLabel = new JLabel("Ad Share:");
        AdShareLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        contentPane.add(AdShareLabel, "cell 2 10,grow");

        adShareFrom = new JTextField();
        contentPane.add(adShareFrom, "cell 3 10,grow");
        adShareFrom.setColumns(13);

        JLabel label_2 = new JLabel("%");
        contentPane.add(label_2, "cell 4 10,grow");

        JLabel label_3 = new JLabel("-");
        contentPane.add(label_3, "cell 5 10,grow");

        adShareTo = new JTextField();
        contentPane.add(adShareTo, "cell 6 10,grow");
        adShareTo.setColumns(13);

        JLabel label_4 = new JLabel("%");
        contentPane.add(label_4, "cell 7 10,grow");

        JLabel SearchShareLabel = new JLabel("Search Share:");
        SearchShareLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        contentPane.add(SearchShareLabel, "cell 2 11,alignx trailing,growy");

        searchShareFrom = new JTextField();
        contentPane.add(searchShareFrom, "cell 3 11,grow");
        searchShareFrom.setColumns(10);

        JLabel label_6 = new JLabel("%");
        contentPane.add(label_6, "cell 4 11,grow");

        JLabel label_7 = new JLabel("-");
        contentPane.add(label_7, "cell 5 11,grow");

        searchShareTo = new JTextField();
        contentPane.add(searchShareTo, "cell 6 11,grow");
        searchShareTo.setColumns(10);

        JLabel label_8 = new JLabel("%");
        contentPane.add(label_8, "cell 7 11,grow");
        contentPane.add(selectFolderButton, "cell 0 14,grow");

        outputFolderPathLabel = new JLabel();
        contentPane.add(outputFolderPathLabel, "cell 1 14 8 1,grow");

        final JLabel statusLabel = new JLabel(unstartedText, JLabel.CENTER);
        contentPane.add(statusLabel, "cell 1 15 8 1,grow");

        JButton runButton = new JButton(runText);
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (keywordTextField.getText().isEmpty() && inputFile == null) {
                    return;
                }

                statusLabel.setIcon(new ImageIcon(AdwordsFrame.class.getResource("/cn/dehui/zbj1984105/loading.gif")));
                statusLabel.setText(runningText);

                String fileName = "keywords.%s.0.csv".replaceFirst("%s", dateFormat.format(new Date()));
                KeywordFetcherV201209.initLogger(outputFolder == null ? fileName : outputFolder.getAbsolutePath() + "/"
                        + fileName, maxLinePerFile);
                logger.info("关键字,竞争程度,全球每月搜索量,本地每月搜索量");

                KeywordFetcherV201209.limit = limit;

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            List<String> keywordList = getKeywords();
                            Parameter parameter = buildParameter(matchTypeComboBox, languageComboBox, categoryTree,
                                    locationComboBox, deviceComboBox, highCheckBox, mediumCheckBox, lowCheckBox,
                                    gmsFrom, gmsTo, msFrom, msTo, adShareFrom, adShareTo, searchShareFrom,
                                    searchShareTo);

                            for (String keyword : keywordList) {

                                // get parameters
                                parameter.keyword = keyword;

                                CountDownLatch startSignal = new CountDownLatch(1);
                                CountDownLatch doneSignal = new CountDownLatch(threadCount);

                                int index = 0;
                                for (int i = 0; i < threadCount; i++) {
                                    try {
                                        KeywordFetcherV201209 fetcher = new KeywordFetcherV201209(parameter, index + i
                                                * fetchSize, fetchSize, threadCount, startSignal, doneSignal);
                                        new Thread(fetcher).start();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                        i--;
                                        continue;
                                    }
                                }

                                startSignal.countDown(); // let all threads proceed

                                doneSignal.await();
                                statusLabel.setText("keyword: " + keyword);
                            }
                            statusLabel.setIcon(null);
                            statusLabel.setText(finishedText);
                            //                            setEnabledAll(true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    private Parameter buildParameter(final JComboBox matchTypeComboBox,
                            final JComboBox languageComboBox, final JTree categoryTree,
                            final JComboBox locationComboBox, final JComboBox deviceComboBox, JCheckBox highCheckBox,
                            JCheckBox mediumCheckBox, JCheckBox lowCheckBox, JTextField gmsFrom, JTextField gmsTo,
                            JTextField msFrom, JTextField msTo, JTextField adShareFrom, JTextField adShareTo,
                            JTextField searchShareFrom, JTextField searchShareTo) {

                        Parameter parameter = new Parameter();

                        if (!excludeTextField.getText().trim().isEmpty()) {
                            String[] excludeWords = excludeTextField.getText().trim().split(";");
                            parameter.excludeKeywords.addAll(Arrays.asList(excludeWords));
                        }

                        parameter.keywordMatchType = (KeywordMatchType) matchTypeComboBox.getSelectedItem();

                        if (languageComboBox.getSelectedItem() != null) {
                            parameter.languageId = Long.parseLong(((KeyValueEntry) languageComboBox.getSelectedItem()).id);
                        }

                        if (!locationTextField.getText().isEmpty()) {
                            parameter.locationId = Long.parseLong(locationTextField.getText().trim());
                        }

                        if (!categoryTextField.getText().isEmpty()) {
                            parameter.categoryId = Integer.parseInt(categoryTextField.getText().trim());
                        }

                        parameter.deviceType = (DeviceType) deviceComboBox.getSelectedItem();

                        if (highCheckBox.isSelected()) {
                            parameter.competitionLevelList.add(CompetitionSearchParameterLevel.HIGH);
                        }
                        if (mediumCheckBox.isSelected()) {
                            parameter.competitionLevelList.add(CompetitionSearchParameterLevel.MEDIUM);
                        }
                        if (lowCheckBox.isSelected()) {
                            parameter.competitionLevelList.add(CompetitionSearchParameterLevel.LOW);
                        }

                        if (!gmsFrom.getText().isEmpty() || !gmsTo.getText().isEmpty()) {
                            LongComparisonOperation longComparisonOperation = new LongComparisonOperation();
                            longComparisonOperation.setMinimum(gmsFrom.getText().isEmpty() ? 0 : Long.parseLong(gmsFrom
                                    .getText().trim()));
                            longComparisonOperation.setMaximum(gmsTo.getText().isEmpty() ? Long.MAX_VALUE : Long
                                    .parseLong(gmsTo.getText().trim()));
                            parameter.globalMonthlySearchData = longComparisonOperation;
                        }

                        if (!msFrom.getText().isEmpty() || !msTo.getText().isEmpty()) {
                            LongComparisonOperation longComparisonOperation = new LongComparisonOperation();
                            longComparisonOperation.setMinimum(msFrom.getText().isEmpty() ? 0 : Long.parseLong(msFrom
                                    .getText().trim()));
                            longComparisonOperation.setMaximum(msTo.getText().isEmpty() ? Long.MAX_VALUE : Long
                                    .parseLong(msTo.getText().trim()));

                            parameter.monthlySearchData = longComparisonOperation;
                        }

                        if (!adShareFrom.getText().isEmpty() || !adShareTo.getText().isEmpty()) {
                            DoubleComparisonOperation doubleComparisonOperation = new DoubleComparisonOperation();
                            doubleComparisonOperation.setMinimum(adShareFrom.getText().isEmpty() ? 0 : (Double
                                    .parseDouble(adShareFrom.getText().trim()) / 100));
                            doubleComparisonOperation.setMaximum(adShareTo.getText().isEmpty() ? 1 : (Double
                                    .parseDouble(adShareTo.getText().trim()) / 100));
                            parameter.adShareData = doubleComparisonOperation;
                        }

                        if (!searchShareFrom.getText().isEmpty() || !searchShareTo.getText().isEmpty()) {
                            DoubleComparisonOperation doubleComparisonOperation = new DoubleComparisonOperation();
                            doubleComparisonOperation.setMinimum(searchShareFrom.getText().isEmpty() ? 0 : (Double
                                    .parseDouble(searchShareFrom.getText().trim()) / 100));
                            doubleComparisonOperation.setMaximum(searchShareTo.getText().isEmpty() ? 1 : (Double
                                    .parseDouble(searchShareTo.getText().trim()) / 100));
                            parameter.searchShareData = doubleComparisonOperation;
                        }

                        return parameter;
                    }
                }.start();
            }

        });
        contentPane.add(runButton, "cell 0 15,grow");
    }

    private List<String> getKeywords() throws IOException {
        if (!keywordTextField.getText().trim().isEmpty()) {
            return Arrays.asList(keywordTextField.getText().trim());
        }

        List<String> keywordList = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = br.readLine()) != null) {
            keywordList.add(line);
        }

        br.close();

        return keywordList;
    }

    private void loadConf() {
        try {
            Map map = AdWordsUser.getMap("adwords.properties");

            fetchSize = Integer.parseInt(map.get("fetch.size").toString());
            threadCount = Integer.parseInt(map.get("thread.count").toString());
            limit = Integer.parseInt(map.get("limit.per.keyword").toString());
            maxLinePerFile = map.get("max.line.per.file").toString();

            String locale = System.getProperty("locale");
            if (locale == null) {
                locale = "en";
            }
            keywordText = map.get("text.keyword." + locale).toString();
            importText = map.get("text.import." + locale).toString();
            matchTypeText = map.get("text.match.type." + locale).toString();
            categoryText = map.get("text.category." + locale).toString();
            categoryErrorText = map.get("text.category.error." + locale).toString();
            languageText = map.get("text.language." + locale).toString();
            languageErrorText = map.get("text.language.error." + locale).toString();
            locationText = map.get("text.location." + locale).toString();
            locationErrorText = map.get("text.location.error." + locale).toString();
            deviceText = map.get("text.device." + locale).toString();
            selectOutputFolderText = map.get("text.select.output.folder." + locale).toString();
            unstartedText = map.get("text.unstarted." + locale).toString();
            runText = map.get("text.run." + locale).toString();
            runningText = map.get("text.running." + locale).toString();
            finishedText = map.get("text.finished." + locale).toString();
            allText = map.get("text.all." + locale).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TreeNode loadTreeConf(String confFileName) throws Exception {
        CategoryTreeNode root = new CategoryTreeNode("0", "", allText);

        Map<String, CategoryTreeNode> categoryMap = new HashMap<String, CategoryTreeNode>();
        categoryMap.put(root.id, root);

        BufferedReader br = new BufferedReader(new FileReader(confFileName));
        String line;
        while ((line = br.readLine()) != null) {
            String[] tmp = line.split(",");
            String id = tmp[tmp.length - 2];
            String parentId = tmp[tmp.length - 1];
            String title = tmp[tmp.length - 3];
            if (tmp.length > 3) {
                tmp[0] = tmp[0].substring(1);
                tmp[tmp.length - 3] = tmp[tmp.length - 3].substring(0, tmp[tmp.length - 3].length() - 1);

                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < tmp.length - 2; i++) {
                    sb.append(tmp[i]).append(",");
                }
                title = sb.substring(0, sb.length() - 1);
            }

            CategoryTreeNode node = new CategoryTreeNode(id, parentId, title.substring(1, title.length() - 1));

            CategoryTreeNode parentNode = categoryMap.get(node.parentId);
            if (parentNode == null) {
                throw new Exception("找不到parent node, parentId: " + node.parentId + ", nodeId: " + node.id);
            }

            parentNode.add(node);

            categoryMap.put(node.id, node);
        }

        br.close();

        return root;
    }

    private List<KeyValueEntry> loadKeyValueConf(String confFileName) throws IOException {
        List<KeyValueEntry> list = new ArrayList<KeyValueEntry>();
        BufferedReader br = new BufferedReader(new FileReader(confFileName));
        String line;
        while ((line = br.readLine()) != null) {
            String[] tmp = line.split(",");
            list.add(new KeyValueEntry(tmp[1], tmp[0]));
        }

        br.close();

        return list;
    }
}

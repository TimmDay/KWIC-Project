
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author
 */
public class ProjectGUI {

    private final JFrame frame;
    private JTextField filename, keyword;
    private JTextArea leftResults, rightUpResults, rightDownResults;
    private JComboBox<String> keywordList, inputText, posTags, tokens, lemmas;
    private JComboBox<Integer> numPrev, numAfter;
    private DataModelEd project;
    private final JPanel levels;
    private JRadioButton activate, deactivate;
    private JButton tagged_SentButton, tokensButton, posButton, sentencesButton;
    private DefaultComboBoxModel<String> tagsModel, tokensModel, lemmasModel, keywordmodel;
    private String savedtext = null;
    private JList<String> histList;

    public ProjectGUI() {
        frame = new JFrame("Java Project");
        frame.setSize(1000, 500);
        frame.setMinimumSize(new Dimension(500, 400));
        frame.setMaximumSize((new Dimension(1000, 900)));

        //main layout of the top level window
        frame.getContentPane().setLayout(new BorderLayout());

        levels = new JPanel();
        levels.setLayout(new BoxLayout(levels, BoxLayout.Y_AXIS));

        //setup Norh
        setupLeve1();
        setupLevel2();

        /*JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel panel1 = new JPanel();
        
        panel1.setBackground(Color.red);
        JPanel panel2 = new JPanel();
        panel2.setBackground(Color.GREEN);
        tabbedPane.addTab("Tab 1", panel1);
        tabbedPane.addTab("Tab 2", panel2);
        // frame.getContentPane().add(tabbedPane);*/
        setuplevel3();
        //setupLevel4();
        frame.getContentPane().add(BorderLayout.NORTH, levels);
        //set up Center
        setupLevel5();

        //set up East
        setUpEast();

        frame.addWindowListener(new MyWindowListener());

        frame.setVisible(true);
    }

    private void setupLeve1() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem m;

        m = new JMenuItem("load");
        m.addActionListener(new LDButtonHandler());
        fileMenu.add(Box.createVerticalStrut(10));
        fileMenu.add(m);

        fileMenu.add(Box.createVerticalStrut(10));
        fileMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
        fileMenu.add(Box.createVerticalStrut(10));

        m = new JMenuItem("Save");
        m.addActionListener(new SAVEButtonHandler());
        fileMenu.add(m);
        fileMenu.add(Box.createVerticalStrut(10));

        m = new JMenuItem("Save as...");
        m.addActionListener(new SAVE_AsButtonHandler());
        fileMenu.add(m);

        fileMenu.add(Box.createVerticalStrut(10));
        fileMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
        fileMenu.add(Box.createVerticalStrut(10));

        m = new JMenuItem("Clear");
        m.addActionListener(new CLEARButtonHandler());
        fileMenu.add(Box.createVerticalStrut(10));
        fileMenu.add(m);
        fileMenu.add(Box.createVerticalStrut(10));

        m = new JMenuItem("Exit");
        m.addActionListener(x -> System.exit(0));
        fileMenu.add(m);
        fileMenu.add(Box.createVerticalStrut(10));

        /*        memoMenu.add(saveMenu);
        
        JMenu getMenu = new JMenu("Get");
        
        m = new JMenuItem("Get Memo 1");
        m.addActionListener(this);
        getMenu.add(m);
        
        m = new JMenuItem("Get Memo 2");
        m.addActionListener(this);
        getMenu.add(m);
        
        memoMenu.add(getMenu);
        
        m = new JMenuItem("Clear");
        m.addActionListener(this);
        memoMenu.add(m);
        
        m = new JMenuItem("Exit");
        m.addActionListener(this);
        memoMenu.add(m);*/
        JMenu historyMenu = new JMenu("History");
        historyMenu.add(Box.createVerticalStrut(10));

        JMenu view = new JMenu("view history");
        DefaultListModel<String> aListModel = new DefaultListModel<>();
        histList = new JList<>(aListModel);
        JScrollPane listPane = new JScrollPane(histList);
        listPane.setPreferredSize(new Dimension(120, 100));
        listPane.setMaximumSize(new Dimension(120, 100));
        view.add(listPane);
        ///m.addActionListener(...);
        historyMenu.add(view);
        historyMenu.add(Box.createVerticalStrut(10));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(Box.createVerticalStrut(10));

        m = new JMenuItem("Info");
        helpMenu.add(m);
        helpMenu.add(Box.createVerticalStrut(10));

        JMenuBar mBar = new JMenuBar();
        mBar.add(fileMenu);
        mBar.add(Box.createHorizontalStrut(10));
        mBar.add(historyMenu);
        mBar.add(Box.createHorizontalStrut(10));
        mBar.add(helpMenu);

        frame.setJMenuBar(mBar);
    }

    private void setupLevel2() {
        // set up a JLabel for the Headline
        JLabel headLineLabel = new JLabel("KWIC - Program");
        headLineLabel.setForeground(Color.BLUE);
        headLineLabel.setFont(new Font(Font.SANS_SERIF, Font.CENTER_BASELINE, 20));

        // nested BoxLayouts (vertical and horizontal) with glue are nice to center components
        JPanel level2 = new JPanel();
        level2.setLayout(new BoxLayout(level2, BoxLayout.Y_AXIS));
        JPanel level1CenterPanel = new JPanel();
        level1CenterPanel.setLayout(new BoxLayout(level1CenterPanel, BoxLayout.X_AXIS));

        level1CenterPanel.add(Box.createHorizontalGlue());
        level1CenterPanel.add(headLineLabel);
        level1CenterPanel.add(Box.createHorizontalGlue());

        level2.add(Box.createVerticalStrut(3));
        level2.add(level1CenterPanel);
        level2.add(Box.createVerticalStrut(3));
        level2.setBackground(Color.red);

        levels.add(level2);
        levels.add(new JSeparator(SwingConstants.HORIZONTAL));//!!!!
    }

    private void setuplevel3() {
        //left side
        JLabel leftLabel = new JLabel("Enter a file name or URL");
        leftLabel.setMinimumSize(new Dimension(70, 20));
        //leftLabel.setPreferredSize(new Dimension(120, 20));
        leftLabel.setMaximumSize(new Dimension(120, 20));

        // DefaultComboBoxModel text = new DefaultComboBoxModel(new String[] {"1","2", "3", "4", "5", "6", "7"});
        inputText = new JComboBox(new String[]{"Wikipedia", "File", "Typed text"}); //instance variable!!
        inputText.setSelectedIndex(0);//default inpute text
        inputText.setToolTipText("Choose the input text");
        inputText.setMinimumSize(new Dimension(70, 20));
        inputText.setPreferredSize(new Dimension(90, 20));
        inputText.setMaximumSize(new Dimension(100, 20));
        inputText.setEnabled(true);
        inputText.setEditable(false);
        inputText.addActionListener(new InputTextHandler());

        JPanel labelPanel = new JPanel();
        labelPanel.add(leftLabel);
        labelPanel.add(Box.createRigidArea(new Dimension(43, 0)));
        labelPanel.add(inputText);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));

        filename = new JTextField("file.txt or wikipedia URL", 15); //instance variable!!
        filename.setToolTipText("You can only enter file with \".txt\" extension, wikipedia URL for english documents"
                + " or you can type a text");
        filename.setMinimumSize(new Dimension(70, 20));
        filename.setPreferredSize(new Dimension(90, 20));
        filename.setMaximumSize(new Dimension(100, 20));
        //filename.addActionListener(.....);

        JButton load = new JButton("load");
        load.setToolTipText("load the input file or input URL");
        load.setMinimumSize(new Dimension(70, 20));
        load.setPreferredSize(new Dimension(90, 20));
        load.setMaximumSize(new Dimension(100, 20));
        load.addActionListener(new LDButtonHandler());

        JPanel textButtonPanel = new JPanel();
        textButtonPanel.add(filename);
        textButtonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textButtonPanel.add(load);
        textButtonPanel.add(Box.createHorizontalGlue());
        textButtonPanel.setLayout(new BoxLayout(textButtonPanel, BoxLayout.X_AXIS));

        //and add the left label and textButtonPanel into another component
        JPanel leftPanel = new JPanel();

        leftPanel.add(labelPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(textButtonPanel);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));//end left

        //center
        JLabel centerLabel = new JLabel("Enter a keaword");
        centerLabel.setMinimumSize(new Dimension(100, 20));
        centerLabel.setPreferredSize(new Dimension(179, 20));
        centerLabel.setMaximumSize(new Dimension(179, 20));
        keywordmodel = new DefaultComboBoxModel<>();
        keywordmodel.addElement("Token");
        keywordmodel.addElement("Lemma");
        keywordmodel.addElement("POS");
        keywordList = new JComboBox(keywordmodel);
        keywordList.setSelectedIndex(0);
        keywordList.setToolTipText("Choose the keyword category");
        keywordList.setMinimumSize(new Dimension(70, 20));
        keywordList.setPreferredSize(new Dimension(90, 20));
        keywordList.setMaximumSize(new Dimension(100, 20));
        keywordList.setEnabled(false);
        // keywordList.addActionListener();

        JPanel center1 = new JPanel();
        center1.add(centerLabel);
        center1.add(Box.createRigidArea(new Dimension(10, 0)));
        center1.add(keywordList);
        center1.add(Box.createHorizontalGlue());
        center1.setLayout(new BoxLayout(center1, BoxLayout.X_AXIS));

        keyword = new JTextField("Enter a Keyword", 15); //instance variable!!
        keyword.setToolTipText("Enter the keyword and press search");
        keyword.setMinimumSize(new Dimension(70, 20));
        keyword.setPreferredSize(new Dimension(90, 20));
        keyword.setMaximumSize(new Dimension(100, 20));
        //keyword.addActionListener(.....);

        JButton search = new JButton("Search");
        search.setBackground(Color.GREEN);
        search.setForeground(Color.BLACK);
        search.setToolTipText("Search for the given keyword");
        search.setMinimumSize(new Dimension(70, 20));
        search.setPreferredSize(new Dimension(90, 20));
        search.setMaximumSize(new Dimension(100, 20));
        search.setForeground(Color.BLUE);
        search.addActionListener(new SearchButtonHandler());

        JPanel textButtonPanel2 = new JPanel();
        textButtonPanel2.add(keyword);
        textButtonPanel2.add(Box.createRigidArea(new Dimension(20, 0)));
        textButtonPanel2.add(search);
        textButtonPanel2.add(Box.createHorizontalGlue());
        textButtonPanel2.setLayout(new BoxLayout(textButtonPanel2, BoxLayout.X_AXIS));

        //and add the center and textButtonPanel2 into another component
        JPanel centerPanel = new JPanel();

        centerPanel.add(center1);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(textButtonPanel2);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));//end center

        //rightPanel
        //label....
        JLabel rightLabel = new JLabel("Number of words preceding / following the keyword");
        rightLabel.setMinimumSize(new Dimension(70, 20));
        //rightLabel.setPreferredSize(new Dimension(90, 20));
        // rightLabel.setMaximumSize(new Dimension(100, 20));

        JPanel rightlabelPanel = new JPanel();
        rightlabelPanel.add(rightLabel);
        rightlabelPanel.add(Box.createHorizontalGlue());
        rightlabelPanel.setLayout(new BoxLayout(rightlabelPanel, BoxLayout.X_AXIS));

        //two ComboBoxs
        //DefaultComboBoxModel prev = new DefaultComboBoxModel(new String[] {"1","2", "3", "4", "5", "6", "7"});
        numPrev = new JComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}); //instance variable!!
        numPrev.setSelectedIndex(2);//default number
        numPrev.setToolTipText("Enter only digits");
        numPrev.setMinimumSize(new Dimension(50, 20));
        numPrev.setPreferredSize(new Dimension(50, 20));
        numPrev.setMaximumSize(new Dimension(50, 20));
        numPrev.setEnabled(false);
        numPrev.setEditable(false);
        //numPrev.addActionListener(new RadioButtonsListener());

        // DefaultComboBoxModel after = new DefaultComboBoxModel(new String[] {"1","2", "3", "4", "5", "6", "7"});
        numAfter = new JComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}); //instance variable!!
        numAfter.setSelectedIndex(2);//default number
        numAfter.setToolTipText("Enter only digits");
        numAfter.setMinimumSize(new Dimension(50, 20));
        numAfter.setPreferredSize(new Dimension(50, 20));
        numAfter.setMaximumSize(new Dimension(50, 20));
        numAfter.setEnabled(false);
        numAfter.setEditable(false);
        //  numAfter.addActionListener());

        JPanel comboPanel = new JPanel();
        comboPanel.add(new JLabel("Preceding words:"));
        comboPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        comboPanel.add(numPrev);
        comboPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        comboPanel.add(new JLabel("Following word:"));
        comboPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        comboPanel.add(numAfter);
        comboPanel.add(Box.createHorizontalGlue());
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));

        //and add the left label and textButtonPanel into another component
        JPanel rightPanel = new JPanel();

        rightPanel.add(rightlabelPanel);
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(comboPanel);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));//end

        //last right panel
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        //Border border = BorderFactory.createTitledBorder("Specify numbers");
        // radioPanel.setBorder(border);
        ButtonGroup group = new ButtonGroup();
        activate = new JRadioButton("Activate");
        activate.setEnabled(false);
        activate.addActionListener(new RadioButtonsListener());

        deactivate = new JRadioButton("Deactivate");
        deactivate.setEnabled(false);
        deactivate.addActionListener(new RadioButtonsListener());
        radioPanel.add(activate);
        group.add(activate);

        radioPanel.add(deactivate);
        group.add(deactivate);//end

        //mainPanel
        JPanel main = new JPanel();
        main.add(Box.createRigidArea(new Dimension(5, 0)));
        main.add(leftPanel);
        main.add(new JSeparator(SwingConstants.VERTICAL));
        main.add(centerPanel);
        main.add(new JSeparator(SwingConstants.VERTICAL));
        main.add(rightPanel);
        main.add(Box.createHorizontalStrut(10));
        main.add(radioPanel);

        main.add(Box.createHorizontalGlue());
        main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));

        //the last
        JPanel allPanel = new JPanel();
        allPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        allPanel.add(main);
        allPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));

        levels.add(allPanel);
        levels.add(new JSeparator(SwingConstants.HORIZONTAL));//!!!!
    }

    private void setupLevel5() {

        leftResults = new JTextArea("Search results", 10, 40);
        leftResults.setEditable(true); //the user can copy-paste his/her text and load it...

        //add this area into JScrollPane
        JScrollPane leftListPane = new JScrollPane(leftResults);

        leftResults.setEditable(true);

        //create the topPanel TextArea
        rightUpResults = new JTextArea("Search results", 10, 20);
        rightUpResults.setEditable(false);
        //add this area into JScrollPane
        JScrollPane rightUpListPane = new JScrollPane(rightUpResults);

        rightDownResults = new JTextArea("Search results", 10, 20);
        rightDownResults.setEditable(false);
        //add this area into JScrollPane
        JScrollPane rightDownListPane = new JScrollPane(rightDownResults);
        //add the right areas in the right area panel
        JPanel rightArea = new JPanel();
        rightArea.setLayout(new BoxLayout(rightArea, BoxLayout.Y_AXIS));
        rightArea.add(rightUpListPane);
        rightArea.add(Box.createVerticalStrut(5));
        rightArea.add(rightDownListPane);

        //add all areas in the main panel
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(leftListPane);
        panel.add(Box.createRigidArea(new Dimension(5, 5)));
        panel.add(rightArea);

        //add to JFrame
        frame.getContentPane().add(BorderLayout.CENTER, panel);

    }

    /**
     * set up west of top level window
     */
    private void setUpEast() {
        //1st Panel

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        Border border1 = BorderFactory.createTitledBorder("Documents stats");
        labelPanel.setBorder(border1);
        labelPanel.setPreferredSize(new Dimension(170, 130));
        labelPanel.setMaximumSize(new Dimension(170, 130));

        tokensButton = new JButton("Tokens");
        tokensButton.setToolTipText("Search for the given keyword");
        tokensButton.setMinimumSize(new Dimension(90, 20));
        tokensButton.setPreferredSize(new Dimension(140, 20));
        tokensButton.setMaximumSize(new Dimension(140, 20));
        tokensButton.setForeground(Color.BLUE);
        tokensButton.setEnabled(false);
        tokensButton.addActionListener(new TokenButtonHandler());

        posButton = new JButton("POS_Tags");
        posButton.setToolTipText("Search for the given keyword");
        posButton.setMinimumSize(new Dimension(90, 20));
        posButton.setPreferredSize(new Dimension(140, 20));
        posButton.setMaximumSize(new Dimension(140, 20));
        posButton.setForeground(Color.BLUE);
        posButton.setEnabled(false);
        posButton.addActionListener(new POSTAGSButtonHandler());

        sentencesButton = new JButton("Sentences");
        sentencesButton.setToolTipText("Search for the given keyword");
        sentencesButton.setMinimumSize(new Dimension(90, 20));
        sentencesButton.setPreferredSize(new Dimension(140, 20));
        sentencesButton.setMaximumSize(new Dimension(140, 20));
        sentencesButton.setForeground(Color.BLUE);
        sentencesButton.setEnabled(false);
        //Sentences.addActionListener(......);

        tagged_SentButton = new JButton("Sentence / Tags");
        tagged_SentButton.setToolTipText("Search for the given keyword");
        tagged_SentButton.setMinimumSize(new Dimension(90, 20));
        tagged_SentButton.setPreferredSize(new Dimension(140, 20));
        tagged_SentButton.setMaximumSize(new Dimension(140, 20));
        tagged_SentButton.setForeground(Color.BLUE);
        tagged_SentButton.setEnabled(false);
        tagged_SentButton.addActionListener(new SENTandTAGSButtonHandler());

        //add the button into this panel
        labelPanel.add(Box.createVerticalStrut(10));
        labelPanel.add(Box.createHorizontalStrut(15));
        labelPanel.add(tokensButton);
        labelPanel.add(Box.createVerticalStrut(10));
        labelPanel.add(posButton);
        // labelPanel.add(Box.createVerticalStrut(10));
        // labelPanel.add(sentencesButton);
        labelPanel.add(Box.createVerticalStrut(10));
        labelPanel.add(tagged_SentButton);
        labelPanel.add(Box.createVerticalStrut(15));

        JPanel labelPanalX = new JPanel();
        // labelPanalX.add(Box.createHorizontalStrut(10));
        labelPanalX.add(labelPanel);
        labelPanalX.setLayout(new BoxLayout(labelPanalX, BoxLayout.X_AXIS));

        //second 
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
        Border border = BorderFactory.createTitledBorder("Stats cluster");
        comboPanel.setBorder(border);

        comboPanel.setPreferredSize(new Dimension(170, 150));
        comboPanel.setMaximumSize(new Dimension(170, 150));

        JLabel tagslabel = new JLabel("Pos Tags:");

        tagsModel = new DefaultComboBoxModel<>();
        posTags = new JComboBox(tagsModel);
        posTags.setToolTipText("Enter only digits");
        posTags.setMinimumSize(new Dimension(90, 20));
        posTags.setPreferredSize(new Dimension(100, 20));
        posTags.setMaximumSize(new Dimension(100, 20));
        posTags.setEnabled(false);
        posTags.setEditable(false);
        posTags.addActionListener(new POSTagsComboBoxHandler());

        JPanel tagslabelPanalX = new JPanel();
        tagslabelPanalX.add(tagslabel);
        tagslabelPanalX.add(Box.createHorizontalStrut(5));
        tagslabelPanalX.add(posTags);
        tagslabelPanalX.setLayout(new BoxLayout(tagslabelPanalX, BoxLayout.X_AXIS));

        JLabel tokenslabel = new JLabel("Tokens:");

        tokensModel = new DefaultComboBoxModel<>();
        tokens = new JComboBox(tokensModel);
        tokens.setToolTipText("Enter only digits");
        tokens.setMinimumSize(new Dimension(90, 20));
        tokens.setPreferredSize(new Dimension(100, 20));
        tokens.setMaximumSize(new Dimension(100, 20));
        tokens.setEnabled(false);
        tokens.setEditable(false);
        tokens.addActionListener(new TokensComboBoxHandler());

        JPanel tokenslabelPanalX = new JPanel();
        tokenslabelPanalX.add(tokenslabel);
        tokenslabelPanalX.add(Box.createHorizontalStrut(17));
        tokenslabelPanalX.add(tokens);
        tokenslabelPanalX.setLayout(new BoxLayout(tokenslabelPanalX, BoxLayout.X_AXIS));

        JLabel lemmaslabel = new JLabel("Lemmas:");

        lemmasModel = new DefaultComboBoxModel<>();
        lemmas = new JComboBox(lemmasModel);

        lemmas.setToolTipText("Enter only digits");
        lemmas.setMinimumSize(new Dimension(90, 20));
        lemmas.setPreferredSize(new Dimension(100, 20));
        lemmas.setMaximumSize(new Dimension(100, 20));
        lemmas.setEnabled(false);
        lemmas.setEditable(false);
        lemmas.addActionListener(new LemmasComboBoxHandler());

        JPanel lemmaslabelPanalX = new JPanel();
        lemmaslabelPanalX.add(lemmaslabel);
        lemmaslabelPanalX.add(Box.createHorizontalStrut(9));
        lemmaslabelPanalX.add(lemmas);
        lemmaslabelPanalX.setLayout(new BoxLayout(lemmaslabelPanalX, BoxLayout.X_AXIS));

        comboPanel.add(Box.createVerticalStrut(10));
        comboPanel.add(tagslabelPanalX);
        comboPanel.add(Box.createVerticalStrut(10));
        comboPanel.add(tokenslabelPanalX);
        comboPanel.add(Box.createVerticalStrut(10));
        comboPanel.add(lemmaslabelPanalX);
        comboPanel.add(Box.createVerticalStrut(10));//end comboPanel

        //Top panel that holds labelPanelX and comboPanel
        JPanel topPanel = new JPanel();
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topPanel.add(labelPanalX);
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topPanel.add(comboPanel);
        topPanel.add(Box.createVerticalGlue());
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));//end topPanel

        //add the topPanel to top lavel frame in EAST position 
        frame.getContentPane().add(BorderLayout.EAST, topPanel);
    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class LDButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (filename == null) //it means the cancel button!!!
            {
                JOptionPane.showMessageDialog(frame, "No input to download");

            } else {
                try {
                    String command = (String) inputText.getSelectedItem();

                    if (command.equals("Typed text")) {

                        project = new DataModelEd(leftResults.getText(), command);
                    } else {
                        project = new DataModelEd(filename.getText(), command);
                    }
                    enableFunctions();//enable all functions we need....

                    //show documents stats
                    rightDownResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
                    //leftResults.setFont(new Font("Monaco", Font.PLAIN, 12));
                    rightDownResults.setMargin(new Insets(10, 10, 10, 10));
                    rightDownResults.setCaretPosition(5);
                    rightDownResults.setText(project.documentWideStats());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "FileNotFoundException");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid url. Try again!");
                }
            }

        }
    }

    private void enableFunctions() {
        //when the load is succesful:
        // 1) default is the deactivate action of the radio button
        deactivate.setEnabled(true);
        deactivate.setSelected(true);

        activate.setEnabled(true);

        // 2) enable the keywordList area 
        keywordList.setEnabled(true);

        // 3)  fill the posTags combo box
        tagsModel.removeAllElements();//remove all elements from previous search
        for (String pos : project.getTagCluster().keySet()) {
            tagsModel.addElement(pos);
        }
        posTags.setEnabled(true);

        // 4) fill tokens combo box
        tokensModel.removeAllElements();
        for (String tok : project.getTokenTags().keySet()) {
            tokensModel.addElement(tok);
        }
        tokens.setEnabled(true);

        // 5) fill lemmas combo boox
        lemmasModel.removeAllElements();
        /*for (String lem : project.getLemmaSentences().keySet()) {
        lemmasModel.addElement(lem);
        }*/
        for (String lem : project.getSentencesWithLemma()) {
            lemmasModel.addElement(lem);
        }
        lemmas.setEnabled(true);

        // 6) enable the buttons of Ducument stats group
        tokensButton.setEnabled(true);
        posButton.setEnabled(true);
        sentencesButton.setEnabled(true);
        tagged_SentButton.setEnabled(true);

        // 7) initially tetx for keyword field
        keyword.setText("Enter a keyword");

    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class SearchButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            leftResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            //leftResults.setFont(new Font("Monaco", Font.PLAIN, 12));
            leftResults.setMargin(new Insets(30, 40, 30, 40));
            leftResults.setCaretPosition(5);

            rightUpResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            //leftResults.setFont(new Font("Monaco", Font.PLAIN, 12));
            rightUpResults.setMargin(new Insets(10, 10, 10, 10));
            rightUpResults.setCaretPosition(5);

            String command = (String) keywordList.getSelectedItem();

            DefaultListModel<String> aListModel = (DefaultListModel<String>) histList.getModel();
            aListModel.addElement(keyword.getText());

            if (activate.isSelected()) {
                Integer numPrevious = (Integer) numPrev.getSelectedItem();
                Integer numAfter = (Integer) numPrev.getSelectedItem();
                if (keyword.getText() == null) {
                    JOptionPane.showMessageDialog(frame, "there is no such a word in this Document");
                } else if (command.equals("Token")) {

                    String key = keyword.getText().toLowerCase();
                    if (!project.getTokenTags().keySet().contains(keyword.getText().trim())) {
                        JOptionPane.showMessageDialog(frame, "there is no such a token in this Document");
                    } else {
                        leftResults.setText(project.findTOKENAndItsNeighbours(key, numPrevious, numAfter));
                        rightUpResults.setText(project.statisticsOfToken(key));
                        try {
                            highlightKeyword3(leftResults, key);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (command.equals("Lemma")) {

                    String lem = keyword.getText().trim().toLowerCase();// project.getLemma(keyword.getText().trim());
                    if (!project.getLemmaSentences().keySet().contains(lem)) {
                        JOptionPane.showMessageDialog(frame, "there is no such a lemma in this Document");
                    } else {
                        ArrayList<String> list = project.getTokensHavingThisLemma(lem);
                        try {
                            leftResults.setText(project.findLEMMAndItsNeighbours(lem, numPrevious, numAfter));
                            highlightKeywordWithLIST(leftResults, list);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } else if (command.equals("POS")) {
                    if (!project.getTagCluster().keySet().contains(keyword.getText().trim())) {
                        JOptionPane.showMessageDialog(frame, "there is no such a POS TAG in this Document");
                    } else {
                        ArrayList<String> list = project.getTokensHavingThisPOSTag(keyword.getText());

                        try {
                            leftResults.setText(project.findPOSTagAndItsNeighbours(keyword.getText(), numPrevious, numAfter));
                            highlightKeywordWithLIST(leftResults, list);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }

            } else {

                if (keyword.getText() == null) {
                    JOptionPane.showMessageDialog(frame, "there is no such a word in this Document");
                } else if (command.equals("Token")) {
                    String key = keyword.getText().toLowerCase();
                    if (!project.getTokenTags().keySet().contains(keyword.getText().trim())) {
                        JOptionPane.showMessageDialog(frame, "there is no such a token in this Document");
                    } else {

                        leftResults.setText(project.getSentWithThisToken(key)); /////
                        rightUpResults.setText(project.statisticsOfToken(key));
                        try {
                            highlightKeyword3(leftResults, key);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (command.equals("Lemma")) {
                    String lem = keyword.getText().trim().toLowerCase();// project.getLemma(keyword.getText().trim());
                    if (!project.getSentencesWithLemma().contains(lem)) {
                        JOptionPane.showMessageDialog(frame, "there is no such a lemma in this Document");
                    } else {
                        leftResults.setText(project.getSentWithThisLemma_2(lem));
                        ArrayList<String> list = project.getTokensHavingThisLemma(lem);

                        try {
                            highlightKeywordWithLIST(leftResults, list);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } else if (command.equals("POS")) {
                    if (!project.getTagCluster().keySet().contains(keyword.getText().trim())) {
                        JOptionPane.showMessageDialog(frame, "there is no such a POS TAG in this Document");
                    } else {

                      //  leftResults.setText(project.getSentWithThisPOS_2(keyword.getText()));
                      //  ArrayList<String> list = project.getTokensHavingThisPOSTag(keyword.getText());

                        try {
                            highlightKeywordForPOSSSSSSS(leftResults,keyword.getText() );
                           // highlightKeywordWithLIST(leftResults, list);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }
        }
    }

    private void highlightKeyword3(JTextArea area, String keyword) throws BadLocationException {
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);

        Document doc = area.getDocument();
        String p = " " + keyword + " ";
        String text = doc.getText(0, doc.getLength()).toLowerCase();

        String[] textarray = area.getText().split(" ");
        for (int i = 0; i < textarray.length; i++) {
            int pos = 0;
            // Search for pattern
            while ((pos = text.indexOf(p, pos)) >= 0) {

                highlighter.addHighlight(pos + 1, pos + p.length() - 1, painter);
                pos += p.length();

            }
        }

    }

    private void highlightKeywordWithLIST(JTextArea area, ArrayList<String> list) throws BadLocationException {
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);

        Document doc = area.getDocument();

        String text = doc.getText(0, doc.getLength()).toLowerCase();

        String[] textarray = area.getText().split(" ");
        for (String pattern : list) {
            pattern = pattern.toLowerCase();
            String p = " " + pattern + " ";
            for (int i = 0; i < textarray.length; i++) {
                int pos = 0;
                // Search for pattern
                while ((pos = text.indexOf(p, pos)) >= 0) {

                    highlighter.addHighlight(pos + 1, pos + p.length() - 1, painter);
                    pos += p.length();

                }
            }
        }

    }
    private void highlightKeywordForPOSSSSSSS(JTextArea area, String pos) throws BadLocationException {
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);

        Document doc = area.getDocument();

        String text = doc.getText(0, doc.getLength()).toLowerCase();
        
        area.setText("");
        int num = 1;
        for(int j = 0; j < project.getSentenceWithPOS().keySet().size(); j++){
             ArrayList<String> sublist = new ArrayList<>();
            for(int i = 0; i< project.getSentenceWithPOS().get(j).length; i++){
                if(pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])){
                    area.append(num + ". ");
                    for(int k = 0; k<= i; k++){
                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }
                     highlighter.addHighlight(area.getText().length()-project.getSentenceWithTokens().get(j)[i].length()-1, area.getText().length() - 1, painter);
                      for(int k = i +1; k< project.getSentenceWithPOS().get(j).length; k++){
                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }
                      area.append("\n");
                       num++; 
               }
                 //ArrayList<String> sublist = new ArrayList<>();
                if(pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])){
                  
                    area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i] 
                            + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] + ")\n");

                }
               
               //area.append("\n");
            }
            
          // info.add(sublist);
        }

        /*String[] textarray = area.getText().split(" ");
        for (String pattern : list) {
        pattern = pattern.toLowerCase();
        String p = " " + pattern + " ";
        for (int i = 0; i < textarray.length; i++) {
        int pos = 0;
        // Search for pattern
        while ((pos = text.indexOf(p, pos)) >= 0) {
        
        highlighter.addHighlight(pos + 1, pos + p.length() - 1, painter);
        pos += p.length();
        
        }
        }
        }*/

    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class InputTextHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (inputText.getSelectedItem().equals("Typed text")) {
                leftResults.setLineWrap(true);
                leftResults.setEditable(true);
            } else if (inputText.getSelectedItem().equals("Wikipedia") || inputText.getSelectedItem().equals("File")) {
                leftResults.setEditable(false);
            }
        }
    }

    /*
         * ActionListener for the radio buttons
     */
    private class RadioButtonsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("Activate")) {
                numPrev.setEnabled(true);
                numAfter.setEnabled(true);
            } else if (e.getActionCommand().equals("Deactivate")) {
                numPrev.setEnabled(false);
                numAfter.setEnabled(false);;
            }

        }
    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class TokensComboBoxHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //or String command = String.valueOf(tokens.getSelectedItem());
            String command = (String) (tokens.getSelectedItem());
            keyword.setText(command);
            keywordList.setSelectedItem("Token");

        }
    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class LemmasComboBoxHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //or String command = String.valueOf(tokens.getSelectedItem());
            String command = (String) (lemmas.getSelectedItem());
            keyword.setText(command);
            keywordList.setSelectedItem("Lemma");

        }
    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class POSTagsComboBoxHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //or String command = String.valueOf(tokens.getSelectedItem());
            String command = (String) (posTags.getSelectedItem());
            keyword.setText(command);
            keywordList.setSelectedItem("POS");

        }
    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class SAVEButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            //prompt the user to enter a name for the file
            if (savedtext == null) {
                savedtext = JOptionPane.showInputDialog(frame, "Enter a file name which the elements are stored in");
                if (new File(savedtext).exists()) { //if file already exists
                    //ask the user if he wants to overwrite it
                    if (JOptionPane.showConfirmDialog(frame, "A file with this name exists. Do you want to replace the old file?")
                            == JOptionPane.YES_OPTION) {
                        storeInFile(savedtext);

                    } else {
                        savedtext = JOptionPane.showInputDialog(frame, "choose another name for the file");
                        if (savedtext != null) {
                            storeInFile(savedtext);
                        }
                    }
                }
            } else {
                storeInFile(savedtext);
            }

        }

    }

    /**
     * private class SAVE_ASButtonHandler for handling the event fired by load
     * buttton
     */
    private class SAVE_AsButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //prompt the user to enter a name for the file
            String fileName = JOptionPane.showInputDialog(frame, "Enter a file name which the elements are stored in");

            if (fileName == null) { //if user clicks on cancel !!!
                JOptionPane.showMessageDialog(frame, "you canceled the loading");
            } else if (new File(fileName).exists()) { //if file already exists
                //ask the user if he wants to overwrite it
                if (JOptionPane.showConfirmDialog(frame, "A file with this name exists. Do you want to replace the old file?")
                        == JOptionPane.YES_OPTION) {
                    storeInFile(fileName);

                } else {
                    fileName = JOptionPane.showInputDialog(frame, "choose another name for the file");
                    if (fileName != null) {
                        storeInFile(fileName);
                    }
                }
            } else {
                storeInFile(fileName);
            }

        }

    }

    /**
     * private method to print in the file
     *
     * @param aName
     */
    private void storeInFile(String aName) {
        try (PrintWriter output = new PrintWriter(new File(aName))) {
            String newline = System.getProperty("line.separator");

            output.println("THE RESULTS FOR YOUR SEARCH ARE:" + newline);

            //for the text in leftResults:
            String linesLeft[] = leftResults.getText().split("\\r?\\n");

            for (String line : linesLeft) {
                if (!line.isEmpty()) {
                    output.println(line + newline);
                }

            }
            output.println("THE STATISTICS RESULTS:" + newline);

            // output.println(rightUpResults.getText() + newline);
            //for the right down text
            String linesRightDown[] = rightDownResults.getText().split("\\r?\\n");
            for (String line : linesRightDown) {
                if (!line.isEmpty()) {
                    output.println(line + newline);
                }

            }

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage() + "EXXXX");
        }
    }

    /**
     * private class CLEARButtonHandler for handling the event fired by load
     * buttton
     */
    private class CLEARButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //CLEAR ALL RESULTS

            leftResults.setText("");
            rightUpResults.setText("");
            rightDownResults.setText("");
            keyword.setText("");
            filename.setText("");

        }

    }

    /**
     * private class TOKENSButtonHandler for handling the event fired by load
     * buttton
     */
    private class TokenButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            leftResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            leftResults.setMargin(new Insets(10, 10, 10, 10));
            leftResults.setCaretPosition(2);
            leftResults.setText(project.showInformationForAlltokens());

        }

    }

    /**
     * private class POSTAGSButtonHandler for handling the event fired by load
     * buttton
     */
    private class POSTAGSButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            leftResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            leftResults.setMargin(new Insets(10, 10, 10, 10));
            leftResults.setCaretPosition(2);
            leftResults.setText(project.showInformationForAllPOStags());

        }

    }

    /**
     * private class POSTAGSButtonHandler for handling the event fired by load
     * buttton
     */
    private class SENTandTAGSButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            leftResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            leftResults.setMargin(new Insets(10, 10, 10, 10));
            leftResults.setCaretPosition(2);
            leftResults.setText(project.printTokensAndTagsPerSentece());

        }

    }

    /**
     * Our window listener terminates the program when the close window button
     * is clicked.
     */
    private class MyWindowListener extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            e.getWindow().dispose();
        }
    }

    public static void main(String[] args) {
        ProjectGUI pr = new ProjectGUI();
    }

}

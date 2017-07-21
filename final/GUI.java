
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;


/**
 *
 * @author Savvas and Stella
 */
public class GUI {

    private final JFrame frame;
    private JTextField filename, keyword;
    private JTextArea leftResults, rightUpResults, rightDownResults;
    private JComboBox<String> keywordList, inputText, posTags, tokens, lemmas;
    private JComboBox<Integer> numPrev, numAfter;
    private DataStoreAndSearch project;
    private final JPanel levels;
    private JRadioButton activate, deactivate;
    private JButton tagged_SentButton, tokensButton, posButton;
    private DefaultComboBoxModel<String> tagsModel, tokensModel, lemmasModel, keywordmodel;
    private String savedtext = null;
    private JList<String> histList;
    private int histNumber = 1;

    public GUI() {
        frame = new JFrame("Java Project");
        frame.setSize(1000, 500);
        frame.setMinimumSize(new Dimension(700, 400));
        frame.setMaximumSize((new Dimension(1500, 900)));

        //main layout of the top level window
        frame.getContentPane().setLayout(new BorderLayout());

        levels = new JPanel();
        levels.setLayout(new BoxLayout(levels, BoxLayout.Y_AXIS));

        //setup Norh
        setupLeve1();
        setupLevel2();

        setuplevel3();
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



        m = new JMenuItem("Exit");
        m.addActionListener(x -> System.exit(0));
        fileMenu.add(m);
        fileMenu.add(Box.createVerticalStrut(10));

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
        m.addActionListener(new InfoButtonHandler());
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
        load.setToolTipText("Load the input file or input URL");
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
        JLabel centerLabel = new JLabel("Enter a keyword");
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

        keyword = new JTextField("Search term here", 15); //instance variable!!
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
        numPrev.setToolTipText("Choose the number of words precceding the keyword");
        numPrev.setMinimumSize(new Dimension(60, 20));
        numPrev.setPreferredSize(new Dimension(60, 20));
        numPrev.setMaximumSize(new Dimension(60, 20));
        numPrev.setEnabled(false);
        numPrev.setEditable(false);
        //numPrev.addActionListener(new RadioButtonsListener());

        // DefaultComboBoxModel after = new DefaultComboBoxModel(new String[] {"1","2", "3", "4", "5", "6", "7"});
        numAfter = new JComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}); //instance variable!!
        numAfter.setSelectedIndex(2);//default number
        numAfter.setToolTipText("Choose the number of words following the keyword");
        numAfter.setMinimumSize(new Dimension(60, 20));
        numAfter.setPreferredSize(new Dimension(60, 20));
        numAfter.setMaximumSize(new Dimension(60, 20));
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

        leftResults.setEditable(false);

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
        tokensButton.setToolTipText("Shows all information for Tokens");
        tokensButton.setMinimumSize(new Dimension(90, 20));
        tokensButton.setPreferredSize(new Dimension(140, 20));
        tokensButton.setMaximumSize(new Dimension(140, 20));
        tokensButton.setForeground(Color.BLUE);
        tokensButton.setEnabled(false);
        tokensButton.addActionListener(new TokenButtonHandler());

        posButton = new JButton("POS_Tags");
        posButton.setToolTipText("Shows all information for POS ");
        posButton.setMinimumSize(new Dimension(90, 20));
        posButton.setPreferredSize(new Dimension(140, 20));
        posButton.setMaximumSize(new Dimension(140, 20));
        posButton.setForeground(Color.BLUE);
        posButton.setEnabled(false);
        posButton.addActionListener(new POSTAGSButtonHandler());


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
        Border border = BorderFactory.createTitledBorder("Available Search Terms");
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
        topPanel.add(comboPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topPanel.add(labelPanalX);
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

                        project = new DataStoreAndSearch(leftResults.getText(), command);
                    } else {
                        project = new DataStoreAndSearch(filename.getText(), command);
                    }
                    enableFunctions();//enable all functions we need....

                    //show documents stats
                    rightDownResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
                    rightDownResults.setMargin(new Insets(10, 10, 10, 10));
                    rightDownResults.setCaretPosition(5);
                    //rightDownResults.setText(project.documentWideStats());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Please check your input");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Please check your input");
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
        for (String pos : project.getTagWithTokens().keySet()) {
            tagsModel.addElement(pos);
        }
        posTags.setEnabled(true);

        // 4) fill tokens combo box
        tokensModel.removeAllElements();
        for (String tok : project.getTokensAlphabetically()) {
            tokensModel.addElement(tok);
        }
        tokens.setEnabled(true);

        // 5) fill lemmas combo boox
        lemmasModel.removeAllElements();
        for (String lem : project.getLemmasAlphabetically()) {
            lemmasModel.addElement(lem);
        }
        lemmas.setEnabled(true);

        // 6) enable the buttons of Ducument stats group
        tokensButton.setEnabled(true);
        posButton.setEnabled(true);
        tagged_SentButton.setEnabled(true);

        // 7) initially tetx for keyword field
        keyword.setText("Enter a keyword");

        rightDownResults.setText(project.documentWideStats());

    }

    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class SearchButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            leftResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            leftResults.setMargin(new Insets(30, 40, 30, 40));
            leftResults.setCaretPosition(5);

            rightUpResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            rightUpResults.setMargin(new Insets(10, 10, 10, 10));
            rightUpResults.setCaretPosition(5);

            rightDownResults.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            rightDownResults.setMargin(new Insets(10, 10, 10, 10));
            rightDownResults.setCaretPosition(5);
            rightDownResults.setText(project.documentWideStats());//remains constant

            //return the keyword category
            String command = (String) keywordList.getSelectedItem();

            //get the model fro histList and fill it with the keywords that the user is searcing for
            DefaultListModel<String> aListModel = (DefaultListModel<String>) histList.getModel();
            if(!aListModel.contains(keyword.getText()))//no duplicates in the list
                aListModel.addElement(histNumber + ". " +keyword.getText());
            histNumber++;

            // if the activate radio button is selected:
            if (activate.isSelected()) {
                Integer numPrevious = (Integer) numPrev.getSelectedItem();//get the number from combo box
                Integer numAf = (Integer) numAfter.getSelectedItem();//get the number from combo box
                if (keyword.getText() == null) {
                    JOptionPane.showMessageDialog(frame, "there is no such a word in this Document");
                } else if (command.equals("Token")) {
                    String key = keyword.getText();
                    if (!project.getTokenTags().keySet().contains(key)) {
                        JOptionPane.showMessageDialog(frame, "there is no such a token in this Document");
                    } else {
                        rightUpResults.setText(project.statisticsOfToken(key));
                        try {
                            highlightSearchByTOKENAndItsNeighbours(leftResults, key, numPrevious, numAf);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(DataStoreAndSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (command.equals("Lemma")) {
                    String lem = keyword.getText().trim().toLowerCase();// project.getLemma(keyword.getText().trim());
                    if(!project.getLemma(lem).isEmpty())
                        lem = project.getLemma(lem);


                    if (!project.getLemmaWithSentences().keySet().contains(lem)) {
                        JOptionPane.showMessageDialog(frame, "there is no such a lemma in this Document");
                    } else {
                        rightUpResults.setText(project.statisticsOfLemma(lem));
                        try {
                            highlightSearchByLEMMAAndItsNeighbours(leftResults, lem, numPrevious, numAf);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(DataStoreAndSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } else if (command.equals("POS")) {
                    if (!project.getTagWithTokens().keySet().contains(keyword.getText().trim())) {
                        JOptionPane.showMessageDialog(frame, "there is no such a POS TAG in this Document");
                    } else {
                        rightUpResults.setText(project.statisticsOfPOS(keyword.getText().trim()));
                        try {
                            highlightSearchByPOSAndItsNeighbours(leftResults, keyword.getText().trim(), numPrevious, numAf);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(DataStoreAndSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            } else {
                if (keyword.getText() == null) {
                    JOptionPane.showMessageDialog(frame, "there is no such a word in this Document");
                } else if (command.equals("Token")) {
                    String key = keyword.getText().toLowerCase();
                    if (!project.getTokenTags().keySet().contains(key)) {
                        JOptionPane.showMessageDialog(frame, "there is no such a token in this Document");
                    } else {
                        rightUpResults.setText(project.statisticsOfToken(key));
                        try {
                            highlightKeywordForToken(leftResults, key);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(DataStoreAndSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (command.equals("Lemma")) {

                    String lem = keyword.getText().trim().toLowerCase();// project.getLemma(keyword.getText().trim());
                    if(!project.getLemma(lem).isEmpty())
                        lem = project.getLemma(lem);


                    if (!project.getLemmaWithSentences().keySet().contains(lem)) {
                        JOptionPane.showMessageDialog(frame, "there is no such a lemma in this Document");
                    } else {
                        rightUpResults.setText(project.statisticsOfLemma(lem));
                        try {
                            highlightKeywordForLemma(leftResults, lem);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(DataStoreAndSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (command.equals("POS")) {
                    if (!project.getTagWithTokens().keySet().contains(keyword.getText().trim())) {
                        JOptionPane.showMessageDialog(frame, "there is no such a POS TAG in this Document");
                    } else {
                        rightUpResults.setText(project.statisticsOfPOS(keyword.getText().trim()));
                        try {
                            highlightKeywordForSearchByPOS(leftResults,keyword.getText() );
                        } catch (BadLocationException ex) {
                            Logger.getLogger(DataStoreAndSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
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
                filename.setEnabled(false);
            } else if (inputText.getSelectedItem().equals("Wikipedia") || inputText.getSelectedItem().equals("File")) {
                leftResults.setEditable(false);
                filename.setEnabled(true);
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
                numAfter.setEnabled(false);
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
                storeInFile(savedtext);
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

            String linesRightUp[] = rightUpResults.getText().split("\\r?\\n");
            for (String line : linesRightUp) {
                if (!line.isEmpty()) {
                    output.println(line + newline);
                }

            }

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage() + "EXXXX");
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
            leftResults.setText(project.getInformationForAlltokens());

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
            leftResults.setText(project.getTextWithFormTOKEN_TAG());
        }
    }

    /**
     * USER GUIDE
     * private class POSTAGSButtonHandler for handling the event fired by load
     * button
     */
    private class InfoButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFrame infoframe = new JFrame("KWIC Program manual");
            infoframe.setSize(650, 600);
            infoframe.setMinimumSize(new Dimension(300, 400));
            infoframe.setMaximumSize((new Dimension(300, 700)));


            JTextArea infotxt = new JTextArea(10,10);
            infotxt.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 15));
            infotxt.setMargin(new Insets(10, 10, 10, 10));
            infotxt.setBackground(Color.CYAN);
            infotxt.setText("USER GUIDE\nhello user, welcome to our KWIC engine\n\n" +
                    "1.0 LOAD CORPUS\n" +
                    "Enter the file path or URL in the input at the top left of screen\n" +
                    "please select your input type in the drop down to the right.\n" +
                    "NOTE: if you select 'Typed Text', you will type your text into the main window\n" +
                    "click LOAD to generate the linguistic statistics for your doc\n\n" +
                    "2.0 SEARCHING\n" +
                    "you can search by token (strict search), lemma or POS tag\n" +
                    "enter your search term - token, lemma or POS Tag (Penn Tree bank) in the search box\n " +
                    "select the search type you want from the drop down\n" +
                    "\tTOKEN SEARCH: a strict search. will only match exactly what you type\n" +
                    "\tLEMMA SEARCH: will find the lemma of what you type, \n" +
                    "\t  and will match all tokens in the text that share this lemma.\n" +
                    "\tPOS TAG SEARCH: will search for the Penn Treebank Tag that you type,\n" +
                    "\t  and will match all tokens in the text tat have that tag\n\n" +
                    "3.0 WORDS PRECEDING/FOLLOWING\n" +
                    "yu can refine your search by selecting the number of words before and after the match that you\n" +
                    " want to display.\n" +
                    "click activate, in the top right corner, and make your selections in the drop downs.\n\n" +
                    "4.0 VIEW AND USE AVAILABLE SEARCH TERMS\n" +
                    "You can auto populate the search field by selecting available search terms \n" +
                    "from the Tags/Tokens/Lemmas " +
                    "drop downs to the right of screen\n\n" +
                    "5.0 SUPER BONUS STATISTICS\n" +
                    "You can get additional document-wide statistics by clicking on the three buttons in the \n" +
                    "'Document Stats' section\n\n" +
                    "6.0 SAVING YOUR OUTPUT\n" +
                    "To save a file, select 'Save' or 'Save As' from the file menu.");
            JScrollPane infoPane = new JScrollPane(infotxt);

            infoframe.getContentPane().add(infoPane);

            infoframe.addWindowListener(new MyWindowListener());
            infoframe.setVisible(true);

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



    // GUI HIGHLIGHTING FIXES

    /*
     * Private method - a GUI fix for te highlighting with neighbors
     */
    private void highlightKeywordForToken(JTextArea area, String token) throws BadLocationException{
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);
        area.setText("THE RESULTS OF YOUR SEARCH ARE: \n\n");


        for(int j = 0; j < project.getSentenceWithTokens().keySet().size(); j++){

            for(int i = 0; i< project.getSentenceWithTokens().get(j).length; i++){
                if(project.getSentenceWithTokens().get(j)[i].equalsIgnoreCase(token)){
                    area.append(j + ". ");
                    for(int k = 0; k<= i; k++){
                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }
                    highlighter.addHighlight(area.getText().length()-project.getSentenceWithTokens().get(j)[i].length()-1, area.getText().length() - 1, painter);
                    for(int k = i +1; k< project.getSentenceWithTokens().get(j).length; k++){
                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }

                }
                if(token.equalsIgnoreCase(project.getSentenceWithTokens().get(j)[i])){

                    area.append("\n(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                            + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] + " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                }
            }
        }
    }

    /*
     * Private method - a GUI fix for te highlighting with neighbors
     */
    private void highlightKeywordForLemma(JTextArea area, String lemma) throws BadLocationException{
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);

        area.setText("THE RESULTS OF YOUR SEARCH ARE: \n\n");

        for(int j = 0; j < project.getSentenceWithLemmas().keySet().size(); j++){

            for(int i = 0; i< project.getSentenceWithLemmas().get(j).length; i++){
                if(lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])){
                    area.append(j + ". ");
                    for(int k = 0; k<= i; k++){
                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }
                    highlighter.addHighlight(area.getText().length()-project.getSentenceWithTokens().get(j)[i].length()-1, area.getText().length() - 1, painter);
                    for(int k = i +1; k< project.getSentenceWithLemmas().get(j).length; k++){
                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }

                }
                if(lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])){
                    area.append("\n(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                            + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                }
            }
        }
    }

    /*
     * Private method - a GUI fix for te highlighting with neighbors
     */
    private void highlightKeywordForSearchByPOS(JTextArea area, String pos) throws BadLocationException {
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);

        area.setText("THE RESULTS OF YOUR SEARCH ARE: \n\n");

        for(int j = 0; j < project.getSentenceWithPOS().keySet().size(); j++){
            for(int i = 0; i< project.getSentenceWithPOS().get(j).length; i++){
                if(pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])){
                    area.append(j + ". ");
                    for(int k = 0; k<= i; k++){
                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }
                    highlighter.addHighlight(area.getText().length()-project.getSentenceWithTokens().get(j)[i].length()-1, area.getText().length() - 1, painter);

                    for(int k = i +1; k< project.getSentenceWithPOS().get(j).length; k++){

                        area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                    }
                    area.append("\n");

                }
                //ArrayList<String> sublist = new ArrayList<>();
                if(pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])){
                    area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                            + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                    //+ TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i])
                }
            }
        }
    }


    /*
     * Private method - a GUI fix for te highlighting with neighbors
     */
    private void highlightSearchByPOSAndItsNeighbours(JTextArea area, String pos, int numPrev, int numAfter) throws BadLocationException {
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);

        area.setText("THE RESULTS OF YOUR SEARCH ARE: \n\n");

        for (int j = 0; j < project.getSentenceWithPOS().keySet().size(); j++) {
            for (int i = 0; i < project.getSentenceWithPOS().get(j).length; i++) {
                if (pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])) {
                    //
                    if ((i - numPrev < 0) && (i + numAfter >= project.getSentenceWithPOS().get(j).length)) {
                        area.append(j + ". ");
                        for (int k = 0; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k < project.getSentenceWithPOS().get(j).length; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i + numAfter >= project.getSentenceWithPOS().get(j).length) && (i - numPrev >= 0)) {
                        area.append(j + ". ");
                        for (int k = i - numPrev; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k < project.getSentenceWithPOS().get(j).length; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i - numPrev < 0) && (i + numAfter < project.getSentenceWithPOS().get(j).length)) {
                        area.append(j + ". ");
                        for (int k = 0; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k <= i + numAfter; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i - numPrev >= 0) && (i + numAfter < project.getSentenceWithPOS().get(j).length)) {

                        area.append(j + ". ");
                        for (int k = i - numPrev; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k <= i + numAfter; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (pos.equalsIgnoreCase(project.getSentenceWithPOS().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    }
                }
            }
        }
    }

    /*
     * Private method - a GUI fix for te highlighting with neighbors
     */
    private void highlightSearchByLEMMAAndItsNeighbours(JTextArea area, String lemma, int numPrev, int numAfter) throws BadLocationException {
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);

        area.setText("THE RESULTS OF YOUR SEARCH ARE: \n\n");

        for (int j = 0; j < project.getSentenceWithLemmas().keySet().size(); j++) {
            for (int i = 0; i < project.getSentenceWithLemmas().get(j).length; i++) {
                if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {
                    //
                    if ((i - numPrev < 0) && (i + numAfter >= project.getSentenceWithTokens().get(j).length)) {

                        area.append(j + ". ");
                        for (int k = 0; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k < project.getSentenceWithPOS().get(j).length; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i + numAfter >= project.getSentenceWithTokens().get(j).length) && (i - numPrev >= 0)) {
                        area.append(j + ". ");
                        for (int k = i - numPrev; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k < project.getSentenceWithPOS().get(j).length; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i - numPrev < 0) && (i + numAfter < project.getSentenceWithTokens().get(j).length)) {
                        area.append(j + ". ");
                        for (int k = 0; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k <= i + numAfter; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i - numPrev >= 0) && (i + numAfter < project.getSentenceWithTokens().get(j).length)) {

                        area.append(j + ". ");
                        for (int k = i - numPrev; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k <= i + numAfter; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    }
                }
            }
        }
    }

    /*
     * Private method - a GUI fix for te highlighting with neighbors
     */
    private void highlightSearchByTOKENAndItsNeighbours(JTextArea area, String lemma, int numPrev, int numAfter) throws BadLocationException {
        Highlighter highlighter = area.getHighlighter();
        highlighter.removeAllHighlights();//remove all previous
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);

        area.setText("THE RESULTS OF YOUR SEARCH ARE: \n");

        for (int j = 0; j < project.getSentenceWithTokens().keySet().size(); j++) {
            for (int i = 0; i < project.getSentenceWithTokens().get(j).length; i++) {
                if (lemma.equalsIgnoreCase(project.getSentenceWithTokens().get(j)[i])) {
                    if ((i - numPrev < 0) && (i + numAfter >= project.getSentenceWithTokens().get(j).length)) {

                        area.append(j + ". ");
                        for (int k = 0; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k < project.getSentenceWithPOS().get(j).length; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {
                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i + numAfter >= project.getSentenceWithTokens().get(j).length) && (i - numPrev >= 0)) {
                        area.append(j + ". ");
                        for (int k = i - numPrev; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k < project.getSentenceWithPOS().get(j).length; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {
                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i - numPrev < 0) && (i + numAfter < project.getSentenceWithTokens().get(j).length)) {
                        area.append(j+ ". ");
                        for (int k = 0; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k <= i + numAfter; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {
                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    } else if ((i - numPrev >= 0) && (i + numAfter < project.getSentenceWithTokens().get(j).length)) {

                        area.append(j + ". ");
                        for (int k = i - numPrev; k <= i; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        highlighter.addHighlight(area.getText().length() - project.getSentenceWithTokens().get(j)[i].length() - 1, area.getText().length() - 1, painter);
                        for (int k = i + 1; k <= i + numAfter; k++) {
                            area.append(project.getSentenceWithTokens().get(j)[k] + " ");
                        }
                        area.append("\n");

                        if (lemma.equalsIgnoreCase(project.getSentenceWithLemmas().get(j)[i])) {

                            area.append("(Token: " + project.getSentenceWithTokens().get(j)[i] + "   Lemma : " + project.getSentenceWithLemmas().get(j)[i]
                                    + "   POSTag: " + project.getSentenceWithPOS().get(j)[i] +  " -> " + TagToEnglish.tagUpdater(project.getSentenceWithPOS().get(j)[i]) + ")\n\n");
                        }
                    }
                }
            }
        }
    }


    /**
     * main method for GUI
     * @param args
     */
    public static void main(String[] args) {
        GUI pr = new GUI();
    }
}

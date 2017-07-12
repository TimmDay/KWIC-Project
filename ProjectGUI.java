
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Savvas
 */
public class ProjectGUI {

    private final JFrame frame;
    private JTextField filename, keyword;
    private JTextArea leftResults, rightResults;
    private JComboBox keywordList, numPrev, numAfter;
    private KeyWordFinder project;
    private final JPanel levels;

    public ProjectGUI() {
        frame = new JFrame("Java Project");
        frame.setSize(1000, 500);
        frame.setMinimumSize(new Dimension(500,400));
        frame.setMaximumSize((new Dimension(1000,700)));

        //main layout
        frame.getContentPane().setLayout(new BorderLayout());
        
        levels = new JPanel();
        levels.setLayout(new BoxLayout(levels, BoxLayout.Y_AXIS));
        
        
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
        setupLevel4();
     
        setupLevel5();
        frame.getContentPane().add(BorderLayout.NORTH, levels);

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
        // m.addActionListener(......);
        fileMenu.add(m);
        fileMenu.add(Box.createVerticalStrut(10));

         m = new JMenuItem("Save as...");
        // m.addActionListener(......);
        fileMenu.add(m);

        fileMenu.add(Box.createVerticalStrut(10));
        fileMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
        fileMenu.add(Box.createVerticalStrut(10));

        m = new JMenuItem("Clear");
        // m.addActionListener(......);
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
        JMenu toolMenu = new JMenu("Tools");

        m = new JMenuItem("Font");
        ///m.addActionListener(...);
        toolMenu.add(m);

        m = new JMenuItem("Color");
        ///m.addActionListener(...);
        toolMenu.add(m);

        JMenuBar mBar = new JMenuBar();
        mBar.add(fileMenu);
        mBar.add(Box.createHorizontalStrut(10));
        mBar.add(toolMenu);

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
   
    private void setuplevel3(){
        //left side
        JLabel leftLabel = new JLabel("Enter a file name or URL");
        leftLabel.setMinimumSize(new Dimension(70, 20));
        //leftLabel.setPreferredSize(new Dimension(90, 20));
        leftLabel.setMaximumSize(new Dimension(100, 20));
        filename = new JTextField("file.txt or wikipedia URL",15); //instance variable!!
        filename.setToolTipText("You can only enter file with \".txt\" extension or wikipedia URL for english documents");
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
        
        JPanel labelPanel = new JPanel();
        labelPanel.add(leftLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        
        JPanel textButtonPanel = new JPanel();
        textButtonPanel.add(filename);
        textButtonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        textButtonPanel.add(load);
        textButtonPanel.add(Box.createHorizontalGlue());
        textButtonPanel.setLayout(new BoxLayout(textButtonPanel, BoxLayout.X_AXIS));
        
        //and add the left label and textButtonPanel into another component
        JPanel leftPanel = new JPanel();
        
        leftPanel.add(labelPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0,5)));
        leftPanel.add(textButtonPanel);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));//end left
        
        //center
        JLabel centerLabel = new JLabel("Enter a keaword");
        centerLabel.setMinimumSize(new Dimension(100, 20));
        centerLabel.setPreferredSize(new Dimension(179, 20));
        centerLabel.setMaximumSize(new Dimension(179, 20));
        keywordList = new JComboBox(new String[]{"Word", "Lemma", "POS", "Sentence"});
        keywordList.setSelectedIndex(0);
        keywordList.setToolTipText("Choose the keyword category");
        keywordList.setMinimumSize(new Dimension(70, 20));
        keywordList.setPreferredSize(new Dimension(90, 20));
        keywordList.setMaximumSize(new Dimension(100, 20));
        //filename.addActionListener(.....);
        
        
        
        JPanel center1 = new JPanel();
        center1.add(centerLabel);
        center1.add(Box.createRigidArea(new Dimension(10,0)));
        center1.add(keywordList);
        center1.add(Box.createHorizontalGlue());
        center1.setLayout(new BoxLayout(center1, BoxLayout.X_AXIS));
        
        
        
        keyword = new JTextField("Enter a Keyword e.g.: book",15); //instance variable!!
        keyword.setToolTipText("Enter the keyword and press searche");
        keyword.setMinimumSize(new Dimension(70,20));
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
        textButtonPanel2.add(Box.createRigidArea(new Dimension(20,0)));
        textButtonPanel2.add(search);
        textButtonPanel2.add(Box.createHorizontalGlue());
        textButtonPanel2.setLayout(new BoxLayout(textButtonPanel2, BoxLayout.X_AXIS));
        
        //and add the center and textButtonPanel2 into another component
        JPanel centerPanel = new JPanel();
        
        centerPanel.add(center1);
        centerPanel.add(Box.createRigidArea(new Dimension(0,5)));
        centerPanel.add(textButtonPanel2);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));//end center
        
        //rightPanel
        //label....
        JLabel rightLabel = new JLabel("Enter the number of words that precede and follow the keyword");
        rightLabel.setMinimumSize(new Dimension(70, 20));
        //rightLabel.setPreferredSize(new Dimension(90, 20));
       // rightLabel.setMaximumSize(new Dimension(100, 20));
        
        JPanel rightlabelPanel = new JPanel();
        rightlabelPanel.add(rightLabel);
        rightlabelPanel.add(Box.createHorizontalGlue());
        rightlabelPanel.setLayout(new BoxLayout(rightlabelPanel, BoxLayout.X_AXIS));
        
        //two ComboBoxs
        //DefaultComboBoxModel prev = new DefaultComboBoxModel(new String[] {"1","2", "3", "4", "5", "6", "7"});
        numPrev = new JComboBox(new String[] {"1","2", "3", "4", "5", "6", "7"}); //instance variable!!
        numPrev.setSelectedIndex(2);//default number
        numPrev.setToolTipText("Enter only digits");
        numPrev.setMinimumSize(new Dimension(50, 20));
        numPrev.setPreferredSize(new Dimension(50, 20));
        numPrev.setMaximumSize(new Dimension(50, 20));
        numPrev.setEnabled(true);
        numPrev.setEditable(true);
        //numPrev.addActionListener(.....);  <---
        
       // DefaultComboBoxModel after = new DefaultComboBoxModel(new String[] {"1","2", "3", "4", "5", "6", "7"});
        numAfter = new JComboBox(new String[] {"1","2", "3", "4", "5", "6", "7"}); //instance variable!!
        numAfter.setSelectedIndex(2);//default number
        numAfter.setToolTipText("Enter only digits");
        numAfter.setMinimumSize(new Dimension(50, 20));
        numAfter.setPreferredSize(new Dimension(50, 20));
        numAfter.setMaximumSize(new Dimension(50, 20));
        numAfter.setEnabled(true);
        numAfter.setEditable(true);
        //numAfter.addActionListener(.....); <---
        
        
        JPanel comboPanel = new JPanel();
        comboPanel.add(new JLabel("Preceding words:"));
        comboPanel.add(Box.createRigidArea(new Dimension(5,0)));
        comboPanel.add(numPrev);
        comboPanel.add(Box.createRigidArea(new Dimension(100,0)));
        comboPanel.add(new JLabel("Following word:"));
        comboPanel.add(Box.createRigidArea(new Dimension(5,0)));
        comboPanel.add(numAfter);
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
        
        //and add the left label and textButtonPanel into another component
        JPanel rightPanel = new JPanel();
        
        rightPanel.add(rightlabelPanel);
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createRigidArea(new Dimension(0,5)));
        rightPanel.add(comboPanel);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));//end
 
        
        //mainPanel
        JPanel main = new JPanel();
        main.add(Box.createRigidArea(new Dimension(5,0)));
        main.add(leftPanel);
        main.add(new JSeparator(SwingConstants.VERTICAL));
        main.add(centerPanel);
        main.add(new JSeparator(SwingConstants.VERTICAL));
        main.add(rightPanel);
        main.add(Box.createHorizontalGlue());
        main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));
        
        //the last
        JPanel allPanel = new JPanel();
        allPanel.add(Box.createRigidArea(new Dimension(0,5)));
        allPanel.add(main);
        allPanel.add(Box.createRigidArea(new Dimension(0,5)));
        allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));
        
        
        levels.add(allPanel);
        levels.add(new JSeparator(SwingConstants.HORIZONTAL));//!!!!
    }
    
    
    private void setupLevel4(){
        //rightPanel
        JButton tokens = new JButton("Tokens");
        tokens.setToolTipText("Search for the given keyword");
        tokens.setMinimumSize(new Dimension(120, 20));
        tokens.setPreferredSize(new Dimension(140, 20));
        tokens.setMaximumSize(new Dimension(140, 20));
        tokens.setForeground(Color.BLUE);
        //Tokens.addActionListener(......);
        
        JButton pos_Tags = new JButton("POS_Tags");
        pos_Tags.setToolTipText("Search for the given keyword");
        pos_Tags.setMinimumSize(new Dimension(120, 20));
        pos_Tags.setPreferredSize(new Dimension(140, 20));
        pos_Tags.setMaximumSize(new Dimension(140, 20));
        pos_Tags.setForeground(Color.BLUE);
        //POS_Tags.addActionListener(......);
        
        JButton sentences = new JButton("Sentences");
        sentences.setToolTipText("Search for the given keyword");
        sentences.setMinimumSize(new Dimension(120, 20));
        sentences.setPreferredSize(new Dimension(140, 20));
        sentences.setMaximumSize(new Dimension(140, 20));
        sentences.setForeground(Color.BLUE);
        //Sentences.addActionListener(......);
        
        JButton tagged_Sent = new JButton("Sentence / Tags");
        tagged_Sent.setToolTipText("Search for the given keyword");
        tagged_Sent.setMinimumSize(new Dimension(120, 20));
        tagged_Sent.setPreferredSize(new Dimension(140, 20));
        tagged_Sent.setMaximumSize(new Dimension(140, 20));
        tagged_Sent.setForeground(Color.BLUE);
        //Tagged_Sent.addActionListener(......);
        
        JPanel right = new JPanel();
        right.add(Box.createHorizontalGlue());
        right.add(tokens);
        right.add(Box.createRigidArea(new Dimension(140,0)));
        right.add(pos_Tags);
        right.add(Box.createRigidArea(new Dimension(140,0)));
        right.add(sentences);
        right.add(Box.createRigidArea(new Dimension(140,0)));
        right.add(tagged_Sent);
        //right.add(Box.createRigidArea(new Dimension(40,0)));
        right.add(Box.createHorizontalGlue());
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
       
       // right.setBackground(Color.red);
       
        
        //the last
        JPanel allPanel = new JPanel();
        allPanel.add(right);
        allPanel.add(Box.createRigidArea(new Dimension(0,2)));
        allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));
        allPanel.setBackground(Color.red);
        
        
        levels.add(allPanel);
        levels.add(new JSeparator(SwingConstants.HORIZONTAL));//!!!!
        
    }
    
    private void setupLevel5(){
        
        leftResults = new JTextArea("Search results", 10, 30);
        
        leftResults.setEditable(true); //the user can copy-paste his/her text and load it...
        
        //add this area into JScrollPane
        JScrollPane leftListPane = new JScrollPane(leftResults);
        leftResults.setLineWrap(true);
        //leftResults.setWrapStyleWord(true);
        
        //create the right TextArea
        rightResults = new JTextArea("Search results", 10, 20);
        
        rightResults.setEditable(false);
        //add this area into JScrollPane
        JScrollPane rightListPane = new JScrollPane(rightResults);
        //add this ListPane into a Panel
        JPanel panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(leftListPane);
        panel.add(Box.createRigidArea(new Dimension(5,5)));
        panel.add(rightListPane);
        
         //add to JFrame
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        
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
            } else 
            {
                try {
                    project = new KeyWordFinder(filename.getText());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "FileNotFoundException");
                    
                }
            }

        }
    }
    
    /**
     * private class LDButtonHandler for handling the event fired by load
     * buttton
     */
    private class SearchButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (filename == null) 
            {
                JOptionPane.showMessageDialog(frame, "No input to download");
            } else 
            {
                try {
                    if(project == null)
                       project = new KeyWordFinder(filename.getText());
                    
                    leftResults.setFont(new Font(Font.DIALOG_INPUT, Font.HANGING_BASELINE, 15));
                    //leftResults.setFont(new Font("Monaco", Font.PLAIN, 12));
                    leftResults.setMargin(new Insets(30, 40, 30, 40));

                    leftResults.setCaretPosition(5);
                    if(keyword == null)
                        JOptionPane.showMessageDialog(frame, "there is no such a word in this Document");
                    else{
                        leftResults.setText(project.findSentencesOfWord2(keyword.getText()));
                        Highlighter highlighter = leftResults.getHighlighter();
                        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
                         int pos = 0;
                       while( (pos = leftResults.getText().indexOf(keyword.getText(), pos)) > 0){
                       
                        try {
                            highlighter.addHighlight(pos, pos + keyword.getText().length(), painter);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ProjectGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        pos += keyword.getText().length();
                    }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "FileNotFoundException");
                    
                }
            }
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

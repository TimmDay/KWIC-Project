
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.lemmatizer.*;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 *
 * @authors names...................
 */
public class DataModel {

    //instance variables
    private String word;
    private String tag;
    private String text; //the input text as a single String 
    private String[] sentences;//holds all sentances from the given source
    private ArrayList<String> tokensPerSent;//holds all tokens from the given sentence
    private ArrayList<String> tokensAndTagsPerSentece;//holds all tokens from the given sentence
    private int numOfTokens;
    private int frequency;
    private HashMap<String, Integer> wordFreq;
    private HashMap<String, Integer> tagFreq;
    private HashMap<String, ArrayList<String>> LemmasWords;
    private HashMap<String, ArrayList<String>> wordAndTags;//holds the words as thisWord and their tags stored in an ArrayList
    private HashMap<String, ArrayList<String>> tagCluster;//gold the tags as keys and the words having this tag stored in an ArrayList

    public DataModel(String inputText, String command) throws IOException {

        if (command.equals("url")) { //call the Scraper class and read the site
            Scraper souped = new Scraper(inputText);
            text = souped.getCorpus();
        }
        if (command.equals("string")) {//take this String as text
            text = inputText;
        }
        if (command.equals("file")) { // call the readFromFile method and read the file
            text = readFromFile(inputText);

        }

        /*
            use Apache open nlp for tokenizer / lemmatizer and sentence detector
         */
        //Loading sentence detector model 
        InputStream inputStream = new FileInputStream("en-sent.bin"); //create the input stream for the sentence model
        SentenceModel sentencModel = new SentenceModel(inputStream);
        SentenceDetectorME detector = new SentenceDetectorME(sentencModel);//Instantiating the SentenceDetectorME class 

        sentences = detector.sentDetect(text);//Detecting the sentences and tore the sentences into the array  

        //create the stream for the POS tags
        InputStream inputStream2 = new FileInputStream("en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStream2);//
        POSTaggerME tagger = new POSTaggerME(posModel);//use postaggerME -class

        //create the stream for the tokens
        InputStream stream = new FileInputStream("en-token.bin");
        TokenizerModel model = new TokenizerModel(stream);
        Tokenizer tokenizer = new TokenizerME(model);

        //create the stream for the tokens
        InputStream stream2 = new FileInputStream("en-lemmatizer.txt");
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(stream2);

        //instantiate the hash maps obejcts and fill them
        wordAndTags = new HashMap<>();
        numOfTokens = 0;
        wordFreq = new HashMap<>();
        tagCluster = new HashMap<>();
        tagFreq = new HashMap<>();
        LemmasWords = new HashMap<>();
        tokensAndTagsPerSentece = new ArrayList<>();
        for (String sentence : sentences) {//FOR EACH SENTENCE
            //sentence = correctMarks(sentence).trim();
            String[] tokens = tokenizer.tokenize(sentence);//generate 1) the tokens for each sentence....
            String[] tagsOfthisSent = tagger.tag(tokens);//... 2) the coresponding tags
            String[] lemmas = lemmatizer.lemmatize(tokens, tagsOfthisSent);//... and 3) the corresponding lemmas
            for (int i = 0; i < lemmas.length; i++) { // <-- tim this is your update method. 
                if (lemmas[i].equals("O")) {
                    lemmas[i] = tokens[i];
                }
            }

            POSSample sample = new POSSample(tokens, tagsOfthisSent);//holds each sentence and tags together!
            tokensAndTagsPerSentece.add(sample.toString()); //stores each sentence with its tags in an Arraylist
            if (tokens.length > 0) {
                numOfTokens += tokens.length;//update num of tokens
                for (int i = 0; i < tokens.length; i++) {
                    String thisWord = tokens[i].trim();
                    String thisTag = tagsOfthisSent[i].trim();
                    String lem = lemmas[i].trim();
                    if (wordAndTags.containsKey(thisWord)) {
                        String frWord = thisWord.toLowerCase();//for freaqueny -> no case sensitive
                        wordFreq.put(frWord, wordFreq.get(frWord) + 1);//increment by 1
                        if (!wordAndTags.get(thisWord).contains(thisTag)) {
                            wordAndTags.get(thisWord).add(thisTag);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>(); //create an ArrayList
                        list.add(thisTag);
                        wordAndTags.put(thisWord, list);
                        String frWord = thisWord.toLowerCase();//for freaqueny -> no case sensitive
                        wordFreq.put(frWord, 1);

                    }

                    /*fill the tagCluster and Tagfrep HahMaps 
                    tagCluster--> the tag  is the KEY and the VALUE is an ArrayList that holdes all
                    words in this text having this tag
                    tagfreq --> the tag is the KEY and the Value is the key's frequency in this text
                     */
                    if (tagCluster.containsKey(thisTag)) {
                        tagFreq.put(thisTag, tagFreq.get(thisTag) + 1);
                        if (!tagCluster.get(thisTag).contains(thisWord)) {
                            tagCluster.get(thisTag).add(thisWord);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>(); //create a ArrayList
                        list.add(thisWord);
                        tagCluster.put(thisTag, list);
                        tagFreq.put(thisTag, 1);
                    }

                    /*fill the LemmasWords HahMap
                    LemmasWords--> the lemma  is the KEY and the VALUE is an ArrayList that holdes all
                    words that have this lemma in this text
                     */
                    if (LemmasWords.containsKey(lem)) {
                        if (!LemmasWords.get(lem).contains(thisWord)) {
                            LemmasWords.get(lem).add(thisWord);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(thisWord);
                        LemmasWords.put(lem, list);
                    }

                }
            }
            //close all streams
            stream.close();
            inputStream.close();
            inputStream2.close();
            stream2.close();
        }
    }
    /**
     * helper method that read the text from a file
     * @param fileName the file to be read
     * @return the text from the file as a single string 
     * @throws IOException 
     */
    private String readFromFile(String fileName) throws IOException {
        //create the stream for reading the file
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        String currentLine;
        text = "";
        while ((currentLine = br.readLine()) != null) {
            text += currentLine;
        }
        br.close();//close stream
        return text;
    }
    
    /**
     * getter method for finding the lemma from a given word
     * @param word the word searching the lemma for
     * @return the lemma of word
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public String getLemma(String word) throws FileNotFoundException, IOException {

        if (word == null) { // if null...
            return null;//...terminate the method
        }
        String val = "";
        for (String e : LemmasWords.keySet()) {
            if (LemmasWords.get(e).contains(word)) {
                val = e;
            }
        }
        return val;
    }

    /**
     * getter mmethod 
     * @return the number of tokens in this text
     */
    public int numOfTokens() {
        return numOfTokens;
    }
    
    /**
     * getter method  search all tags for the given argument
     * @param word
     * @return the tags for this word 
     */
    public ArrayList<String> findAllTagsForThisword(String word) {
        if (wordAndTags.containsKey(word)) { //if the word is in the hash map
            return wordAndTags.get(word); //return its tags as an arraylist
        } else {
            return null;
        }
    }
    /**
     * 
     * we dont need this method.... is just to see the print.....
     */
    public void findtag(String s) {
        if (wordAndTags.containsKey(s)) {
            System.out.println("key: \"" + s + "\" its tags: " + wordAndTags.get(s));
        }
    }
    /**
     * getter method frequencyOfWord
     * @param aWord
     * @return the frequency of this word (no case sensitive)
     */
    public int frequencyOfWord(String aWord) {
        if (aWord == null) { 
            return -1;
        }
        aWord = aWord.toLowerCase();
        if (wordFreq.containsKey(aWord)) { //if the word is in the text ...
            return wordFreq.get(aWord);//... return its frequency
        } else {
            return -1;// 
        }
    }
    
   
    /**
     * getter method
     * @return the all sentences of this text in separate lines
     */
    public String printSentences() {
        //int i = 0;
        String val = "";
        for (String elem : sentences) {
            val += elem + "\n";
           // System.out.println(++i + elem);
        }
        return val;
    }

    /**
     * getter method
     * @return all tokens that this has text with their tag(s)
     */
    public String printAlltokensAndTags() {
        String val = "";
        for (String key : wordAndTags.keySet()) {
             val += "\"" + key + "\": " + wordAndTags.get(key) + "   ";
            //System.out.print("\"" + key + "\": " + wordAndTags.get(key) + "/ /");
        }
        return val;
    }
    /**
     * getter method
     * @return all tags that this has text with their word(s)
     */
    public String printAllTagsAndTheirWords() {
        String val = "";
        for (String key : tagCluster.keySet()) {
            val += "\"" + key + "\": " + tagCluster.get(key) + "    ";
           // System.out.print("\"" + key + "\": " + tagCluster.get(key) + "/ /");
        }
        return val;
    }
    /**
     * getter method
     * @return all words and the coresponding frequancy for each word
     */
    public String printWordFrequencies() {
        String val = "";
         for (String key : wordFreq.keySet()) {
             val += "\"" + key + "\": " + wordFreq.get(key) + "    ";
           // System.out.print("\"" + key + "\": " + wordFreq.get(key) + "/ /");
        }
        return val;
    }
    /**
     * getter method
     * @return all tags and the coresponding frequancy for each tag
     */
    public String printTAGFrequencies() {
        String val = "";
        for (String key : tagFreq.keySet()){
            val += "\"" + key + "\": " + tagFreq.get(key) + "    ";
            //System.out.print("\"" + key + "\": " + tagFreq.get(key) + "/ /");
        }
        return val;
    }
    /**
     * getter method
     * @return each sentece with the tags for this sentence : I_PRP have_VB .....
     */
    public String  printTokensAndTagsPerSentece() {
        String val = "";
        for(String sent : tokensAndTagsPerSentece){
            sent += "\n";
           // System.out.println(sent);
        }
        return val;
    }
    
    
    /**
     * getter method 
     * @return each kind of lemma for the given text and the words that have this lemma
     */
    public String printLemmasAndWords() {
        String val = "";
        for(String key : LemmasWords.keySet()){
            val += "\"" + key + "\": " + LemmasWords.get(key) + "   ";
            //System.out.print("\"" + key + "\": " + LemmasWords.get(key) + "/ /");
        }
        return val;
    }

    /**
     * getter method
     *
     * @return the number of distinct tags in this document
     */
    public int getNumOfdistinctTags() {
        return tagFreq.size();
    }

    /**
     * getter method
     *
     * @return the number of all tags in this document
     */
    public int getNumOfAlllTags() { // is equals with the numOfTokens.....(just for verification)

        int sum = 0;
        for (Integer num : tagFreq.values()) {
            sum += num;
        }
        return sum;
    }
    /**
     * getter method
     * @param tag
     * @return the frequency as percentage  of this tag in this text 
     */

    public double getFrequencyOfTag(String tag) { //it is not case sensitive 
        if (tag == null) {
            return -1;
        }
        tag = tag.toUpperCase();
        if (tagFreq.containsKey(tag)) {
            double val;
            val = ((double) tagFreq.get(tag) * 100.0) / (double) getNumOfAlllTags();

            //val =Double.valueOf(new DecimalFormat("").format(val));
            return val;
        } else {
            return -1;
        }
    }
    /**
     * getter method 
     * @param s 
     * @return  all sentences containing this word
     */
    public String findSentencesOfWord(String s) {
        if (s == null) {
            return null;
        }
        String val = "";
        ArrayList<String> list = new ArrayList<>();
        for (String sent : sentences) {
            if (sent.contains(s)) {
                list.add(sent);
            }
        }
       for (String sent : list) {
            val += sent + "\n\n";
            //System.out.println(sent);
            
        }
       return val;
    }


    /**
     * getter method 
     * @param s
     * @param numPrev
     * @param numAfter 
     */
    public void findWordAndItsNeighbours(String s, int numPrev, int numAfter) {
        if (s == null) {
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        for (String sent : sentences) {
            //sent = correctMarks(sent).trim();
            String[] ar = sent.split("\\s+");
            for (int i = 0; i < ar.length; i++) {
                if (ar[i].equalsIgnoreCase(s)) {
                    if (i - numPrev < 0 && i + numAfter >= ar.length) {
                        numPrev = numPrev - (numPrev - i);
                        numAfter = numAfter - (numAfter - (ar.length - 1 - i));
                        String rsl = "";
                        for (int j = i - numPrev; j <= numAfter + i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    } else if (i + numAfter >= ar.length) {
                        numAfter = numAfter - (numAfter - (ar.length - 1 - i));
                        //numPrev =  i - (i -numPrev);
                        String rsl = "";
                        for (int j = i - numPrev - 1; j <= numAfter + i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    } else if (i - numPrev < 0) {
                        numPrev = numPrev - (numPrev - i);
                        String rsl = "";
                        for (int j = i - numPrev; j <= numAfter + i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    } else {
                        String rsl = "";

                        for (int j = i - numPrev; j <= numAfter + i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    }
                }
            }

        }
        list.forEach((elem) -> {
            System.out.println(elem);
        });
    }

    

    public static void main(String[] args) {
        try {
            DataModel my = new DataModel("ex.txt", "file");
            //or
           //DataModel my = new DataModel("https://en.wikipedia.org/wiki/The_Beatles", "url"); 
           // or as string........
           //DataModel my = new DataModel("The Beatles were an English rock band formed in Liverpool in 1960. With members John Lennon, Paul McCartney, George Harrison and Ringo Starr, they became widely regarded as the foremost and most influential act of the rock era.[1] Rooted in skiffle, beat and 1950s rock and roll, the Beatles later experimented with several musical styles, ranging from pop ballads and Indian music to psychedelia and hard rock, often incorporating classical elements and unconventional recording techniques in innovative ways. In 1963 their enormous popularity first emerged as "Beatlemania", and as the group's music grew in sophistication in subsequent years, led by primary songwriters Lennon and McCartney, they came to be perceived as an embodiment of the ideals shared by the counterculture of the 1960s.", "string"); 
           //try the methodssssss..........
           my.findAllTagsForThisword("accunt");
           System.out.println(my.printAllTagsAndTheirWords());
 

        } catch (IOException ex) {
            Logger.getLogger(DataModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

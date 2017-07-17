
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
import opennlp.tools.lemmatizer.*;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 *
 * @authors names...................
 */
public class DataModelEd {

    //instance variables
    private String text; //the input text as a single String 
    private String[] sentences;//holds all sentances from the given source

    private HashMap<Integer, String[]> sentenceWithTokens;    // key: index of each sentence in sentences, value: list of posTags in that sentence
    private HashMap<Integer, String[]> sentenceWithPOS;       // for calculating lemmas. key: sentence index, value: list of lemmas
    private HashMap<Integer, String[]> sentenceWithLemmas;    // key: index of each sentence in sentences, value: list of lemmas in that sentence

    private ArrayList<String> tokensAndTagsPerSentece;//holds all posTags from the given sentence
    private int numOfTokens;

    private HashMap<String, Integer> wordFreq;
    private HashMap<String, Integer> tagFreq;

    private HashMap<String, ArrayList<String>> wordAndTags;//holds the words as thisToken and their tags stored in an ArrayList
    private HashMap<String, ArrayList<String>> tagCluster;//hold the tags as keys and the tokens having this tag stored in an ArrayList

    private HashMap<String, ArrayList<String>> lemmaSentences; // key: lemma value: sentences that lemma appear
    private HashMap<String, ArrayList<String>> lemmaTokens;
    private HashMap<String, ArrayList<String>> lemmaTags; // key: lemma value: tags of posTags that hold lemma

    private HashMap<String, ArrayList<String>> tokenSentences; // key: token value: sentences that TOKEN appear
    private HashMap<String, ArrayList<String>> tokenTags; // key: lemma value: tags of posTags that hold lemma 

    public DataModelEd(String inputText, String command) throws IOException, Exception {

        if (command.equals("Wikipedia")) { //call the Scraper class and read the site
            Scraper souped = new Scraper(inputText);
            text = souped.getCorpus();
        }
        if (command.equals("Typed text")) {//take this String as text
            text = inputText;
        }
        if (command.equals("File")) { // call the readFromFile method and read the file
            text = readFromFile(inputText);

        }

        text = text.replaceAll("\\[\\d+\\]", "");
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

        //create the stream for the posTags
        InputStream stream = new FileInputStream("en-token.bin");
        TokenizerModel model = new TokenizerModel(stream);
        Tokenizer tokenizer = new TokenizerME(model);

        //create the stream for the lemmas
        InputStream stream2 = new FileInputStream("en-lemmatizer.txt");
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(stream2);

        //instantiate the hash maps obejcts and fill them
        sentenceWithTokens = new HashMap<>();
        sentenceWithPOS = new HashMap<>();
        sentenceWithLemmas = new HashMap<>();
        wordAndTags = new HashMap<>();
        numOfTokens = 0;
        wordFreq = new HashMap<>();
        tagCluster = new HashMap<>();
        tagFreq = new HashMap<>();
        lemmaTokens = new HashMap<>();
        tokensAndTagsPerSentece = new ArrayList<>();
        lemmaSentences = new HashMap<>();
        lemmaTags = new HashMap<>();
        tokenSentences = new HashMap<>();
        tokenTags = new HashMap<>();
        for (int j = 0; j < sentences.length; j++) {//FOR EACH SENTENCE
            //sentence = correctMarks(sentence).trim();
            String[] tokens = tokenizer.tokenize(sentences[j]);//generate 1) the posTags for each sentence....
            String[] tagsOfthisSent = tagger.tag(tokens);//... 2) the coresponding tags
            String[] lemmas = lemmatizer.lemmatize(tokens, tagsOfthisSent);//... and 3) the corresponding lemmas
            sentenceWithTokens.put(j, tokens);
            sentenceWithPOS.put(j, tagsOfthisSent);
            sentenceWithLemmas.put(j, lemmas);
            for (int i = 0; i < lemmas.length; i++) { // <-- tim this is your update method. 
                if (lemmas[i].equals("O")) {
                    lemmas[i] = tokens[i];
                }
            }
            POSSample sample = new POSSample(tokens, tagsOfthisSent);//holds each sentence and tags together!
            tokensAndTagsPerSentece.add(sample.toString()); //stores each sentence with its tags in an Arraylist
            if (tokens.length > 0) {
                numOfTokens += tokens.length;//update num of posTags
                for (int i = 0; i < tokens.length; i++) {
                    String thisToken = tokens[i].trim();
                    String thisTag = tagsOfthisSent[i].trim();
                    String lem = lemmas[i].trim();

                    if (wordAndTags.containsKey(thisToken)) {
                        String frWord = thisToken.toLowerCase();//for freaqueny -> no case sensitive
                        wordFreq.put(frWord, wordFreq.get(frWord) + 1);//increment by 1
                        if (!wordAndTags.get(thisToken).contains(thisTag)) {
                            wordAndTags.get(thisToken).add(thisTag);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>(); //create an ArrayList
                        list.add(thisTag);
                        wordAndTags.put(thisToken, list);
                        String frWord = thisToken.toLowerCase();//for freaqueny -> no case sensitive
                        wordFreq.put(frWord, 1);
                    }
                    if (tokenSentences.containsKey(thisToken)) {
                        if (!tokenSentences.get(thisToken).contains(sentences[j])) {
                            tokenSentences.get(thisToken).add(sentences[j]);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(sentences[j]);
                        tokenSentences.put(thisToken, list);
                    }
                    if (tokenTags.containsKey(thisToken)) {
                        if (!tokenTags.get(thisToken).contains(thisTag)) {
                            tokenTags.get(thisToken).add(thisTag);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(thisTag);
                        tokenTags.put(thisToken, list);
                    }
                    if (lemmaSentences.containsKey(lem)) {
                        if (!lemmaSentences.get(lem).contains(sentences[j])) {
                            lemmaSentences.get(lem).add(sentences[j]);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(sentences[j]);
                        lemmaSentences.put(lem, list);
                    }

                    if (lemmaTags.containsKey(lem)) {
                        if (!lemmaTags.get(lem).contains(thisTag)) {
                            lemmaTags.get(lem).add(thisTag);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(thisTag);
                        lemmaTags.put(lem, list);
                    }
                    /*fill the tagCluster and Tagfrep HahMaps
                    tagCluster--> the tag  is the KEY and the VALUE is an ArrayList that holdes all
                    words in this text having this tag
                    tagfreq --> the tag is the KEY and the Value is the key's frequency in this text
                     */
                    if (tagCluster.containsKey(thisTag)) {
                        tagFreq.put(thisTag, tagFreq.get(thisTag) + 1);
                        if (!tagCluster.get(thisTag).contains(thisToken)) {
                            tagCluster.get(thisTag).add(thisToken);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>(); //create a ArrayList
                        list.add(thisToken);
                        tagCluster.put(thisTag, list);
                        tagFreq.put(thisTag, 1);
                    }

                    /*fill the lemmaTokens HahMap
                    lemmaTokens--> the lemma  is the KEY and the VALUE is an ArrayList that holdes all
                    words that have this lemma in this text
                     */
                    if (lemmaTokens.containsKey(lem)) {
                        if (!lemmaTokens.get(lem).contains(thisToken)) {
                            lemmaTokens.get(lem).add(thisToken);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(thisToken);
                        lemmaTokens.put(lem, list);
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
     *
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
     * getter method
     * @param lemma
     * @return 
     */
    public HashMap<String, ArrayList<String>> getLemmaSentences(){
        return lemmaSentences;
    }
    /**
     * 
     * @return the hash map tokenTags
     */
    public HashMap<String, ArrayList<String>> getTokenTags(){
        return tokenTags;
    }
    
    public HashMap<String, ArrayList<String>> getTagCluster(){
        return tagCluster;
    }
    
    public HashMap<Integer, String[]> getSentenceWithTokens(){
        return sentenceWithTokens;
    }
    
    public ArrayList<String> getTokensHavingThisLemma(String lemma){
        if (lemma == null) {
            return null;
        }
 
        if(!lemmaTokens.keySet().contains(lemma)) {
            return null;
        }
        else
            return lemmaTokens.get(lemma);
    }
    public ArrayList<String> getTokensHavingThisPOSTag(String pos){
        if (pos == null) {
            return null;
        }
 
        if(!tagCluster.keySet().contains(pos)) {
            return null;
        }
        else
            return tagCluster.get(pos);
    }

    //when we search with lemma, return posTags that appear with this lemma
    public String getTokensOfLemma(String lemma) {
        if (lemma == null) {
            return null;
        }
        String tokens = "";
        for (String lem : lemmaTokens.get(lemma)) {
            tokens += lem + "\n";
        }
        return tokens;
    }

    public String getTagsOfLemma(String lemma) {
        if (lemma == null) {
            return null;
        }
        String tags = "";
        for (String tag : lemmaTags.get(lemma)) {
            tags += tag + "\n";
        }
        return tags;
    }

    public String getSentencesOfLemma(String lemma) {
        if (lemma == null) {
            return null;
        }
        String sentences = "";
        int i = 1;
        for (String sentence : lemmaSentences.get(lemma)) {
            sentences += i + ". " + sentence + "\n\n";
            i++;
        }
        return sentences;
    }

    public String getSentencesOfToken(String token) {
        if (token == null) {
            return null;
        }
        String sentences = "";
        if (!tokenSentences.containsKey(token)) {
            return "key word \"" + token + "\" does not exist.";
        } else {

            int i = 1;
            for (String sentence : tokenSentences.get(token)) {
                sentences += i + ". " + sentence + "\n\n";
                i++;
            }
        }
        return sentences;
    }
    public String getSentencesOfPOSTag(String pos) {
        if (pos == null) {
            return null;
        }
        String sentences = "";
        
        if (!tagCluster.containsKey(pos)) {
            return "POS Tag \"" + pos + "\" does not exist.";
        } else {
            int i = 1;
            for(String token : tagCluster.get(pos)){
            for (String sentence : tokenSentences.get(token)) {
                sentences += i + ". " + sentence + "\n\n";
                i++;
            }
            }
        }
        return sentences;
    }

    public String getTagsOfToken(String token) {
        if (token == null) {
            return null;
        }
        String tags = "";
        if (!tokenTags.containsKey(token)) {
            return "key word \"" + token + "\" does not exist.";
        } else {
            for (String tag : tokenTags.get(token)) {
                tags += tag + "\n";
            }
        }
        return tags;
    }

    /**
     * getter method
     *
     * @return the number of posTags in this text
     */
    public int numOfTokens() {
        return numOfTokens;
    }

    /**
     * getter method frequencyOfWord
     *
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
     * getter method for finding the lemma from a given word
     * @param word the word searching the lemma for
     * @return the lemma of word
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public String getLemma(String word) {

        if (word == null) { // if null...
            return null;//...terminate the method
        }
        String val = "";
        for (String e : lemmaTokens.keySet()) {
            if (lemmaTokens.get(e).contains(word)) {
                val = e;
            }
        }
        return val;
    }

    /**
     * getter method
     *
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
     *
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
     *
     * @return all tags and the coresponding frequancy for each tag
     */
    public String printTAGFrequencies() {
        String val = "";
        for (String key : tagFreq.keySet()) {
            val += "\"" + key + "\": " + tagFreq.get(key) + "    ";
            //System.out.print("\"" + key + "\": " + tagFreq.get(key) + "/ /");
        }
        return val;
    }

    /**
     * getter method
     *
     * @return each sentece with the tags for this sentence : I_PRP have_VB
     * .....
     */
    public String printTokensAndTagsPerSentece() {
        String val = "";
        for (String sent : tokensAndTagsPerSentece) {
            sent += "\n";
            // System.out.println(sent);
        }
        return val;
    }

    /**
     * getter method
     *
     * @return each kind of lemma for the given text and the words that have
     * this lemma
     */
    public String printLemmasAndTokens() {
        String val = "";
        for (String key : lemmaTokens.keySet()) {
            val += "\"" + key + "\": " + lemmaTokens.get(key) + "   ";
            //System.out.print("\"" + key + "\": " + lemmaTokens.get(key) + "/ /");
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
     *
     * @param tag
     * @return the frequency as percentage of this tag in this text
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

    public String findTOKENAndItsNeighbours(String aToken, int numPrev, int numAfter) {
        if (aToken == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < sentenceWithTokens.keySet().size(); i++) {
            String[] tokens = sentenceWithTokens.get(i);
            for (int j = 0; j < tokens.length; j++) {
                if (tokens[j].equalsIgnoreCase(aToken.trim())) {
                    if ((j - numPrev < 0) && (j + numAfter >= tokens.length)) {
                    String rsl = "";
                    for (int t = 0; t <= tokens.length - 2; t++) {
                    rsl += tokens[t] + " ";
                    }
                    rsl += tokens[tokens.length-1];
                    list.add(rsl);
                    } else if ((j + numAfter >= tokens.length) && (j - numPrev >= 0)) {
                    String rsl = "";
                    for (int t = j- numPrev; t <= tokens.length - 2; t++) {
                    rsl += tokens[t] + " ";
                    }
                    rsl += tokens[tokens.length-1];
                    list.add(rsl);
                    } else if ((j - numPrev < 0) && (j + numAfter < tokens.length)) {
                        String rsl = "";
                        for (int t = 0; t <= j + numAfter; t++) {
                            rsl += tokens[t] + " ";
                        }
                        list.add(rsl);
                    }else if ((j - numPrev >= 0) && (j + numAfter < tokens.length)) {
                    String rsl = "";
                    for (int t = j - numPrev; t <= j + numAfter; t++) {
                    rsl += tokens[t] + " ";
                    }
                    list.add(rsl);
                    }

                }
            }
        }
        String val = "";
        for (String x : list) {
            val += x + "\n";
        }

        return val;
    }
    
    public String findLEMMAndItsNeighbours(String aWord, int numPrev, int numAfter) throws IOException {
        if (aWord == null) {
            return null;
        }
        String lem = getLemma(aWord);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < sentenceWithLemmas.keySet().size(); i++) {
            String[] tokens = sentenceWithTokens.get(i);
            String[] lemmas = sentenceWithLemmas.get(i);
            for (int j = 0; j < lemmas.length; j++) {
                if (lemmas[j].equalsIgnoreCase(lem.trim())) {
                    if ((j - numPrev < 0) && (j + numAfter >= tokens.length)) {
                    String rsl = "";
                    for (int t = 0; t <= tokens.length - 2; t++) {
                    rsl += tokens[t] + " ";
                    }
                    rsl += tokens[tokens.length-1];
                    list.add(rsl);
                    } else if ((j + numAfter >= tokens.length) && (j - numPrev >= 0)) {
                    String rsl = "";
                    for (int t = j- numPrev; t <= tokens.length - 2; t++) {
                    rsl += tokens[t] + " ";
                    }
                    rsl += tokens[tokens.length-1];
                    list.add(rsl);
                    } else if ((j - numPrev < 0) && (j + numAfter < tokens.length)) {
                        String rsl = "";
                        for (int t = 0; t <= j + numAfter; t++) {
                            rsl += tokens[t] + " ";
                        }
                        list.add(rsl);
                    }else if ((j - numPrev >= 0) && (j + numAfter < tokens.length)) {
                    String rsl = "";
                    for (int t = j - numPrev; t <= j + numAfter; t++) {
                    rsl += tokens[t] + " ";
                    }
                    list.add(rsl);
                    }

                }
            }
        }
        String val = "";
        for (String x : list) {
            val += x + "\n";
        }

        return val;
    }
    public String findPOSTagAndItsNeighbours(String aPOStag, int numPrev, int numAfter) throws IOException {
        if (aPOStag == null) {
            return null;
        }
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        
        for (int i = 0; i < sentenceWithPOS.keySet().size(); i++) {
            String[] posTags = sentenceWithPOS.get(i);
            ArrayList<String> subList = new ArrayList<>();
            for (int j = 0; j < posTags.length; j++) {
                if (posTags[j].equalsIgnoreCase(aPOStag.trim())) {
                    if ((j - numPrev < 0) && (j + numAfter >= posTags.length)) {
                    String rsl = "";
                    for (int t = 0; t <= posTags.length - 2; t++) {
                    rsl += posTags[t] + " ";
                    }
                    rsl += posTags[posTags.length-1];
                    subList.add(rsl);
                    list.add(subList);
                    } else if ((j + numAfter >= posTags.length) && (j - numPrev >= 0)) {
                    String rsl = "";
                    for (int t = j- numPrev; t <= posTags.length - 2; t++) {
                    rsl += posTags[t] + " ";
                    }
                    rsl += posTags[posTags.length-1];
                    subList.add(rsl);
                    list.add(subList);
                    } else if ((j - numPrev < 0) && (j + numAfter < posTags.length)) {
                        String rsl = "";
                        for (int t = 0; t <= j + numAfter; t++) {
                            rsl += posTags[t] + " ";
                        }
                        subList.add(rsl);
                        list.add(subList);
                    }else if ((j - numPrev >= 0) && (j + numAfter < posTags.length)) {
                    String rsl = "";
                    for (int t = j - numPrev; t <= j + numAfter; t++) {
                    rsl += posTags[t] + " ";
                    }
                    subList.add(rsl);
                    list.add(subList);
                    }

                }
            }
        }
    
        String val = "";
        int i = 0;
        for (ArrayList<String> x : list) {
            val += i + " Sentence: ";
            for(String y : x){
                val += x; 
            }
            val += "\n";
            i++;
        }

        return val;
    }
    public void prinSentencesWithTokens(int x) {
        
            String[] tokens = sentenceWithTokens.get(x);
            String rel = "";
            for (int j = 0; j < tokens.length; j++) {
                rel += tokens[j] + "- ";
            }
        System.out.println(rel);

    }
  /**
   * helper method
   * find the number of sentences having this tag
   */
    private ArrayList<Integer> getNumberOfSentWithThisToken(String aToken) {
        if (aToken == null) {
            return null;
        }
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < sentenceWithTokens.keySet().size(); i++) {
            for (String token : sentenceWithTokens.get(i)) {
                if (token.equalsIgnoreCase(aToken)) {
                    list.add(i); //automatic boxing...
                }
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        try {
            DataModelEd my =  new DataModelEd("https://en.wikipedia.org/wiki/The_Beatles", "Wikipedia");
          
       //     System.out.println("Lemma search results: ");
      //      System.out.println("Sentences that lemma occurs:\n" + my.getSentencesOfLemma("account"));
        //    System.out.println("Tokens of that lemma in it:\n" + my.getTokensOfLemma("account"));
      //      System.out.println("Tags of tokens of that lemma in it:\n" + my.getTagsOfLemma("account"));
      //      System.out.println("Token search results: ");
       //     System.out.println("Sentences that token occurs:\n" + my.getSentencesOfToken("account"));
         //   System.out.println("Tags of the token:\n" + my.getTagsOfToken("accounted"));
           // System.out.println("Tags of the token:\n"+ my.getSentencesOfLemma("released"));
              System.out.println("tokens and their neighbours:\n"+ my.findPOSTagAndItsNeighbours("NN", 1,1));
     my.prinSentencesWithTokens(2);

        } catch (IOException ex) {
            System.out.println("sdgdgadg");
           // Logger.getLogger(DataModelEd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private HashMap<String, Integer> tokenFreq;
    private HashMap<String, Integer> tagFreq;

    private HashMap<String, ArrayList<String>> tokenTags;//holds the words as key and their tags stored in an ArrayList
    private HashMap<String, ArrayList<String>> tagCluster;//hold the tags as keys and the tokens having this tag stored in an ArrayList

    private HashMap<String, ArrayList<String>> lemmaSentences; // key: lemma value: sentences that lemma appear
    private HashMap<String, ArrayList<String>> lemmaTokens;
    private HashMap<String, ArrayList<String>> lemmaTags; // key: lemma value: tags of posTags that hold lemma

    private HashMap<String, ArrayList<String>> tokenSentences; // key: token value: sentences that TOKEN appear
    private ArrayList<String> totalToken;
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
        tokenTags = new HashMap<>();
        numOfTokens = 0;
        tokenFreq = new HashMap<>();
        tagCluster = new HashMap<>();
        tagFreq = new HashMap<>();
        lemmaTokens = new HashMap<>();
        tokensAndTagsPerSentece = new ArrayList<>();
        lemmaSentences = new HashMap<>();
        lemmaTags = new HashMap<>();
        tokenSentences = new HashMap<>();
        totalToken = new ArrayList<>();
        
        for (int j = 0; j < sentences.length; j++) {//FOR EACH SENTENCE
            //sentence = correctMarks(sentence).trim();
            String[] tokens = tokenizer.tokenize(sentences[j]);//generate 1) the posTags for each sentence....
            String[] tagsOfthisSent = tagger.tag(tokens);//... 2) the coresponding tags
            String[] lemmas = lemmatizer.lemmatize(tokens, tagsOfthisSent);//... and 3) the corresponding lemmas
            
            for (int i = 0; i < lemmas.length; i++) {
                lemmas[i] = lemmas[i].toLowerCase();
                tokens[i] = tokens[i].toLowerCase();
                if (lemmas[i].equals("O")) {
                    lemmas[i] = tokens[i];
                }
                if (lemmas[i].length() > 1) {
                    if (lemmas[i].charAt(0) == ('"')) {
                        lemmas[i] = lemmas[i].substring(1);
                    }
                    if (lemmas[i].charAt(0) == ('\'')) {
                        lemmas[i] = lemmas[i].substring(1);
                    }
                    if (lemmas[i].charAt(lemmas[i].length() - 1) == ('"')) {
                        lemmas[i] = lemmas[i].substring(0, lemmas[i].length());
                    }
                    if (lemmas[i].charAt(lemmas[i].length() - 1) == ('\'')) {
                        lemmas[i] = lemmas[i].substring(0, lemmas[i].length());
                    }
                }
                if (tokens[i].length() > 1) {
                    if (tokens[i].charAt(0) == ('"')) {
                        tokens[i] = tokens[i].substring(1);
                    }
                    if (tokens[i].charAt(0) == ('\'')) {
                        tokens[i] = tokens[i].substring(1);
                    }
                    if (tokens[i].charAt(tokens[i].length() - 1) == ('"')) {
                        tokens[i] = tokens[i].substring(0, tokens[i].length());
                    }
                    if (tokens[i].charAt(tokens[i].length() - 1) == ('\'')) {
                        tokens[i] = tokens[i].substring(0, tokens[i].length());
                    }
                }
            }
            sentenceWithTokens.put(j, tokens);
            sentenceWithPOS.put(j, tagsOfthisSent);
            sentenceWithLemmas.put(j, lemmas);
            POSSample sample = new POSSample(tokens, tagsOfthisSent);//holds each sentence and tags together!
            tokensAndTagsPerSentece.add(sample.toString()); //stores each sentence with its tags in an Arraylist
            if (tokens.length > 0) {
                numOfTokens += tokens.length;//update num of posTags
                for (int i = 0; i < tokens.length; i++) {
                    String thisToken = tokens[i].trim();
                    String thisTag = tagsOfthisSent[i].trim();
                    String lem = lemmas[i].trim().toLowerCase();
                    totalToken.add(thisToken);

                    if (tokenTags.containsKey(thisToken)) {
                        String frWord = thisToken.toLowerCase();//for freaqueny -> no case sensitive
                        tokenFreq.put(frWord, tokenFreq.get(frWord) + 1);//increment by 1
                        if (!tokenTags.get(thisToken).contains(thisTag)) {
                            tokenTags.get(thisToken).add(thisTag);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>(); //create an ArrayList
                        list.add(thisTag);
                        tokenTags.put(thisToken, list);
                        String frWord = thisToken.toLowerCase();//for freaqueny -> no case sensitive
                        tokenFreq.put(frWord, 1);
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
                            lemmaTags.get(lem).add(thisTag.toLowerCase());
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
    public ArrayList<String> getSentencesWithLemma(){
        ArrayList<String> list = new ArrayList<>();
        for(Integer num : sentenceWithLemmas.keySet()){
            for(String lem : sentenceWithLemmas.get(num)){
                if(!list.contains(lem))
                    list.add(lem);
            }
            
        }
        Collections.sort(list);
        return list;
    }
     public ArrayList<String> getSentencesWithToken(){
        ArrayList<String> list = new ArrayList<>();
        for(Integer num : sentenceWithTokens.keySet()){
            for(String tok : sentenceWithTokens.get(num)){
                if(!list.contains(tok))
                    list.add(tok);
            }
            
        }
        Collections.sort(list);
        return list;
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
    public HashMap<Integer, String[]> getSentenceWithPOS(){
        return sentenceWithPOS;
    }
    public HashMap<Integer, String[]> getSentenceWithLemmas(){
        return sentenceWithLemmas;
    }
    

   
   
    public String getSentWithThisToken(String token){
        if (token == null) {
            return null;
        }
        ArrayList<String[]> rsl = new ArrayList<>();
        ArrayList<ArrayList<String>> info = new ArrayList<>();
       // ArrayList<String> sublist = new ArrayList<>();
        for(int j = 0; j < sentenceWithTokens.keySet().size(); j++){
             ArrayList<String> sublist = new ArrayList<>();
            for(int i = 0; i< sentenceWithTokens.get(j).length; i++){
                if(token.equalsIgnoreCase(sentenceWithTokens.get(j)[i])){
                    if(!rsl.contains(sentenceWithTokens.get(j)))
                        rsl.add(sentenceWithTokens.get(j));
                       // rsl.add(sentences[num]);
               }
                 //ArrayList<String> sublist = new ArrayList<>();
                if(token.equalsIgnoreCase(sentenceWithTokens.get(j)[i])){
                  
                    sublist.add("(Token: " + sentenceWithTokens.get(j)[i] + "   Lemma : " + sentenceWithLemmas.get(j)[i] 
                            + "   POSTag: " + sentenceWithPOS.get(j)[i] + ")");
                     info.add(sublist);  
                }
                    
            }
          // info.add(sublist);
        }
        String val = "";
        int num = 1;
        for(int j = 0; j < rsl.size(); j++){
            val += num + ". ";
            for(int i = 0; i < rsl.get(j).length; i++){
                val += rsl.get(j)[i] + " ";              
            }
           // for(int i = 0; i < info.size(); i++){
                for(int k = 0; k < info.get(j).size(); k++){
                    val += "\n" + info.get(j).get(k) + " ";
                }
           // }
            val += "\n\n";
            num++;
        }
        
        return val;
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
        lemma = lemma.toLowerCase();
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
    
    public String showInformationForAlltokens(){
        String val = "Tokens from this document have the following pos tags: \n\n";
        int i = 1;
        for (String key : tokenTags.keySet()) {
            String key2 = key.toLowerCase();
            val += i + ". \"" + key + "\": has " + tokenTags.get(key).size() 
                   + " POS tag(s) and its frequency is:  " + tokenFreq.get(key2) + " or " +
                    String.format("%.2f", getFrequencyOfToken(key2)) + "% -> " +  tokenTags.get(key) +"\n\n";
            i++;
        }
        
        return val;
    }
    public String showInformationForAllPOStags(){
        String val = "POS TAGS from this document match the following tokens: \n\n";
        int i = 1;
        for (String key : tagCluster.keySet()) {
            val += i + ". \"" + key + "\": matches " + tagCluster.get(key).size() 
                     + " token(s) and its frequency is:  " + tagFreq.get(key) 
                    + "  or " + String.format( "%.2f", getFrequencyOfTag(key) ) + "%  -> " + tagCluster.get(key) +  "\n\n";
            i++;//String.format( "%.2f", dub ) 
        }
        
        return val;
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
        if (tokenFreq.containsKey(aWord)) { //if the word is in the text ...
            return tokenFreq.get(aWord);//... return its frequency
        } else {
            return -1;// 
        }
    }
    
    /**
     * getter method for finding the lemma from a given word
     * @param word the word searching the lemma for
     * @return the lemma of word
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
        for (String key : tokenFreq.keySet()) {
            val += "\"" + key + "\": " + tokenFreq.get(key) + "    ";
            // System.out.print("\"" + key + "\": " + tokenFreq.get(key) + "/ /");
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
        String val = "     Parsed sentences:\n";
        int i = 1;
        for (String sent : tokensAndTagsPerSentece) {
            val += i + ". " +  sent + "\n\n";
            // System.out.println(sent);
            i++;
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
     * needddddddddddddddd
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
    /**
     * neeeeeeeeeeeeeeeeddd
     * @param tok
     * @return 
     */
     public double getFrequencyOfToken(String tok) { 
        if (tok == null) {
            return -1;
        }
        
        if (tokenFreq.containsKey(tok)) {
            double val;
            val = ((double) tokenFreq.get(tok) * 100.0) / (double) numOfTokens;

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
                    for (int t = 0; t <= tokens.length - 1; t++) {
                    rsl += tokens[t] + " ";
                    }
                    rsl += "\n(Token: " + sentenceWithTokens.get(i)[j] + "   Lemma : " + sentenceWithLemmas.get(i)[j] 
                            + "   POSTag: " + sentenceWithPOS.get(i)[j] + ")";
                    list.add(rsl);
                    } else if ((j + numAfter >= tokens.length) && (j - numPrev >= 0)) {
                    String rsl = "";
                    for (int t = j- numPrev; t <= tokens.length - 1; t++) {
                    rsl += tokens[t] + " ";
                    }
                    rsl += "\n(Token: " + sentenceWithTokens.get(i)[j] + "   Lemma : " + sentenceWithLemmas.get(i)[j] 
                            + "   POSTag: " + sentenceWithPOS.get(i)[j] + ")";
                    list.add(rsl);
                    } else if ((j - numPrev < 0) && (j + numAfter < tokens.length)) {
                        String rsl = "";
                        for (int t = 0; t <= j + numAfter; t++) {
                            rsl += tokens[t] + " ";
                        }
                        rsl += "\n(Token: " + sentenceWithTokens.get(i)[j] + "   Lemma : " + sentenceWithLemmas.get(i)[j] 
                            + "   POSTag: " + sentenceWithPOS.get(i)[j] + ")";
                        list.add(rsl);
                    }else if ((j - numPrev >= 0) && (j + numAfter < tokens.length)) {
                    String rsl = "";
                    for (int t = j - numPrev; t <= j + numAfter; t++) {
                    rsl += tokens[t] + " ";
                    }
                    rsl += "\n(Token: " + sentenceWithTokens.get(i)[j] + "   Lemma : " + sentenceWithLemmas.get(i)[j] 
                            + "   POSTag: " + sentenceWithPOS.get(i)[j] + ")";
                    list.add(rsl);
                    }

                }
            }
        }
        String val = "";
        int i = 1;
        for (String x : list) {
            val += i + ". " +  x + "\n\n";
            i++;
        }

        return val;
    }
    
    public String findLEMMAndItsNeighbours(String lem, int numPrev, int numAfter) throws IOException {
        if (lem == null) {
            return null;
        }
        //String tok = getLemma(aWord);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < sentenceWithLemmas.keySet().size(); i++) {
            String[] tokens = sentenceWithTokens.get(i);
            String[] lemmas = sentenceWithLemmas.get(i);
            for (int j = 0; j < lemmas.length; j++) {
                if (lemmas[j].equalsIgnoreCase(lem.trim())) {
                    if ((j - numPrev < 0) && (j + numAfter >= tokens.length)) {
                    String rsl = "";
                    for (int t = 0; t <= tokens.length - 1; t++) {
                    rsl += tokens[t] + " ";
                    }
                    list.add(rsl);
                    } else if ((j + numAfter >= tokens.length) && (j - numPrev >= 0)) {
                    String rsl = "";
                    for (int t = j- numPrev; t <= tokens.length - 1; t++) {
                    rsl += tokens[t] + " ";
                    }
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
        int i = 1;
        String val = "";
        for (String x : list) {
            val += i + ". " + x + "\n\n";
            i++;
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
            String[] tokens = sentenceWithTokens.get(i);
            ArrayList<String> subList = new ArrayList<>();
            for (int j = 0; j < posTags.length; j++) {
                if (posTags[j].equalsIgnoreCase(aPOStag.trim())) {
                    if ((j - numPrev < 0) && (j + numAfter >= posTags.length)) {
                    String rsl = "";
                    for (int t = 0; t <= posTags.length - 1; t++) {
                    rsl += tokens[t] + " ";
                    }
                    subList.add(rsl);
                    list.add(subList);
                    } else if ((j + numAfter >= posTags.length) && (j - numPrev >= 0)) {
                    String rsl = "";
                    for (int t = j- numPrev; t <= posTags.length - 1; t++) {
                    rsl += tokens[t] + " ";
                    }
                    subList.add(rsl);
                    list.add(subList);
                    } else if ((j - numPrev < 0) && (j + numAfter < posTags.length)) {
                        String rsl = "";
                        for (int t = 0; t <= j + numAfter; t++) {
                            rsl += tokens[t] + " ";
                        }
                        subList.add(rsl);
                        list.add(subList);
                    }else if ((j - numPrev >= 0) && (j + numAfter < posTags.length)) {
                    String rsl = "";
                    for (int t = j - numPrev; t <= j + numAfter; t++) {
                    rsl += tokens[t] + " ";
                    }
                    subList.add(rsl);
                    list.add(subList);
                    }

                }
            }
        }
    
        String val = "";
        int i = 1;
        for (ArrayList<String> x : list) {
            val += i + ")  ";
            int j = 1;
            for (String y : x) {
                val += j + ". " + y + "\n";
                j++;
            }
            if(x.size() > 1)
                val += "(There are " + x.size() +  " tokens having this POS tags (" + aPOStag + ")" + ")";
            j = 1;
            val += "\n\n";
            i++;
        }
        return val;
    }


    
    public String documentWideStats() {

        String result = "";
        String result2 = "";
        String totalNumberofTokens = "Total number of tokens in the text:" + " " + numOfTokens();
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> tokenList = new ArrayList<>();


        Object[] a = tagFreq.entrySet ( ).toArray ( );
        Arrays.sort ( a, ( o1, o2 ) -> ( ( Map.Entry <String, Integer> ) o2 ).getValue ( )
        .compareTo ( ( ( Map.Entry <String, Integer> ) o1 ).getValue ( ) ) );
        for ( Object e : a ) {

        list.add( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + " "
        + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times" + "\n" );
        }


        for ( int i = 0 ; i < 5 ; i++ ) {

        result += list.get ( i );}

        Object[] b = tokenFreq.entrySet ( ).toArray ( );
        Arrays.sort ( b, ( Object o2, Object o3 ) -> ( ( Map.Entry <String, Integer> ) o3 ).getValue ( )
        .compareTo ( ( ( Map.Entry <String, Integer> ) o2 ).getValue ( ) ) );
        for ( Object e : b ) {
        if ((( ( Map.Entry <String, Integer> ) e ).getKey( )).length() > 3)
        tokenList.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + " "
        + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times" + "\n" );
        }

        for ( int i = 0 ; i < 5 ; i++ ) result2 += tokenList.get ( i );


        return "\n" + totalNumberofTokens + "\n" + "\n" + "Top 5 POS Tags in the Text:" + "\n" + result + "\n" + "Top 5 Tokens in the Text (with more than 3 letters):" + "\n" + result2;
}
     public String statisticsOfToken(String aWord) {

    String freqOfToken = ""; 
    String resu = "";
    String result = "";
    String resul = "";
    String tag = "";
    String formattedResult = "";
    String resulte = "";
    String resulten = "";
    ArrayList<String> tags = new ArrayList<String>(); 
    ArrayList<String> prevWord = new ArrayList<>();
    ArrayList<String> nextWord = new ArrayList<>();
    ArrayList<String> prec = new ArrayList<>();
    ArrayList<String> next = new ArrayList<>();
    ArrayList<String> nextP = new ArrayList<>();
    ArrayList<ArrayList<String>> dict = new ArrayList<>();
    ArrayList<ArrayList<String>> dict2 = new ArrayList<>();
    ArrayList<String> hel = new ArrayList<>();
    ArrayList<String> tes = new ArrayList<>();
    ArrayList<String> tesi = new ArrayList<>();
    List<String> res = new ArrayList<>();
    List<String> re = new ArrayList<>();
    List<String> rem = new ArrayList<>();
    List<String> remo = new ArrayList<>();



        // get the frequency of the token in the text using frequencyOfWord and numOfTokens

        freqOfToken = "Frequency of the keyword in the text:" + " " +  frequencyOfWord(aWord) + " " + "out of" + " " + numOfTokens() + "total tokens in the text";



        //frequency of POS Tag of that Token:

        //if the tokenTag HashMap contains the key aWord
        if (tokenTags.containsKey(aWord))


            //store the value of that key in the variable tags
            tags = tokenTags.get(aWord);

        //loop through the items in tags
        for (String temp : tags) {

            //add the frequeny of the tag to the String variable result
            result += temp + " " + getFrequencyOfTag ( temp ) + "\n";

            //format result so that it can be displayed with only 2 decimals
            formattedResult += temp + " " + result.format ( "%.02f", getFrequencyOfTag ( temp ) ) + "%" + " " + "out of the total number of POS" + "\n";

        }

        //getting the most likely to be preceeding token
        //loop through the totalToken List
        for (int i = 1; i < totalToken.size(); i++)

        //if the keyword is in the list
        {  if(aWord.equalsIgnoreCase(totalToken.get(i)))

            //add the token with one lower index
            prevWord.add(totalToken.get(i-1).toLowerCase()); }

            //create a new HashMap
        HashMap<String, Integer> occurrences = new HashMap<>();

        //loop through items in prevWords
        for ( String word :prevWord) {

            //create Integer oldCount to get the count of the number of times keyword appears in the text
            Integer oldCount = occurrences.get(word);

            //if the keyword appears for the first time, set oldCount to 0
            if ( oldCount == null ) {
                oldCount = 0;
            }

            //for each occurence, add 1 to the count
            occurrences.put(word, oldCount + 1);
        }


        //create an array and transform the entries in occurences to elements in that array
        Object[] b = occurrences.entrySet( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( b, ( Object o2, Object o3 ) -> ( ( Map.Entry <String, Integer> ) o3 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o2 ).getValue ( ) ) );

        //add all of the keys and their values to Arraylist prec
        for ( Object e : b ) {
            prec.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                    + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" );
        }

        //reduce the size of prec to get the first 5 elements
        if (prec.size() > 4)
        {
            res = prec.subList(0, 5);  }

        //if there are no 5 elements, just return the list
        else res = prec;

        //put all of the elements in res into a string called resu
        for (String e: res) resu += e;



        //getting the most likely to be following token
        //loop through totalTokens
        for (int i = 1; i < totalToken.size(); i++)

        //if aWord is found in totaltoken
        {  if(aWord.equalsIgnoreCase(totalToken.get(i)))

            //add the next word (index +1)to the list nextWord
            nextWord.add(totalToken.get(i+1).toLowerCase()); }


         //create a new hashmap occur
        HashMap<String, Integer> occur = new HashMap<>();

        //loop through items in prevWords
        for ( String word :nextWord) {

            //create Integer oldC to get the count of the number of times keyword appears in the text
            Integer oldC = occur.get(word);

            //if the keyword appears for the first time, set oldCount to 0
            if ( oldC == null ) {
                oldC = 0;
            }

            //for each occurence, add 1 to the count
            occur.put(word, oldC + 1);
        }

        //create an array and transform the entries in occur to elements in that array
        Object[] t = occur.entrySet( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( t, ( Object o4, Object o5 ) -> ( ( Map.Entry <String, Integer> ) o5 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o4 ).getValue ( ) ) );

        //add all of the keys and their values to Arraylist next
        for ( Object e : t ) {

            if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 )
                next.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" ) ;

            else next.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                    + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" ) ;
        }


        //reduce the size of next to get the first 5 elements
        if (next.size() > 4)

        {
            re = next.subList(0, 5);  }

        //if there are no 5 elements, just return the list

        else re = next;

        //put all of the elements in res into a string called resul

        for (String e: re) resul += e;


        //getting the preceeding POS Tag of that token

        //loop through prevWord
        for (String e : prevWord)

        //if tokenTags contains the key e
        { if (tokenTags.containsKey(e))
        {
            //get the value (the POS Tag) of e and store it in dict
          dict.add(tokenTags.get(e)); } }

          //loop through the arraylists in dict
        for (ArrayList<String> g : dict) {

            //add the strings in the array lists to hel
            for (String h : g) {
                hel.add(h);
            }
        }

        //create a new hashmap counters
        HashMap<String, Integer> counters = new HashMap<>();

        //loop through the strings in hel
        for (String i : hel)

        //if there is the key i in counters
        { if (counters.containsKey (i))
        {
            //for each occurence of i - add 1 to the count
            counters.put(i, counters.get(i)+1); }
            else
             counters.put(i, 1);

        }

        //create an array and transform the entries in counters to elements in that array
        Object[] m = counters.entrySet( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( m, ( Object o5, Object o6 ) -> ( ( Map.Entry <String, Integer> ) o6 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o5 ).getValue ( ) ) );
        for ( Object e : m ) {

            //add all of the keys and their values to Arraylist tes
            if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 && ((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1 )
                tes.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" ) ;

            else if (((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1) tes.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                    + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" ) ;
        }

        //reduce the size of tes to get the first 5 elements
        if (tes.size() > 4)
        {
            rem = tes.subList(0, 5);  }
            
        //if there are no 5 elements, just return the list
        else rem = tes;


        //store elements of rem in a String called resulte
        for (String e: rem) resulte += e;



        //Most Likely to be following Tag of the keyword:
        

        //loop through nextWord
        for (String e : nextWord)
        
        //if tokenTags contains the key e 
        { if (tokenTags.containsKey(e))
        {
            //add the value (the POS Tag) of e to dict2
            dict2.add(tokenTags.get(e)); } }
        
        //loop through the ArrayLists in dict2
        for (ArrayList<String> g : dict2) {

            //add the strings in the ArrayLists to nextP
            for (String h : g) {
                nextP.add(h);
            }
        }

        //create new Hash Map countes
        HashMap<String, Integer> countes = new HashMap<>();
        
        //loop through nextP
        for (String i : nextP)
        
        //if countes contains the key i 
        { if (countes.containsKey (i))
        {
            //add 1 to countes for each occurence of i 
            countes.put(i, countes.get(i)+1); }
        else
            countes.put(i, 1);

        }

        //create an array and transform the entries in countes to elements in that array
        Object[] z = countes.entrySet( ).toArray ( );
        
        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( z, ( Object o8, Object o9 ) -> ( ( Map.Entry <String, Integer> ) o9 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o8 ).getValue ( ) ) );
        for ( Object e : z ) {

            //add all of the keys and their values to Arraylist tesi
            if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 && ((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1 )
                tesi.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" ) ;

            else if (((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1) tesi.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                    + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" ) ;
        }

        //reduce the size of tes to get the first 5 elements
        if (tesi.size() > 4)
        {
            remo = tesi.subList(0, 5);  }

        //if there are no 5 elements, just return the list
        else remo = tesi;

        //store elements of rem in a String called resulten
        for (String e: remo) resulten += e;



        //return the final string with all the infos 
        return freqOfToken + "\n" + "\n" + "Most likely to be the preceding Token of the keyword:\n" + resu + "\n" +  "Most likely to be the following Token of the keyword:\n" + resul + "\n" +
                "Frequency of the POS tag of the keyword:\n" + formattedResult + "\n" +
                "Most likely to be preceeding Tag of the keyword (More than 1 character in length):" + "\n" + resulte
                + "\n" + "Most likely to be following Tag of this keyword (More than 1 character in length):" + "\n" + resulten;

}
     public String statisticsOfLemma(String lemma) {

      String result = "";
      String t = "";
      ArrayList<String> he = new ArrayList <> ( );

      he = lemmaTokens.get(lemma);
        ArrayList<ArrayList<String>> dict2 = new ArrayList<>();
        ArrayList<String> nextP2 = new ArrayList<>();
        String result2 = "";
        ArrayList<String> nextP = new ArrayList<>();


        //loop through items in next
        for (String i : he)


        //If tokenTags contains i as a key
        if (tokenTags.containsKey(i))
        {
            //add the value of key i (the POS Tags)
            dict2.add(tokenTags.get(i)); }


        //loop through the arraylists in dict2
        for (ArrayList<String> m : dict2) {

            //loop through strings in that arraylists
            for (String h : m) {

                //add the strings to nextP2
                nextP2.add(h);
            }
        }

        for (String n : nextP2)

        {
            result += n + " ";

        }

        ArrayList<ArrayList<String>> dict = new ArrayList<>();

        //loop through items in next
        for (String i : he)

        {

            String r = getTagsOfToken(i);
            result2 += i + " " + "appeared" + " " + frequencyOfWord ( i ) + " " + "times in the text" + "\n" + r;



        }


        return "POS Tags with that lemma:" + "\n"+ result + "\n" +"\n" + "Tokens with that lemma:" + "\n" + result2;
}
     
     public String statisticsOfPOS(String po) {

        //instance variables

        ArrayList<String> he = new ArrayList<>();
        ArrayList<String> j = new ArrayList<>();
        List<String> k = new ArrayList<>();
        ArrayList<String> v = new ArrayList<>();
        ArrayList<String> prev = new ArrayList<>();
        ArrayList<String> next = new ArrayList<>();
        HashMap<String, Integer> m = new HashMap<>();
        String result = "";
        String resul = "";
        String resultem = "";
        ArrayList<String> tesi = new ArrayList<>();
        ArrayList<String> tesim = new ArrayList<>();
        List<String> remo = new ArrayList<>();
        List<String> remom = new ArrayList<>();



        //getting frequency of the Tag and formatting it, so it has only 2 decimals
        String frequency = getFrequencyOfTag ( po ) + "\n";
        String formattedResult = frequency.format ( "%.02f", getFrequencyOfTag ( po ) ) + "%" + " " + "out of the total number of POS" + "\n";

        //Get most frequent Tokens with that Tag:
        //Store the values of the tagCluster Map into he
         he = tagCluster.get(po);

         //loop through Strings in he
         for (String e : he)
         {
             //put e as the key and the frequency of e as the value of hashmap m
           m.put(e, frequencyOfWord(e));

         }

        //create an array l and transform the entries in m to elements in that array
        Object[] l = m.entrySet( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( l, ( Object o10, Object o11 ) -> ( ( Map.Entry <String, Integer> ) o11 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o10 ).getValue ( ) ) );
        for ( Object e : l ) {

            //add all of the keys and their values to Arraylist j

            if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 && ((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1 )
                j.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" ) ;

            else if (((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1) j.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
                    + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" ) ;

        }

        //reduce the size of j to get the first 5 elements
        if (j.size() > 4)
        {

            k = j.subList(0, 5);  }

        //if there are no 5 elements, just return the list
        else k = j;

        //store elements of k in a String called resul
        for (String e: k) resul += e;


        //getting Most likely to be preceeding POS Tag
        //loop through elements in he
        for (String i : he)

        //loop through items in totalToken
        {   for (int n = 1; n < totalToken.size(); n++)

            //if i matches the token at index n
            {  if(i.equalsIgnoreCase(totalToken.get(n)))

                //add its neighbor (preceeding token) at index n-1
                prev.add(totalToken.get(n-1).toLowerCase()); } }


        ArrayList<ArrayList<String>> dict = new ArrayList<>();
        HashMap<String, Integer> countes = new HashMap<>();
        ArrayList<String> nextP = new ArrayList<>();

        //loop through items in prev
        for (String i : prev)

        //If tokenTags contains i as a key
        { if (tokenTags.containsKey(i))
        {
            //add the value of key i (the POS Tags)
            dict.add(tokenTags.get(i)); } }

            //loop through the arraylists in dict
        for (ArrayList<String> g : dict) {

            //loop through strings in that arraylists
            for (String h : g) {

                //add the strings to nextP
                nextP.add(h);
            }
        }

        //loop through Strings in nextP
        for (String i : nextP)

        //if i is a key of countes
        { if (countes.containsKey (i))
        {
            //put i and the count +1 (for each occurence of i) into countes
            countes.put(i, countes.get(i)+1); }
        else
            countes.put(i, 1);

        }

        //create an array z and transform the entries in m to elements in that array
        Object[] z = countes.entrySet( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( z, ( Object o8, Object o9 ) -> ( ( Map.Entry <String, Integer> ) o9 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o8 ).getValue ( ) ) );
        for ( Object e : z ) {

            //add all of the keys and their values to Arraylist tesi
            if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 && ((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1 )
                tesi.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text before" + " " + po + "\n" ) ;

            else if (((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1) tesi.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                   +  "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text before" + " " + po + "\n" ) ;
        }


        //reduce the size of tesi to get the first 5 elements
        if (tesi.size() > 4)
        {
            remo = tesi.subList(0, 5);  }

        //if there are no 5 elements, just return the list
        else remo = tesi;

        //store elements of remo in a String called result
        for (String e: remo) result += e;




        //getting Most likely to be following POS Tag
        //loop through elements in he
        for (String i : he)

        //loop through items in totalToken
        {   for (int n = 1; n < totalToken.size(); n++)

        //if i matches the token at index n
        {  if(i.equalsIgnoreCase(totalToken.get(n)))

            //add its neighbor (preceeding token) at index n+1
            next.add(totalToken.get(n+1).toLowerCase()); } }


        ArrayList<ArrayList<String>> dict2 = new ArrayList<>();
        HashMap<String, Integer> countes2 = new HashMap<>();
        ArrayList<String> nextP2 = new ArrayList<>();

        //loop through items in next
        for (String i : next)

        //If tokenTags contains i as a key
        { if (tokenTags.containsKey(i))
        {
            //add the value of key i (the POS Tags)
            dict2.add(tokenTags.get(i)); } }

        //loop through the arraylists in dict2
        for (ArrayList<String> t : dict2) {

            //loop through strings in that arraylists
            for (String h : t) {

                //add the strings to nextP2
                nextP2.add(h);
            }
        }

        //loop through Strings in nextP2
        for (String i : nextP2)

        //if i is a key of countes2
        { if (countes2.containsKey (i))
        {
            //put i and the count +1 (for each occurence of i) into countes2
            countes2.put(i, countes2.get(i)+1); }
        else
            countes2.put(i, 1);

        }

        //create an array r and transform the entries in countes2 to elements in that array
        Object[] r = countes2.entrySet( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( r, ( Object o10, Object o11 ) -> ( ( Map.Entry <String, Integer> ) o11 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o10 ).getValue ( ) ) );
        for ( Object e : r ) {

            //add all of the keys and their values to Arraylist tesim
            if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 && ((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1 )
                tesim.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text after" + " " + po + "\n" );

            else if (((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1) tesim.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                    +  "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text after" + " " +  po + "\n") ;
        }


        //reduce the size of tesi to get the first 5 elements
        if (tesim.size() > 4)
        {
            remom = tesim.subList(0, 5);  }

        //if there are no 5 elements, just return the list
        else remom = tesim;

        //store elements of remo in a String called result
        for (String e: remom) resultem += e;


        //return all of the reuslts (as a String)
        return "Frequency of that POS Tag:" + " " + formattedResult + "\n" + "Most frequent Tokens with that Tag:" + "\n" + resul + "\n"
                + "Most likely to be preceeding POS Tag" + "\n" + result  + "\n"
                + "Most likely to be following  POS Tag" + "\n" + resultem;

}
     

    public static void main(String[] args) throws Exception {
        try {
           // DataModelEd my =  new DataModelEd("https://en.wikipedia.org/wiki/The_Beatles", "Wikipedia");
          DataModelEd my =  new DataModelEd("exx.txt", "File");
       //     System.out.println("Lemma search results: ");
      //      System.out.println("Sentences that lemma occurs:\n" + my.getSentencesOfLemma("account"));
        //    System.out.println("Tokens of that lemma in it:\n" + my.getTokensOfLemma("account"));
      //      System.out.println("Tags of tokens of that lemma in it:\n" + my.getTagsOfLemma("account"));
      //      System.out.println("Token search results: ");
       //     System.out.println("Sentences that token occurs:\n" + my.getSentencesOfToken("account"));
         //   System.out.println("Tags of the token:\n" + my.getTagsOfToken("accounted"));
            //System.out.println("Tags of the token:\n"+ my.getSentencesOfLemma("account"));
            System.out.println(my.getNumOfAlllTags());
        
     

        } catch (IOException ex) {
            System.out.println("sdgdgadg");
           // Logger.getLogger(DataModelEd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

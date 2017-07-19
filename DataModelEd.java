
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
            
            for (int i = 0; i < lemmas.length; i++) { // <-- tim this is your update method. 
                if (lemmas[i].equals("O")) {
                    lemmas[i] = tokens[i];
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
                    String lem = lemmas[i].trim();
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
                    /*if (tokenTags.containsKey(thisToken)) {
                    if (!tokenTags.get(thisToken).contains(thisTag)) {
                    tokenTags.get(thisToken).add(thisTag);
                    }
                    } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(thisTag);
                    tokenTags.put(thisToken, list);
                    }*/
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
    
    public String getSentWithThisPOS_2(String pos){
        if (pos == null) {
            return null;
        }
       
        ArrayList<String[]> rsl = new ArrayList<>();
        for(Integer num : sentenceWithPOS.keySet()){
            for(String s : sentenceWithPOS.get(num)){
                if(pos.equalsIgnoreCase(s)){
                    
                    if(!rsl.contains(sentenceWithTokens.get(num)))
                        rsl.add(sentenceWithTokens.get(num));
                }
               
                    
            }
            
        }
        String val = "";
        int i = 1;
        for(String[] ar : rsl){
            val += i + ". ";
            for(String tok : ar){
                val += tok + " ";     
            }
            val+="\n\n";
            i++;    
        }
        return val;
    }
    
   
    public String getSentWithThisToken_2(String token){
        if (token == null) {
            return null;
        }
        ArrayList<String[]> rsl = new ArrayList<>();
        for(Integer num : sentenceWithTokens.keySet()){
            for(String s : sentenceWithTokens.get(num)){
                if(token.equalsIgnoreCase(s)){
                    if(!rsl.contains(sentenceWithTokens.get(num)))
                        rsl.add(sentenceWithTokens.get(num));
                }
                    
            }
        }
        String val = "";
        int i = 1;
        for(String[] ar : rsl){
            val += i + ". ";
            for(String tok : ar){
                val += tok + " ";
                
            }
            
            val+="\n\n";
            i++;    
        }
        return val;
    }
    
    public String getSentWithThisLemma_2(String lemma){
        if (lemma == null) {
            return null;
        }
        ArrayList<String[]> rsl = new ArrayList<>();
        for(Integer num : sentenceWithLemmas.keySet()){
            for(String s : sentenceWithLemmas.get(num)){
                if(lemma.equalsIgnoreCase(s)){
                    if(!rsl.contains(sentenceWithTokens.get(num)))
                        rsl.add(sentenceWithTokens.get(num));
                }
                    
            }
        }
        String val = "";
        int i = 1;
        for(String[] ar : rsl){
            val += i + ". ";
            for(String tok : ar){
                val += tok + " ";
                
            }
            val+="\n\n";
            i++;    
        } 
        return val;
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
                    + " pos tag(s):   " + tokenTags.get(key) + "   and its frequency is:  " + tokenFreq.get(key2) + "\n\n";
            i++;
        }
        
        return val;
    }
    public String showInformationForAllPOStags(){
        String val = "POS TAGS from this document match the following tokens: \n\n";
        int i = 1;
        for (String key : tagCluster.keySet()) {
            val += i + ". \"" + key + "\": matches " + tagCluster.get(key).size() 
                    + " token(s):   " + tagCluster.get(key) + "   and its frequency is:  " + tagFreq.get(key) 
                    + "  or " + String.format( "%.2f", getFrequencyOfTag(key) ) + "%\n\n";
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
        //String lem = getLemma(aWord);
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

        String token = "";
        String resu = "";
        String result = "";
        String resul = "";
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
        




        token = "Frequency of the keyword in the text:" + " " +  frequencyOfWord(aWord) + " " + "out of" + " " + numOfTokens();


        if (tokenTags.containsKey(aWord))

        tags = tokenTags.get(aWord);


        for (String temp : tags) {

        result += temp + " " + getFrequencyOfTag ( temp ) + "\n";
        formattedResult += temp + " " + result.format ( "%.02f", getFrequencyOfTag ( temp ) ) + "%" + " " + "out of the total number of POS" + "\n";

        }

        for (int i = 1; i < totalToken.size(); i++)
        {  if(aWord.equalsIgnoreCase(totalToken.get(i)))
        prevWord.add(totalToken.get(i-1).toLowerCase()); }

        HashMap<String, Integer> occurrences = new HashMap<>();

        for ( String word :prevWord) {
        Integer oldCount = occurrences.get(word);
        if ( oldCount == null ) {
        oldCount = 0;
        }
        occurrences.put(word, oldCount + 1);
        }

        Object[] b = occurrences.entrySet( ).toArray ( );
        Arrays.sort ( b, ( Object o2, Object o3 ) -> ( ( Map.Entry <String, Integer> ) o3 ).getValue ( )
        .compareTo ( ( ( Map.Entry <String, Integer> ) o2 ).getValue ( ) ) );
        for ( Object e : b ) {
        prec.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" );
        }
        
        if (prec.size() > 4)
        {res = prec.subList(0,5);}
        
        else res = prec;
        
        for (String e: res) resu += e;



        for (int i = 1; i < totalToken.size(); i++)
        {  if(aWord.equalsIgnoreCase(totalToken.get(i)))
        nextWord.add(totalToken.get(i+1).toLowerCase()); }

        HashMap<String, Integer> occur = new HashMap<>();

        for ( String word :nextWord) {
        Integer oldC = occur.get(word);
        if ( oldC == null ) {
        oldC = 0;
        }
        occur.put(word, oldC + 1);
        }

        Object[] t = occur.entrySet( ).toArray ( );
        Arrays.sort ( t, ( Object o4, Object o5 ) -> ( ( Map.Entry <String, Integer> ) o5 ).getValue ( )
        .compareTo ( ( ( Map.Entry <String, Integer> ) o4 ).getValue ( ) ) );
        for ( Object e : t ) {

        if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 )
        next.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" ) ;

        else next.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" ) ;
        }

         if (next.size() > 4)
        {re = next.subList(0,5);}
        
        else re = next;
        
        for (String e: re) resul += e;




        for (String e : prevWord)
        { if (tokenTags.containsKey(e))
        {
        dict.add(tokenTags.get(e)); } }

        for (ArrayList<String> g : dict) {

        for (String h : g) {
        hel.add(h);
        }
        }

        HashMap<String, Integer> counters = new HashMap<>();
        for (String i : hel)
        { if (counters.containsKey (i))
        {
        counters.put(i, counters.get(i)+1); }
        else
        counters.put(i, 1);

        }

        Object[] m = counters.entrySet( ).toArray ( );
        Arrays.sort ( m, ( Object o5, Object o6 ) -> ( ( Map.Entry <String, Integer> ) o6 ).getValue ( )
        .compareTo ( ( ( Map.Entry <String, Integer> ) o5 ).getValue ( ) ) );
        for ( Object e : m ) {

        if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 && ((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1 )
        tes.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" ) ;

        else if (((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1) tes.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" ) ;
        }
              if (tes.size() > 4)
        {rem = tes.subList(0,5);}
        
        else rem = tes;

        for (String e: rem) resulte += e;



        //Most Likely to be following Tag of the keyword:


        for (String e : nextWord)
        { if (tokenTags.containsKey(e))
        {
        dict2.add(tokenTags.get(e)); } }

        for (ArrayList<String> g : dict2) {

        for (String h : g) {
        nextP.add(h);
        }
        }

        HashMap<String, Integer> countes = new HashMap<>();
        for (String i : nextP)
        { if (countes.containsKey (i))
        {
        countes.put(i, countes.get(i)+1); }
        else
        countes.put(i, 1);

        }

        Object[] z = countes.entrySet( ).toArray ( );
        Arrays.sort ( z, ( Object o8, Object o9 ) -> ( ( Map.Entry <String, Integer> ) o9 ).getValue ( )
        .compareTo ( ( ( Map.Entry <String, Integer> ) o8 ).getValue ( ) ) );
        for ( Object e : z ) {

        if (((( Map.Entry <String, Integer> ) e ).getValue ( )) == 1 && ((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1 )
        tesi.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" ) ;

        else if (((( Map.Entry <String, Integer> ) e ).getKey ( )).length () > 1) tesi.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) +  ":" + " "
        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" ) ;
        }

         if (tesi.size() > 4)
        {remo = tesi.subList(0,5);}
        
        else remo = tesi;
        
     
        for (String e: remo) resulten += e;




        return token + "\n" + "\n" + "Most likely to be the preceding Token of the keyword:\n" + resu + "\n" +  "Most likely to be the following Token of the keyword:\n" + resul + "\n" +
        "Frequency of the POS tag of the keyword:\n" + formattedResult + "\n" +
        "Most likely to be preceeding Tag of the keyword (More than 1 character in length):" + "\n" + resulte
        + "\n" + "Most likely to be following Tag of this keyword (More than 1 character in length):" + "\n" + resulten;


}

    public static void main(String[] args) throws Exception {
        try {
           // DataModelEd my =  new DataModelEd("https://en.wikipedia.org/wiki/The_Beatles", "Wikipedia");
          DataModelEd my =  new DataModelEd("ex.txt", "File");
       //     System.out.println("Lemma search results: ");
      //      System.out.println("Sentences that lemma occurs:\n" + my.getSentencesOfLemma("account"));
        //    System.out.println("Tokens of that lemma in it:\n" + my.getTokensOfLemma("account"));
      //      System.out.println("Tags of tokens of that lemma in it:\n" + my.getTagsOfLemma("account"));
      //      System.out.println("Token search results: ");
       //     System.out.println("Sentences that token occurs:\n" + my.getSentencesOfToken("account"));
         //   System.out.println("Tags of the token:\n" + my.getTagsOfToken("accounted"));
            //System.out.println("Tags of the token:\n"+ my.getSentencesOfLemma("account"));
             System.out.println("tokens and their neighbours:\n"+ my.findPOSTagAndItsNeighbours("NN", 1,1));
     my.prinSentencesWithTokens(2);

        } catch (IOException ex) {
            System.out.println("sdgdgadg");
           // Logger.getLogger(DataModelEd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

import java.io.*;
import java.util.*;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.lemmatizer.*;


/**
 * DataStoreAndSearch - this is a data store for the linguistics stats from the open nlp tools
 * and also provides and populates additional instance variables to structure information in various ways
 * for easy retrieval.
 * The search process can be seen in documentWideStats(), statisticsOfToken(String aWord), statisticsOfPOS(String tag)
 * abd statisticsOfLemma(String lemma)
 * @authors Savvas Chatzipanagiotidis, Tim Day, Stella Lee, Mareile Winkler
 */

public class DataStoreAndSearch {

    //INSTANCE VARIABLES

    private String text;                                       // the input text as a single String
    private String[] sentences;                                // holds all segmented sentences from input text
    private HashMap<Integer, String[]> sentenceWithTokens;     // key: index of each sentence in sentences, value: list of posTags in that sentence
    private HashMap<Integer, String[]> sentenceWithPOS;        // for calculating lemmas. key: sentence index, value: list of lemmas
    private HashMap<Integer, String[]> sentenceWithLemmas;     // key: index of each sentence in sentences, value: list of lemmas in that sentence

    private ArrayList<String> sentenceListWithFormTOKEN_TAG;   // a list of sentences, where the sentences are in the form token_TAG

    private HashMap<String, Integer> tokenWithFreq;                // key: each token, val: the frequency in the text
    private HashMap<String, Integer> tagWithFreq;                  // key: each tag, val: the frequency in the text

    private HashMap<String, ArrayList<String>> tokenWithTags;      // key: each token, val: a list of all the tags occurring with that token
    private HashMap<String, ArrayList<String>> tagWithTokens;      // key: each tag, val: a list of every token that has that tag
    private HashMap<String, ArrayList<String>> lemmaWithSentences; // key: each lemma, val: list of sentences containing that lemma
    private HashMap<String, ArrayList<String>> lemmaWithTokens;    // key: each lemma, val: list of tokens with that lemma
    private HashMap<String, ArrayList<String>> lemmaWithTags;      // key: each lemma, val: list of tags relating to the tokens with that lemma

    private HashMap<String, ArrayList<String>> tokenWithSentences; // key: each token, val: sentences with that TOKEN in them
    private int numOfTokens;                                   // total count of tokens in the text
    private ArrayList<String> listAllTokens;                      // a list of all tokens in order of appearance

    
    /**
     * CONSTRUCTOR
     * @param inputText this will be modified depending on the user-selected text source (url, file, typed)
     * @param command   this is sent to the constructor depending
     * @throws IOException
     * @throws Exception general coverage for the multitude of exceptions that can come from the open nlp tools
     */
    public DataStoreAndSearch(String inputText, String command) throws IOException, Exception {

        // load text differently, depending on user input type
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

        text = text.replaceAll("\\[\\d+\\]", ""); // cleans up the text data. it had bracketed numbers

        //instantiate the instance variables
        sentenceWithTokens = new HashMap<>();
        sentenceWithPOS = new HashMap<>();
        sentenceWithLemmas = new HashMap<>();
        tokenWithTags = new HashMap<>();
        numOfTokens = 0;
        tokenWithFreq = new HashMap<>();
        tagWithTokens = new HashMap<>();
        tagWithFreq = new HashMap<>();
        lemmaWithTokens = new HashMap<>();
        sentenceListWithFormTOKEN_TAG = new ArrayList<>();
        lemmaWithSentences = new HashMap<>();
        lemmaWithTags = new HashMap<>();
        tokenWithSentences = new HashMap<>();
        listAllTokens = new ArrayList<>();


        // APACHE OPEN NLP

        // sentence segmenter
        InputStream senStream = new FileInputStream("en-sent.bin");
        SentenceModel sentenceModel = new SentenceModel(senStream);
        SentenceDetectorME detector = new SentenceDetectorME(sentenceModel);
        sentences = detector.sentDetect(text); //String[]. loads 'sentences' instance variable

        // tokenizer - set up
        InputStream tokStream = new FileInputStream("en-token.bin");
        TokenizerModel model = new TokenizerModel(tokStream);
        Tokenizer tokenizer = new TokenizerME(model);

        // POS tagger - set up
        InputStream tagStream = new FileInputStream("en-pos-maxent.bin");
        POSModel posModel = new POSModel(tagStream);//
        POSTaggerME tagger = new POSTaggerME(posModel);//use postaggerME -class

        // Lemmatizer - set up
        InputStream lemStream = new FileInputStream("en-lemmatizer.dict");
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(lemStream);


        //  FOR EACH SENTENCE
        for (int j = 0; j < sentences.length; j++) {

            sentences[j] = deleteMarks(sentences[j]); //todo
            String[] tokens = tokenizer.tokenize(sentences[j]);   // open nlp. generate tokens for this sentence
            String[] tags = tagger.tag(tokens);                   // open nlp. generate tags for this sentence
            String[] lemmas = lemmatizer.lemmatize(tokens, tags); // open nlp. generate lemmas for this sentence


            // DATA CLEANING.
            // this loop is for fixing case sensitivity of lemmas for a flexible lemma search
            // also removing erroneous quotation marks returned by the open nlp tools
            for (int i = 0; i < lemmas.length; i++) {

                lemmas[i] = lemmas[i].toLowerCase(); // all lemmas to lowercase for more flexible search

                if (lemmas[i].equals("o")) {
                    lemmas[i] = tokens[i];           // if no lemma exists, use the token
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

            // populate the instance variables for this sentence (sentence index is stored as key)
            sentenceWithTokens.put(j, tokens);
            sentenceWithPOS.put(j, tags);
            sentenceWithLemmas.put(j, lemmas);

            POSSample sample = new POSSample(tokens, tags);        // generates a version of this sentence in form token_tag
            sentenceListWithFormTOKEN_TAG.add(sample.toString());  // stores this underscored sentence

            
            // UPDATE 'frequencies' related instance variables

            if (tokens.length > 0) {                               // only for sentences with more than 0 tokens
                numOfTokens += tokens.length;                      // update num of posTags

                for (int i = 0; i < tokens.length; i++) {
                    String thisToken = tokens[i].trim();
                    String thisTag = tags[i].trim();
                    String lem = lemmas[i].trim().toLowerCase();
                    listAllTokens.add(thisToken);

                    if (tokenWithTags.containsKey(thisToken)) {
                        String frWord = thisToken.toLowerCase();          // for frequency -> not case sensitive
                        tokenWithFreq.put(frWord, tokenWithFreq.get(frWord) + 1); // increment by 1
                        if (!tokenWithTags.get(thisToken).contains(thisTag)) {
                            tokenWithTags.get(thisToken).add(thisTag);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>(); //create an ArrayList
                        list.add(thisTag);
                        tokenWithTags.put(thisToken, list);
                        String frWord = thisToken.toLowerCase();   // for frequency -> not case sensitive
                        tokenWithFreq.put(frWord, 1);
                    }
                    
                    if (tokenWithSentences.containsKey(thisToken)) {
                        if (!tokenWithSentences.get(thisToken).contains(sentences[j])) {
                            tokenWithSentences.get(thisToken).add(sentences[j]);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(sentences[j]);
                        tokenWithSentences.put(thisToken, list);
                    }
                    
                    if (lemmaWithSentences.containsKey(lem)) {
                        if (!lemmaWithSentences.get(lem).contains(sentences[j])) {
                            lemmaWithSentences.get(lem).add(sentences[j]);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(sentences[j]);
                        lemmaWithSentences.put(lem, list);
                    }

                    if (lemmaWithTags.containsKey(lem)) {
                        if (!lemmaWithTags.get(lem).contains(thisTag)) {
                            lemmaWithTags.get(lem).add(thisTag.toLowerCase());
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(thisTag);
                        lemmaWithTags.put(lem, list);
                    }
                    
                    /*
                    fill the tagWithTokens and Tagfrep HashMaps
                    tagWithTokens--> the tag  is the KEY and the VALUE is an ArrayList that holdes all
                    words in this text having this tag
                    tagfreq --> the tag is the KEY and the Value is the key's frequency in this text
                     */
                    if (tagWithTokens.containsKey(thisTag)) {
                        tagWithFreq.put(thisTag, tagWithFreq.get(thisTag) + 1);
                        if (!tagWithTokens.get(thisTag).contains(thisToken)) {
                            tagWithTokens.get(thisTag).add(thisToken);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>(); //create a ArrayList
                        list.add(thisToken);
                        tagWithTokens.put(thisTag, list);
                        tagWithFreq.put(thisTag, 1);
                    }

                    /*fill the lemmaTokens HashMap
                    lemmaTokens--> the lemma  is the KEY and the VALUE is an ArrayList that holdes all
                    words that have this lemma in this text
                     */
                    if (lemmaWithTokens.containsKey(lem)) {
                        if (!lemmaWithTokens.get(lem).contains(thisToken)) {
                            lemmaWithTokens.get(lem).add(thisToken);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(thisToken);
                        lemmaWithTokens.put(lem, list);
                    }
                }
            }
            
            // close the open NLP streams
            senStream.close();
            tokStream.close();
            tagStream.close();
            lemStream.close();
        }
    }

    /*
     * HELPER method that read the text from a file
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
        br.close(); // close stream
        return text;
    }

    /*
     * HELPER METHOD
     * delete the parks (but not the panctuation ones)
     * @Param word
     * @return
     */
    private static String deleteMarks(String word) {

        word = word.replace("(", "");
        word = word.replace(")", "");
        word = word.replace("»", "");
        word = word.replace("«", "");
        word = word.replace("-", "");
        word = word.replace("\"", "");
        word = word.replace("[", "");
        word = word.replace("]", "");
        word = word.replace("\"\"", "");
        word = word.replace("'", "");
        word = word.replace("\\", "");
        word = word.replace("'", "");
        word = word.replace("'", "");
        return word;
    }




    // GETTER METHODS
    
    /**
     * GET lemmaWithSentences
     * for finding the sentences that contain a certain lemma
     * @return a hash map with lemmas as keys, and a list of sentences that contain each lemma as value
     */
    public HashMap<String, ArrayList<String>> getLemmaWithSentences(){
        return lemmaWithSentences;
    }


    /**
     * GET all lemmas from sentenceWithLemma
     * goes through each sentence
     * @return a alphabetically sorted list of all lemmas in the document
     */
    public ArrayList<String> getLemmasAlphabetically(){
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
     * GET all tokens from sentenceWithTokens
     * @return an alphabetically sorted list of all tokens in the document
     */
    public ArrayList<String> getTokensAlphabetically(){
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
     * GET
     * @return the hash map token with tags
     */
    public HashMap<String, ArrayList<String>> getTokenTags(){
        return tokenWithTags;
    }

    /**
     * GET
     * @return the hash map tag with tokens
     */
    public HashMap<String, ArrayList<String>> getTagWithTokens(){
        return tagWithTokens;
    }

    /**
     * GET
     * @return the hash map sentence with tokens
     */
    public HashMap<Integer, String[]> getSentenceWithTokens(){
        return sentenceWithTokens;
    }

    /**
     * GET
     * @return the hash map sentence with tags
     */
    public HashMap<Integer, String[]> getSentenceWithPOS(){
        return sentenceWithPOS;
    }

    /**
     * GET
     * @return the hash map sentence with lemmas
     */
    public HashMap<Integer, String[]> getSentenceWithLemmas(){
        return sentenceWithLemmas;
    }


    /**
     * GET
     * @param token
     * @return a string, that lists (for display) all of the tags that appear with this token in the document
     */
    public String getTagsOfToken(String token) {
        if (token == null) {
            return token;
        }
        String tags = "";
        if (!tokenWithTags.containsKey(token)) {
            return "key word \"" + token + "\" does not exist.";
        } else {
            for (String tag : tokenWithTags.get(token)) {
                tags += tag + "\n";
            }
        }
        return tags;
    }

    /**
     * GET
     * @return a String, that lists all the POS tags that exist in the document
     */
    public String getInformationForAlltokens(){
        String val = "Tokens from this document have the following pos tags: \n\n";
        int i = 1;
        for (String key : tokenWithTags.keySet()) {
            String key2 = key.toLowerCase();
            val += i + ". \"" + key + "\": has " + tokenWithTags.get(key).size()
                    + " POS tag(s) and its frequency is:  " + tokenWithFreq.get(key2) + " or " +
                    String.format("%.2f", getFrequencyOfToken(key2)) + "% -> " +  tokenWithTags.get(key) +"\n\n";
            i++;
        }
        return val;
    }

    /**
     * GET
     * @return a String, that lists the info for each pos tag
     */
    public String showInformationForAllPOStags(){
        String val = "POS TAGS from this document match the following tokens: \n\n";
        int i = 1;
        for (String key : tagWithTokens.keySet()) {
            val += i + ". \"" + key + "\": matches " + tagWithTokens.get(key).size()
                    + " token(s) and its frequency is:  " + tagWithFreq.get(key)
                    + "  or " + String.format( "%.2f", getFrequencyOfTag(key) ) + "%  -> " + tagWithTokens.get(key) +  "\n\n";
            i++;//String.format( "%.2f", dub )
        }

        return val;
    }

    /**
     * GET method
     * @return the number of tokens (or lemmas or tags, they all match) in this text
     */
    public int numOfTokens() {
        return numOfTokens;
    }

    
    /**
     * GET the frequencyOfWord
     * @param aWord
     * @return the frequency of this word (not case sensitive)
     */
    public int frequencyOfWord(String aWord) {
        if (aWord == null) {
            return -1;
        }
        aWord = aWord.toLowerCase();
        if (tokenWithFreq.containsKey(aWord)) { //if the word is in the text ...
            return tokenWithFreq.get(aWord);//... return its frequency
        } else {
            return -1;//
        }
    }

    /**
     * GET the lemma of a given word
     * @param word the word we want the lemma for
     * @return the lemma of word
     */
    public String getLemma(String word) {

        if (word == null) {  // if null...
            return word;     //...terminate the method
        }
        String val = "";
        for (String e : lemmaWithTokens.keySet()) {
            if (lemmaWithTokens.get(e).contains(word)) {
                val = e;
            }
        }
        return val;
    }


    /**
     * GET 
     * used in the GUI for special document reformat
     * @return a string of the whole document, written in form token_tag
     * example: I_PRP have_VB...
     */
    public String getTextWithFormTOKEN_TAG() {
        String val = "     Parsed sentences:\n";
        int i = 1;
        for (String sent : sentenceListWithFormTOKEN_TAG) {
            val += i + ". " +  sent + "\n\n";
            // System.out.println(sent);
            i++;
        }
        return val;
    }



    /**
     * GET the frequency of a token (as a percentage)
     * @param tok - the token we want the frequency of
     * @return double. a percentage value for the frequency
     * -1 if the token is not in tet
     */
    public double getFrequencyOfToken(String tok) {
        if (tok == null) {
            return -1;
        }

        if (tokenWithFreq.containsKey(tok)) {
            double val;
            val = ((double) tokenWithFreq.get(tok) * 100.0) / (double) numOfTokens;

            return val;

        } else {
            return -1;
        }
    }


    /**
     * GET
     * frequency of a tag in the text as a percentage
     * @param tag - a tag that occurs in the text
     * @return double. the frequency as percentage of this tag in this text
     * returns -1 if tag does not appear in text
     */
    public double getFrequencyOfTag(String tag) { //it is not case sensitive
        if (tag == null) {
            return -1;
        }
        tag = tag.toUpperCase();
        if (tagWithFreq.containsKey(tag)) {
            double val;
            val = ((double) tagWithFreq.get(tag) * 100.0) / (double) numOfTokens();

            return val;
        } else {
            return -1;
        }
    }



    /**
     * Analyse the Document for relevant statistics
     * (to be displayed in the bottom right window of the GUI, upon document load)
     * @return a String summary of the stats, for import to the GUI
     * @author Mareile Winkler
     */
    public String documentWideStats() {

        String result = "";
        String result2 = "";
        String totalNumberofTokens = "Total Token Count:" + " " + numOfTokens();
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> tokenList = new ArrayList<>();


        Object[] a = tagWithFreq.entrySet ( ).toArray ( );
        Arrays.sort ( a, ( o1, o2 ) -> ( ( Map.Entry <String, Integer> ) o2 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o1 ).getValue ( ) ) );
        for ( Object e : a ) {

            list.add( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + " "
                    + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times" + "\n" );
        }

        for ( int i = 0 ; i < 5 ; i++ ) {

            result += list.get ( i );}

        Object[] b = tokenWithFreq.entrySet ( ).toArray ( );
        Arrays.sort ( b, ( Object o2, Object o3 ) -> ( ( Map.Entry <String, Integer> ) o3 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o2 ).getValue ( ) ) );
        for ( Object e : b ) {
            if ((( ( Map.Entry <String, Integer> ) e ).getKey( )).length() > 3)
                tokenList.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + " "
                        + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times" + "\n" );
        }

        for ( int i = 0 ; i < 5 ; i++ ) result2 += tokenList.get ( i );

        return "\n" + totalNumberofTokens + "\n" + "\n" + "Top 5 POS Tags:" + "\n" + result + "\n" + "Top 5 Tokens (> 3 letters):" + "\n" + result2;
    }



    /**
     * Generate the relevant statistics of a Token search of the document
     * (to be displayed in the top right window of the GUI, after a Token search)
     * @param aWord - the token to search for
     * @return a String summary of the token search statistics, ready for GUI importing
     * @author Mareile Winkler
     */
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

        freqOfToken = "Token Density:" + " " +  frequencyOfWord(aWord) + " " + "out of" + " " + numOfTokens() + " tokens";
        
        //frequency of POS Tag of that Token:

        //if the tokenTag HashMap contains the key aWord
        if (tokenWithTags.containsKey(aWord))


            //store the value of that key in the variable tags
            tags = tokenWithTags.get(aWord);

        //loop through the items in tags
        for (String temp : tags) {

            //add the frequeny of the tag to the String variable result
            result += temp + " " + getFrequencyOfTag ( temp ) + "\n";

            //format result so that it can be displayed with only 2 decimals
            formattedResult += temp + " " + result.format ( "%.02f", getFrequencyOfTag ( temp ) ) + "%" + " " + "out of the total number of POS" + "\n";

        }

        //getting the most likely to be preceding token
        //loop through the totalToken List
        for (int i = 1; i < listAllTokens.size(); i++)

        //if the keyword is in the list
        {  if(aWord.equalsIgnoreCase(listAllTokens.get(i)))

            //add the token with one lower index
            prevWord.add(listAllTokens.get(i-1).toLowerCase()); }

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
        for (int i = 1; i < listAllTokens.size(); i++)

        //if aWord is found in totaltoken
        {  if(aWord.equalsIgnoreCase(listAllTokens.get(i)))

            //add the next word (index +1)to the list nextWord
            nextWord.add(listAllTokens.get(i+1).toLowerCase()); }


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
        { if (tokenWithTags.containsKey(e)) {
            //get the value (the POS Tag) of e and store it in dict
            dict.add(tokenWithTags.get(e)); } }

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
        for (String i : hel) {
            if (counters.containsKey (i)) {  //if there is the key i in counters
            counters.put(i, counters.get(i)+1); } //for each occurence of i - add 1 to the count
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
        { if (tokenWithTags.containsKey(e))
        {
            //add the value (the POS Tag) of e to dict2
            dict2.add(tokenWithTags.get(e)); } }

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
        return freqOfToken + "\n" + "\n" + "Most common Token preceding keyword:\n" + resu + "\n" +  "Most common Token following keyword:\n" + resul + "\n" +
                "Frequency of the POS tag of the keyword:\n" + formattedResult + "\n" +
                "Most common tag preceeding keyword (not including symbols):" + "\n" + resulte
                + "\n" + "Most common tag following keyword (not including symbols):" + "\n" + resulten;

    }


    /**
     * Generate the relevant statistics of a LEMMA search of the document
     * (to be displayed in the top right window of the GUI, after a lemma search)
     * @param lemma - the lemma to search for
     * @return a String summary of the lemma search statistics, ready for GUI importing
     * @author Mareile Winkler
     */
    public String statisticsOfLemma(String lemma) {

        String result = "";
        String t = "";
        ArrayList<String> he = new ArrayList <> ( );

        he = lemmaWithTokens.get(lemma);
        ArrayList<ArrayList<String>> dict2 = new ArrayList<>();
        ArrayList<String> nextP2 = new ArrayList<>();
        String result2 = "";
        ArrayList<String> nextP = new ArrayList<>();


        //loop through items in next
        for (String i : he)


            //If tokenTags contains i as a key
            if (tokenWithTags.containsKey(i))
            {
                //add the value of key i (the POS Tags)
                dict2.add(tokenWithTags.get(i)); }


        //loop through the arraylists in dict2
        for (ArrayList<String> m : dict2) {

            //loop through strings in that arraylists
            for (String h : m) {

                //add the strings to nextP2
                nextP2.add(h);
            }
        }

        for (String n : nextP2) {
            result += n + " ";

        }

        ArrayList<ArrayList<String>> dict = new ArrayList<>();

        //loop through items in next
        for (String i : he) {

            String r = getTagsOfToken(i);
            result2 += i + " " + "appeared" + " " + frequencyOfWord ( i ) + " " + "times in the text" + "\n" + r;
        }
        return "POS Tags with that lemma:" + "\n"+ result + "\n" +"\n" + "Tokens with that lemma:" + "\n" + result2;
    }



    /**
     * Generate the relevant statistics of a POS tag serach of the document
     * (to be displayed in the top right window of the GUI, after a pos tag search)
     * @param po - the pos tag to search for
     * @return a String summary of the POS tag search statistics, ready for GUI importing
     * @author Mareile Winkler
     */
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
        //Store the values of the tagWithTokens Map into he
        he = tagWithTokens.get(po);

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
        {   for (int n = 1; n < listAllTokens.size(); n++)

        //if i matches the token at index n
        {  if(i.equalsIgnoreCase(listAllTokens.get(n)))

            //add its neighbor (preceeding token) at index n-1
            prev.add(listAllTokens.get(n-1).toLowerCase()); } }


        ArrayList<ArrayList<String>> dict = new ArrayList<>();
        HashMap<String, Integer> countes = new HashMap<>();
        ArrayList<String> nextP = new ArrayList<>();

        //loop through items in prev
        for (String i : prev)

        //If tokenTags contains i as a key
        { if (tokenWithTags.containsKey(i))
        {
            //add the value of key i (the POS Tags)
            dict.add(tokenWithTags.get(i)); } }

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
        {   for (int n = 1; n < listAllTokens.size(); n++)

        //if i matches the token at index n
        {  if(i.equalsIgnoreCase(listAllTokens.get(n)))

            //add its neighbor (preceeding token) at index n+1
            next.add(listAllTokens.get(n+1).toLowerCase()); } }


        ArrayList<ArrayList<String>> dict2 = new ArrayList<>();
        HashMap<String, Integer> countes2 = new HashMap<>();
        ArrayList<String> nextP2 = new ArrayList<>();

        //loop through items in next
        for (String i : next)

        //If tokenTags contains i as a key
        { if (tokenWithTags.containsKey(i))
        {
            //add the value of key i (the POS Tags)
            dict2.add(tokenWithTags.get(i)); } }

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
}

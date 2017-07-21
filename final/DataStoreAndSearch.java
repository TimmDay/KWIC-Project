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
     * @param aToken - the token to search for
     * @return a String summary of the token search statistics, ready for GUI importing
     * @author Mareile Winkler
     */

    public String statisticsOfToken( String aToken ) {

        //results
        String freqOfToken = "";                                        //used in the final return String
        String FrequencyOfPOSResult = "";                               //used in the final return String
        String formattedPOSResult = "";
        List <String> InterimResultPreceeding = new ArrayList <> ( );
        String preceedingTokensResult = "";                             //used in the final return String
        List <String> InterimResultFollowing = new ArrayList <> ( );
        String followingTokensResult = "";                              //used in the final return String
        List <String> InterimResultPreceedingPOS = new ArrayList <> ( );
        String preceedingPOSResult = "";                                //used in the final return String
        List <String> InterimResultFollowingPoS = new ArrayList <> ( );
        String followingPOSResult = "";                                 //used in the final return String

        ArrayList <String> tagsOfTheToken = new ArrayList <String> ( );
        ArrayList <String> prevToken = new ArrayList <> ( );
        ArrayList <String> sortedPreceedings = new ArrayList <> ( );
        ArrayList <String> nextToken = new ArrayList <> ( );
        ArrayList <String> sortedFollowing = new ArrayList <> ( );
        ArrayList <ArrayList <String>> POSTagsOfPrev = new ArrayList <> ( );
        ArrayList <String> SingletagOfPrev = new ArrayList <> ( );
        ArrayList <String> sortedPreceedingPOS = new ArrayList <> ( );
        ArrayList <ArrayList <String>> POSTagsOfFoll = new ArrayList <> ( );
        ArrayList <String> SingleTagOfNext = new ArrayList <> ( );
        ArrayList <String> sortedFollowingPOS = new ArrayList <> ( );


        // get the frequency of the token in the text using frequencyOfWord and numOfTokens

        freqOfToken = "Token Density:" + " " + frequencyOfWord ( aToken ) + " " + "out of" + " " + numOfTokens ( ) + " tokens";

        //frequency of POS Tag of that Token:

        //if the tokenTag HashMap contains the key aWord
        if (tokenWithTags.containsKey ( aToken ))


            //store the value of that key in the ArrayList tagsOfTheToken
            tagsOfTheToken = tokenWithTags.get ( aToken );

        //loop through the items in tags
        for ( String tags : tagsOfTheToken ) {

            //add the frequeny of the tag to the String variable FrequencyOfPOSResult
            FrequencyOfPOSResult += tags + " " + getFrequencyOfTag ( tags ) + "\n";

            //format result so that it can be displayed with only 2 decimals
            formattedPOSResult += tags + " " + FrequencyOfPOSResult.format ( "%.02f", getFrequencyOfTag ( tags ) ) + "%" + " " + "out of the total number of POS" + "\n";

        }

        //getting the most likely to be preceding token
        //loop through the totalToken List
        for ( int i = 1 ; i < listAllTokens.size ( ) ; i++ )

        //if the keyword is in the list
        {
            if (aToken.equalsIgnoreCase ( listAllTokens.get ( i ) ))

                //add the token with one lower index to prevToken
                prevToken.add ( listAllTokens.get (i-1).toLowerCase ( ) );
        }

        //See how often the tokens occur
        //create a new HashMap
        HashMap <String, Integer> occurrencesOfPerceeding = new HashMap <> ( );

        //loop through items in prevToken
        for ( String word : prevToken ) {

            //create Integer oldCount to get the count of the number of times keyword appears in the text
            Integer oldCount = occurrencesOfPerceeding.get ( word );

            //if the keyword appears for the first time, set oldCount to 0
            if (oldCount == null) {
                oldCount = 0;
            }

            //for each occurence, add 1 to the count
            occurrencesOfPerceeding.put ( word, oldCount + 1 );
        }

        //Sort preceeding tokens by the highest number of appearance
        //create an array and transform the entries in occurences to elements in that array
        Object[] b = occurrencesOfPerceeding.entrySet ( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( b, ( Object o2, Object o3 ) -> ( ( Map.Entry <String, Integer> ) o3 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o2 ).getValue ( ) ) );

        //add all of the keys and their values to Arraylist sortedPreceedings
        for ( Object e : b ) {
            sortedPreceedings.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                    + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" );
        }

        //reduce the size of sortedPreceedings to get the first 5 elements
        if (sortedPreceedings.size ( ) > 4) {
            InterimResultPreceeding = sortedPreceedings.subList ( 0, 5 );
        }

        //if there are no 5 elements, just return the list
        else InterimResultPreceeding = sortedPreceedings;

        //put all of the elements in res into a string called preceedingTokensResult
        for ( String e : InterimResultPreceeding ) preceedingTokensResult += e;


        //getting the most likely to be following token
        //loop through listAllTokens
        for ( int i = 1 ; i < listAllTokens.size ( ) ; i++ )

        //if aWord is found in listAllTokens
        {
            if (aToken.equalsIgnoreCase ( listAllTokens.get ( i ) ))

                //add the next word (index +1)to the list nextToken
                nextToken.add ( listAllTokens.get ( i + 1 ));
        }


        //create a new hashmap NextTokenOccurence
        HashMap <String, Integer> NextTokenOccurence = new HashMap <> ( );

        //loop through items in nextToken
        for ( String word : nextToken ) {

            //create Integer Nextcount to get the count of the number of times keyword appears in the text
            Integer Nextcount = NextTokenOccurence.get ( word );

            //if the keyword appears for the first time, set Nextcount to 0
            if (Nextcount == null) {
                Nextcount = 0;
            }

            //for each occurence, add 1 to the count
            NextTokenOccurence.put ( word, Nextcount + 1 );
        }

        //create an array and transform the entries in NextTokenOccurence to elements in that array
        Object[] t = NextTokenOccurence.entrySet ( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( t, ( Object o4, Object o5 ) -> ( ( Map.Entry <String, Integer> ) o5 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o4 ).getValue ( ) ) );


        //add all of the keys and their values to Arraylist sortedFollowing
        for ( Object e : t ) {

            if (( ( ( Map.Entry <String, Integer> ) e ).getValue ( ) ) == 1)
                sortedFollowing.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" );

            else sortedFollowing.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                    + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" );
        }


        //reduce the size of sortedFollowing to get the first 5 elements
        if (sortedFollowing.size ( ) > 4)

        {
            InterimResultFollowing = sortedFollowing.subList ( 0, 5 );
        }

        //if there are no 5 elements, just return the list

        else InterimResultFollowing = sortedFollowing;

        //put all of the elements in res into a string called followingTokenResult

        for ( String e : InterimResultFollowing ) followingTokensResult += e;


        //getting the preceeding POS Tag of that token

        //loop through prevToken
        for ( String e : prevToken )

        //if tokenWithTags contains the key e
        {
            if (tokenWithTags.containsKey ( e )) {
                //get the value (the POS Tags) of e and store the arrayLists containing the POS Tags
                POSTagsOfPrev.add ( tokenWithTags.get ( e ) );
            }
        }

        //loop through the arraylists in POSTagsOfPrev
        for ( ArrayList <String> tags : POSTagsOfPrev ) {

            //add the strings in the array lists to SingletagOfPrev
            for ( String tag : tags ) {
                SingletagOfPrev.add ( tag );
            }
        }

        //create a new hashmap prevTagCoung
        HashMap <String, Integer> prevTagCount = new HashMap <> ( );

        //loop through the strings in SingletagOfPrev
        for ( String i : SingletagOfPrev )

        //if there is the key i in prevTagCoung
        {
            if (prevTagCount.containsKey ( i )) {
                //for each occurence of i - add 1 to the count
                prevTagCount.put( i, prevTagCount.get ( i ) + 1 );

            } else
                prevTagCount.put ( i, 1 );

        }

        //create an array and transform the entries in prevTagCount to elements in that array
        Object[] m = prevTagCount.entrySet ( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( m, ( Object o5, Object o6 ) -> ( ( Map.Entry <String, Integer> ) o6 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o5 ).getValue ( ) ) );
        for ( Object e : m ) {

            //add all of the keys and their values to Arraylist sortedPreceedingPOS
            if (( ( ( Map.Entry <String, Integer> ) e ).getValue ( ) ) == 1 && ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                sortedPreceedingPOS.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" );

            else if (( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                sortedPreceedingPOS.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" );
        }

        //reduce the size of sortedPreceedingPOS to get the first 5 elements
        if (sortedPreceedingPOS.size ( ) > 4) {
            InterimResultPreceedingPOS = sortedPreceedingPOS.subList ( 0, 5 );
        }

        //if there are no 5 elements, just return the list
        else InterimResultPreceedingPOS = sortedPreceedingPOS;


        //store elements of InterimResultPreceedingPOS in a String called preceedingPOSResult
        for ( String e : InterimResultPreceedingPOS ) preceedingPOSResult += e;


        //Most Likely to be following Tag of the keyword:

        //loop through nextToken
        for ( String e : nextToken )

        //if tokenWithTags contains the key e
        {
            if (tokenWithTags.containsKey ( e )) {
                //add the value (the POS Tag) of e to POSTagsOfFoll
                POSTagsOfFoll.add ( tokenWithTags.get ( e ) );
            }
        }

        //loop through the ArrayLists in POSTagsOfFoll
        for ( ArrayList <String> tags : POSTagsOfFoll ) {

            //add the strings in the ArrayLists to SingleTagOfNext
            for ( String tag : tags ) {
                SingleTagOfNext.add ( tag );
            }
        }

        //create new Hash Map countOfFollowingPOS
        HashMap <String, Integer> countOfFollowingPOS = new HashMap <> ( );

        //loop through SingleTagOfNext
        for ( String i : SingleTagOfNext )

        //if countOfFollowingPOS contains the key i
        {
            if (countOfFollowingPOS.containsKey ( i )) {
                //add 1 to count for each occurence of i
                countOfFollowingPOS.put ( i, countOfFollowingPOS.get ( i ) + 1 );
            } else
                countOfFollowingPOS.put ( i, 1 );

        }

        //create an array and transform the entries in countOfFollowingPOS to elements in that array
        Object[] z = countOfFollowingPOS.entrySet ( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( z, ( Object o8, Object o9 ) -> ( ( Map.Entry <String, Integer> ) o9 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o8 ).getValue ( ) ) );
        for ( Object e : z ) {

            //add all of the keys and their values to Arraylist sortedFollowingPOS
            if (( ( ( Map.Entry <String, Integer> ) e ).getValue ( ) ) == 1 && ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                sortedFollowingPOS.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" );

            else if (( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                sortedFollowingPOS.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" );
        }

        //reduce the size of sortedFollowingPOS to get the first 5 elements
        if (sortedFollowingPOS.size ( ) > 4) {
            InterimResultFollowingPoS = sortedFollowingPOS.subList ( 0, 5 );
        }

        //if there are no 5 elements, just return the list
        else InterimResultFollowingPoS = sortedFollowingPOS;

        //store elements of InterimResultFollowingPOS in a String called followingPOSREsult
        for ( String e : InterimResultFollowingPoS ) followingPOSResult += e;


        //return the final string with all the infos
        return freqOfToken + "\n" + "\n" + "Most common Token preceding keyword:\n" + preceedingTokensResult + "\n" + "Most common Token following keyword:\n" + followingTokensResult + "\n" +
                "Frequency of the POS tag of the keyword:\n" + formattedPOSResult + "\n" +
                "Most common tag preceeding keyword (not including symbols):" + "\n" + preceedingPOSResult
                + "\n" + "Most common tag following keyword (not including symbols):" + "\n" + followingPOSResult;

    }

    /**
     * Generate the relevant statistics of a LEMMA search of the document
     * (to be displayed in the top right window of the GUI, after a lemma search)
     * @param lemma - the lemma to search for
     * @return a String summary of the lemma search statistics, ready for GUI importing
     * @author Mareile Winkler
     */

    public String statisticsOfLemma( String lemma ) {


        ArrayList <String> TokensOfLemma = new ArrayList <> ( );
        ArrayList <ArrayList <String>> TagsOfLemma = new ArrayList <> ( );
        ArrayList <String> SingleTags = new ArrayList <> ( );
        String POSTagresult = "";
        String result2 = "";


        //load tokensOfLemma with the values of lemmaWithtokens
        TokensOfLemma = lemmaWithTokens.get ( lemma );

        //loop through items in TokensOfLemma
        for ( String token : TokensOfLemma )


            //If tokenWithTags contains i as a key
            if (tokenWithTags.containsKey ( token )) {
                //add the value of key i (the POS Tags)
                TagsOfLemma.add ( tokenWithTags.get ( token ) );
            }


        //loop through the arraylists in TagsOfLemma
        for ( ArrayList <String> tags : TagsOfLemma ) {

            //loop through strings in that arraylists
            for ( String tag : tags ) {

                //add the strings to SingleTags
                SingleTags.add ( tag );
            }
        }

        //loop through the Strings in SingleTags and and it to the POSTagresult
        for ( String n : SingleTags ) {
            POSTagresult += n + " ";

        }


        //loop through items in TokensOfLemma
        for ( String i : TokensOfLemma ) {

            String tags = getTagsOfToken ( i );
            result2 += i + " " + "appeared" + " " + frequencyOfWord ( i ) + " " + "times in the text" + "\n" + tags;
        }

        return "POS Tags with that lemma:" + "\n" + POSTagresult + "\n" + "\n" + "Tokens with that lemma:" + "\n" + result2;
    }

    /**
     * Generate the relevant statistics of a POS tag serach of the document
     * (to be displayed in the top right window of the GUI, after a pos tag search)
     * @param aTag - the pos tag to search for
     * @return a String summary of the POS tag search statistics, ready for GUI importing
     * @author Mareile Winkler
     */

    public String statisticsOfPOS( String aTag ) {

        //instance variables

        ArrayList <String> tokens = new ArrayList <> ( );
        HashMap <String, Integer> FrequencyOfToken = new HashMap <> ( );
        ArrayList <String> sortedFrequencyOfToken = new ArrayList <> ( );
        List <String> InterimResultFrequencyToken = new ArrayList <> ( );
        String resultToken = "";
        ArrayList <String> prevToken = new ArrayList <> ( );
        ArrayList <String> SortedPrev = new ArrayList <> ( );
        ArrayList <String> nextToken = new ArrayList <> ( );
        List <String> InterimResultPrev = new ArrayList <> ( );
        String resultPrev = "";
        ArrayList <String> SortedFoll = new ArrayList <> ( );
        List <String> InterimResultFoll = new ArrayList <> ( );
        String resultFoll = "";


        //getting frequency of the Tag and formatting it, so it has only 2 decimals
        String frequencyOfPOS = getFrequencyOfTag ( aTag ) + "\n";
        String formattedResultFrequency = frequencyOfPOS.format ( "%.02f", getFrequencyOfTag ( aTag ) ) + "%" + " " + "out of the total number of POS" + "\n";

        //Get most frequent Tokens with that Tag:
        //Store the values of the tagWithTokens Map into ArrayList tokens
        tokens = tagWithTokens.get ( aTag );

        //loop through Strings in tokens
        for ( String e : tokens ) {
            //put e as the key and the frequency of e as the value of hashmap FrequencyOfWord
            FrequencyOfToken.put ( e, frequencyOfWord ( e ) );

        }

        //create an array l and transform the entries in FrequencyOfToken to elements in that array
        Object[] l = FrequencyOfToken.entrySet ( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( l, ( Object o10, Object o11 ) -> ( ( Map.Entry <String, Integer> ) o11 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o10 ).getValue ( ) ) );
        for ( Object e : l ) {


            //add all of the keys and their values to Arraylist sortedFrequencyOfToken
            if (( ( ( Map.Entry <String, Integer> ) e ).getValue ( ) ) == 1 && ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                sortedFrequencyOfToken.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text" + "\n" );

            else if (( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                sortedFrequencyOfToken.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text" + "\n" );
        }

        //reduce the size of sortedFrequencyOfToken to get the first 5 elements
        if (sortedFrequencyOfToken.size ( ) > 4) {

            InterimResultFrequencyToken = sortedFrequencyOfToken.subList ( 0, 5 );
        }

        //if there are no 5 elements, just return the list
        else InterimResultFrequencyToken = sortedFrequencyOfToken;

        //store elements of k in a String called resultToken
        for ( String e : InterimResultFrequencyToken ) resultToken += e;


        //getting Most likely to be preceeding POS Tag
        //loop through elements in tokens
        for ( String i : tokens )

        //loop through items in listAllTokens
        {
            for ( int n = 1 ; n < listAllTokens.size ( ) ; n++ )

            //if i matches the token at index n
            {
                if (i.equalsIgnoreCase ( listAllTokens.get ( n ) ))

                    //add its neighbor (preceeding token) at index n-1
                    prevToken.add ( listAllTokens.get ( n - 1 ).toLowerCase ( ) );
            }
        }


        ArrayList <ArrayList <String>> POSTagofPrev = new ArrayList <> ( );
        HashMap <String, Integer> countPrev = new HashMap <> ( );
        ArrayList <String> SingleTagPrev = new ArrayList <> ( );

        //loop through items in prevToken
        for ( String i : prevToken )

        //If tokenWithTags contains i as a key
        {
            if (tokenWithTags.containsKey ( i )) {
                //add the value of key i (the POS Tags) to POSTagsofPrev
                POSTagofPrev.add ( tokenWithTags.get ( i ) );
            }
        }

        //loop through the arraylists in POSTagsOfPrev
        for ( ArrayList <String> tags : POSTagofPrev ) {

            //loop through strings in that arraylist
            for ( String tag : tags ) {

                //add the strings to SingleTagPrev
                SingleTagPrev.add ( tag );
            }
        }

        //loop through Strings in SingleTagPrev
        for ( String i : SingleTagPrev )

        //if i is a key of countPrev
        {
            if (countPrev.containsKey ( i )) {
                //put i and the count +1 (for each occurence of i) into countPrev
                countPrev.put ( i, countPrev.get ( i ) + 1 );
            } else
                countPrev.put ( i, 1 );

        }

        //create an array z and transform the entries in countPrev to elements in that array
        Object[] z = countPrev.entrySet ( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( z, ( Object o8, Object o9 ) -> ( ( Map.Entry <String, Integer> ) o9 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o8 ).getValue ( ) ) );
        for ( Object e : z ) {

            //add all of the keys and their values to Arraylist SortedPrev
            if (( ( ( Map.Entry <String, Integer> ) e ).getValue ( ) ) == 1 && ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                SortedPrev.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text before" + " " + aTag + "\n" );

            else if (( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                SortedPrev.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text before" + " " + aTag + "\n" );
        }


        //reduce the size of SortedPrev to get the first 5 elements
        if (SortedPrev.size ( ) > 4) {
            InterimResultPrev = SortedPrev.subList ( 0, 5 );
        }

        //if there are no 5 elements, just return the list
        else InterimResultPrev = SortedPrev;

        //store elements of InterimResultPrev in a String called resultPrev
        for ( String e : InterimResultPrev ) resultPrev += e;


        //getting Most likely to be following POS Tag
        //loop through elements in tokens
        for ( String i : tokens )

        //loop through items in listAlltokens
        {
            for ( int n = 1 ; n < listAllTokens.size ( ) ; n++ )

            //if i matches the token at index n
            {
                if (i.equalsIgnoreCase ( listAllTokens.get ( n ) ))

                    //add its neighbor (preceeding token) at index n+1
                    nextToken.add ( listAllTokens.get ( n + 1 ).toLowerCase ( ) );
            }
        }


        ArrayList <ArrayList <String>> POSTagofFoll = new ArrayList <> ( );
        HashMap <String, Integer> countFoll = new HashMap <> ( );
        ArrayList <String> SingleTagFoll = new ArrayList <> ( );

        //loop through items in nextToken
        for ( String i : nextToken )

        //If tokenWithTags contains i as a key
        {
            if (tokenWithTags.containsKey ( i )) {
                //add the value of key i (the POS Tags)
                POSTagofFoll.add ( tokenWithTags.get ( i ) );
            }
        }

        //loop through the arraylists in POSTagOfFoll
        for ( ArrayList <String> tags : POSTagofFoll ) {

            //loop through strings in that arraylists
            for ( String tag : tags ) {

                //add the strings to SingleTagFoll
                SingleTagFoll.add ( tag );
            }
        }

        //loop through Strings in SingleTagFoll
        for ( String i : SingleTagFoll )

        //if i is a key of countFoll
        {
            if (countFoll.containsKey ( i )) {
                //put i and the count +1 (for each occurence of i) into countFoll
                countFoll.put ( i, countFoll.get ( i ) + 1 );
            } else
                countFoll.put ( i, 1 );

        }

        //create an array r and transform the entries in countFoll to elements in that array
        Object[] r = countFoll.entrySet ( ).toArray ( );

        //sort the array by using a Comparator via lambda expression
        Arrays.sort ( r, ( Object o10, Object o11 ) -> ( ( Map.Entry <String, Integer> ) o11 ).getValue ( )
                .compareTo ( ( ( Map.Entry <String, Integer> ) o10 ).getValue ( ) ) );
        for ( Object e : r ) {

            //add all of the keys and their values to Arraylist SortedFoll
            if (( ( ( Map.Entry <String, Integer> ) e ).getValue ( ) ) == 1 && ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                SortedFoll.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "time in the text after" + " " + aTag + "\n" );

            else if (( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) ).length ( ) > 1)
                SortedFoll.add ( ( ( Map.Entry <String, Integer> ) e ).getKey ( ) + ":" + " "
                        + "appeared" + " " + ( ( Map.Entry <String, Integer> ) e ).getValue ( ) + " " + "times in the text after" + " " + aTag + "\n" );
        }


        //reduce the size of SortedFoll to get the first 5 elements
        if (SortedFoll.size ( ) > 4) {
            InterimResultFoll = SortedFoll.subList ( 0, 5 );
        }

        //if there are no 5 elements, just return the list
        else InterimResultFoll = SortedFoll;

        //store elements of InterimResultFoll in a String called result
        for ( String e : InterimResultFoll ) resultFoll += e;

        //return all of the reuslts (as a String)
        return "Frequency of that POS Tag:" + " " + formattedResultFrequency + "\n" + "Most frequent Tokens with that Tag:" + "\n" + resultToken + "\n"
                + "Most likely to be preceeding POS Tag" + "\n" + resultPrev + "\n"
                + "Most likely to be following  POS Tag" + "\n" + resultFoll;
    }


}

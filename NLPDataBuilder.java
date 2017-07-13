import opennlp.tools.sentdetect.SentenceDetectorME;    // Sentence Segmenter
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;               // Tokenizer
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;                  // Tagger
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;  // Lemmatizer

import java.io.*;
import java.util.ArrayList;                            // utilities for data storage
import java.util.HashMap;



public class NLPDataBuilder {

    // INSTANCE VARIABLES

    // DATA FOR SEARCH TERM SEARCH RESULTS - search by token, search by lemma
    private String[] sentences;                               // the indices of the sentences in this acts as the keys for the next two
    private HashMap<Integer, String[]> sentenceWithTokens;    // key: index of each sentence in sentences, value: list of tokens in that sentence
    private HashMap<Integer, String[]> sentenceWithPOS;       // for calculating lemmas. key: sentence index, value: list of lemmas
    private HashMap<Integer, String[]> sentenceWithLemmas;    // key: index of each sentence in sentences, value: list of lemmas in that sentence



// TODO
//    // DATA FOR DOCUMENT-WIDE STATISTICS
//    private HashMap<String, Integer> tokenFreq;                       //key: token, value: freq
//    private HashMap<String, Integer> tagFreq;                         //key: POStag, value: freq
//// ?    private HashMap<String, ArrayList<String>> tagCluster;          //key: tag, value: list of tokens with this tag
//    private int totalTokensInDocument;
//    // total number sentences = sentences.length


    // CONSTRUCTOR

    /**
     * this constructor uses openNLP sentence segemnter to get the sentences of a text, and then tokenizer, tagger and lemmatizer
     * to fill the other instance variables.
     * The indices of the sentences in String[] sentences are used to relate all the HashMaps with the same key
     * @param text - a string representing the whole document, url/file/string
     */
    public NLPDataBuilder(String text, String command) throws IOException {

        text = this.formatText(text, command); // gets the string from the url/file, and updates text appropriately
        this.loadSentences(text); // this will give us the indexes and sentences that we will use to reference everything else

        // instantiate instance variables
        sentenceWithTokens = new HashMap<>();
        sentenceWithPOS = new HashMap<>();
        sentenceWithLemmas = new HashMap<>();


        // SET UP THE OPEN NLP TOOLS
        InputStream tokenStream = new FileInputStream("en-token.bin");     // TOKENIZER
        TokenizerModel model = new TokenizerModel(tokenStream);
        Tokenizer tokenizer = new TokenizerME(model);
        tokenStream.close();

        InputStream tagStream = new FileInputStream("en-pos-maxent.bin");  // TAGGER
        POSModel tagModel = new POSModel(tagStream);
        POSTaggerME tagger = new POSTaggerME(tagModel);
        tagStream.close();

        InputStream lemStream = new FileInputStream("en-lemmatizer.dict"); // LEMMATIZER
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(lemStream);
        lemStream.close();


        // use open nlp to set up INSTANCE VARIABLE hash maps (one sentence at a time)
        for (int i=0; i < sentences.length; i++) {
            String[] tokens = tokenizer.tokenize(sentences[i]);
            sentenceWithTokens.put(i, tokens);

            String[] tags = tagger.tag(tokens);
            sentenceWithPOS.put(i, tags);

            String[] lemmas = lemmatizer.lemmatize(tokens, tags);
            sentenceWithLemmas.put(i, lemmas);
        }
    }


    /*
     * LOAD SENTENCES - helper method for constructor
     * loads the sentences array instance variable using open nlp
     */
    private void loadSentences(String text) {
        try {
            InputStream stream = new FileInputStream("en-sent.bin");
            SentenceModel model = new SentenceModel(stream);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
            sentences = sentenceDetector.sentDetect(text);  //loads INS VAR sentences
            stream.close();

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("sentence segment error");
        }
    }


    /*
     * FORMAT TEXT - helper method for constructor
     * updates the input text to get the appropriate string for a url input, file input or string input
     */
    private String formatText(String text, String command){

        if (command.equals("url")) {
            Scraper souped = new Scraper(text);
            text = souped.getCorpus();
        }
        if (command.equals("string")){
            // do nothing. text is just text
        }
        if (command.equals("file")) { // todo. test this
            String fileName = text; //this is the text that the user entered
            text = ""; //this is the text we will get back, to do the open nlp stuff to

            //create the stream for reading the file
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    text += currentLine;
                }
                br.close(); //close stream
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return text;
    }




    // SEARCH METHOD
    // todo ? how to validate user input? to make sure that they entered one word
    /**
     * search the text for a specific token
     * @param searchTerm - a String. must be a single token
     * @return an Object SearchResult, which contains all the data required for display in the GUI
     */
    public SearchResult searchByToken(String searchTerm){

        SearchResult result = new SearchResult();
        result.searchTerm = searchTerm;

        for (int i=0; i<sentences.length; i++) {
            String[] sen = sentenceWithTokens.get(i);

            for (int j=0; j<sen.length; j++){

                if (sen[j].equalsIgnoreCase(searchTerm)){   // MATCH!

                    result.numMatches++;                                                                // increment match counter
                    if (!result.indexSenMatches.contains(i)){result.indexSenMatches.add(i);}            // add the index of sentence to display
                    result.tagsOfMatches.add(sentenceWithPOS.get(i)[j]);                                // add the tag of this match
                    if (j != 0){result.tagsPrecedingWords.add(sentenceWithPOS.get(i)[j-1]);}            // add the tag preceding this match
                    if (j != sen.length-1){result.tagsFollowingWords.add(sentenceWithPOS.get(i)[j+1]);} // add the tag following this match
                }
            } // end for (token iteration)
        } // end for (sentences iteration)
        return result;
    }


    /**
     * search the text for lemma-matches
     * first we find the lemma of the search term, then search all the lemmas in the text for matches
     * we return the TOKENS of the lemmas that match.
     * @param searchTerm - a String. must be a single token or lemma.
     *                   If it is a token, the method automaticaly calculates the lemma
     * @return an Object SearchResult, which contains all the data required for display in the GUI
     */
    public SearchResult searchByLemma(String searchTerm) {

        String lemma = getLemma(searchTerm); // find lemma of search term

        if (lemma.equals("O")) {             // if searchTerm has no lemma (is O), just run the searchByToken method by default
            return searchByToken(searchTerm);
        }

        SearchResult result = new SearchResult();
        result.searchTerm = searchTerm;

        for (int i=0; i<sentences.length; i++) {
            String[] senOfLemmas = sentenceWithLemmas.get(i);

            for (int j = 0; j < senOfLemmas.length; j++) {

                if (senOfLemmas[j].equalsIgnoreCase(lemma)) {  // MATCHED LEMMA!

                    result.numMatches++;                                        // increment match counter
                    if (!result.indexSenMatches.contains(i)){result.indexSenMatches.add(i);} // add the index of sentence to display
                    result.tagsOfMatches.add(sentenceWithPOS.get(i)[j]);        // add the tag of this match
                    result.tagsPrecedingWords.add(sentenceWithPOS.get(i)[j-1]); // add the tag preceding this match
                    result.tagsFollowingWords.add(sentenceWithPOS.get(i)[j+1]); // add the tag following this match

                    result.tokensOfMatches.add(sentenceWithTokens.get(i)[j]);   // add the token that corresponds to this lemma
                }
            }
        }
        return result;
    }

    /**
     * INNER CLASS Object to contain the data for GUI
     * this object is returned by the search methods,
     * it is defined to contain all the data that we plan on displaying in the GUI
     * -- if we change our minds on what we want to display, we can just change this part
     */
    private class SearchResult {
        private String searchTerm;
        private int numMatches;
        private ArrayList<Integer> indexSenMatches;
        private ArrayList<String> tagsOfMatches;
        private ArrayList<String> tagsPrecedingWords;
        private ArrayList<String> tagsFollowingWords;
        private ArrayList<String> tokensOfMatches;    // for lemma search

        /*
         * CONSTRUCTOR - here instantiate the data that we want for the GUI
         */
        public SearchResult() {
            numMatches = 0;
            indexSenMatches = new ArrayList<>(); // use the index to grab the sentences from String[] sentences instance variable
            tagsOfMatches = new ArrayList<>();
            tagsPrecedingWords = new ArrayList<>();
            tagsFollowingWords = new ArrayList<>();
            tokensOfMatches = new ArrayList<>();
        }

        /**
         * DISPLAY RESULTS
         * a void method for console testing
         */
        public void displayResults(){
            System.out.println("ORIGINAL SEARCH TERM: " + searchTerm);
            System.out.println("MATCHING SENTENCES:");

            for (int i : indexSenMatches) {
                System.out.println(sentences[i]);
            }

            System.out.println("\nNUMBER OF MATCHES: " + numMatches);

            System.out.print("POS TAGS OF MATCHES: ");
            for (String tag : tagsOfMatches) {
                System.out.print(" " + tag + " ");
            }

            System.out.print("\nPOS TAGS PRECEDING: ");
            for (String tag : tagsPrecedingWords) {
                System.out.print(" " + tag + " ");
            }

            System.out.print("\nPOS TAGS FOLLOWING: ");
            for (String tag : tagsFollowingWords) {
                System.out.print(" " + tag + " ");
            }

            System.out.print("\nTOKENS OF MATCH: ");
            for (String tok : tokensOfMatches) {
                System.out.print(" " + tok + " ");
            }
        }
    }


    /**
     * a static method for finding the lemma of a single word
     * - we use it for finding the lemma of the users search term in searchByLemma()
     * ie, when the user selects the 'search for lemma' option in the GUI
     * @param word a one token string
     * @return String. the lemma of the input string. or the string itself if no lemma found
     */
    public static String getLemma(String word) {

        // have to make strings -> String[]'s for the NLP tools to use them
        //the input word is a token
        String[] lemma = new String[1];

        try {
            // Get tag
            String[] token = {word}; //open nlp requires a String[], so just str wont work

            InputStream stream = new FileInputStream("en-pos-maxent.bin");
            POSModel model = new POSModel(stream);
            POSTaggerME tagger = new POSTaggerME(model);
            String[] tag = tagger.tag(token);
            stream.close();

            //get lemma
            InputStream stream2 = new FileInputStream("en-lemmatizer.dict");
            DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(stream2);
            lemma = lemmatizer.lemmatize(token, tag);
            stream.close();

        } catch (Exception e) {
            System.out.println("Tag or Lemma Load Error");
            // add stack trace
        }

        System.out.println(lemma[0].getClass().getName());
        return lemma[0];
    }


    /**
     * main method, for testing
     */
    public static void main(String[] args) {
        try {
            NLPDataBuilder test = new NLPDataBuilder("https://en.wikipedia.org/wiki/The_Beatles", "url");
//            SearchResult result = test.searchByToken("engineer");
            SearchResult result = test.searchByLemma("engineer");
            result.displayResults();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


import opennlp.tools.sentdetect.SentenceDetectorME;    // Sentence Segmenter
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;               // Tokenizer
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;                  // Tagger
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;  // Lemmatizer

import java.io.FileInputStream;                        // I.O for openNLP tools, models
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;                            // utilities for data storage
import java.util.HashMap;



public class NLPDataBuilder {

    // INSTANCE VARIABLES

    // DATA FOR SEARCH TERM SEARCH RESULTS - search by token, search by lemma
    private String[] sentences;                               // the indices of the sentences in this acts as the keys for the next two
    private HashMap<Integer, String[]> sentenceWithTokens;    // key: index of each sentence in sentences, value: list of tokens in that sentence
    private HashMap<Integer, String[]> sentenceWithPOS;       // for calculating lemmas. key: sentence index, value: list of lemmas
    private HashMap<Integer, String[]> sentenceWithLemmas;    // key: index of each sentence in sentences, value: list of lemmas in that sentence
    private HashMap<Integer, String[]> sentenceWithTokTagLem; //
    //string[] because open nlp lemma needs that



    // DATA FOR DOCUMENT Wide STATISTICS
    private HashMap<String, Integer> tokenFreq;                        //key: token, value: freq
    private HashMap<String, Integer> tagFreq;                         //key: POStag, value: freq
//    private HashMap<String, ArrayList<String>> tagCluster;            //key: tag, value: list of tokens with this tag
    private int totalTokensInDocument;
    // total number sentences = sentences.length


    // CONSTRUCTOR

    /**
     *
     * @param text - a string representing the whole document, url/file/string
     */
    public NLPDataBuilder(String text) throws IOException {

        this.loadSentences(text); // this will give us the indexes and sentences that we will use to reference everything else

        // instantiate instance variables
        sentenceWithTokens = new HashMap<>();
        sentenceWithPOS = new HashMap<>();
        sentenceWithLemmas = new HashMap<>();
        sentenceWithTokTagLem = new HashMap<>();


        // SET UP THE OPEN NLP TOOLS
        InputStream tokenStream = new FileInputStream("en-token.bin");
        TokenizerModel model = new TokenizerModel(tokenStream);
        Tokenizer tokenizer = new TokenizerME(model);
        tokenStream.close();

        InputStream tagStream = new FileInputStream("en-pos-maxent.bin");
        POSModel tagModel = new POSModel(tagStream);
        POSTaggerME tagger = new POSTaggerME(tagModel);
        tagStream.close();

        InputStream lemStream = new FileInputStream("en-lemmatizer.dict");
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(lemStream);
        lemStream.close();


        // use open nlp to set up INSTANCE VARIABLE HASH MAPS (per sentence)
        for (int i=0; i < sentences.length; i++) {

            String[] tokens = tokenizer.tokenize(sentences[i]);
            sentenceWithTokens.put(i, tokens);

            String[] tags = tagger.tag(tokens);
            sentenceWithPOS.put(i, tags);

            String[] lemmas = lemmatizer.lemmatize(tokens, tags);
            sentenceWithLemmas.put(i, lemmas);

            String[] allData = new String[tokens.length];
            for (int j=0; j<tokens.length; j++) {
                allData[j] = tokens[j] + "_" + tags[j] + "_" + lemmas[j];
            }
            sentenceWithTokTagLem.put(i,allData);
        }
    }


    /*
     * loads the sentences array using open nlp
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





    // SEARCH METHODS

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
     * search by lemma
     * first, it finds the lemma of the search term
     *
     * @param searchTerm
     * @return
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
     * Object to contain the GUI data
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


        public SearchResult() {
            numMatches = 0;
            indexSenMatches = new ArrayList<>(); // use the index to grab the sentences from String[] sentences instance variable
            tagsOfMatches = new ArrayList<>();
            tagsPrecedingWords = new ArrayList<>();
            tagsFollowingWords = new ArrayList<>();
            tokensOfMatches = new ArrayList<>();
        }

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
     * a static method for finding the lemma of a single word - we use it for finding the lemma of the users search term
     * when they select 'search for lemma'
     * @param word a one token string
     * @return String. the lemma of the input string. or the string itself if no lemma found
     */
    public static String getLemma(String word) {

        //user check for no whitespace in input? //please enter a single word

        //the input word is a token
        String[] lemma = new String[1];

        try {
            // Get tag
            String[] token = {word}; //open nlp requires a Strin[], so just str wont work

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

    
    public static void main(String[] args) {

        Scraper sc = new Scraper("https://en.wikipedia.org/wiki/The_Beatles");
        String corpus = sc.getCorpus();

        try {
            NLPDataBuilder test = new NLPDataBuilder(corpus);
//            SearchResult result = test.searchByToken("engineer");
            SearchResult result = test.searchByLemma("engineer");
            result.displayResults();
            


        } catch (Exception e) {

        }
    }
}


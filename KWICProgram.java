/**
 * Created by timday on 7/8/17.
 */

//todo searchByLemma method.
// will need to import the lemma machinery here, lemmatize the search term, and then check against the lemmas of each sentence
/**
 * This class contructs a linguistic data structure based on the input text, using open nlp tools, upon instantiation.
 * It also contains the methods for searching the data model both by the search term and the lemma of the search term.
 * The private class Search result organises the returned search data for delivery to the visualisation.
 * The open nlp linguistics tools are all contained in the Sentence.java class. Even though the
 * sentence segmentation only occurs here, we decided it was neater to keep it all together
 */

import java.util.ArrayList;


public class KWICProgram {

    protected ArrayList<Sentence> linguistStatsBucket; // for storing all the linguistic data related to the input text
    protected String[] sentences;


    /**
     * CONSTRUCTOR
     * @param text - a single string of text for the program to break into linguistic components
     */
    public KWICProgram(String text) {
        try {
            sentences = Sentence.getSentences(text); //to segment sentences use static method from Sentence class

            linguistStatsBucket = new ArrayList<Sentence>(sentences.length); // populate linguistic data structure, as series of Sentence objs
            for (String sen : sentences) {
                Sentence obj = new Sentence(sen);    // the constructor of Sentence class adds the tokens, tags, lemmas, etc
                linguistStatsBucket.add(obj);       // add this sentence and its data to instance variable
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error with input string");
        }
    }


    /**
     * SEARCH FOR WORD
     * @param searchTerm - a single token string
     * @return object SearchResult - this contains a collection of structured data relating to the search result
     * if no search result found, it returns null.
     */
    public SearchResult searchForWord(String searchTerm) {

        SearchResult result = new SearchResult();

        for (Sentence sen : linguistStatsBucket) {
            String[] tokens = sen.getTokens();
            boolean tokenFoundInSen = false;                            // we want to return only one sentence, even if it contains multiple matches
            for (int i=0; i<tokens.length; i++) {

                if (searchTerm.equalsIgnoreCase(tokens[i])) {
                    if (!tokenFoundInSen) {                             // if sentence already added, don't add again
                        result.sentencesWithWord.add(sen.getSentence());
                        tokenFoundInSen = true;
                    }

                    result.countMatches++;
                    String[] tags = sen.getTags();                      // method from Sentence.java class
                    result.posTagsMatches.add(tags[i]);
                    result.posTagsWordsPreceding.add(tags[i-1]);
                    result.posTagsWordsFollowing.add(tags[i+1]);
                    String[] lemmas = sen.getLemmas();                  // method from Sentence.java class
                    result.lemmaMatches.add(lemmas[i]);
                }
            }
        }
        return result;
    }


    /**
     * SEARCH FOR LEMMA
     * finds the lemma of the search term, and matches all words that have the same lemma
     * eg: a search for 'watches' would match 'watching', 'watched', 'watch', 'watches', etc.
     * @param searchTerm
     * @return Object SearchResult. Structured data representing the linguistic info for all matches in text
     * returns the result of searchForWord() if the search term has no lemma
     */
    public SearchResult searchForLemma(String searchTerm) {

        SearchResult result = new SearchResult();         //searchResults stats added to this object

        String searchLem = Sentence.getLemma(searchTerm); //first, find the lemma of the search term
        if (searchLem.equals("0")) {
            return searchForWord(searchTerm);             //if search term has no lemma, just search for the input
        }

        for (Sentence sen : linguistStatsBucket) {        //check the lemma lists in all the sentence objects for a match

            String[] lemmas = sen.getLemmas();            //method from Sentence.java
            boolean lemmaFoundInSen = false;              //oly add a sentence once, even if it contains multiple matches

            for (int i = 0; i < lemmas.length; i++) {

                if (searchLem.equalsIgnoreCase(lemmas[i])) {

                    if (!lemmaFoundInSen) {
                        result.sentencesWithWord.add(sen.getSentence());
                        lemmaFoundInSen = true;
                    }

                    result.countMatches++;

                    String[] tags = sen.getTags();
                    result.posTagsMatches.add(tags[i]);
                    if (i >= 1) {
                        result.posTagsWordsPreceding.add(tags[i - 1]);
                    }
                    if (i < lemmas.length-1) {
                        result.posTagsWordsFollowing.add(tags[i + 1]);
                    }
                    String[] tokens = sen.getTokens();
                    result.tokenMatches.add(tokens[i]);
                }
            }
        }
        return result;
    }


    /**
     * print search results
     * mainly for testing purposes. just prints a summary of the SearchResult object to the console
     * @param word
     */
    public void printSearchResults(String word) {

        SearchResult result = searchForLemma(word); //todo have word vs lemma passed in somehow

        if (result != null) {

            System.out.println("Number of matches:  " + result.countMatches);

            System.out.println();
            for (String sen : result.sentencesWithWord){
                System.out.println(sen);
            }

            System.out.println();
            System.out.print("POS of matches:   ");
            for (String pos : result.posTagsMatches) {
                System.out.print(pos + " ");
            }

            System.out.println();
            System.out.print("POS of previous:  ");
            for (String pos : result.posTagsWordsPreceding) {
                System.out.print(pos + " ");
            }

            System.out.println();
            System.out.print("POS of following: ");
            for (String pos : result.posTagsWordsFollowing) {
                System.out.print(pos + " ");
            }

            System.out.println();
            System.out.print("lemma of match: ");
            for (String lem : result.lemmaMatches) {
                System.out.print(lem + " ");
            }

            System.out.println();
            System.out.print("token of match: ");
            for (String lem : result.tokenMatches) {
                System.out.print(lem + " ");
            }

            System.out.println("\n\n");
        }
    }


    /*
     *  This class outlines the data structure for the collecting of linguistic stats that relate to the matches
     *  of a search of the input text by a search term
     */
    private class SearchResult {

        private ArrayList<String> sentencesWithWord;
        int countMatches = 0;
        private ArrayList<String> posTagsMatches;
        private ArrayList<String> posTagsWordsPreceding;
        private ArrayList<String> posTagsWordsFollowing;
        private ArrayList<String> lemmaMatches;
        private ArrayList<String> tokenMatches;

        public SearchResult(){
            sentencesWithWord = new ArrayList<>();
            posTagsMatches = new ArrayList<>();
            posTagsWordsPreceding = new ArrayList<>();
            posTagsWordsFollowing = new ArrayList<>();
            lemmaMatches = new ArrayList<>();
            tokenMatches = new ArrayList<>();
        }

    }

    /*
     * just for testing
     */
    public static void main(String[] args){

        String testString = " Tim Day is an Australian student who watches Australian football. " +
                "But he lives in Germany, selling Watches. The time difference means he has to wake up early to watch." +
                " He doesn't mind, watching beats sleeping. He learns about computer science by watching youtube, and Australian language tools afterwards. " +
                "He's one of the purveyors of the finest of fine arts, truncating sentences while searching for lemmas.";


        KWICProgram test = new KWICProgram(testString);

//        test.printSearchResults("watches");
//        test.printSearchResults("Australian");
        test.printSearchResults("watches");

        System.out.println(Sentence.getLemma("watches")); // test the get lemma method

//        test.article.get(6).printTokensTagsLemmas();

//        for (Sentence sen : test.article) {
//            sen.printTokensWithTags();
//        }

    }
}

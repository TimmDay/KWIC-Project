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
     * @param command String. an 'instruction' for the method, which changes the input text based on a radio button selection from the user
     *                a wiki url will send "url", file path "file" and plain text "string"
     */
    public KWICProgram(String text, String command) {
        try {
            if (command.equals("url")) {
                Scraper souped = new Scraper(text);
                text = souped.getCorpus();
            }
            if (command.equals("string")){
                // do nothing
            }
            if (command.equals("file")) {
                // do buffered reader stuff to get text string
                
            }
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
                    if (i >= 1) {
                        result.posTagsWordsPreceding.add(tags[i - 1]); // if loop, to protect against pout of bounds
                    }
                    if (i < tags.length-1) {
                        result.posTagsWordsFollowing.add(tags[i + 1]);
                    }
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
        if (searchLem.equals("O")) {
            searchLem = searchTerm;                       //if search term has no lemma, just search for the input
        }

        for (Sentence sen : linguistStatsBucket) {        //check the lemma lists in all the sentence objects for a match

            String[] lemmas = sen.getLemmas();            //method from Sentence.java
            boolean lemmaFoundInSen = false;              //only add a sentence once, even if it contains multiple matches

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
                        result.posTagsWordsPreceding.add(tags[i - 1]); // if loop, to protect against pout of bounds
                    }
                    if (i < tags.length-1) {
                        result.posTagsWordsFollowing.add(tags[i + 1]);
                    }

                    result.lemmaMatches.add(lemmas[i]); // will obv be all the same here

                    String[] tokens = sen.getTokens();
                    result.tokenMatches.add(tokens[i]); // add the full token of the lemma match
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

            System.out.println();
            for (String sen : result.sentencesWithWord){
                System.out.println(sen);
            }

            System.out.println("Number of matches:  " + result.countMatches);

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

//        String testString = "A ranking is a relationship between a set of items such that, for any two items, the first is either 'ranked higher than', 'ranked lower than' or 'ranked equal to' the second.[1] In mathematics, this is known as a weak order or total preorder of objects. It is not necessarily a total order of objects because two different objects can have the same ranking. The rankings themselves are totally ordered. For example, materials are totally preordered by hardness, while degrees of hardness are totally ordered. By reducing detailed measures to a sequence of ordinal numbers, rankings make it possible to evaluate complex information according to certain criteria.[2] Thus, for example, an Internet search engine may rank the pages it finds according to an estimation of their relevance, making it possible for the user quickly to select the pages they are likely to want to see. Analysis of data obtained by ranking commonly requires non-parametric statistics.   It is not always possible to assign rankings uniquely. For example, in a race or competition two (or more) entrants might tie for a place in the ranking.[3] When computing an ordinal measurement, two (or more) of the quantities being ranked might measure equal. In these cases, one of the strategies shown below for assigning the rankings may be adopted. A common shorthand way to distinguish these ranking strategies is by the ranking numbers that would be produced for four items, with the first item ranked ahead of the second and third (which compare equal) which are both ranked ahead of the fourth. These names are also shown below. In competition ranking, items that compare equal receive the same ranking number, and then a gap is left in the ranking numbers. The number of ranking numbers that are left out in this gap is one less than the number of items that compared equal. Equivalently, each item's ranking number is 1 plus the number of items ranked above it. This ranking strategy is frequently adopted for competitions, as it means that if two (or more) competitors tie for a position in the ranking, the position of all those ranked below them is unaffected (i.e., a competitor only comes second if exactly one person scores better than them, third if exactly two people score better than them, fourth if exactly three people score better than them, etc.). Thus if A ranks ahead of B and C (which compare equal) which are both ranked ahead of D, then A gets ranking number 1 (\"first\"), B gets ranking number 2 (\"joint second\"), C also gets ranking number 2 (\"joint second\") and D gets ranking number 4 (\"fourth\"). Sometimes, competition ranking is done by leaving the gaps in the ranking numbers before the sets of equal-ranking items (rather than after them as in standard competition ranking).[where?] The number of ranking numbers that are left out in this gap remains one less than the number of items that compared equal. Equivalently, each item's ranking number is equal to the number of items ranked equal to it or above it. This ranking ensures that a competitor only comes second if they score higher than all but one of their opponents, third if they score higher than all but two of their opponents, etc. Thus if A ranks ahead of B and C (which compare equal) which are both ranked head of D, then A gets ranking number 1 (\"first\"), B gets ranking number 3 (\"joint third\"), C also gets ranking number 3 (\"joint third\") and D gets ranking number 4 (\"fourth\"). In this case, nobody would get ranking number 2 (\"second\") and that would be left as a gap. In dense ranking, items that compare equal receive the same ranking number, and the next item(s) receive the immediately following ranking number. Equivalently, each item's ranking number is 1 plus the number of items ranked above it that are distinct with respect to the ranking order. Thus if A ranks ahead of B and C (which compare equal) which are both ranked ahead of D, then A gets ranking number 1 (\"first\"), B gets ranking number 2 (\"joint second\"), C also gets ranking number 2 (\"joint second\") and D gets ranking number 3 (\"third\"). In ordinal ranking, all items receive distinct ordinal numbers, including items that compare equal. The assignment of distinct ordinal numbers to items that compare equal can be done at random, or arbitrarily, but it is generally preferable to use a system that is arbitrary but consistent, as this gives stable results if the ranking is done multiple times. An example of an arbitrary but consistent system would be to incorporate other attributes into the ranking order (such as alphabetical ordering of the competitor's name) to ensure that no two items exactly match. With this strategy, if A ranks ahead of B and C (which compare equal) which are both ranked ahead of D, then A gets ranking number 1 (\"first\") and D gets ranking number 4 (\"fourth\"), and either B gets ranking number 2 (\"second\") and C gets ranking number 3 (\"third\") or C gets ranking number 2 (\"second\") and B gets ranking number 3 (\"third\"). In computer data processing, ordinal ranking is also referred to as \"row numbering\". Items that compare equal receive the same ranking number, which is the mean of what they would have under ordinal rankings. Equivalently, the ranking number of 1 plus the number of items ranked above it plus half the number of items equal to it. This strategy has the property that the sum of the ranking numbers is the same as under ordinal ranking. For this reason, it is used in computing Borda counts and in statistical tests (see below). Thus if A ranks ahead of B and C (which compare equal) which are both ranked ahead of D, then A gets ranking number 1 (\"first\"), B and C each get ranking number 2.5 (average of \"joint second/third\") and D gets ranking number 4 (\"fourth\"). Here is an example: Suppose you have the data set 1.0, 1.0, 2.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0. The ordinal ranks are 1, 2, 3, 4, 5, 6, 7, 8, 9. For v = 1.0, the fractional rank is the average of the ordinal ranks: (1 + 2) / 2 = 1.5. In a similar manner, for v = 5.0, the fractional rank is (7 + 8 + 9) / 3 = 8.0. Thus the fractional ranks are: 1.5, 1.5, 3.0, 4.5, 4.5, 6.0, 8.0, 8.0, 8.0 In statistics, \"ranking\" refers to the data transformation in which numerical or ordinal values are replaced by their rank when the data are sorted. For example, the numerical data 3.4, 5.1, 2.6, 7.3 are observed, the ranks of these data items would be 2, 3, 1 and 4 respectively. For example, the ordinal data hot, cold, warm would be replaced by 3, 1, 2. In these examples, the ranks are assigned to values in ascending order. (In some other cases, descending ranks are used.) Ranks are related to the indexed list of order statistics, which consists of the original dataset rearranged into ascending order. Some kinds of statistical tests employ calculations based on ranks. Examples include: The distribution of values in decreasing order of rank is often of interest when values vary widely in scale; this is the rank-size distribution (or rank-frequency distribution), for example for city sizes or word frequencies. These often follow a power law. Some ranks can have non-integer values for tied data values. For example, when there is an even number of copies of the same data value, the above described fractional statistical rank of the tied data ends in ½. Microsoft Excel provides two ranking functions, the Rank.EQ function which assigns competition ranks (\"1224\") and the Rank.AVG function which assigns fractional ranks (\"1 2.5 2.5 4\") as described above. \n";

        KWICProgram test = new KWICProgram(testString, "string");

//        test.printSearchResults("watches");
        test.printSearchResults("Australian");
//        test.printSearchResults("rank");
//        test.printSearchResults("watches");

        System.out.println(Sentence.getLemma("watches")); // test the get lemma method

//        test.article.get(6).printTokensTagsLemmas();

//        for (Sentence sen : test.article) {
//            sen.printTokensWithTags();
//        }

    }
}

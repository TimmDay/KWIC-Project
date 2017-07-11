/**
 * Created by timday on 7/8/17.
 */

//todo searchByLemma method.
// will need to import the lemma machinery here, lemmatize the search term, and then check against the lemmas of each sentence

import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.io.InputStream;

public class KWICProgram {

    ArrayList<Sentence> article;
    String[] sentences;


// CONSTRUCTOR
    public KWICProgram(String text) {

        try {
            InputStream stream = new FileInputStream("en-sent.bin");
            SentenceModel model = new SentenceModel(stream);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
            sentences = sentenceDetector.sentDetect(text); // takes raw text
            stream.close();

            article = new ArrayList<Sentence>(sentences.length); // create the article, as series of Sentence objs
            for (String sen : sentences) {
                Sentence obj = new Sentence(sen); //constructor adds the tokens, etc
                article.add(obj);
            }

        } catch (Exception e) {
            System.out.println("file not found. model?");
        }
    }


    public SearchResult searchModelForWord(String searchTerm) {

        SearchResult result = new SearchResult();

        for (Sentence sen : article) {
            String[] tokens = sen.getTokens();
            boolean tokenFoundInSen = false;

            for (int i=0; i<tokens.length; i++) {

                if (searchTerm.equalsIgnoreCase(tokens[i])) {

                    if (!tokenFoundInSen) {
                        result.sentencesWithWord.add(sen.getSentence());
                        tokenFoundInSen = true;
                    }

                    result.countMatches++;
                    String[] tags = sen.getTags();
                    result.posTagsMatches.add(tags[i]);
                    result.posTagsWordsPreceding.add(tags[i-1]);
                    result.posTagsWordsFollowing.add(tags[i+1]);
                    String[] lemmas = sen.getLemmas();
                    result.lemmaMatches.add(lemmas[i]);
                }
            }
        }

        return result;
    }

    public SearchResult searchModelForLemma(String searchTerm) {

        SearchResult result = new SearchResult();

        // make lemma out of search term
        String searchLem = Sentence.getLemma(searchTerm);
        if (searchLem.equals("0")) {
            return null;
        }

        // check the lemma lists in all the sentence objects for a match
        for (Sentence sen : article) {

            String[] lemmas = sen.getLemmas();
            boolean lemmaFoundInSen = false;

            //todo if lemma doesn't exist, abandon search, or just search for the input instead
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


    public void printSearchResults(String word) {

        SearchResult result = searchModelForLemma(word); //todo have word vs lemma passed in somehow

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

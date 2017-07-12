import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Class Sentence
 * this class contains all the open nlp tools that are used in this project.
 * Including Tokenizer, POSTagger, Lemmatizer and Sentence Segmenter
 * Its primary purpose is to populate a data structure for the storage of linguistic data obtained from an input sentence String
 * It also contains some static methods for the one-off use of open NLP tools, such as a sentence segmenter.
 */
public class Sentence {

    private String sentence;
    private String[] tokens;
    private String[] tags;
    private String[] lemmas;


    /**
     * CONSTRUCTOR
     * uses several helper methods that use open nlp tools to populate the linguistic data for the sentence
     * @param sen
     */
    public Sentence(String sen) {
        this.sentence = sen;
        this.loadTokens();
        this.loadPOS();
        this.loadLemma();
        this.updateLemmas();

    }


    /*
     * helper for constructor. Uses Apache open nlp
     */
    private void loadTokens() {
        try {
            InputStream stream = new FileInputStream("en-token.bin");
            TokenizerModel model = new TokenizerModel(stream);
            Tokenizer tokenizer = new TokenizerME(model);
            tokens = tokenizer.tokenize(sentence); //ins var
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Token Load Error");
        }
    }
    /*
     * helper for constructor. Uses Apache open nlp
     */
    private void loadPOS() {
        try {
            InputStream stream = new FileInputStream("en-pos-maxent.bin");
            POSModel model = new POSModel(stream);
            POSTaggerME tagger = new POSTaggerME(model);
            tags = tagger.tag(tokens);
            stream.close();


        } catch (Exception e) {
            System.out.println("POS Tag Load Error");
        }
    }

    /*
     * helper for constructor. Uses Apache open nlp
     */
    private void loadLemma() {
        try {
            InputStream stream = new FileInputStream("en-lemmatizer.dict");
            DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(stream);
            lemmas = lemmatizer.lemmatize(tokens, tags);
            stream.close();

        } catch (Exception e) {
            System.out.println("Lemma Load Error");
        }

    }
    /*
     * updates the lemma list to replace empty lemmas (String capital O) with the token
     */
    private void updateLemmas() {
        for (int i=0; i<lemmas.length; i++) {
            if (lemmas[i].equals("O")){
                lemmas[i] = tokens[i];
            }
        }
    }

    // GET METHODS
    /**
     * get sentence
     * @return String. the plain sentence that this object has data for
     */
    public String getSentence() {
        return sentence;
    }
    /**
     * get tokens
     * @return String[]
     */
    public String[] getTokens() {
        return tokens;
    }
    /**
     * get tags
     * @return String[]
     */
    public String[] getTags() {
        return tags;
    }
    /**
     * get lemmas
     * @return String[]
     */
    public String[] getLemmas() {
        return lemmas;
    }

    public void printTokens() {
        for (String tok : tokens) {
            System.out.println(tok);
        }
    }

    /**
     * print tags to console. for testing
     */
    public void printTags() {
        for (String tag : tags) {
            System.out.println(tag);
        }
    }

    /**
     * print lemmas to console. for testing
     */
    public void printLemmas() {
        System.out.println(lemmas); //returns null
        for (String lem : lemmas) {
            System.out.println(lem);
        }
    }

    /**
     * print tokens, tags and lemmas to console. for testing
     */
    public void printTokensTagsLemmas() {
        for (int i=0; i<tokens.length; i++) {
            System.out.printf("TOKEN: %-9s TAG: %-4s LEMMA: %-8s\n", tokens[i], tags[i], lemmas[i]);

        }
    }

    // STATIC METHODS

    /**
     * a static method for finding the lemma of a single word
     * @param word a one token string
     * @return String. the lemma of the input string. or the string itself if no lemma found
     */
    public static String getLemma(String word) {

        //user check for no whitespace in input? //please enter a single word

        //the input word is a token
        String[] lemma = new String[1];

        try {
            // Get tags
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

    public static String[] getSentences(String text){
        String[] sentences = {};
        try {
            InputStream stream = new FileInputStream("en-sent.bin");
            SentenceModel model = new SentenceModel(stream);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
            sentences = sentenceDetector.sentDetect(text); // takes raw text
            stream.close();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("sentence segment error");
        }
        return sentences;
    }



    public static void main(String[] args) {
        Sentence test = new Sentence("There isn't a higher mountain than Everest, that isn't submerged in the ocean.");
        System.out.println(test.getLemma("bananas"));
        test.printTokensTagsLemmas();
        System.out.println(test.getSentences("This one. That one. These ones"));
//        test.printTags();
//        test.printLemmas();

    }

}

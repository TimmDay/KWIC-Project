import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by timday on 7/8/17.
 */
public class Sentence {

    private String sentence;
    private String[] tokens;
    private String[] tags;
    private String[] lemmas;


    public Sentence(String sen) {
        this.sentence = sen;
        this.loadTokens();
        this.loadPOS();
        this.loadLemma();

    }

    public String getSentence() {
        return sentence;
    }
    public String[] getTokens() {
        return tokens;
    }
    public String[] getTags() {
        return tags;
    }
    public String[] getLemmas() {
        return lemmas;
    }


    private void loadTokens() {
        try {
            InputStream stream = new FileInputStream("en-token.bin");
            TokenizerModel model = new TokenizerModel(stream);
            Tokenizer tokenizer = new TokenizerME(model);
            tokens = tokenizer.tokenize(sentence); //ins var
            stream.close();

        } catch (Exception e) {
            System.out.println("Token Load Error");
        }
    }

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

    private void loadLemma() {
        try {
            InputStream stream = new FileInputStream("en-lemmatizer.dict");
            DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(stream);
            lemmas = lemmatizer.lemmatize(tokens, tags);
            stream.close();

            // for loop to check lemmas and return input word as lemma when 0

        } catch (Exception e) {
            System.out.println("Lemma Load Error");
        }
    }




    public void printTokens() {
        for (String tok : tokens) {
            System.out.println(tok);
        }
    }

    public void printTags() {
        for (String tag : tags) {
            System.out.println(tag);
        }
    }

    public void printLemmas() {
        System.out.println(lemmas); //returns null
        for (String lem : lemmas) {
            System.out.println(lem);
        }
    }

    public void printTokensTagsLemmas() {
        for (int i=0; i<tokens.length; i++) {
            System.out.printf("TOKEN: %-9s TAG: %-4s LEMMA: %-8s\n", tokens[i], tags[i], lemmas[i]);

        }
        double num = 14.364356247274;
        String name = "sgwohwgruhowrg";
        System.out.printf("PERCENTAGE: %-7.3f   STRING: %s", num, name);
    }

    public static String getLemma(String word) {

        //user check for no whitespace in input? //please enter a single word

        //the input word is a token
        String[] lemma = new String[1];

        try {
            // Get tag
            String[] token = new String[1];
            token[0] = word;
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

        return lemma[0];
    }



    public static void main(String[] args) {
        Sentence test = new Sentence("There isn't a higher mountain than Everest, that isn't submerged in the ocean.");
        test.printTokensTagsLemmas();

//        test.printTags();
//        test.printLemmas();

    }
}

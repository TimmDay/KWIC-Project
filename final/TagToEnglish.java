/**
 * Class. converts a pos tag to a description
 * @author Tim
 */
public class TagToEnglish {
    /**
     * static method
     * @param tag
     * @return a String that describes that tag
     */
    public static String tagUpdater(String tag){
        String result = "";

        switch (tag) {

            case "CC" :
                result = "Coordinating conjunction";
                break;

            case "CD" :
                result = "Cardinal number";
                break;

            case "DT" :
                result = "Determiner";
                break;

            case "EX" :
                result = "Existential there";
                break;

            case "FW" :
                result = "Foreign word";
                break;

            case "IN" :
                result = "Preposition, subordinating conjunction";
                break;

            case "N/that" :
                result = "that as subordinator";
                break;

            case "JJ" :
                result = "Adjective";
                break;

            case "JJR" :
                result = "Adjective, comparative";
                break;

            case "JJS" :
                result = "Adjective, superlative";
                break;

            case "LS" :
                result = "List marker";
                break;

            case "MD" :
                result = "Modal";
                break;

            case "NN" :
                result = "Noun";
                break;

            case "NNS" :
                result = "Noun plural";
                break;

            case "NP" :
                result = "Proper noun, singular";
                break;

            case "NNP" : // penn treebank
                result = "Proper noun, singular";
                break;

            case "NPS" :
                result = "Proper noun, plural";
                break;

            case "NNPS" : // penn treebank
                result = "Proper noun, plural";
                break;

            case "PDT" :
                result = "Predeterminer";
                break;

            case "POS" :
                result = "Possessive ending";
                break;

            case "PRP" : // penn treebank
                result = "Personal pronoun";
                break;

            case "PRP$" : // penn treebank
                result = "Possessive pronoun";
                break;

            case "RB" :
                result = "Adverb";
                break;

            case "RBR" :
                result = "Adverb, comparative";
                break;

            case "RBS" :
                result = "Adverb, superlative";
                break;

            case "RP" :
                result = "Particle";
                break;

            case "SENT" :
                result = "Sentence-break punctuation";
                break;

            case "SYM" :
                result = "Symbol";
                break;

            case "TO" :
                result = "Infinitive ‘to’";
                break;

            case "UH" :
                result = "Interjection";
                break;

            case "VB" :
                result = "Verb, base form";
                break;

            case "VBD" :
                result = "Verb, past tense";
                break;

            case "VBG" :
                result = "Verb, gerund/present participle";
                break;

            case "VBN" :
                result = "Verb, past participle";
                break;

            case "VBP" :
                result = "Verb, sing. present, non-3rd person";
                break;

            case "VBZ" :
                result = "Verb, 3rd person sing. present";
                break;

            case "WDT" :
                result = "Wh-determiner";
                break;

            case "WP" :
                result = "Wh-pronoun";
                break;

            case "WP$" :
                result = "Possessive wh-pronoun";
                break;

            case "WRB" :
                result = "Wh-abverb";
                break;

            default :
                result = tag;
                break;
        }
        return result;
    }
}

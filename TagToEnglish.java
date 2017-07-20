/**
 * Created by timday on 7/19/17.
 */
public class TagToEnglish {

    public static String tagUpdater(String tag){
        String result = "";

        switch (tag) {

            case "CC" :
                result = "coordinating conjunction";
                break;

            case "CD" :
                result = "Cardinal number";
                break;

            case "DT" :
                result = "noun";
                break;

            case "EX" :
                result = "existential there";
                break;

            case "FW" :
                result = "foreign word";
                break;

            case "IN" :
                result = "preposition, subordinating conjunction";
                break;

            case "N/that" :
                result = "that as subordinator";
                break;

            case "JJ" :
                result = "adjective";
                break;

            case "JJR" :
                result = "adjective, comparative";
                break;

            case "JJS" :
                result = "adjective, superlative";
                break;

            case "LS" :
                result = "list marker";
                break;

            case "MD" :
                result = "modal";
                break;

            case "NN" :
                result = "noun";
                break;

            case "NNS" :
                result = "noun plural";
                break;

            case "NP" :
                result = "proper noun, singular";
                break;

            case "NNP" : // penn treebank
                result = "proper noun, singular";
                break;

            case "NPS" :
                result = "proper noun, plural";
                break;

            case "NNPS" : // penn treebank
                result = "proper noun, plural";
                break;

            case "PDT" :
                result = "predeterminer";
                break;

            case "POS" :
                result = "possessive ending";
                break;

            case "PP" :
                result = "personal pronoun";
                break;

            case "PP$" :
                result = "possessive pronoun";
                break;

            case "PRP" : // penn treebank
                result = "personal pronoun";
                break;

            case "PRP$" : // penn treebank
                result = "possessive pronoun";
                break;

            case "RB" :
                result = "adverb";
                break;

            case "RBR" :
                result = "adverb, comparative";
                break;

            case "RBS" :
                result = "adverb, superlative";
                break;

            case "RP" :
                result = "particle";
                break;

            case "SENT" :
                result = "Sentence-break punctuation";
                break;

            case "SYM" :
                result = "Symbol";
                break;

            case "TO" :
                result = "infinitive ‘to’";
                break;

            case "UH" :
                result = "interjection";
                break;

            case "VB" :
                result = "verb be, base form";
                break;

            case "VBD" :
                result = "verb be, past tense";
                break;

            case "VBG" :
                result = "verb be, gerund/present participle";
                break;

            case "VBN" :
                result = "verb, past participle";
                break;

            case "VBP" :
                result = "verb be, sing. present, non-3d";
                break;

            case "VBZ" :
                result = "verb be, 3rd person sing. present";
                break;

            case "VH" :
                result = "verb have, base form";
                break;

            case "VHD" :
                result = "verb have, past tense";
                break;

            case "VHG" :
                result = "verb have, gerund/present participle";
                break;

            case "VHN" :
                result = "\tverb have, past participle";
                break;

            case "VHP" :
                result = "verb have, sing. present, non-3d";
                break;

            case "VHZ" :
                result = "verb have, 3rd person sing. present";
                break;

            case "VV" :
                result = "verb, base form";
                break;

            case "VVD" :
                result = "verb, past tense";
                break;

            case "VVG" :
                result = "verb, gerund/present participle";
                break;

            case "VVN" :
                result = "verb, past participle";
                break;

            case "VVP" :
                result = "verb, sing. present, non-3d";
                break;

            case "VVZ" :
                result = "verb, 3rd person sing. present";
                break;

            case "WDT" :
                result = "wh-determiner";
                break;

            case "WP" :
                result = "wh-pronoun";
                break;

            case "WP$" :
                result = "possessive wh-pronoun";
                break;

            case "WRB" :
                result = "wh-abverb";
                break;

            default :
                result = tag;
                break;
        }
        return result;
    }
    // DEMO
//     public static void main(String[] args) {
//         System.out.println(TagToEnglish.tagUpdater("NNS"));
//         System.out.println(TagToEnglish.tagUpdater("VV"));
//     }
}
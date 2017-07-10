
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.lemmatizer.*;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Savvas
 */
public class KeyWordFinder {
    //instance variables
    private String word;
    private String tag;
    private String text;
    private String[] sentences;//holds all sentances from the given source
    private ArrayList<String> tokensPerSent;//holds all tokens from the given sentence
    private ArrayList<String> tokensAndTagsPerSentece;//holds all tokens from the given sentence
    private int numOfTokens;
    private int frequency;
    private HashMap<String, Integer> wordFreq;
    private HashMap<String, Integer> tagFreq;
    private HashMap<String, ArrayList<String>> LemmasWords;
    private HashMap<String, ArrayList<String>> wordAndTags;//holds the words as thisWord and their tags stored in an ArrayList
    private HashMap<String, ArrayList<String>> tagCluster;//gold the tags as keys and the words having this tag stored in an ArrayList
    
    public KeyWordFinder(String fileName) throws IOException {
        //create the stream for reading the file
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        String currentLine;
        text = "";
        while ((currentLine = br.readLine()) != null) {
               text += currentLine;
            }
        br.close();//close stream
        
        //Loading sentence detector model 
        InputStream inputStream = new FileInputStream("en-sent.bin");
        SentenceModel sentencModel = new SentenceModel(inputStream);

        //Instantiating the SentenceDetectorME class 
        SentenceDetectorME detector = new SentenceDetectorME(sentencModel);

        //Detecting the sentence
        sentences = detector.sentDetect(text);// store the sentences into the array   !!!
        
        
        //create the stream for the POS tags
        InputStream inputStream2 = new FileInputStream("en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStream2);//

        //use postaggerME -class
        POSTaggerME tagger = new POSTaggerME(posModel);
       // String[] tags = tagger.getAllPosTags();
       
       //get the token from each sentence
        WhitespaceTokenizer token = WhitespaceTokenizer.INSTANCE;
        //create lammatizer
        InputStream inputStreamLem = new FileInputStream("lemmatizer.bin");
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(inputStreamLem);
        
        wordAndTags = new HashMap<>();
        numOfTokens = 0;
        wordFreq = new HashMap<>();
        tagCluster = new HashMap<>();
        tagFreq = new HashMap<>();
      // LemmasWords = new HashMap<>();
        tokensAndTagsPerSentece = new ArrayList<>();
        for (String sentence : sentences) {//for each sentences
            sentence = correctMarks(sentence).trim();
            String[] tokens = token.tokenize(sentence);//split the current sentence....
            String[] tagsOfthisSent = tagger.tag(tokens);//... and generate the coresponding tags
            //String[] lemmas = lemmatizer.lemmatize(tokens, tagsOfthisSent);
            
            
            POSSample sample = new POSSample(tokens, tagsOfthisSent);//holds each sentence and tags together
            tokensAndTagsPerSentece.add(sample.toString());
            if(tokens.length > 0){
                numOfTokens += tokens.length;
            
            for(int i = 0; i < tokens.length; i++){
                
                String thisWord = tokens[i].trim();
                String thisTag = tagsOfthisSent[i].trim();
                //String lem = lemmas[i].trim();
                if(wordAndTags.containsKey(thisWord)){
                    String frWord = thisWord.toLowerCase();//for freaqueny -> no case sensitive
                    wordFreq.put(frWord, wordFreq.get(frWord) + 1);//increment by 1
                    if(!wordAndTags.get(thisWord).contains(thisTag)){
                        wordAndTags.get(thisWord).add(thisTag);  
                   }
                }
                else
                {
                    ArrayList<String> list = new ArrayList<>(); //create a ArrayList
                    list.add(thisTag);
                    wordAndTags.put(thisWord, list);
                    String frWord = thisWord.toLowerCase();//for freaqueny -> no case sensitive
                    wordFreq.put(frWord, 1);
                    
                }
                
                //fill the tagCluster
                //here: the tag  is the KEY and and thisWord is the VALUE
                if(tagCluster.containsKey(thisTag)){
                    tagFreq.put(thisTag, tagFreq.get(thisTag) + 1);
                    if(!tagCluster.get(thisTag).contains(thisWord))
                        tagCluster.get(thisTag).add(thisWord);
                }
                else
                {
                    ArrayList<String> list = new ArrayList<>(); //create a ArrayList
                    list.add(thisWord);
                    tagCluster.put(thisTag, list);
                    tagFreq.put(thisTag, 1);
                }
                
               
                /*                if(LemmasWords.containsKey(lem)){
                if(!LemmasWords.get(lem).contains(thisWord))
                LemmasWords.get(lem).add(thisWord);
                }
                else
                {
                ArrayList<String> list = new ArrayList<>();
                list.add(thisWord);
                LemmasWords.put(lem, list);
                }
                */
        
                
            }
        }
            inputStream.close();
            inputStream2.close();
            inputStreamLem.close();;
        }
        
        
        /*        String[] tokens = token.tokenize("i saw the man who loves you"); //tokens of the sentence
        
        String[] tagsOfthis = tagger.tag(tokens);
        for (String a : tagsOfthis) {
        System.out.print(a + " ");
        }
        System.out.println("  togehter");
        //together
        POSSample sample = new POSSample(tokens, tagsOfthis);
        System.out.println(sample.toString());*/

    }
    
    /*    public HashMap<String, ArrayList<String>> lemmasWord() throws IOException{
    HashMap<String, ArrayList<String>> map = new HashMap<>();
    
    for(String word : wordAndTags.keySet()){
    for(String tag : wordAndTags.get(word)){
    String lemma = getLemma(word, tag);
    if(map.containsKey(lemma)){
    if(!map.get(lemma).contains(word))
    map.get(lemma).add(word);
    }
    else
    {
    ArrayList<String> list = new ArrayList<>();
    list.add(word);
    map.put(lemma, list);
    }
    }
    }
    return map;
    }
    
    public String getLemma(String word, String pos) throws FileNotFoundException, IOException{
    //create stream Lemmatizer
    InputStream inputStreamLem = new FileInputStream("lemmatizer.bin");
    DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(inputStreamLem);
    if(word == null)
    return null;
    String val = "";
    for(String e : wordAndTags.keySet()){
    String[] w = {word};
    if(findtag2(word).contains(pos))
    {
    String[] t = {pos};
    String[] rsl = lemmatizer.lemmatize(w, t);
    if(rsl[0].length() > 1)
    val =  rsl[0];
    else
    val = word;
    }
    
    
    }
    inputStreamLem.close();
    return val;
    }*/

  
    private static String deleteMarks(String word) {
        word = word.replace(".", "");
        word = word.replace(",", "");
        word = word.replace("?", "");
        word = word.replace("!", "");
        word = word.replace("(", "");
        word = word.replace(")", "");
        word = word.replace("»", "");
        word = word.replace("«", "");
        word = word.replace(":", "");
        word = word.replace(";", "");
        word = word.replace("-", "");//
        

        return word;
    } //deleteMarks(String word)
    private static String correctMarks(String word) {
        char ending = word.charAt(word.length()-1);
        word = word.substring(0, word.length()-1);
        word += " " + ending;
        
        

        return word;
    } //deleteMarks(String word)
    
    public int numOfTokens(){
        return numOfTokens;
    }
    public ArrayList findtag2(String s){
        if(wordAndTags.containsKey(s))
            return wordAndTags.get(s);
        else
            return null;
    }
    public void findtag(String s){
        if(wordAndTags.containsKey(s))
            System.out.println("key: \"" + s + "\" its tags: " + wordAndTags.get(s));
    }
    public int frequencyOfWord(String aWord){
        if (aWord == null)
            return -1;
        aWord = aWord.toLowerCase();
        if(wordFreq.containsKey(aWord))
            return wordFreq.get(aWord);
        else
            return -1;
    }
    public void printSe(){
        int i = 0;
        for(String elen : sentences){
            elen = correctMarks(elen);
            System.out.println(++i + elen);
        }
    }
    public void printAlltokensAndTags(){
       for(String key : wordAndTags.keySet())
            System.out.print( "\"" + key + "\": " + wordAndTags.get(key) + "/ /");
    }
    public void printAllTagsAndTheirWords(){
       for(String key : tagCluster.keySet())
            System.out.print( "\"" + key + "\": " + tagCluster.get(key) + "/ /");
    }
    
    public void printWordFrequencies(){
        wordFreq.keySet().forEach((key) -> {
            System.out.print( "\"" + key + "\": " + wordFreq.get(key) + "/ /");
        });
    }
    public void printTAGFrequencies(){
        tagFreq.keySet().forEach((key) -> {
            System.out.print( "\"" + key + "\": " + tagFreq.get(key) + "/ /");
        });
    }
    public void printTokensAndTagsPerSentece(){
        tokensAndTagsPerSentece.forEach(x -> System.out.println(x));
    }
    /*    public void printLemmasAndWords(){
    LemmasWords.keySet().forEach((key) -> {
    System.out.print( "\"" + key + "\": " + tagFreq.get(key) + "/ /");
    });
    }*/
    /**
     * getter method 
     * @return the number of distickt tags in this document
     */
    public int getNumOfdistinctTags(){
        return tagFreq.size();
    }
    /**
     * getter method
     * @return the number of all tags in this document
     */
     public int getNumOfTags(){
        
        int sum = 0;
            for(Integer num : tagFreq.values())
                sum += num;
            return sum;
    }
    public double getFrequencyOfTag(String tag){ //it is not case sensitive 
        if(tag ==  null)
            return -1;
        tag = tag.toUpperCase();
        if(tagFreq.containsKey(tag))
        {
            double val;
            val =  ((double)tagFreq.get(tag) *100.0) / (double)getNumOfTags();
            
            //val =Double.valueOf(new DecimalFormat("").format(val));
            return val;
        }
        else
            return -1;
    }
    public void findSentencesOfWord(String s){
        if(s == null)
            return;
        ArrayList<String> list = new ArrayList<>();
        for(String sent : sentences){
            if(sent.contains(s))
                list.add(sent);
        }
        list.forEach((elem) -> {
            System.out.println(elem);
        });
    }
    public void findWordAndItsNeighbours(String s, int numPrev, int numAfter) {
        if (s == null) {
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        for (String sent : sentences) {
            sent = correctMarks(sent).trim();
            String[] ar = sent.split("\\s+");
            for (int i = 0; i < ar.length; i++) {
                if (ar[i].equalsIgnoreCase(s)) {
                    if (i - numPrev < 0 && i + numAfter >= ar.length) {
                        numPrev = numPrev - (numPrev - i);
                        numAfter = numAfter - (numAfter - (ar.length -1  - i));
                        String rsl = "";
                        for (int j = i - numPrev; j <= numAfter+ i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    } else if (i + numAfter >= ar.length) {
                        numAfter = numAfter - (numAfter - (ar.length  - 1 - i));
                        //numPrev =  i - (i -numPrev);
                        String rsl = "";
                        for (int j = i - numPrev-1; j <= numAfter + i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    } else if (i - numPrev < 0) {
                        numPrev =  numPrev - (numPrev - i);
                        String rsl = "";
                        for (int j = i - numPrev; j <= numAfter + i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    }
                    else{
                        String rsl = "";
                        
                        for (int j = i-numPrev; j <= numAfter+i; j++) {
                            rsl += ar[j] + " ";
                        }
                        list.add(rsl);
                    }
                }
            }

        }
        list.forEach((elem) -> {
            System.out.println(elem);
        });
    }
    
    public void setag() throws FileNotFoundException, IOException {
        //create the stream for the POS tags
        InputStream inputStream2 = new FileInputStream("en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStream2);//

        //use postaggerME -class
        POSTaggerME tagger = new POSTaggerME(posModel);
       // String[] tags = tagger.getAllPosTags();
       HashMap<String, ArrayList<String>> map = new HashMap<>();
       //get the token from each sentence
        WhitespaceTokenizer token = WhitespaceTokenizer.INSTANCE;
        for (String sentence : sentences) {//for each sentence
            sentence = correctMarks(sentence);
            String[] tokens = token.tokenize(sentence);//split the current sentence....
            for(String a : tokens)
                System.out.print(a + "-");
            System.out.print("->");
            String[] tagsOfthisSent = tagger.tag(tokens);//... and generate the coresponding tokens
            for(String a : tagsOfthisSent)
                System.out.print(a + "-");
            System.out.println();
            
            for(int i = 0; i < tokens.length; i++){
                String key = tokens[i].replace(".", "").trim();
                String tagOfKey = tagsOfthisSent[i].trim();
                if(map.containsKey(key)){
                    if(!map.get(key).contains(tagOfKey)){
                        //ArrayList<String> list = map.get(thisWord);
                        //list.add(tag);
                        map.get(key).add(tagOfKey);
                   }
                }
                else{
                    ArrayList<String> list = new ArrayList<>(); //create a ArrayList
                    list.add(tagOfKey);
                    map.put(key, list);
                }
            }
        }
        for(String key : map.keySet())
            System.out.print( "\"" + key + "\": " + map.get(key) + "/ /");
    }
    public static void main(String[] args){
        try {
            KeyWordFinder my = new KeyWordFinder("ex.txt");
            my.findtag("account");
            my.findtag("!");
            //my.printSe();
            my.setag();
            System.out.println("\n");
            System.out.println(my.numOfTokens());
            System.out.println();
            System.out.println(my.frequencyOfWord("an"));
            System.out.println();
            my.printAlltokensAndTags();
            System.out.println();
            System.out.println();
            my.printAllTagsAndTheirWords();
            System.out.println();
            System.out.println();
            my.printWordFrequencies();
            System.out.println();
            System.out.println();
            my.printTAGFrequencies();
            System.out.println();
            System.out.println();
            my.findSentencesOfWord("account");
            System.out.println();
            System.out.println();
            my.findWordAndItsNeighbours("account", 2, 100);
            System.out.println();
            System.out.println();
            double fr = my.getFrequencyOfTag("nn");
            System.out.printf("PERCENTAGE: %.2f ", fr);
            System.out.println(my.getNumOfdistinctTags());
            System.out.println(my.getNumOfTags());
            System.out.println();
           my.printTokensAndTagsPerSentece();
           System.out.println();
           //System.out.println(my.getLemma("plans", "NNS"));
          
        } catch (IOException ex) {
            Logger.getLogger(KeyWordFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


Lessons Learned

- Object Oriented programming techniques.
we tried to get fancy with the hash maps, but in practice, having everything in one class proved messy. Making edits was difficult, as they affected a wide range of things instead of being contained. Using sentence or word objects would also have made printing to XML easier.
That all said, our technique did work and return nice results

- the code:
private String[] sentences;    // holds all segmented sentences from input text
private HashMap<Integer, String[]> sentenceWithTokens; // key: index of each sentence, value: list of posTags in that sentence
private HashMap<Integer, String[]> sentenceWithPOS; // for calculating lemmas. key: sentence index, value: list of lemmas
private HashMap<Integer, String[]> sentenceWithLemmas; 

we set this up nicely for a 'coordinate lookup' eg: sentenceWithTokes.get(i)[j], this form can be used for direct lookup of any info.
But then we didn't use it everywhere. We had a confluence of various techniques. We could cut some runtime from our program by going through the code and making all our methods follow the same methodology.

- Testing. We mainly used User Testing, and Procedural testing using print methods to the console. Again, using better OO principles we could have made this a lot easier

- Version Control.
we would have saved

- Variable naming and comments.
Especially when working in a team, it is important to be on top of this, It can be difficult to follow each others code otherwise

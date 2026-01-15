import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
        String corpus = in.readAll();
        corpus = corpus.replaceAll("\\r\\n", " ").replaceAll("\\n", " ").replaceAll("\\r", " ");
        for(int i = 0; i < corpus.length() - windowLength; i++){
            String subString = corpus.substring(i, i + windowLength);
            if(CharDataMap.containsKey(subString)){
                List temp = CharDataMap.get(subString);
                temp.update(corpus.charAt(i + windowLength));
                CharDataMap.put(subString, temp);
            }
            else {
                List list = new List();
                list.addFirst(corpus.charAt(i + windowLength));
                CharDataMap.put(subString, list);
            }
        }
        for(List list : CharDataMap.values()){
            calculateProbabilities(list);

        }
        }
	

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		int totalCount = 0;
        ListIterator iterator = probs.listIterator(0);
        Node current = iterator.current;
        while (current != null) {
            totalCount += current.cp.count;
            current = current.next;
        }
        current = iterator.current;
        iterator.current.cp.p = (double)iterator.current.cp.count / totalCount;
        iterator.current.cp.cp = iterator.current.cp.p;
        while (iterator.current.next != null) {
            iterator.current.next.cp.p = (double)iterator.current.next.cp.count/ totalCount;
            iterator.current.next.cp.cp = iterator.current.cp.cp +   iterator.current.next.cp.p;
            iterator.current = iterator.current.next;
        } 
    }

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		ListIterator listIterator =  probs.listIterator(0);
        Node current = listIterator.current;
        double rand = randomGenerator.nextDouble();
        while (current != null) {
            if(current.cp.cp >= rand)
                return current.cp.chr;
            current = current.next;
            
        }
		return ' ';
	}
    

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		String newTxt = initialText, window = initialText;
        for(int i = 0; i < textLength; i++) {
            window = newTxt.substring(i,i+window.length());
            char c = getRandomChar(CharDataMap.get(window));
            newTxt += c;
        }
        return newTxt;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]), generatedTextLength =Integer.parseInt(args[2]);
        String initialTxt = args[1], fileName = args[4] ;
        Boolean randomGeneratedTxt = args[3].equals("random");
        LanguageModel lm;
        if(randomGeneratedTxt)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
        lm.train(fileName);
        System.out.println(lm.generate(initialTxt, generatedTextLength));
    }
}

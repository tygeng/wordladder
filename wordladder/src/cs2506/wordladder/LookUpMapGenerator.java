/**
 *
 */
package cs2506.wordladder;

import java.util.Comparator;

import java.util.HashSet;

import java.util.ListIterator;

import java.util.Map;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import java.util.GregorianCalendar;

import java.util.HashMap;

import java.util.Iterator;

import java.util.ArrayList;

import java.util.List;

import java.util.TreeSet;

/**
 * This class is the to generate a fast loop up map for the searching process.
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
/**
 *
 * @author Tianyu
 * @version Sep 5, 2012
 */
public class LookUpMapGenerator {
    // The list of sub dictionaries that help to generate the fast look up map
    // the last dictionary with word longer or equal to NUM_DICS
    private List<TreeSet<String>> dics;
    // The hash set only used to check whether a word is in the dictionary when
    // word of length -1 is also considered a related word
    private HashSet<String> dicSet;

    /**
     * The number of sub dictionaries. 5 is the optimized number for most
     * scenario.
     */
    public static final int NUM_DICS = 5;

    /**
     * The number of fast loop up map generating thread.
     */
    public static final int NUM_MAP_GEN_THREAD = 8;

    // True if word length can vary
    private boolean variableLength;
    // True if word letter can vary
    private boolean variableLetter;
    // The original dictionary
    private List<String> oridic;
    // The number of finished dictionaries
    private volatile int finishedDic;
    // The number of finished map generating thread
    private volatile int finishedMapThread;

    /**
     * The sub dictionary initializer. A sub dictionary is a TreeSet<String>
     * such that words are sorted if the id th letter is omitted. Therefore,
     * when generating the look up map the program won't need to go over the
     * whole dictioanry and use the expensive related() or longerRelated()
     * method to compare whether two String are related or not.
     *
     * @author Tianyu
     * @version Sep 5, 2012
     */
    class DicInitializer extends Thread {
        private int id;

        /**
         * Constructor for DicInitializer
         *
         * @param id
         *            the id of this thread
         */
        DicInitializer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            TreeSet<String> set = dics.get(id);
            Iterator<String> it = oridic.iterator();
            String word;
            while (it.hasNext()) {
                word = it.next();
                if (word.length() > id) {
                    set.add(word);
                }
            }
            synchronized (LookUpMapGenerator.this) {
                finishedDic++;
                System.out.println("Subdictionary " + id
                        + " has been generated.");
                // notify the LoopUpMapGenerator when all threads are finished
                if (finishedDic == NUM_DICS) {
                    LookUpMapGenerator.this.notify();
                }
            }

        }
    }

    /**
     * Constructor for LookUpMapGenerator
     *
     * @param dic
     *            the dictionary upon which this map should generate
     * @param variableLetter
     *            true if any word letter can vary
     * @param variableLength
     *            true if word length can vary
     *
     */
    public LookUpMapGenerator(TreeSet<String> dic,
            boolean variableLetter, boolean variableLength) {
        finishedDic = 0;
        finishedMapThread = 0;

        System.out.println("NUM_DICS = " + NUM_DICS
                + ". Looking for exact length match: " + variableLength);
        this.variableLetter = variableLetter;
        this.variableLength = variableLength;
        dics = new ArrayList<TreeSet<String>>(NUM_DICS);
        // Since dicSet is only to generate shorter related word for any given
        // word, it can be null if variableLength is false
        if (!variableLength) {
            dicSet = null;
        }
        else {
            dicSet = new HashSet<String>(dic);
        }

        oridic = new ArrayList<String>(dic);
        // initialize the sub dictionaries with the SmartComparator that omit a
        // certain letter
        for (int i = 0; i < NUM_DICS - 1; i++) {
            dics.add(new TreeSet<String>(new SmartComparator(i)));
        }
        // the last dictionary is a normal one so there don't need to be a smart
        // comparator
        dics.add(new TreeSet<String>());

    }

    /**
     * Constructor for the smart comparator.
     *
     * @author Tianyu Geng (tony1)
     * @version Aug 30, 2012
     */
    public class SmartComparator implements Comparator<String> {
        private int omission;

        /**
         * Constructor for SmartComparator.
         *
         * @param omission
         *            the letter to omit
         */
        public SmartComparator(int omission) {
            this.omission = omission;
        }

        @Override
        public int compare(String arg0, String arg1) {

            char[] str0 = arg0.toCharArray();
            char[] str1 = arg1.toCharArray();
            char c0 = str0[omission];
            char c1 = str1[omission];
            for (int i = omission; i < str0.length - 1; i++) {
                str0[i] = str0[i + 1];
            }
            str0[str0.length - 1] = c0;
            for (int i = omission; i < str1.length - 1; i++) {
                str1[i] = str1[i + 1];
            }
            str1[str1.length - 1] = c1;
            for (int i = 0; i < (str0.length < str1.length ? str0.length
                    : str1.length); i++) {
                if (str0[i] != str1[i]) {
                    return str0[i] - str1[i];
                }
            }
            return str0.length < str1.length ? -1 : 1;

        }
    }

    /**
     * Initialize all the sub dictionaries
     *
     * @throws InterruptedException
     *             This exception should never thrown
     */
    private void initializeDics() throws InterruptedException {

        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < NUM_DICS; i++) {
            exec.execute(new DicInitializer(i));
        }
        exec.shutdown();
        synchronized (this) {
            while (finishedDic != NUM_DICS) {
                this.wait();
            }
        }
    }

    /**
     * Find the related words given a source word. This method takes advantage
     * of the subdictioanries and is used to genreate the fast loop up map.
     *
     * @param word
     *            the source word
     * @return a list of words related to the source word
     */
    private List<String> relatedWords(String word) {
        List<String> result = new ArrayList<String>();
        int length = word.length();
        if (variableLetter) {
            // extract the related words from first NUM_DICS-1 dictionaries
            for (int i = 0; i < NUM_DICS - 1 && i < length; i++) {
                // the start and end of the interesting area in the TreeSet
                char[] start = word.toCharArray();
                char[] end = start.clone();
                // used to limit the searching range
                start[i] = ' ';
                end[i] = '{';

                Iterator<String> it =
                        dics.get(i)
                                .subSet(new String(start),
                                        new String(end)).iterator();
                // add all related words to the resulted list from the
                // interesting area
                while (it.hasNext()) {
                    String relatedWord = it.next();
                    if (relatedWord.length() == length
                            && relatedWord.charAt(i) != word.charAt(i)) {
                        result.add(relatedWord);
                    }
                }
            }
            // extract the related words from the last dictionary
            if (length >= NUM_DICS) {
                String start = word.substring(0, NUM_DICS - 1) + " ";

                String end = word.substring(0, NUM_DICS - 1) + "{";
                Iterator<String> it =
                        dics.get(NUM_DICS - 1).subSet(start, end)
                                .iterator();
                while (it.hasNext()) {
                    String relatedWord = it.next();
                    if (relatedWord.length() == length
                            && related(relatedWord, word)) {
                        result.add(relatedWord);
                    }
                }
            }
        }
        if (variableLength) {
            // for +1 letter words:
            int longer = length + 1;
            for (int i = 0; i < NUM_DICS - 1 && i < longer; i++) {
                // the start and end of the interesting area in the TreeSet
                char[] start = new char[longer];
                for (int j = 0; j < longer; j++) {
                    if (j < i) {
                        start[j] = word.charAt(j);
                    }
                    else if (j > i) {
                        start[j] = word.charAt(j - 1);
                    }
                }
                char[] end = start.clone();
                start[i] = ' ';
                end[i] = '{';
                Iterator<String> it =
                        dics.get(i)
                                .subSet(new String(start),
                                        new String(end)).iterator();
                // add all related words to the resulted list from the
                // interesting area
                while (it.hasNext()) {
                    String relatedWord = it.next();
                    if (relatedWord.length() == longer) {
                        result.add(relatedWord);
                    }
                }
            }
            // extract the +1 related words from the last dictionary
            if (longer >= NUM_DICS) {
                String start = word.substring(0, NUM_DICS - 1) + " ";

                String end = word.substring(0, NUM_DICS - 1) + "{";
                Iterator<String> it =
                        dics.get(NUM_DICS - 1).subSet(start, end)
                                .iterator();
                while (it.hasNext()) {
                    String relatedWord = it.next();
                    if (relatedWord.length() == longer
                            && longerRelated(relatedWord, word)) {
                        result.add(relatedWord);
                    }
                }
            }
            // find the shorter related words;
            String lastShorterWord = null;
            for (int i = 0; i < length - 1; i++) {
                String shorterWord =
                        word.substring(0, i) + word.substring(i + 1);
                if (dicSet.contains(shorterWord)
                        && !(shorterWord.equals(lastShorterWord))) {
                    result.add(shorterWord);
                    lastShorterWord = shorterWord;
                }

            }
            String shorterWord = word.substring(0, length - 1);
            if (dicSet.contains(shorterWord)) {
                result.add(shorterWord);
            }
        }
        return result;
    }

    /**
     * Determine whether two same-length words are related or not
     *
     * @param word1
     *            the first word
     * @param word2
     *            the second word
     * @return true if the two words are related
     * @precondition the two words must be of the same length
     */
    private boolean related(String word1, String word2) {
        int diff = 0;
        for (int i = NUM_DICS - 1; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                diff++;
                if (diff != 1)
                    return false;
            }
        }
        return diff == 1;
    }

    /**
     * Determine whether the two words are related with the longer one letter
     * longer than the source word
     *
     * @param longer
     *            the longer word
     * @param src
     *            the source word
     * @return true if the two words are related
     * @precondition the longer word must be one letter longer than the source
     *               word
     */
    private boolean longerRelated(String longer, String src) {
        int diff = 0;
        int j = NUM_DICS - 1;
        for (int i = NUM_DICS - 1; i < src.length(); i++, j++) {
            if (src.charAt(i) != longer.charAt(j)) {
                diff++;
                if (diff > 1)
                    return false;
                if (i == src.length() - 1 && diff == 1)
                    return false;
                i--;
            }
        }
        return diff < 2;

    }

    /**
     * Generate the fast look up map
     *
     * @return the genreate map
     * @throws InterruptedException
     *             This exception should never be thrown
     */
    public synchronized HashMap<String, List<String>> generateMap()
            throws InterruptedException {
        long startTime = new GregorianCalendar().getTimeInMillis();
        initializeDics();

        HashMap<String, List<String>> map =
                new HashMap<String, List<String>>();

        ExecutorService exec = Executors.newCachedThreadPool();
        int length = oridic.size() / NUM_MAP_GEN_THREAD;
        System.out.println("Original dictionary size: " + oridic.size());
        for (int i = 0; i < NUM_MAP_GEN_THREAD; i++) {

            int start = length * i;
            int end;

            if (i == NUM_MAP_GEN_THREAD - 1) {
                end = oridic.size();
            }
            else {
                end = length * (i + 1) + 1;
            }
            System.out.println("Thread " + i + " start from index "
                    + start + " to " + end);
            ListIterator<String> it = oridic.listIterator(start);
            exec.execute(new MapInitializer(map, it, end));

        }
        exec.shutdown();
        while (finishedMapThread != NUM_MAP_GEN_THREAD) {
            this.wait();
        }

        long endTime = new GregorianCalendar().getTimeInMillis();
        System.out.println("Fast look up map has been generated.");
        System.out.println("The preparation process used "
                + (endTime - startTime) / 1000.0 + " seconds.");
        return map;
    }

    /**
     * Threads to generate part of the map.
     *
     * @author Tianyu
     * @version Sep 5, 2012
     */
    class MapInitializer extends Thread {
        private Map<String, List<String>> map;

        private ListIterator<String> it;
        private int end;

        /**
         * Constructor for MapInitializer
         *
         * @param map
         *            the map to put word relationships
         * @param it
         *            the list iterator pointing to the place where this thread
         *            should start from
         * @param end
         *            the position where this thread should stop
         */
        public MapInitializer(Map<String, List<String>> map,
                ListIterator<String> it, int end) {
            this.map = map;
            this.it = it;
            this.end = end;
        }

        /**
         *
         */
        @Override
        public void run() {
            while (it.nextIndex() != end) {
                String word = it.next();

                List<String> words = relatedWords(word);
                if (words.size() != 0) {

                    synchronized (map) {
                        map.put(word, words);
                    }
                }
            }
            // make sure only one thread is writing the map
            synchronized (LookUpMapGenerator.this) {
                System.out.println("Map generating thread "
                        + finishedMapThread++ + " has finished.");
                if (finishedMapThread == NUM_MAP_GEN_THREAD) {
                    LookUpMapGenerator.this.notify();
                }
            }
        }
    }
}

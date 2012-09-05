/**
 *
 */
package cs2506.wordladder;

import java.util.HashSet;

import java.util.ListIterator;

import java.util.Map;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import java.util.GregorianCalendar;

import java.util.Calendar;

import java.util.Collection;

import java.util.HashMap;

import java.util.SortedSet;

import java.util.Iterator;

import java.util.ArrayList;

import java.util.List;

import java.util.TreeSet;

/**
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
public class LookUpMapGenerator {
    private List<TreeSet<String>> dics;
    private HashSet<String> dicSet;
    public static final int NUM_DICS = 5;
    public static final int NUM_MAP_GEN_THREAD = 8;

    private boolean sameLengthWord;
    private List<String> oridic;
    private volatile int finishedDic;
    private volatile int finishedMapThread;

    class DicInitializer implements Runnable {
        private int i;

        DicInitializer(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            TreeSet<String> set = dics.get(i);
            Iterator<String> it = oridic.iterator();
            String word;
            while (it.hasNext()) {
                word = it.next();
                if (word.length() > i) {
                    set.add(word);
                }
            }
            synchronized (LookUpMapGenerator.this) {
                finishedDic++;
                System.out.println("Subdictionary " + i
                        + " has been generated.");
                if (finishedDic == NUM_DICS) {

                    LookUpMapGenerator.this.notify();
                }
            }

        }
    }

    public LookUpMapGenerator(TreeSet<String> dic, boolean sameLengthWord) {
        finishedDic = 0;
        finishedMapThread = 0;

        System.out.println("NUM_DICS = " + NUM_DICS
                + ". Looking for exact length match: " + sameLengthWord);
        this.sameLengthWord = sameLengthWord;
        dics = new ArrayList<TreeSet<String>>(NUM_DICS);
        if (sameLengthWord) {
            dicSet = null;
        }
        else {
            dicSet = new HashSet(dic);
        }
        oridic = new ArrayList(dic);

        for (int i = 0; i < NUM_DICS; i++) {
            dics.add(new TreeSet<String>(new SmartComparator(i)));
        }

    }

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

    public List<String> relatedWords(String word) {
        List<String> result = new ArrayList<String>();
        int length = word.length();

        // extract the related words from first NUM_DICS-1 dictionaries
        for (int i = 0; i < NUM_DICS - 1 && i < length; i++) {
            // the start and end of the interesting area in the TreeSet
            char[] start = word.toCharArray();
            char[] end = start.clone();
            start[i] = ' ';
            end[i] = '{';

            Iterator<String> it =
                    dics.get(i)
                            .subSet(new String(start), new String(end))
                            .iterator();
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
                    dics.get(NUM_DICS - 1).subSet(start, end).iterator();
            while (it.hasNext()) {
                String relatedWord = it.next();
                if (relatedWord.length() == length
                        && related(relatedWord, word)) {
                    result.add(relatedWord);
                }
            }
        }
        if (!sameLengthWord) {
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

    public boolean longerRelated(String longer, String ori) {
        int diff = 0;
        int j = NUM_DICS - 1;
        for (int i = NUM_DICS - 1; i < ori.length(); i++, j++) {
            if (ori.charAt(i) != longer.charAt(j)) {
                diff++;
                if (diff > 1)
                    return false;
                if (i == ori.length() - 1 && diff == 1)
                    return false;

                i--;

            }
        }
        return diff < 2;

    }

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

    class MapInitializer implements Runnable {
        Map<String, List<String>> map;

        ListIterator<String> it;
        int end;

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

import java.io.PrintWriter;

import java.util.concurrent.Executors;

import java.util.concurrent.ExecutorService;

import java.util.List;

import java.util.Iterator;

import java.util.LinkedList;

import java.util.TreeSet;

import java.util.HashSet;

import java.util.ArrayList;

import java.util.Map;

/**
 * This class is specifically built for Part 2c and 2d. The two functional
 * methods are start2D() and start2C(). Since 2C is very easy to compete, I
 * didn't use multiple threads to for the calculation. On the other hand, 2d is
 * lightly more expensive. So I used multiple thread to compute the result. The
 * threads are working in a boss/worker model.
 *
 *
 * @author Tianyu Geng (tony1)
 * @version Sep 7, 2012
 */
public class AllRelatedWordsGenerator {
    /**
     * The number of threads that will run simultaneously
     */
    public static final int NUM_THREAD = 4;
    private List<String> dic;
    private Map<String, ArrayList<String>> vLetterMap;
    private Map<String, ArrayList<String>> vLengthMap;
    private TreeSet<String> resultBuffer;
    private LinkedList<String> checker;
    private volatile int threadCount;
    private ExecutorService exe;
    private PrintWriter wt;

    /**
     * Constructor for AllRelatedWordsGenerator
     *
     * @param dic
     *            the dictionary that contains all the words
     * @param vLetterMap
     *            the variable letter fast look up map
     * @param vLengthMap
     *            the variable length fast look up map
     * @param wt
     *            the print writer that output will go to
     */
    public AllRelatedWordsGenerator(List<String> dic,
            Map<String, ArrayList<String>> vLetterMap,
            Map<String, ArrayList<String>> vLengthMap, PrintWriter wt) {
        this.dic = dic;
        this.vLengthMap = vLengthMap;
        this.vLetterMap = vLetterMap;
        resultBuffer = new TreeSet<String>();

        // like the checker in AllLadderGenerator, this checker is to make sure
        // results are output in the correct order
        checker = new LinkedList<String>();
        threadCount = 0;
        exe = Executors.newCachedThreadPool();
        this.wt = wt;
    }

    /**
     * This method calculate the result for part 2d> It starts multiple threads
     * until the procedure is finished.
     *
     * @throws InterruptedException
     *             This exception should never been thrown
     */
    public void start2D() throws InterruptedException {

        Iterator<String> it = dic.iterator();

        while (it.hasNext()) {

            synchronized (this) {
                while (threadCount >= NUM_THREAD) {
                    this.wait();
                }
            }
            synchronized (this) {
                printResult();
                String word = it.next();
                if (vLetterMap.containsKey(word)
                        || vLengthMap.containsKey(word)) {
                    threadCount++;

                    checker.add(word + ",");
                    System.out.println("Finding related words for "
                            + word);
                    exe.execute(new GenThread(word));
                }
            }
        }
        // When all the threads finish, print the result if there is anything
        // left in the buffer.
        synchronized (this) {
            while (threadCount != 0) {
                this.wait();
            }

        }

        printResult();

    }

    /**
     * This method calculates the result for part 2c, which is trivial given the
     * vLengthMap.
     *
     * @param printWriterFor2cExclusively
     *            the PrintWriter 2c should output to
     */
    public void start2C(PrintWriter printWriterFor2cExclusively) {
        Iterator<String> it = dic.iterator();

        while (it.hasNext()) {
            String src = it.next();
            ArrayList<String> relatedWords = vLengthMap.get(src);
            if (relatedWords != null) {
                printWriterFor2cExclusively.print(src);
                for (String word : relatedWords) {
                    printWriterFor2cExclusively.print(", " + word);
                }
                printWriterFor2cExclusively.println();
            }
            printWriterFor2cExclusively.flush();

        }

    }

    /**
     * Print the result in the resultBuffer to the PrintWriter
     */
    private synchronized void printResult() {
        String first = null;
        while (resultBuffer.size() != 0
                && (first = resultBuffer.first()).startsWith(checker
                        .getFirst())) {
            System.out.println("Printing result for "
                    + checker.getFirst());
            wt.println(first);
            wt.flush();
            resultBuffer.remove(first);
            checker.removeFirst();

        }
    }

    /**
     * The thread to generate related words for part2d.
     *
     * @author Tianyu
     * @version Sep 8, 2012
     */
    class GenThread extends Thread {
        private String src;
        private HashSet<String> set;
        private StringBuilder sb;

        /**
         * Constructor for GenThread
         *
         * @param src
         *            the source word
         * @precondition src must be in one of the map
         */
        GenThread(String src) {
            this.src = src;
            set = new HashSet<String>();
            sb = new StringBuilder();
        }

        /**
         * Traverse the map with variable letters, and output all the
         * non-duplicated result to the StringBuilder
         *
         * @param str
         *            the source word to start the traverse
         */
        private void traverseLetterMap(String str) {
            ArrayList<String> relatedWords = vLetterMap.get(str);
            if (relatedWords == null) {
                return;
            }
            Iterator<String> it = relatedWords.iterator();
            while (it.hasNext()) {
                String word = it.next();
                if (set.add(word)) {
                    sb.append(", " + word);
                    traverseLetterMap(word);
                }
            }
        }

        /**
         * Traverse both map. When related words are from the variable letter
         * map, this method will recursively call itself. When related words are
         * from the variable length map, the method will call the
         * traverLetterMap method. In this way, if a source word has already
         * found any related words with +/- length, then the function won't find
         * any more related words with +/- length for the related words found in
         * the previous round. Thus ensuring that all words with length variable
         * are all closely related to the source words or any variable letter
         * words related to the source word.
         *
         * I think this is a pretty efficient way to satisfy part 2d's
         * complicated requirement.
         *
         * @param str
         */
        private void traverseAll(String str) {
            ArrayList<String> sameLengthWords = vLetterMap.get(str);
            if (sameLengthWords != null) {
                Iterator<String> it = sameLengthWords.iterator();
                while (it.hasNext()) {
                    String word = it.next();
                    if (set.add(word)) {
                        sb.append(", " + word);
                        traverseAll(word);
                    }
                }
            }
            ArrayList<String> variableLengthWords = vLengthMap.get(str);
            if (variableLengthWords != null) {
                Iterator<String> it = variableLengthWords.iterator();
                while (it.hasNext()) {
                    String word = it.next();
                    if (set.add(word)) {
                        sb.append(", " + word);
                        traverseLetterMap(word);
                    }
                }
            }
        }

        /**
         * The standard run method. It will notify the parent class instance
         * (boss) after this thread (worker) has finished.
         */
        public void run() {
            set.add(src);
            sb.append(src);
            traverseAll(src);
            synchronized (AllRelatedWordsGenerator.this) {

                resultBuffer.add(sb.toString());
                threadCount--;
                AllRelatedWordsGenerator.this.notify();
            }
        }
    }

}

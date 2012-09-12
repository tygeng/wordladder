import java.util.HashSet;

import java.util.GregorianCalendar;

import java.util.LinkedHashSet;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import java.util.LinkedList;

import java.io.PrintWriter;

import java.util.Iterator;

import java.util.Comparator;

import java.util.TreeSet;

import java.util.ArrayList;

import java.util.List;

import java.util.Map;

/**
 * This class find all the ladders given a fast look up map.
 *
 * @author Tianyu Geng (tony1)
 * @version Sep 5, 2012
 */
public class AllLadderGenerator {
    /**
     * The maximum number of thread allowed. 4 is good enough to achieve
     * performance since the bottle neck for this program to run on rlogin
     * cluster is I/O speed.
     */
    public static final int NUM_THREAD = 4;

    /**
     * The order less ladder class.
     *
     * @author Tianyu Geng
     * @version Sep 11, 2012
     */
    class Ladders extends ArrayList<String> {
        /**
         * Constructor for Ladders. A explicit Ladders class is to make sure the
         * HashSet regards ladders with same words but different orders the same
         * ladders.
         *
         * @param stack
         *            the stack to initialize this ladder with
         */
        public Ladders(LinkedHashSet<String> stack) {
            super(stack);
        }

        public int hashCode() {
            int result = 0;
            for (String word : this) {
                result += word.hashCode();
            }
            return result;
        }

        public boolean equals(Object other) {
            if (other == null
                    || !this.getClass().equals(other.getClass())
                    || this.size() != ((Ladders) other).size()
                    || !this.get(this.size() - 1).equals(
                            ((Ladders) other).get(this.size() - 1))) {
                return false;
            }
            Ladders o = (Ladders) other;
            for (String word : o) {
                if (!this.contains(word)) {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * This class represents the thread to generate all word ladders related to
     * the given word. The class carry out a depth search and traverse through
     * all the words and find all the ladders.
     *
     * @author Tianyu Geng (tony1)
     * @version Sep 5, 2012
     */
    class GenThread extends Thread {

        private String src; // the source word that word ladders will grow from
        private LinkedHashSet<String> stack; // the stack to keep track of the
                                             // depth search
        private TreeSet<Ladders> ladders; // the ladders this thread
                                          // has generated
        private HashSet<Ladders> set;

        /**
         * Constructor for GenThread
         *
         * @param src
         *            the source word from which word ladders will grow
         * @precondition the source word must be in the fast look up map
         */
        public GenThread(String src) {

            stack = new LinkedHashSet<String>();
            ladders = new TreeSet<Ladders>(new LadderComparator());

            this.src = src;
        }

        /**
         * The run method that will execute when this thread starts to execute
         */
        public void run() {
            // add the source word to the stack
            stack.add(src);
            set = new HashSet<Ladders>();

            // traverse this word, this is the enter of the depth search
            traverse(src);

            // Call the addLadder method in the parent class to add the newly
            // found ladder to the main collection.
            addLadders(ladders);

            // this thread has finished so it calls the parent class's
            // threadMinus to let it know it can create another thread now

            synchronized (AllLadderGenerator.this) {
                // notify the parent class to continue creating new threads
                threadCount--;
                AllLadderGenerator.this.notify();

            }

        }

        /**
         * The depth search method. This is a recursive function.
         *
         * @param str
         *            the word to search around
         */
        private void traverse(String str) {

            Iterator<String> it = map.get(str).iterator();
            // while the word still has words around, continue to traverse them
            while (it.hasNext()) {
                String word = it.next();
                // if the stack contains the word, it means this word has been
                // visited, so it should not be added to the current ladder
                if (!stack.contains(word)) {
                    stack.add(word);
                    // put this new ladder to the collection of ladders in this
                    // thread
                    Ladders currentLadders = new Ladders(stack);
                    if (set.add(currentLadders)) {
                        ladders.add(currentLadders);

                    }
                    // search around this new word
                    traverse(word);
                }

            }
            // remove this current word from the selection
            stack.remove(str);
        }

    }

    /**
     * The comparator to sort the ladders according to the last word and then
     * ladder length.
     *
     * This comparator doesn't need to consider the first ladder since it is
     * only used inside the GenThread class to sort ladders that start from the
     * same word.
     *
     * @author Tianyu
     * @version Sep 5, 2012
     */
    static class LadderComparator implements
            Comparator<ArrayList<String>> {

        public int
                compare(ArrayList<String> arg0, ArrayList<String> arg1) {

            if (!arg0.get(arg0.size() - 1).equals(
                    arg1.get(arg1.size() - 1))) {
                return arg0.get(arg0.size() - 1).compareTo(
                        arg1.get(arg1.size() - 1));
            }
            int diff = arg0.size() - arg1.size();
            if (diff == 0) {
                return 1;
            }
            return diff;
        }
    }

    private Map<String, ArrayList<String>> map; // the fast look up map

    // The big collection of buffers. it exists because different threads may
    // not finish searching at the same time. To keep their orders correct,
    // Early results will be kept in this cache area.
    private volatile TreeSet<TreeSet<Ladders>> resultBuffer;

    // the output writer
    private PrintWriter wt;

    // the checker to keep track of the order of the ladders. When a thread is
    // created, the source word it should work with is put into the checker, so
    // that when it finished, only the ladders match the first element in the
    // checker will be printed.
    private volatile LinkedList<String> checker;

    // the number of thread currently in execution
    private volatile int threadCount;

    // the dictionary that contains all the word
    private List<String> dic;
    private ExecutorService exec;

    private boolean certainLengthRequired;

    private int certainLength;

    /**
     * Constructor for AllLadderGenerator.
     *
     * @param map2
     *            the fast look up map
     * @param dic
     *            the dictionary that contains all the word one want to look for
     *            ladders
     * @param wt
     *            the PrintWriter to output result to
     * @param certainLength
     *            specify a number to limit the length of words in ladders
     *
     */
    public AllLadderGenerator(Map<String, ArrayList<String>> map2,
            List<String> dic, PrintWriter wt, int certainLength) {
        exec = Executors.newFixedThreadPool(NUM_THREAD + 1);
        this.dic = dic;
        resultBuffer = new TreeSet<TreeSet<Ladders>>(
        // This class is the ladder comparator for the big collection. It only
        // needs to compare the first word in the ladder.
                new Comparator<TreeSet<Ladders>>() {

                    @Override
                    public int compare(TreeSet<Ladders> o1,
                            TreeSet<Ladders> o2) {
                        int c1 =
                                o1.first().get(0)
                                        .compareTo(o2.first().get(0));
                        return c1;
                    }

                });
        checker = new LinkedList<String>();
        threadCount = 0;
        this.map = map2;
        this.wt = wt;
        this.certainLength = certainLength;
        certainLengthRequired = true;
    }

    /**
     * Constructor for AllLadderGenerator. Words of any length will be
     * considered.
     *
     * @param map2
     *            the fast look up map
     * @param dic
     *            the dictionary that contains all the word one want to look for
     *            ladders
     * @param wt
     *            the PrintWriter to output result to
     */
    public AllLadderGenerator(Map<String, ArrayList<String>> map2,
            List<String> dic, PrintWriter wt) {
        this(map2, dic, wt, 0);
        certainLengthRequired = false;
    }

    /**
     * Add a small collection of ladders to the big collection of ladders.
     *
     * @param ladders
     *            the small collection of ladders
     */
    private synchronized void addLadders(TreeSet<Ladders> ladders) {
        resultBuffer.add(ladders);
    }

    /**
     * Helps generating new thread to search around a given word.
     *
     * @param src
     *            the word to search around
     * @throws InterruptedException
     *             This interruption should never been thrown
     */
    /**
     * @param src
     * @throws InterruptedException
     */
    private void execute(String src) throws InterruptedException {
        // if (resultBuffer.size() > NUM_THREAD * 2) {
        // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        // }
        // else {
        // Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        // }

        synchronized (this) {
            // if no thread is idle, the main thread will be blocked
            while (threadCount >= NUM_THREAD) {
                this.wait();
            }
            printLadders();
            // System.out.println("Creating new thread for "+ src);
            threadCount++;
            checker.add(src);
            exec.execute(new GenThread(src));
            return;
        }

    }

    /**
     * Print ladders in the resultBuffer if the orders are correct
     */
    private void printLadders() {
        // System.out.println("inside printladders()");
        if (resultBuffer.isEmpty()) {
            return;
        }

        TreeSet<Ladders> currentFirst = resultBuffer.first();
        String first = checker.getFirst();

        while (first.equals(currentFirst.first().get(0))) {
            checker.removeFirst();
            resultBuffer.remove(currentFirst);
            // System.out.println("before printladders(TreeSet<ArrayList<String>> "
            // + first+" )");
            printLadders(currentFirst);
            System.out.println("Finished printing ladders for " + first);
            if (checker.isEmpty()) {
                break;
            }
            first = checker.getFirst();
        }
    }

    /**
     * Print the given ladders to the PrintWriter wt. This method will format
     * the output according to the requirement.
     *
     * @param ladders
     *            the given ladder
     */
    private void printLadders(TreeSet<Ladders> ladders) {
        // System.out.println("inside printladders(TreeSet<ArrayList<String>> )");
        Iterator<Ladders> it = ladders.iterator();
        StringBuilder sb = new StringBuilder();
        // System.out.println("before entering the while loop");
        while (it.hasNext()) {
            ArrayList<String> ladder = it.next();
            Iterator<String> ita = ladder.iterator();
            sb.append(ita.next());
            while (ita.hasNext()) {
                sb.append(", " + ita.next());
            }
            sb.append('\n');
        }
        // System.out.println("before printing to the output file");
        wt.print(sb.toString());
        wt.flush();
        // System.out.println("after flushing");
    }

    /**
     * Search all the ladders related to the source word
     *
     * @param src
     *            the source word
     * @throws InterruptedException
     *             this exception should not be thrown
     */
    public void start(String src) throws InterruptedException {

        if (!map.containsKey(src)) {
            return;
        }
        execute(src);
        printLadders();
        synchronized (this) {
            while (threadCount != 0) {
                this.wait();
            }
        }

        while (!checker.isEmpty()) {
            printLadders();
        }
    }

    /**
     * Start the process to search for all ladders. This method is the entry to
     * the seraching process.
     *
     * @throws InterruptedException
     *             this exception should never been thrown
     */
    public void start() throws InterruptedException {

        long startTime = new GregorianCalendar().getTimeInMillis();
        Iterator<String> srcIt = dic.iterator();

        while (srcIt.hasNext()) {
            String src = srcIt.next();

            if (!map.containsKey(src)
                    || (certainLengthRequired && src.length() != certainLength)) {
                continue;
            }
            System.out.println("Building ladders for " + src);
            // put the word into the checker so that the checker will know
            // the
            // order of words

            // System.out.println("Before executing "+src);
            execute(src);
            // System.out.println("After executing " + src);

        }
        synchronized (this) {

            while (threadCount != 0) {
                this.wait();
            }
        }
        while (checker.size() != 0) {
            printLadders();
        }
        long endTime = new GregorianCalendar().getTimeInMillis();
        System.out.println("Generating all ladders used "
                + (endTime - startTime) / 1000.0 + " seconds.");
        System.out.println("Cleaning Cache, please wait...");

    }
}
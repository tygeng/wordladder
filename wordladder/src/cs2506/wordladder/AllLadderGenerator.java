/**
 *
 */
package cs2506.wordladder;

import java.util.concurrent.TimeUnit;

import java.util.GregorianCalendar;

import java.util.LinkedHashSet;

import java.io.Writer;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import java.util.LinkedList;

import java.io.PrintWriter;

import java.util.Iterator;

import java.util.Comparator;

import java.util.TreeSet;

import java.util.ArrayList;

import java.util.HashSet;

import java.util.List;

import java.util.Map;

/**
 *
 * @author Tianyu Geng (tony1)
 * @version Sep 3, 2012
 */

public class AllLadderGenerator {
    private static final int NUM_THREAD = 4;

    public static class CheckerElement {
        private String src;
        private String dst;

        public CheckerElement(String src, String dst) {
            this.src = src;
            this.dst = dst;
        }

        public boolean equals(Object other) {
            if (!(other instanceof CheckerElement))
                return false;
            return src.equals(((CheckerElement) other).src)
                    && dst.equals(((CheckerElement) other).dst);
        }

        public String getDst() {
            return dst;
        }

        public String getSrc() {
            return src;
        }

        public int hashCode() {
            return src.hashCode() + dst.hashCode();
        }

        public String toString() {
            return src + " " + dst;
        }
    }

    public class GenThread extends Thread {

        private String src;
        private String dst;
        private LinkedList<String> stack;
        private HashSet<String> set;
        private TreeSet<ArrayList<String>> ladders;
        private final int id;

        public GenThread(int id, String src, String dst) {
            this.id = id;
            stack = new LinkedList<String>();
            set = new HashSet<String>();
            ladders =
                    new TreeSet<ArrayList<String>>(new LadderComparator());
            this.src = src;
            this.dst = dst;

            idleThreadIndicator[id] = false;

        }

        public TreeSet<ArrayList<String>> getLadders() {
            return ladders;
        }

        public void run() {

            stack.add(src);
            set.add(src);
            set.add(dst);
            traverse(src);
            if (!ladders.isEmpty()) {

                addLadders(ladders);

            }
            else {

                cleanChecker(new CheckerElement(src, dst));
            }
            idleThreadIndicator[id] = true;
            threadMinus();
            synchronized (AllLadderGenerator.this) {

                AllLadderGenerator.this.notify();

            }

        }

        public void traverse(String str) {

            Iterator<String> it = map.get(str).iterator();

            while (it.hasNext()) {
                String word = it.next();
                if (word.equals(dst)) {
                    stack.add(word);

                    ladders.add(new ArrayList<String>(stack));
                    stack.remove(word);
                    continue;

                }
                if (!set.contains(word)) {
                    set.add(word);
                    stack.add(word);
                    traverse(word);

                }

            }
            set.remove(str);
            stack.remove(str);
        }

    }

    static class LadderComparator implements
            Comparator<ArrayList<String>> {

        public int
                compare(ArrayList<String> arg0, ArrayList<String> arg1) {
            int diff = arg0.size() - arg1.size();
            if (diff == 0) {
                return 1;
            }
            return diff;
        }

    }

    private Map<String, List<String>> map;
    private volatile TreeSet<TreeSet<ArrayList<String>>> resultBuffer;
    private PrintWriter wt;
    private volatile LinkedHashSet<CheckerElement> checker;

    private volatile int threadCount;
    private List<String> dic;
    private boolean sameLengthWord;

    private volatile boolean[] idleThreadIndicator;

    private ExecutorService exec;

    public AllLadderGenerator(Map<String, List<String>> map,
            List<String> dic, PrintWriter wt, boolean sameLengthWord) {
        this.sameLengthWord = sameLengthWord;
        exec = Executors.newFixedThreadPool(NUM_THREAD + 1);
        this.dic = dic;
        resultBuffer =
                new TreeSet<TreeSet<ArrayList<String>>>(
                        new Comparator<TreeSet<ArrayList<String>>>() {

                            @Override
                            public int compare(
                                    TreeSet<ArrayList<String>> o1,
                                    TreeSet<ArrayList<String>> o2) {
                                int c1 =
                                        o1.first()
                                                .get(0)
                                                .compareTo(
                                                        o2.first().get(0));
                                if (c1 == 0) {
                                    int c2 =
                                            o1.first()
                                                    .get(o1.first()
                                                            .size() - 1)
                                                    .compareTo(
                                                            o2.first()
                                                                    .get(o2.first()
                                                                            .size() - 1));
                                    return c2;
                                }
                                return c1;
                            }

                        });
        checker = new LinkedHashSet<CheckerElement>();
        threadCount = 0;
        this.map = map;
        this.wt = wt;

        idleThreadIndicator = new boolean[NUM_THREAD];

        for (int i = 0; i < NUM_THREAD; i++) {
            idleThreadIndicator[i] = true;

        }
    }

    public synchronized void
            addLadders(TreeSet<ArrayList<String>> ladders) {
        resultBuffer.add(ladders);
    }

    public synchronized void cleanChecker(CheckerElement ce) {
        checker.remove(ce);
    }

    public void execute(String src, String dst)
            throws InterruptedException {
        synchronized (this) {
            while (!threadIdle()) {
                this.wait(100);
            }
        }
        for (int i = 0; i < NUM_THREAD; i++) {
            if (idleThreadIndicator[i]) {
                updateChecker(src, dst);
                exec.execute(new GenThread(i, src, dst));
                threadPlus();
                return;
            }
        }
    }

    public synchronized void printLadders() {
        if (resultBuffer.isEmpty()) {
            return;
        }

        TreeSet<ArrayList<String>> currentFirst = resultBuffer.first();
        Iterator<CheckerElement> it = checker.iterator();
        CheckerElement first = it.next();

        if (first.getSrc().equals(currentFirst.first().get(0))
                && first.getDst().equals(
                        currentFirst.first().get(
                                currentFirst.first().size() - 1))) {
            cleanChecker(first);
            resultBuffer.remove(currentFirst);
            printLadders(currentFirst);
        }
    }

    public synchronized void printLadders(
            TreeSet<ArrayList<String>> ladders) {
        Iterator<ArrayList<String>> it = ladders.iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            ArrayList ladder = it.next();
            Iterator<String> ita = ladder.iterator();
            sb.append(ita.next());
            while (ita.hasNext()) {
                sb.append(", " + ita.next());
            }
            sb.append('\n');
        }
        wt.print(sb.toString());
        wt.flush();
    }

    public void start() throws InterruptedException {
        long startTime = new GregorianCalendar().getTimeInMillis();
        Iterator<String> srcIt = dic.iterator();

        while (srcIt.hasNext()) {
            String src = srcIt.next();
            System.out.println("Building ladders for " + src);
            Iterator<String> dstIt = dic.iterator();
            if (!map.containsKey(src)) {
                continue;
            }
            while (dstIt.hasNext()) {
                String dst = dstIt.next();
                if (sameLengthWord && src.length() != dst.length()) {
                    continue;
                }

                if (src.equals(dst) || !map.containsKey(dst)) {
                    continue;
                }

                execute(src, dst);
                printLadders();

            }
        }
        synchronized (this) {

            while (threadCount != 0) {
                this.wait();

            }

        }

        while (!checker.isEmpty()) {
            printLadders();
        }

        long endTime = new GregorianCalendar().getTimeInMillis();
        System.out.println("Generating all ladders used "
                + (endTime - startTime) / 1000.0 + " seconds.");

    }

    public synchronized boolean threadIdle() {
        return threadCount != NUM_THREAD;
    }

    public synchronized void threadMinus() {
        threadCount--;
    }

    public synchronized void threadPlus() {
        threadCount++;
    }

    public synchronized void updateChecker(String start, String end) {
        checker.add(new CheckerElement(start, end));

    }
}
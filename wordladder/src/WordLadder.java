import java.util.GregorianCalendar;

import java.io.FileOutputStream;

import java.io.FileInputStream;

import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;

import java.util.Arrays;

import java.util.HashSet;

import java.io.PrintWriter;

import java.util.concurrent.TimeUnit;

import java.util.Map;

import java.util.List;

import java.util.ArrayList;

import java.io.IOException;

import java.util.TreeSet;

import java.io.FileNotFoundException;

import java.io.FileReader;

import java.io.BufferedReader;

/**
 * This is main class.
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
public class WordLadder {
    // I/O
    static PrintWriter part1;
    static PrintWriter part2a;
    static PrintWriter part2b;
    static PrintWriter part2c;
    static PrintWriter part2d;
    static BufferedReader input;
    static BufferedReader dicText;
    // Dictionary
    static TreeSet<String> dic;
    // Fast look up dictioanry
    static Map<String, ArrayList<String>> vLetterMap;
    static Map<String, ArrayList<String>> vLengthMap;
    static Map<String, ArrayList<String>> bigMap;
    // Source and destination words
    static String src;
    static String dst;

    /**
     * The entry point of this program
     *
     * @param args
     *            no arguments needed for this program
     * @throws InterruptedException
     *             This interruption should never be thrown
     * @throws IOException
     *             thrown if file I/O failed
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InterruptedException,
            IOException {
        try {
            if (args.length == 0) {
                long startTime =
                        new GregorianCalendar().getTimeInMillis();
                part1 = new PrintWriter("part1.txt");
                part2a = new PrintWriter("part2a.txt");
                part2b = new PrintWriter("part2b.txt");
                part2c = new PrintWriter("part2c.txt");
                part2d = new PrintWriter("part2d.txt");
                input = new BufferedReader(new FileReader("input.txt"));
                dicText =
                        new BufferedReader(new FileReader(
                                "dictionary.txt"));
                dic = new TreeSet<String>();
                src = input.readLine();
                dst = input.readLine();

                if (src == null || dst == null
                        || src.length() != dst.length()) {
                    System.err.println("Input file corrupted.");
                    System.err.println("source word: " + src
                            + " destination word: " + dst);
                }
                String word = dicText.readLine();
                while (word != null) {
                    dic.add(word.toLowerCase());
                    word = dicText.readLine();
                }
                ArrayList<String> dicList = new ArrayList<String>(dic);
                LookUpMapGenerator vLetterGen =
                        new LookUpMapGenerator(dic, true, false);

                vLetterMap = vLetterGen.generateMap();
                LookUpMapGenerator vLengthGen =
                        new LookUpMapGenerator(dic, false, true);
                vLengthMap = vLengthGen.generateMap();

                // Part 1
                // ---------------------------------------------------------
                ShortestLadderGenerator slg =
                        new ShortestLadderGenerator(vLetterMap);
                String ladder = slg.findShortestPath(src, dst);

                if (ladder != null) {

                    part1.println(ladder);
                }
                part1.close();
                // Part 2a
                // --------------------------------------------------------
                AllLadderGenerator alg2a =
                        new AllLadderGenerator(vLetterMap, dicList,
                                part2a, src.length());
                alg2a.start();
                // Part 2b
                // --------------------------------------------------------
                AllLadderGenerator alg2b =
                        new AllLadderGenerator(vLetterMap,
                                new ArrayList<String>(dic), part2b);
                alg2b.start();
                part2b.close();

                // Part 2c and
                // 2d--------------------------------------------------
                AllRelatedWordsGenerator arwg =
                        new AllRelatedWordsGenerator(dicList, vLetterMap,
                                vLengthMap, part2d);
                arwg.start2C(part2c);
                part2c.close();
                arwg.start2D();
                part2d.close();
                System.out.println("All parts have been finished.");
                long endTime = new GregorianCalendar().getTimeInMillis();
                System.out.println("The whole procedure takes "
                        + ((endTime - startTime) / 1000.0) + " seconds.");
                System.exit(0);
            }
            // the tiny extra parts.
            else {
                ArrayList<String> words = new ArrayList<String>();
                dic = new TreeSet<String>();
                boolean dicSpecified = false;
                boolean bigMapReadFromCache = false;
                for (int i = 0; i < args.length; i++) {
                    if (args[i].startsWith("-d")) {
                        try {
                            dicText =
                                    new BufferedReader(new FileReader(
                                            args[++i]));
                            String word = dicText.readLine();
                            while (word != null) {
                                dic.add(word.toLowerCase());
                                word = dicText.readLine();
                            }
                            dicSpecified = true;

                        }
                        catch (IOException e) {
                            System.out
                                    .println("Invalid dictionary file.");
                        }
                    }
                    else {
                        words.add(args[i]);
                    }

                }
                if (!dicSpecified) {
                    ObjectInputStream objIn = null;

                    try {
                        objIn =
                                new ObjectInputStream(
                                        new FileInputStream(".map"));

                        Thread dotThread = new Thread(new Runnable() {
                            public synchronized void run() {
                                System.out.print("");
                                while (true) {
                                    System.out.print(".");

                                    try {
                                        TimeUnit.MILLISECONDS.sleep(200);
                                    }
                                    catch (InterruptedException e) {
                                        // nothing needs to be done here.
                                    }

                                }

                            }
                        });
                        dotThread.setPriority(Thread.MAX_PRIORITY);
                        dotThread.start();

                        bigMap =
                                (Map<String, ArrayList<String>>) objIn
                                        .readObject();
                        dotThread.interrupt();

                        objIn.close();
                        bigMapReadFromCache = true;
                        System.out.println();

                    }
                    catch (ClassNotFoundException e) {
                        System.out
                                .println("Dictionary file corrupted, please use "
                                        + "'-d <dictionary_name>' to force the program to create new dictionary cache.");
                    }
                    catch (IOException e) {
                        dicText =
                                new BufferedReader(new FileReader(
                                        "dictionary.txt"));
                        String word = dicText.readLine();
                        while (word != null) {
                            dic.add(word.toLowerCase());
                            word = dicText.readLine();
                        }
                    }
                }
                if (!bigMapReadFromCache) {
                    LookUpMapGenerator mapGen =
                            new LookUpMapGenerator(dic, true, true);
                    bigMap = mapGen.generateMap();
                    ObjectOutputStream objOut =
                            new ObjectOutputStream(new FileOutputStream(
                                    ".map"));
                    System.out
                            .println("Creating new fast look up map on the disk.");
                    objOut.writeObject(bigMap);
                    objOut.close();
                }
                ShortestLadderGenerator slg =
                        new ShortestLadderGenerator(bigMap);
                for (int i = 0; i < words.size() - 1; i++) {
                    System.out.println(slg.findShortestPath(words.get(i),
                            words.get(++i)));
                }
                System.exit(0);
            }
        }
        catch (IOException e) {
            System.err.println("File input/output error.");
            e.printStackTrace();
        }
        finally {
            if (part1 != null) {
                part1.close();
            }
            if (part2a != null) {
                part2a.close();
            }
            if (part2b != null) {
                part2b.close();
            }
            if (part2c != null) {
                part2c.close();
            }
            if (part2d != null) {
                part2d.close();
            }
            if (input != null) {
                input.close();
            }
        }

    }
}

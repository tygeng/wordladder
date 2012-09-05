/**
 *
 */
package cs2506.wordladder;

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
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
public class WordLadder {

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        try {
            BufferedReader dic =
                    new BufferedReader(new FileReader(
                            "short+dictionary.txt"));
            TreeSet<String> set = new TreeSet<String>();
            String word;
            while ((word = dic.readLine()) != null) {
                set.add(word.toLowerCase());
            }

            LookUpMapGenerator gen = new LookUpMapGenerator(set, true);
            // boolean temp = gen.longerRelated("alkamine", "alkamin");

            Map<String, List<String>> map = gen.generateMap();

            // ShortestLadderGenerator ladderGen =
            // new ShortestLadderGenerator(map);
            //
            // for (int i = 0; i < 1; i++) {
            // System.out.println(ladderGen.findShortestPath("shine",
            // "shine"));
            //
            // System.out.println(ladderGen.findShortestPath("double",
            // "create"));
            // System.out.println(ladderGen.findShortestPath("source",
            // "destination"));
            // System.out.println(ladderGen.findShortestPath("if",
            // "shine"));
            // System.out.println(ladderGen.findShortestPath("good",
            // "funny"));
            // System.out.println(ladderGen.findShortestPath("sheep",
            // "book"));
            // System.out.println(ladderGen.findShortestPath("cosmos",
            // "shine"));
            // System.out.println(ladderGen.findShortestPath("great",
            // "place"));
            // System.out.println(ladderGen.findShortestPath("horn",
            // "magic"));
            // System.out.println(ladderGen.findShortestPath("rabbit",
            // "magic"));
            // System.out.println(ladderGen.findShortestPath("cryptography",
            // "geology"));
            // System.out.println(ladderGen.findShortestPath("summer",
            // "winter"));
            // System.out.println(ladderGen.findShortestPath("spring",
            // "autumn"));
            // System.out.println(ladderGen.findShortestPath("computer",
            // "apple"));
            // System.out.println(ladderGen.findShortestPath("screen",
            // "mouse"));
            // System.out.println(ladderGen.findShortestPath("cat",
            // "dog"));
            // System.out.println(ladderGen.findShortestPath("china",
            // "america"));
            // System.out.println(ladderGen.findShortestPath("japan",
            // "korea"));
            //
            // System.out.println(ladderGen.findShortestPath("final",
            // "initial"));
            //
            // }
            PrintWriter wt = new PrintWriter("c:/output.txt");

            ArrayList<String> testlist = new ArrayList<String>();
            testlist.add("ball");
            testlist.add("sail");

            AllLadderGenerator alg =
                    new AllLadderGenerator(map, testlist, wt, true);
//            AllLadderGenerator.GenThread gt =  alg.new GenThread(1, "sail", "ball");
//
//            System.out.println(map.get("tall"));
//            gt.run();
//            System.out.println(gt.getLadders());
            alg.start();




            wt.close();
            // AllLadderGenerator.CheckerElement ce1 = new
            // AllLadderGenerator.CheckerElement("h","g");
            // AllLadderGenerator.CheckerElement ce2 = new
            // AllLadderGenerator.CheckerElement("h","g");
            // System.out.println(ce1.hashCode()==ce2.hashCode());
            // System.out.println(ce1.equals(ce2));
            // HashSet<AllLadderGenerator.CheckerElement> hashset = new
            // HashSet<AllLadderGenerator.CheckerElement>();
            // hashset.add(ce1);
            // System.out.println(hashset.remove(ce2));
            // AllLadderGenerator.GenThread gt = alg.new
            // GenThread(1,"act","art");
            // gt.run();
            // System.out.println(gt.getLadders());

            System.exit(0);

        }
        catch (IOException e) {
            System.err.println("Error opening dictionary!");
        }

    }
}

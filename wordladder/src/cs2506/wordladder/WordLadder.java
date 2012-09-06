/**
 *
 */
package cs2506.wordladder;

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
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
public class WordLadder {
    static PrintWriter wt;
    static BufferedReader rd;
    static ObjectOutputStream objOut;
    static ObjectInputStream objIn;
    static Map<String, ArrayList<String>> map;
    static final String CACHE = "FastLookUpMap";

    /**
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException,
            IOException {
        try {

            rd =
                    new BufferedReader(new FileReader(
                            "medium+dictionary.txt"));
            TreeSet<String> dic = new TreeSet<String>();
            String line = rd.readLine();
            while (line != null) {
                dic.add(line.toLowerCase());
                line = rd.readLine();
            }

            LookUpMapGenerator mpGen =
                    new LookUpMapGenerator(dic, true, true);
            map = mpGen.generateMap();

            ShortestLadderGenerator slg =
                    new ShortestLadderGenerator(map);
            System.out.println(slg.findShortestPath("and", "create"));
//            wt = new PrintWriter("output.txt");
//            AllLadderGenerator alg =
//                    new AllLadderGenerator(map,
//                            new ArrayList<String>(dic), wt);

//            alg.start();
            System.exit(0);
        }
        finally {
            if (wt != null) {
                wt.close();
            }
            if (rd != null) {
                rd.close();
            }
        }

    }
}

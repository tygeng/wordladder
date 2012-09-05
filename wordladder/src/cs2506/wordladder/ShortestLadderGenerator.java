/**
 *
 */
package cs2506.wordladder;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.TimeUnit;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.Set;

import java.util.HashSet;

import java.util.List;

import java.util.Map;

/**
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
public class ShortestLadderGenerator {
    private Map<String, List<String>> map;
    private HashMap<String, Vertex> srcmap;
    private HashMap<String, Vertex> dstmap;
    private List<ArrayList<Vertex>> srcBuffer;
    private List<ArrayList<Vertex>> dstBuffer;
    private boolean findLadder;
    private int bufferIndex;

    public ShortestLadderGenerator(Map<String, List<String>> map) {
        this.map = map;

        findLadder = false;
        srcmap = new HashMap<String, Vertex>(2);
        dstmap = new HashMap<String, Vertex>(2);
        bufferIndex = 0;
        srcBuffer = new ArrayList<ArrayList<Vertex>>();
        dstBuffer = new ArrayList<ArrayList<Vertex>>();
        srcBuffer.add(new ArrayList<Vertex>());
        srcBuffer.add(new ArrayList<Vertex>());

        dstBuffer.add(new ArrayList<Vertex>());
        dstBuffer.add(new ArrayList<Vertex>());

    }

    public synchronized String findShortestPath(String source,
            String destination) throws InterruptedException {
        if (!map.containsKey(source) || !map.containsKey(destination))
            return null;
        StringBuilder sb = new StringBuilder();
        Vertex src = new Vertex(source, null, null, true);
        Vertex dst = new Vertex(destination, null, null, false);
        srcBuffer.get(bufferIndex).add(src);
        dstBuffer.get(bufferIndex).add(dst);

        while (!findLadder && srcBuffer.get(bufferIndex).size() != 0
                && dstBuffer.get(bufferIndex).size() != 0) {

            if (srcBuffer.get(bufferIndex).size() != 0) {
                findLadder |= addRelated(true);
            }
            if (dstBuffer.get(bufferIndex).size() != 0) {
                findLadder |= addRelated(false);
            }
            if (findLadder) {
                break;
            }
            srcBuffer.get(bufferIndex).clear();
            dstBuffer.get(bufferIndex).clear();
            bufferIndex = (bufferIndex + 1) % 2;

        }

        srcmap.clear();
        dstmap.clear();
        dstBuffer.get(0).clear();
        dstBuffer.get(1).clear();
        srcBuffer.get(0).clear();
        srcBuffer.get(1).clear();

        if (findLadder) {
            Vertex vtx = src;
            sb.append(vtx);
            while ((vtx = vtx.next()) != null) {
                sb.append(", " + vtx);
            }
            findLadder = false;
            return sb.toString();
        }
        return null;

    }

    public boolean addRelated(boolean fromSource) {

        Map<String, Vertex> watchMap;
        Map<String, Vertex> addMap;
        List<ArrayList<Vertex>> vtxsBuffer;
        if (fromSource) {
            watchMap = dstmap;
            addMap = srcmap;
            vtxsBuffer = srcBuffer;
        }
        else {
            watchMap = srcmap;
            addMap = dstmap;
            vtxsBuffer = dstBuffer;
        }
        for (Vertex start : vtxsBuffer.get(bufferIndex)) {
//            try {
                for (String str : map.get(start.toString())) {
                    if (watchMap.containsKey(str)) {
                        Vertex midVertex = watchMap.get(str);
                        // System.out.println("start is:\n" + start.info());
                        midVertex.join(start);
                        midVertex.validate();
                        // System.out.println("midVertex is:\n"
                        // + midVertex.info());

                        return true;
                    }
                    if (!addMap.containsKey(str)) {
                        Vertex relatedVtx;
                        if (fromSource) {
                            relatedVtx =
                                    new Vertex(str, start, null, true);
                        }
                        else {
                            relatedVtx =
                                    new Vertex(str, null, start, false);
                        }

                        addMap.put(str, relatedVtx);

                        vtxsBuffer.get((bufferIndex + 1) % 2).add(
                                relatedVtx);
                    }

                }
            }
//            catch (NullPointerException e) {
//                System.err.println(start);
//                System.err.println(map.get(start));
//                System.err.println(start.next()+ " " +start.previous());
//                throw e;
//            }
//        }
        return false;
    }
    // public class RelatedWordAdder implements Runnable {
    // private Vertex start;
    // private Map<String, Vertex> addMap;
    // private Map<String, Vertex> watchMap;
    // private RelatedWordAdder theOther;
    // private boolean fromSource;
    // private List<ArrayList<Vertex>> vtxsBuffer;
    // private int bufferIndex;
    //
    // public RelatedWordAdder(Vertex start, boolean fromSource) {
    // this.start = start;
    // synchronized (ShortestLadderGenerator.this) {
    // threadCount.incrementAndGet();
    // }
    // vtxsBuffer = new ArrayList<ArrayList<Vertex>>(2);
    // vtxsBuffer.add(new ArrayList<Vertex>());
    // vtxsBuffer.add(new ArrayList<Vertex>());
    // bufferIndex = 0;
    // this.fromSource = fromSource;
    // vtxsBuffer.get(bufferIndex).add(start);
    //
    // if (this.fromSource) {
    // this.watchMap = dstmap;
    // this.addMap = srcmap;
    // }
    // else {
    // this.watchMap = srcmap;
    // this.addMap = dstmap;
    // }
    //
    // }
    //
    // /**
    // * @param theOther
    // */
    // public void setTheOther(RelatedWordAdder theOther) {
    // this.theOther = theOther;
    // }
    //
    // public void run() {
    //
    // if (theOther == null) {
    // throw new RuntimeException();
    // }
    // boolean findCoincidence = false;
    //
    // while (!findCoincidence && !findLadder.get()) {
    // if (vtxsBuffer.get(bufferIndex).size() == 0) {
    // break;
    // }
    //
    // findCoincidence = addRelated(vtxsBuffer.get(bufferIndex));
    // if (findCoincidence) {
    // findLadder.set(true);
    //
    // }
    //
    // vtxsBuffer.get(bufferIndex).clear();
    // bufferIndex = (bufferIndex + 1) % 2;
    //
    // // System.out
    // // .println(this + " before mutating synchronizer "
    // // + synchronizer);
    // synchronized (this) {
    // synchronizer.set(!synchronizer.get());
    //
    // if (!synchronizer.get() && !findLadder.get()
    // && !threadCount.equals(1)) {
    //
    // try {
    //
    // this.wait();
    //
    // }
    //
    // catch (InterruptedException e) {
    //
    // }
    //
    // }
    // }
    // synchronized (theOther) {
    // // System.out.println(this + " notify the other");
    // theOther.notify();
    // }
    // }
    //
    // synchronized (ShortestLadderGenerator.this) {
    // // System.out.println("threadCount= " + threadCount);
    // threadCount.decrementAndGet();
    // if (threadCount.equals(0)) {
    //
    // ShortestLadderGenerator.this.notify();
    // }
    // else if (threadCount.equals(1)) {
    // synchronized (theOther) {
    // // System.out.println(this + " notify the other");
    // theOther.notify();
    // }
    // }
    // // System.out.println("threadCount after -- = "
    // // + threadCount);
    // }
    //
    // // System.out.println(this + " has finished");
    //
    // }
    //
    // public boolean addRelated(List<Vertex> vtxs) {
    //
    // for (Vertex start : vtxs) {
    //
    // for (String str : map.get(start.toString())) {
    // if (watchMap.containsKey(str)) {
    // Vertex midVertex = watchMap.get(str);
    // // System.out.println("start is:\n" + start.info());
    // midVertex.join(start);
    // midVertex.validate();
    // // System.out.println("midVertex is:\n"
    // // + midVertex.info());
    //
    // return true;
    // }
    // if (!addMap.containsKey(str)) {
    // Vertex relatedVtx;
    // if (fromSource) {
    // relatedVtx =
    // new Vertex(str, start, null, true);
    // }
    // else {
    // relatedVtx =
    // new Vertex(str, null, start, false);
    // }
    // synchronized (addMap) {
    // addMap.put(str, relatedVtx);
    // }
    // vtxsBuffer.get((bufferIndex + 1) % 2).add(
    // relatedVtx);
    // }
    // }
    //
    // }
    //
    // return false;
    //
    // }
    //
    // }
}

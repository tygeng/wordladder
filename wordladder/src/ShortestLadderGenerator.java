import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

/**
 * This class generate the shortest ladder between two words.
 *
 * @author Tianyu Geng (tony1)
 * @version Aug 30, 2012
 */
public class ShortestLadderGenerator {
    private Map<String, ArrayList<String>> map;
    private HashMap<String, Vertex> srcmap;
    private HashMap<String, Vertex> dstmap;
    private List<ArrayList<Vertex>> srcBuffer;
    private List<ArrayList<Vertex>> dstBuffer;
    private boolean findLadder;
    private int bufferIndex;

    /**
     * This class serves as the node to contain a word. This class is used when
     * an overlapping word has been found in the ShortestLadderGenerator grow
     * method.
     *
     * Each time a new word is found to build the net, the Vertex containing
     * that word and its previous node will be created and added to the list.
     *
     * If an overlapping word is found, then the validate method will be called
     * on the overlapping vertex so that this doubly linked nodes will be
     * connected one by one and form a ladder.
     *
     * @author Tianyu
     * @version Sep 2, 2012
     */
    public static class Vertex {
        private String str;
        private Vertex next;
        private Vertex previous;
        private boolean fromSource;

        /**
         * Constructor for Vertex
         *
         * @param str
         *            the word stored in this vertex
         * @param previous
         *            the previous vertex. It should point to the vertex closer
         *            to the source word.
         * @param next
         *            the next vertex. It should point to the vertex closer to
         *            the destination word.
         * @param fromSource
         *            true if this vertex is grown on the source net
         */
        public Vertex(String str, Vertex previous, Vertex next,
                boolean fromSource) {
            this.str = str;
            this.next = next;
            this.previous = previous;
            this.fromSource = fromSource;

        }

        /**
         * Connect vertices that it connects. This method should only be called
         * on the overlapping word vertex
         *
         * @param goToSource
         *            true if this validating process is going towards to source
         *            word
         */
        public void validate(boolean goToSource) {
            if (previous != null && goToSource) {
                previous.next = this;
                previous.validate(true);
            }
            if (next != null && !goToSource) {
                next.previous = this;
                next.validate(false);
            }

        }

        /**
         * Join this vertex to another vertex. This method is called when an
         * overlapping word is found and needs to join to the two previously
         * separated nets.
         *
         * @param vtx
         *            the vertex to join to
         */
        public void join(Vertex vtx) {
            if (fromSource) {
                next = vtx;
            }
            else {
                previous = vtx;
            }

        }

        /**
         * Validate the vertex chain on both sides.
         */
        public void validate() {
            validate(true);
            validate(false);
        }

        public int hashCode() {
            return str.hashCode();
        }

        public String toString() {
            return str;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Vertex)) {
                return false;
            }
            return this.equals(((Vertex) other).str);
        }

        /**
         * Get the next vertex
         *
         * @return the next vertex
         */
        Vertex next() {
            return next;
        }

        /**
         * Get the previous vertex
         *
         * @return the previous vertex
         */
        Vertex previous() {
            return previous;
        }

    }

    /**
     * Constructor for ShortestLadderGenerator
     *
     * @param map
     *            the fast look up map
     */
    public ShortestLadderGenerator(Map<String, ArrayList<String>> map) {
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

    /**
     * Find the shortest path from the source word to the destination word.
     *
     * Algorithm description:
     *
     * The program will grow to a net of the related words from the source word
     * and destination word simultaneously. Then, as soon as a word is touched
     * by both the source word net and the destination word net, the shortest
     * ladder has been found.
     *
     * Implementation details:
     *
     * Each time more related words grow, all the words on the edge of the nets
     * are added to a buffer list. Then, next time the words grows, words should
     * grow from the previously generated list. Two hash maps are used to keep
     * track of duplications found near source word and destination word. Also,
     * these two hash maps are used to detect if there is an overlapping between
     * the nets of source word and destination word.
     *
     * @param source
     *            the source word
     * @param destination
     *            the destination word
     * @return the String describing the word ladder
     *
     */
    public String findShortestPath(String source, String destination) {
        if (!map.containsKey(source) || !map.containsKey(destination))
            return null;
        StringBuilder sb = new StringBuilder();
        Vertex src = new Vertex(source, null, null, true);
        Vertex dst = new Vertex(destination, null, null, false);
        srcmap.put(source, src);
        dstmap.put(destination, dst);
        // The buffers for source word and destination word
        srcBuffer.get(bufferIndex).add(src);
        dstBuffer.get(bufferIndex).add(dst);
        // if no more words are added to the buffer list, it means the two nets
        // have grown to their biggest size
        while (srcBuffer.get(bufferIndex).size() != 0
                && dstBuffer.get(bufferIndex).size() != 0) {

            if (srcBuffer.get(bufferIndex).size() != 0) {
                if (grow(true)) {
                    findLadder = true;
                    break;
                }
            }
            if (dstBuffer.get(bufferIndex).size() != 0) {
                if (grow(false)) {
                    findLadder = true;
                    break;
                }
            }

            // clear the previous list
            srcBuffer.get(bufferIndex).clear();
            dstBuffer.get(bufferIndex).clear();

            // swap the two buffer lists
            bufferIndex = (bufferIndex + 1) % 2;

        }
        // clear any previously used containers to prepare for a new search
        srcmap.clear();
        dstmap.clear();
        dstBuffer.get(0).clear();
        dstBuffer.get(1).clear();
        srcBuffer.get(0).clear();
        srcBuffer.get(1).clear();
        // if a ladder is found, format it to the required form
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

    /**
     * Grow the net from the source word or the destination word. As soon as a
     * overlapping word is found, this function will return true. Otherwise
     *
     * @param fromSource
     *            true to grow from the source
     * @return true if an overlapping word has been found
     */
    public boolean grow(boolean fromSource) {
        // watchMap is the other map and
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

            for (String str : map.get(start.toString())) {
                // if a overlapping word is found
                if (watchMap.containsKey(str)) {
                    Vertex midVertex = watchMap.get(str);

                    midVertex.join(start);
                    midVertex.validate();

                    return true;
                }
                // if the addMap doesn't contain the word that will be added in
                // to the buffer list
                if (!addMap.containsKey(str)) {
                    Vertex relatedVtx;
                    if (fromSource) {
                        relatedVtx = new Vertex(str, start, null, true);
                    }
                    else {
                        relatedVtx = new Vertex(str, null, start, false);
                    }

                    addMap.put(str, relatedVtx);
                    vtxsBuffer.get((bufferIndex + 1) % 2).add(relatedVtx);
                }

            }
        }

        return false;
    }

}

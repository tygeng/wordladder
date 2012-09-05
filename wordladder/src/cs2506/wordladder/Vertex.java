/**
 *
 */
package cs2506.wordladder;

/**
 *
 * @author Tianyu
 * @version Sep 2, 2012
 */
public class Vertex {
    private String str;
    private Vertex next;
    private Vertex previous;
    private boolean fromSource;

    public Vertex(String str, Vertex previous, Vertex next,
            boolean fromSource) {
        this.str = str;
        this.next = next;
        this.previous = previous;
        this.fromSource = fromSource;

    }

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

    public void join(Vertex vtx) {
        if (fromSource) {
            next = vtx;
        }
        else {
            previous = vtx;
        }

    }

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
        if (other == null) {
            return false;
        }
        return this.equals(((Vertex) other).str);
    }

    public Vertex next() {
        return next;
    }

    public Vertex previous() {
        return previous;
    }

    public String info() {
        return " Vertex: " + str + "\n previous: " + previous
                + "\n next: " + next + "\n fromSource: " + fromSource;
    }
}

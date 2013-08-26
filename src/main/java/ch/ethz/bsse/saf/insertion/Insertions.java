/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.saf.insertion;

import ch.ethz.bsse.saf.informationholder.Globals;
import ch.ethz.bsse.saf.informationholder.Read;
import ch.ethz.bsse.saf.utils.Utils;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author toepfera
 */
public class Insertions {

    private static Insertions INSTANCE = new Insertions();
    private Multiset<InsertionPair> insertions = HashMultiset.create();
    private Map<Integer, List<InsertionTriple>> insertionTriple;

    private Insertions() {
    }

    public static Insertions getINSTANCE() {
        return INSTANCE;
    }

    public void add(Read read) {
        for (Map.Entry<Integer, byte[]> e : read.getWatsonInsertions().entrySet()) {
            insertions.add(new InsertionPair(e.getKey(), e.getValue()), read.getCount());
        }
    }

    public void add(Read[] reads) {
        for (Read r : reads) {
            this.add(r);
        }
    }

    public Map<Integer, List<InsertionTriple>> getInsertions() {
        if (this.insertionTriple == null) {
            this.insertionTriple = new HashMap<>();
            for (Multiset.Entry<InsertionPair> e : this.insertions.entrySet()) {
                if (!this.insertionTriple.containsKey(e.getElement().position)) {
                    this.insertionTriple.put(e.getElement().position, new LinkedList());
                }
                this.insertionTriple.get(e.getElement().position).add(new InsertionTriple(e.getElement().position, e.getElement().sequence, e.getCount()));
            }
            Map<String, List<InsertionTriple>> insertSequenceToTriple = new HashMap<>();
            for (Map.Entry<Integer, List<InsertionTriple>> e : this.insertionTriple.entrySet()) {
                for (InsertionTriple it : e.getValue()) {
                    if (!insertSequenceToTriple.containsKey(Utils.reverse(it.sequence))) {
                        insertSequenceToTriple.put(Utils.reverse(it.sequence), new LinkedList());
                    }
                    insertSequenceToTriple.get(Utils.reverse(it.sequence)).add(it);
                }
            }

            for (Map.Entry<String, List<InsertionTriple>> e : insertSequenceToTriple.entrySet()) {
                if (e.getValue().size() == 1) {
                    if (e.getValue().get(0).count < 10) {
                        e.getValue().clear();
                    }
                    continue;
                }
                int maxTmp = 0;
                for (InsertionTriple it : e.getValue()) {
                    if (it.count > maxTmp) {
                        maxTmp = it.count;
                    }
                }
                if (maxTmp < 10) {
                    continue;
                }
                List<InsertionTriple> merged = new LinkedList<>();
                while (merged.size() != e.getValue().size()) {
                    int max = 0;
                    InsertionTriple it_tmp = null;
                    for (InsertionTriple it : e.getValue()) {
                        if (it.count > max && !merged.contains(it)) {
                            max = it.count;
                            it_tmp = it;
                        }
                    }
                    List<InsertionTriple> merger = new LinkedList<>();
                    for (InsertionTriple it : e.getValue()) {
                        if (it.position > it.position - 10 && it.position < it.position + 10 && !it.equals(it_tmp)) {
                            merger.add(it);
                            it_tmp.count += it.count;
                        }
                    }
                    e.getValue().removeAll(merger);
                    merged.add(it_tmp);
                }
            }
            insertionTriple = new HashMap<>();
            for (Map.Entry<String, List<InsertionTriple>> e : insertSequenceToTriple.entrySet()) {
                if (!e.getValue().isEmpty()) {
                    for (InsertionTriple it : e.getValue()) {
                        if (!this.insertionTriple.containsKey(it.position)) {
                            this.insertionTriple.put(it.position, new LinkedList());
                        }
                        this.insertionTriple.get(it.position).add(new InsertionTriple(it.position, it.sequence, it.count));
                    }
                }
            }
        }
        return insertionTriple;
    }
}

class InsertionPair {

    public int position;
    public byte[] sequence;

    public InsertionPair(int position, byte[] sequence) {
        this.position = position;
        this.sequence = sequence;
    }

    @Override
    public int hashCode() {
        int hashCode = 27;
        hashCode = 29 * hashCode + Arrays.hashCode(sequence);
        hashCode = 29 * hashCode + position;
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InsertionPair other = (InsertionPair) obj;
        if (this.position != other.position) {
            return false;
        }
        if (!Arrays.equals(this.sequence, other.sequence)) {
            return false;
        }
        return true;
    }
}

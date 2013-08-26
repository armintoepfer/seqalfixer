/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.saf.insertion;

import ch.ethz.bsse.saf.informationholder.Read;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                List<InsertionTriple> list = this.insertionTriple.get(e.getElement().position);
                list.add(new InsertionTriple(e.getElement().position, e.getElement().sequence, e.getCount()));
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

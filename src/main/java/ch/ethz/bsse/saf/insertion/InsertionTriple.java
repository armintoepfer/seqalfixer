/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.saf.insertion;

import java.util.Arrays;

/**
 *
 * @author toepfera
 */
public class InsertionTriple {

    public int position;
    public byte[] sequence;
    public int count;

    public InsertionTriple(int position, byte[] sequence, int count) {
        this.position = position;
        this.sequence = sequence;
        this.count = count;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.position;
        hash = 23 * hash + Arrays.hashCode(this.sequence);
        hash = 23 * hash + this.count;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InsertionTriple other = (InsertionTriple) obj;
        if (this.position != other.position) {
            return false;
        }
        if (!Arrays.equals(this.sequence, other.sequence)) {
            return false;
        }
        if (this.count != other.count) {
            return false;
        }
        return true;
    }

    
}

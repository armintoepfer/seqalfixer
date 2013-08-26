/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.saf.utils;

import ch.ethz.bsse.saf.informationholder.Globals;
import ch.ethz.bsse.saf.insertion.InsertionTriple;
import ch.ethz.bsse.saf.insertion.Insertions;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author toepfera
 */
public class Consensus {

    public static String create(int[][] alignment) {
        StringBuilder sb = new StringBuilder();
        for (int L = alignment.length, n = alignment[0].length, j = 0; j < L; j++) {
            int index = -1;
            int max = -1;
            for (int v = 0; v < n; v++) {
                if (alignment[j][v] > max) {
                    index = v;
                    max = alignment[j][v];
                }
            }
            if (max > 0) {
                sb.append(Utils.reverse(index));
            } else {
                sb.append("-");
            }
            //Insertion
            if (Insertions.getINSTANCE().getInsertions().containsKey(j)) {
                List<InsertionTriple> topX = new LinkedList<>();
                int maxLength = 0;
                InsertionTriple longestInsert = null;
                for (InsertionTriple it : Insertions.getINSTANCE().getInsertions().get(j)) {
                    if (it.count > Globals.getINSTANCE().getINSERTIONS()) {
                        topX.add(it);
                        if (it.sequence.length > maxLength) {
                            maxLength = it.sequence.length;
                            longestInsert = it;
                        }
                    }
                }
                if (longestInsert != null) {
                    sb.append(Utils.reverse(longestInsert.sequence));
                }
            }
        }
        return sb.toString();
    }
}

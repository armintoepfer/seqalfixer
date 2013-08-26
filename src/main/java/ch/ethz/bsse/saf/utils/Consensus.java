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
                sb.append("X");
            }
        }
        int[] ns = new int[alignment.length];
        char[] chars = sb.toString().replaceAll("N", "-").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '-') {
                int count = 0;
                for (int j = i; j < chars.length; j++) {
                    if (chars[j] == '-') {
                        count++;
                    } else {
                        break;
                    }
                }
                if (count % 3 != 0) {
                    for (int j = i; j < chars.length; j++) {
                        if (chars[j] == '-') {
                            ns[j] = 4;
                        } else {
                            break;
                        }
                    }
                } else {
                    for (int j = i; j < chars.length; j++) {
                        if (chars[j] == '-') {
                            ns[j] = 5;
                        } else {
                            break;
                        }
                    }
                }
                i += count - 1;
            } else {
                ns[i] = 5;
            }
        }
        sb.setLength(0);
        for (int L = alignment.length, j = 0; j < L; j++) {
            int index = -1;
            int max = -1;
            for (int v = 0; v < ns[j]; v++) {
                if (alignment[j][v] > max) {
                    index = v;
                    max = alignment[j][v];
                }
            }
            if (max > Globals.getINSTANCE().getCOVERAGE()) {
                sb.append(Utils.reverse(index));
            } else {
//                sb.append("X");
            }
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
                    System.out.println("#" + longestInsert.position + "\t" + longestInsert.count + "\t" + Utils.reverse(longestInsert.sequence));
                }
            }
        }

        return sb.toString().replaceAll("N", "-");
    }
}
//Insertion

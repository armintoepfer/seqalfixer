/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.alignmentfixer.utils;

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
            }
        }
        return sb.toString();
    }
}

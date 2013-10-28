/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.saf.utils;

import ch.ethz.bsse.saf.informationholder.Globals;
import ch.ethz.bsse.saf.insertion.InsertionTriple;
import ch.ethz.bsse.saf.insertion.Insertions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author toepfera
 */
public class Consensus {

    public static String create(int[][] alignment) {
        Map<String, String> wobbles = new HashMap<>();
        wobbles.put("A", "A");
        wobbles.put("C", "C");
        wobbles.put("G", "G");
        wobbles.put("T", "T");
        wobbles.put("T", "T");
        wobbles.put("GT", "K");
        wobbles.put("AC", "M");
        wobbles.put("AG", "R");
        wobbles.put("CT", "Y");
        wobbles.put("CG", "S");
        wobbles.put("AT", "W");
        wobbles.put("CGT", "B");
        wobbles.put("ACG", "V");
        wobbles.put("ACT", "H");
        wobbles.put("AGT", "D");
        wobbles.put("ACGT", "N");

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
        StringBuilder output = new StringBuilder();
        if (Globals.getINSTANCE().getINSERTIONS() != Integer.MAX_VALUE) {
            output.append("\n === Insertions ===\n");
            output.append("Pos\tCount\tSequence\n");
        }
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
                    if (it.count >= Globals.getINSTANCE().getINSERTIONS()) {
                        topX.add(it);
                        if (it.sequence.length > maxLength) {
                            maxLength = it.sequence.length;
                            longestInsert = it;
                        }
                    }
                }
                if (longestInsert != null) {
                    sb.append(Utils.reverse(longestInsert.sequence));
                    output.append(longestInsert.position).append("\t").append(longestInsert.count).append("\t").append(Utils.reverse(longestInsert.sequence)).append("\n");
                }
            }
        }
        Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "consensus_gapped.fasta", ">consensus\n" + sb.toString().replaceAll("N", "-"));
        sb = new StringBuilder();

        for (int L = alignment.length, j = 0; j < L; j++) {
            int max = -1;
            double sum = 0;
            for (int v = 0; v < 5; v++) {
                sum += alignment[j][v];
                if (alignment[j][v] > max) {
                    max = alignment[j][v];
                }
            }
            StringBuilder w_sb = new StringBuilder();
            for (int v = 0; v < 4; v++) {
                if (alignment[j][v] / sum > Globals.getINSTANCE().getPLURALITY()) {
                    w_sb.append(Utils.reverse(v));
                }
            }
            if (max > Globals.getINSTANCE().getCOVERAGE()) {
                sb.append(wobbles.get(w_sb.toString()));
            } else {
                if (alignment[j][4] > 10) {
                    sb.append("X");
                }
            }
            if (Insertions.getINSTANCE().getInsertions().containsKey(j)) {
                List<InsertionTriple> topX = new LinkedList<>();
                int maxLength = 0;
                InsertionTriple longestInsert = null;
                for (InsertionTriple it : Insertions.getINSTANCE().getInsertions().get(j)) {
                    if (it.count >= Globals.getINSTANCE().getINSERTIONS()) {
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
        Globals.getINSTANCE().setINSERTION_SUMMARY(output.toString());
        Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "consensus.fasta", ">consensus\n" + sb.toString());
        return sb.toString().replaceAll("N", "-");
    }
}
//Insertion
//TTTTTCACCTCTGCCTAATCATCTCTTGTTCATGTCCTACTGTTCAAGCCTCCAAGCTGTGCCTTGGGTGGCTTTAGGACATGGACATTGACCCTTATAAAGAATTTGGAGCTACTGTGGAGTTACTCTCGTTTTTGCCTGCTGACTTCTTTCCTTCAGTACGAGATCTGCTAGATACCGCCTCAGCTCTGTATCGTGAAGCCTTAGAGTCTCCTGAGCATTGTTCACCTCACCATACTGCACTCAGGCAAGCAATTCTTTGCTGGGGGGACTTTCTAATTTTGACTTGTCTTGGAGCAACCTGGGTGGGTGTTAATTTGGAAGATCAAGCATCTAGGGAGCTAGTAGTCAGTTATGTCAACACTAATATGGGCCTAAAGTTCAGACAACTCTTGTGGTTTCACATTTCTTGTCTCACTTTTGGAAGAGATACAGTTGTAGAGTATTTGGTGTCTTTCGGGGTGTGGATTCGCACTCCTCAAGCTTATAGACCACCAAATGCCCCTATCCTATCAACACTTCCGGAGACTACTGTTGTTAGACGACGAGGCAGGTCCCCTAGAAGAAGAACTCCCTCGCCTCGCAGACGAAGGTCTAAATCGCCGCGTCGCAGAAGATCTCAATCTCGGGAATCTCAATGTTAATATTCCTTGGACTCATAAGGTGGGGAACTTTACTGGGCTTTATTCTTCTACTGTACCTGTCTTTAATCCTCATTGGAAAACACCTTCTTTTCCTAATATACACTTACACCAAGACATTATCCAAAGATGTGAACAGTTTGTAGGCCCACTCACAGTTAATGAGAAAAGAAGATTGCAATTGATCATGCCTGCTAGGTTTTATCCAAAGGTTACCAAATATTTGCCATTGGAGAAGGGTATTAAACCTCATTACCCAGAACATCTAGTTAATCATTACTTCCAAACTAGACACTATTTACACACTCTATGGAAGGCGGGTGTATTATATAAGAGAGAAACATCACGTAGCGCCTCATTTTGTGGGTCACCATATTCTTGGGAACAAGATCTACAGCATGGGGCAGAATCTTTCCACCAGCATTCCTCTGGGATTCTTTCCCGACCACCAGTTGGATCCAGCCTTCAGAGCAAACACCGCAAATCCAGATTGGGACTTCAATCCCAACAAGGACACCTGGCCAGACGCCAACAAGGTAGGAGCTGGAGCATTCGGG---------------------------------------------CAGGCTCAGGGCATACTACAAACTTTGCCAGCAAAGCCGCCTCCTGCCTCCACCAATCGCCAGTCAGGACGGCAGCCTACCCCGCTG---------TTGAGAAACACTCATCCTCAGGCCATG---------------ACCTTCCACCAAACTCTGCAAGCTCC---------------------CCCTGCTGGTGGCTCCAGTTCCGGAACAGTAAACCCTGTTCCGACTACTGCCTCTCACATATCGTCAATCTTCTCGAGGATTGGGGACCCTGCGCTGAATATGGAGAACATCACATCAGGATTCCTAGGACCCCTTCTCGTGTTACAGGCGGGGTTTTTCTTGTTGACAAGAATCCTCACAATACCGCAGAGTCTAGACTCGTGGTGGACTTCTCTCAATTTTCTAGGGGGAACCACCGTGTGTCTTGGCCAAAATTCGCAGTCCCCAACCTCCAATCACTCACCAACCTCCTGTCCTCCGACTTGACCTGGTTATCGCTGGATGTGACTGCGGCATTTTATCATATTCCTCTTCATCCTGCTGCTATGCCTCATCTTCTTGTTGGTTCTTCTGGACTATCAAGGTATGTTGCCCGTTTGTCCTCTAATTCCAGGATCCTCAACCACCAGCACGGGAACATGCCGAACTTGCACGACTCCTGCTCAAGGAACCTCTATGTATCCCTCCTGTTGCTGTACCAAACCTTCGGACGGAAATTGCACCTGTATTCCCATCCCATCATCCTGGGCTTTCGGAAAATTCCTATGGGAGTGGGCCTCAGCCCGTTTCTCCTGGCTCAGTTTGGTAGTGCCATTTGTTCAGTGGTTCGTAGGGCTTTCCCCCACTGTTTGGCTTTCATTTATGCGGCGGATGATGTGGTATTGGGGGCCAAGTCTGTACAGCATCTTGAGTCCCTTTTTACCGCTGTTACCAATTTTCTTTTGTCTCTGGGTATACATTTGGCCCCTAACAAAACAAAGAGATGGGGTTACTCCCTAAATTTTATGGGCTATGTCATTGGATGTTATGGGTCCTTGCCACAAGAACACATCATACATAAAATCAAAGAATGTTTTAGAAAACTTCCTGTTAACAGGCCTATTGATTGGAAAGTATGTCAACGCATTGTGGGCCTTTTGGGTTTTGCTGCCCCTTTTACTCAATGTGGTTATCCTGCTTTAATGCCCTTGTATGCCTGTATTCAATCTAAACAGGCTTTCACTTTCTCGCCAACTTACAAGGCCTTTCTGTGTAAACAATACCTGAACCTTTACCCCGTTGCCAGGCAACGGCCAGGTCTGTGCCAAGTGTTTGCTGACGCAACCCCCACTGGCTGGGGCTTGGCCATAGGCCATCAGCGCATGCGTGGCGCCTTTCAGGCTCCTCTGCCGATCCATACTGCGGAACTCCTCGCCGCTTGTTTTGCTCGCAGCAGGTCTGGAGCAAACATTATCGGGACTGATAACTCTGTTGTCCTCTCCCGCAAATATACATCGTTTCCATGGCTGCTAGGCTGTGCTGCCAACTGGATCCTGCGCGGGACGTCCTTTGTTTACGTCCCGTCGGCGCTGAATCCCGCGGACGACCCTTCTCGGGGTCGCTTGGGGCTCTATCGGCCCCTTCTCCGTCTGCCGTTCCGACCGACCACGGGGCGCACCTCTCTTTACGCGGACTCCCCGTCTGTGCCTTCTCATCTGCCGGACCGTGTGCACTTCGCTTCACCTCTGCACGTCGCATGGAAACCACCGTGAACGCCCACCTTGATCTTGCCCAGATCTTGCCCAAGGTCTTATATAAGAGGACTCTTGGACTCTCTGCAATGTCAACGACCGACCTTGAGGCATACTTCAAAGACTTTCGTTTGTTTAAAGACTGGGAGGAGTTGGGGGAGGAGCTTAGATTAATGATCTTTGTACTAGGAGGCTGTAGGCATAAATTGGTCTGCGCACCAGCACCATGCAAC
//TTTTTCACCTCTGCCTAATCATCTCTTGTTCATGTCCTACTGTTCAAGCCTCCAAGCTGTGCCTTGGGTGGCTTTAGGACATGGACATTGACCCTTATAAAGAATTTGGAGCTACTGTGGAGTTACTCTCGTTTTTGCCTGCTGACTTCTTTCCTTCAGTACGAGATCTGCTAGATACCGCCTCAGCTCTGTATCGTGAAGCCTTAGAGTCTCCTGAGCATTGTTCACCTCACCATACTGCACTCAGGCAAGCAATTCTTTGCTGGGGGGACTTTCTAATTTTGACTTGTCTTGGAGCAACCTGGGTGGGTGTTAATTTGGAAGATCAAGCATCTAGGGAGCTAGTAGTCAGTTATGTCAACACTAATATGGGCCTAAAGTTCAGACAACTCTTGTGGTTTCACATTTCTTGTCTCACTTTTGGAAGAGATACAGTTGTAGAGTATTTGGTGTCTTTCGGGGTGTGGATTCGCACTCCTCAAGCTTATAGACCACCAAATGCCCCTATCCTATCAACACTTCCGGAGACTACTGTTGTTAGACGACGAGGCAGGTCCCCTAGAAGAAGAACTCCCTCGCCTCGCAGACGAAGGTCTAAATCGCCGCGTCGCAGAAGATCTCAATCTCGGGAATCTCAATGTTAATATTCCTTGGACTCATAAGGTGGGGAACTTTACTGGGCTTTATTCTTCTACTGTACCTGTCTTTAATCCTCATTGGAAAACACCTTCTTTTCCTAATATACACTTACACCAAGACATTATCCAAAGATGTGAACAGTTTGTAGGCCCACTCACAGTTAATGAGAAAAGAAGATTGCAATTGATCATGCCTGCTAGGTTTTATCCAAAGGTTACCAAATATTTGCCATTGGAGAAGGGTATTAAACCTCATTACCCAGAACATCTAGTTAATCATTACTTCCAAACTAGACACTATTTACACACTCTATGGAAGGCGGGTGTATTATATAAGAGAGAAACATCACGTAGCGCCTCATTTTGTGGGTCACCATATTCTTGGGAACAAGATCTACAGCATGGGGCAGAATCTTTCCACCAGCATTCCTCTGGGATTCTTTCCCGACCACCAGTTGGATCCAGCCTTCAGAGCAAACACCGCAAATCCAGATTGGGACTTCAATCCCAACAAGGACACCTGGCCAGACGCCAACAAGGTAGGAGCTGGAGCATTCGGGCAGGGTTTCACCCCACCGCACGGAGGCCTTTTGGGGTGGAGCCCTCAGGCTCAGGGCATACTACAAACTTTGCCAGCAAAGCCGCCTCCTGCCTCCACCAATCGCCAGTCAGGACGGCAGCCTACCCCGCTGTCTCCACCTTTGAGAAACACTCATCCTCAGGCCATGCAGTGGAAACCCACAACCTTCCACCAAACTCTGCAAGCTCCCCCTCCCCTGCTGGTGGCTCCAGTTCCGGAACAGTAAACCCTGTTCCGACTACTGCCTCTCACATATCGTCAATCTTCTCGAGGATTGGGGACCCTGCGCTGAATATGGAGAACATCACATCAGGATTCCTAGGACCCCTTCTCGTGTTACAGGCGGGGTTTTTCTTGTTGACAAGAATCCTCACAATACCGCAGAGTCTAGACTCGTGGTGGACTTCTCTCAATTTTCTAGGGGGAACCACCGTGTGTCTTGGCCAAAATTCGCAGTCCCCAACCTCCAATCACTCACCAACCTCCTGTCCTCCGACTTGACCTGGTTATCGCTGGATGTGACTGCGGCATTTTATCATATTCCTCTTCATCCTGCTGCTATGCCTCATCTTCTTGTTGGTTCTTCTGGACTATCAAGGTATGTTGCCCGTTTGTCCTCTAATTCCAGGATCCTCAACCACCAGCACGGGAACATGCCGAACTTGCACGACTCCTGCTCAAGGAACCTCTATGTATCCCTCCTGTTGCTGTACCAAACCTTCGGACGGAAATTGCACCTGTATTCCCATCCCATCATCCTGGGCTTTCGGAAAATTCCTATGGGAGTGGGCCTCAGCCCGTTTCTCCTGGCTCAGTTTGGTAGTGCCATTTGTTCAGTGGTTCGTAGGGCTTTCCCCCACTGTTTGGCTTTCATTTATGCGGCGGATGATGTGGTATTGGGGGCCAAGTCTGTACAGCATCTTGAGTCCCTTTTTACCGCTGTTACCAATTTTCTTTTGTCTCTGGGTATACATTTGGCCCCTAACAAAACAAAGAGATGGGGTTACTCCCTAAATTTTATGGGCTATGTCATTGGATGTTATGGGTCCTTGCCACAAGAACACATCATACATAAAATCAAAGAATGTTTTAGAAAACTTCCTGTTAACAGGCCTATTGATTGGAAAGTATGTCAACGCATTGTGGGCCTTTTGGGTTTTGCTGCCCCTTTTACTCAATGTGGTTATCCTGCTTTAATGCCCTTGTATGCCTGTATTCAATCTAAACAGGCTTTCACTTTCTCGCCAACTTACAAGGCCTTTCTGTGTAAACAATACCTGAACCTTTACCCCGTTGCCAGGCAACGGCCAGGTCTGTGCCAAGTGTTTGCTGACGCAACCCCCACTGGCTGGGGCTTGGCCATAGGCCATCAGCGCATGCGTGGCGCCTTTCAGGCTCCTCTGCCGATCCATACTGCGGAACTCCTCGCCGCTTGTTTTGCTCGCAGCAGGTCTGGAGCAAACATTATCGGGACTGATAACTCTGTTGTCCTCTCCCGCAAATATACATCGTTTCCATGGCTGCTAGGCTGTGCTGCCAACTGGATCCTGCGCGGGACGTCCTTTGTTTACGTCCCGTCGGCGCTGAATCCCGCGGACGACCCTTCTCGGGGTCGCTTGGGGCTCTATCGGCCCCTTCTCCGTCTGCCGTTCCGACCGACCACGGGGCGCACCTCTCTTTACGCGGACTCCCCGTCTGTGCCTTCTCATCTGCCGGACCGTGTGCACTTCGCTTCACCTCTGCACGTCGCATGGAAACCACCGTGAACGCCCACCTTGATCTTGCCCAGATCTTGCCCAAGGTCTTATATAAGAGGACTCTTGGACTCTCTGCAATGTCAACGACCGACCTTGAGGCATACTTCAAAGACTTTCGTTTGTTTAAAGACTGGGAGGAGTTGGGGGAGGAGCTTAGATTAATGATCTTTGTACTAGGAGGCTGTAGGCATAAATTGGTCTGCGCACCAGCACCATGCAAC
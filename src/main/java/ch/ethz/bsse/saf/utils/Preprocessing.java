/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of AlignmentFixer.
 *
 * AlignmentFixer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * AlignmentFixer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AlignmentFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.saf.utils;

import ch.ethz.bsse.saf.informationholder.Globals;
import ch.ethz.bsse.saf.informationholder.Read;
import ch.ethz.bsse.saf.insertion.InsertionTriple;
import ch.ethz.bsse.saf.insertion.Insertions;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Preprocessing {

    private int L;
    private int N = 0;

    public Preprocessing(String genomePath, String alignmentPath) {
        Globals.getINSTANCE().setUNPAIRED(true);
        Read[] alignmentReads = Utils.parseBAMSAM(alignmentPath);
        int[][] alignment = createAlignment(alignmentReads);

        this.saveR();

        if (Globals.getINSTANCE().isCONSENSUS()) {
            Insertions.getINSTANCE().add(alignmentReads);
            String consensus = Consensus.create(alignment);
            Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "consensus.fasta", ">consensus\n" + consensus);
        }
        StatusUpdate.getINSTANCE().println("Reads\t\t\t" + N);
        StatusUpdate.getINSTANCE().println("Uniques\t\t" + alignmentReads.length);
    }

    private int[][] createAlignment(Read[] reads) {
        //fix alignment to position 0
        for (Read r : reads) {
            Globals.getINSTANCE().setALIGNMENT_BEGIN(Math.min(r.getBegin(), Globals.getINSTANCE().getALIGNMENT_BEGIN()));
            Globals.getINSTANCE().setALIGNMENT_END(Math.max(r.getEnd(), Globals.getINSTANCE().getALIGNMENT_END()));
            N += r.getCount();
        }
        L = Globals.getINSTANCE().getALIGNMENT_END() - Globals.getINSTANCE().getALIGNMENT_BEGIN();
        Globals.getINSTANCE().setALIGNMENT_END(L);
        StatusUpdate.getINSTANCE().println("Modifying reads");

        Parallel.ForEach(Arrays.asList(reads), new LoopBody<Read>() {
            @Override
            public void run(Read r) {
                r.shrink();
            }
        });
        StatusUpdate.getINSTANCE().print("Modifying reads\tdone");
        int[][] alignment = computeAlignment(reads, L);
        return alignment;
    }

    private static int[][] computeAlignment(Read[] reads, int L) {
        StatusUpdate.getINSTANCE().println("Alignment summaries\t");

        double entropyCounter = 0;

        AlignmentPair ap = countPos(reads, L);

        int[][] alignment = ap.counts;
        double[][] alignmentWeighted = new double[L][5];

        StringBuilder sb = new StringBuilder();
        StringBuilder sbw = new StringBuilder();
        StringBuilder sbE = new StringBuilder();
        StringBuilder sbCoverage = new StringBuilder();


        char[] alphabet = new char[]{'A', 'C', 'G', 'T', '-'};
        double[] entropy = new double[L];

        sb.append("#Offset: ").append(Globals.getINSTANCE().getALIGNMENT_BEGIN()).append("\n");
        sbw.append("#Offset: ").append(Globals.getINSTANCE().getALIGNMENT_BEGIN()).append("\n");
        sb.append("Pos");
        sbw.append("Pos");
        for (int i = 0; i < alignment[0].length; i++) {
            sb.append("\t").append(alphabet[i]);
            sbw.append("\t").append(alphabet[i]);
        }
        sb.append("\n");
        sbw.append("\n");

        sbE.append("#Offset: ").append(Globals.getINSTANCE().getALIGNMENT_BEGIN()).append("\n");
        for (int i = 0; i < L; i++) {
            int hits = 0;
            sb.append(i);
            sbw.append(i);

            int coveragePos = 0;
            for (int v = 0; v < 5; v++) {
                coveragePos += alignment[i][v];
            }
            sbCoverage.append(Globals.getINSTANCE().getALIGNMENT_BEGIN() + i).append("\t").append(coveragePos).append("\n");
            for (int v = 0; v < 5; v++) {
                alignmentWeighted[i][v] += alignment[i][v] / (double) coveragePos;
                if (alignmentWeighted[i][v] > 0) {
                    entropy[i] -= alignmentWeighted[i][v] * Math.log(alignmentWeighted[i][v]) / Math.log(5);
                }
                sb.append("\t").append(alignment[i][v]);
                sbw.append("\t").append(shorten(alignmentWeighted[i][v]));
                if (alignment[i][v] != 0) {
                    hits++;
                }
            }
            sb.append("\n");
            sbw.append("\n");
            if (hits == 0) {
                System.out.println("Position " + i + " is not covered.");
            }
            sbE.append(i).append("\t").append(entropy[i]).append("\n");
            StatusUpdate.getINSTANCE().print("Alignment summaries\t" + (Math.round((entropyCounter++ / L) * 100)) + "%");
        }

        StatusUpdate.getINSTANCE().print("Alignment summaries\t100%");

        Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "support" + File.separator + "entropy_distribution.txt", sbE.toString());
        Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "support" + File.separator + "allel_distribution.txt", sb.toString());
        Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "support" + File.separator + "allel_distribution_phred_weighted.txt", sbw.toString());
        Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "support" + File.separator + "coverage.txt", sbCoverage.toString());
        return alignment;
    }

    private static AlignmentPair countPos(Read[] reads, int L) {
        AlignmentPair ap = new AlignmentPair(L, 5);

        for (Read r : reads) {
            int begin = r.getWatsonBegin();
            for (int i = 0; i < r.getWatsonLength(); i++) {
                try {
                    ap.counts[i + begin][r.getSequence()[i]] += r.getCount();
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(e);
                }
            }
            if (r.isPaired()) {
                begin = r.getCrickBegin();
                for (int i = 0; i < r.getCrickLength(); i++) {
                    ap.counts[i + begin][r.getCrickSequence()[i]] += r.getCount();
                }
            }
        }
        return ap;
    }

    public static String shorten(double value) {
        String s;
        if (value < 1e-20) {
            s = "0      ";
        } else if (value == 1.0) {
            s = "1      ";
        } else {
            String t = "" + value;
            String r;
            if (t.length() > 7) {
                r = t.substring(0, 7);
                if (t.contains("E")) {
                    r = r.substring(0, 4);
                    r += "E" + t.split("E")[1];
                }
                s = r;
            } else {
                s = String.valueOf(value);
            }
        }
        return s;
    }

    private void saveR() {
        String r = "dir.create(\"plots\",F)\n"
                + "coverage <- read.delim(\"support/coverage.txt\", header=F)\n"
                + "pdf(\"plots/coverage.pdf\",20,12)\n"
                + "par(mar=c(5.1,4.1,4.1,0.1))\n"
                + "plot(coverage$V1,coverage$V2,lwd=2,xlab=\"Position\", main=\"Coverage\",ylab=\"Coverage\",type=\"n\",xaxs=\"i\",cex.main=2,cex.lab=1.4,cex.axis=1.5)\n"
                + "polygon(c(coverage$V1[1],coverage$V1,coverage$V1[nrow(coverage)]),c(0,coverage$V2,0),col=\"lightblue\",density=-10)\n"
                + "dev.off()\n"
                + "pdf(\"plots/snvs.pdf\",15,5)\n"
                + "a = read.table(\"support/entropy_distribution.txt\",header=F)\n"
                + "plot(a$V1,a$V2,pch=18,ylim=c(0,1),main=\"Entropy landscape\",ylab=\"Entropy\",xlab=\"Position\")\n"
                + "dev.off()";
        Utils.saveFile(Globals.getINSTANCE().getSAVEPATH() + "support" + File.separator + "plots.R", r);
    }

    public static void saveCoveragePlot() {
    }
}

class AlignmentPair {

    double[][] weighted;
    int[][] counts;

    public AlignmentPair(int L, int n) {
        this.weighted = new double[L][n];
        this.counts = new int[L][n];
    }
}
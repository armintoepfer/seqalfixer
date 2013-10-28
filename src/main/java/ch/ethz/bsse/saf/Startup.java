/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of SequenceAlignmentFixer.
 *
 * SequenceAlignmentFixer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later
 * version.
 *
 * SequenceAlignmentFixer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SequenceAlignmentFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.saf;

import ch.ethz.bsse.saf.informationholder.Globals;
import ch.ethz.bsse.saf.utils.Preprocessing;
import ch.ethz.bsse.saf.utils.Utils;
import java.io.File;
import java.io.IOException;
import net.sf.samtools.SAMFormatException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Startup {

    public static void main(String[] args) throws IOException {
        new Startup().doMain(args);
        System.exit(0);
    }
    //GENERAL
    @Option(name = "-i")
    private String input;
    @Option(name = "-o", usage = "Path to the output directory (default: current directory)", metaVar = "PATH")
    private String output;
    @Option(name = "-verbose")
    private boolean verbose;
    @Option(name = "-r")
    private String region;
    @Option(name = "-HIV")
    private String hiv;
    @Option(name = "-g")
    private String genome;
    @Option(name = "--consensus")
    private boolean consensus;
    @Option(name = "-insertions")
    private int insertions = Integer.MAX_VALUE;
    @Option(name = "-coverage")
    private int coverage = 1;
    @Option(name = "-merge")
    private boolean merge = false;
    @Option(name = "-plurality")
    private double plurality = 0.05;

    private void setInputOutput() {
        if (output == null) {
            this.output = System.getProperty("user.dir") + File.separator;
        } else {
            Globals.getINSTANCE().setSAVEPATH(this.output);
        }
        if (output.endsWith("/") || output.endsWith("\\")) {
            if (!new File(this.output).exists()) {
                if (!new File(this.output).mkdirs()) {
                    System.out.println("Cannot create directory: " + this.output);
                }
            }
        }
        Globals.getINSTANCE().setSAVEPATH(this.output);
    }

    private void setMainParameters() {
        Globals.getINSTANCE().setDEBUG(this.verbose);
        Globals.getINSTANCE().setPLURALITY(this.plurality);
    }

    private void profile() throws CmdLineException {
        if (this.input == null) {
            throw new CmdLineException("No input given");
        }

        if (this.hiv != null) {
            int begin_hiv = -1;
            int end_hiv = -1;
            switch (this.hiv) {
                case "1":
                case "p17":
                    begin_hiv = 790;
                    end_hiv = 1186;
                    break;
                case "2":
                case "p24":
                    begin_hiv = 1186;
                    end_hiv = 1879;
                    break;
                case "3":
                case "p2p6":
                    begin_hiv = 1879;
                    end_hiv = 2292;
                    break;
                case "4":
                case "prot":
                    begin_hiv = 2253;
                    end_hiv = 2550;
                    break;
                case "5":
                case "RT":
                    begin_hiv = 2550;
                    end_hiv = 3870;
                    break;
                case "6":
                case "RNase":
                    begin_hiv = 3870;
                    end_hiv = 4230;
                    break;
                case "7":
                case "int":
                    begin_hiv = 4230;
                    end_hiv = 5096;
                    break;
                case "8":
                case "vif":
                    begin_hiv = 5041;
                    end_hiv = 5619;
                    break;
                case "9":
                case "vpr":
                    begin_hiv = 5559;
                    end_hiv = 5850;
                    break;
                case "10":
                case "vpu":
                    begin_hiv = 6062;
                    end_hiv = 6310;
                    break;
                case "11":
                case "gp120":
                    begin_hiv = 6225;
                    end_hiv = 7758;
                    break;
                case "12":
                case "gp41":
                    begin_hiv = 7758;
                    end_hiv = 8795;
                    break;
                case "13":
                case "nef":
                    begin_hiv = 8797;
                    end_hiv = 9417;
                    break;
                case "14":
                case "gag":
                    begin_hiv = 790;
                    end_hiv = 2292;
                    break;
                case "15":
                case "pol":
                    begin_hiv = 2085;
                    end_hiv = 5096;
                    break;
                case "16":
                case "env":
                    begin_hiv = 6225;
                    end_hiv = 8795;
                    break;
            }
            Globals.getINSTANCE().setWINDOW_BEGIN(begin_hiv - 1);
            Globals.getINSTANCE().setWINDOW_END(end_hiv - 1);
            Globals.getINSTANCE().setWINDOW(true);
        } else if (this.region != null && !this.region.isEmpty()) {
            String[] r = this.region.split("-");
            Globals.getINSTANCE().setWINDOW_BEGIN(Integer.parseInt(r[0]) - 1);
            Globals.getINSTANCE().setWINDOW_END(Integer.parseInt(r[1]) - 1);
            Globals.getINSTANCE().setWINDOW(true);
        }
        Globals.getINSTANCE().setCONSENSUS(this.consensus);
        Globals.getINSTANCE().setINSERTIONS(this.insertions);
        Globals.getINSTANCE().setCOVERAGE(this.coverage);
        new Preprocessing(genome, input);
        if (insertions != Integer.MAX_VALUE) {
            System.out.println("");
            System.out.println(Globals.getINSTANCE().getINSERTION_SUMMARY());
        }
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        parser.setUsageWidth(80);
        try {
            parser.parseArgument(args);
            setInputOutput();
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            new File(this.output + File.separator + "support/").mkdirs();
            Utils.appendFile(this.output + File.separator + "support/CMD", sb.toString());
            setMainParameters();

            profile();
            System.out.println("");
        } catch (SAMFormatException e) {
            System.err.println("");
            System.err.println("Input file is not in SAM or BAM format.");
            System.err.println(e);
        } catch (CmdLineException cmderror) {
            System.err.println(cmderror.getMessage());
            System.err.println("");
            System.err.println("SequenceAlignmentFixer version: " + Startup.class.getPackage().getImplementationVersion());
            System.err.println("");
            System.err.println("USAGE: java -jar SequenceAlignmentFixer.jar options...\n");
            System.err.println(" -------------------------");
            System.err.println(" === GENERAL options ===");
            System.err.println("  -i INPUT\t\t: Alignment file in BAM or SAM format.");
            System.err.println("  -o PATH\t\t: Path to the output directory (default: current directory).");
            System.err.println("  --consensus\t\t: Create consensus.fasta as output.");
            System.err.println("  -insertions INT\t: Minimal coverage of insertions to be included in consensus (default: do not include insertions).");
            System.err.println("  -coverage INT\t\t: Minimal coverage (default: 0).");
//            System.err.println("  -merge\t\t\t: Merge neighbouring insertions.");
            System.err.println("");
            System.err.println(" -------------------------");
            System.err.println(" === Technical options ===");
            System.err.println("  -XX:NewRatio=9\t: Reduces the memory consumption (RECOMMENDED to use).");
            System.err.println("  -Xms2G -Xmx10G\t: Increase heap space.");
            System.err.println("  -XX:+UseParallelGC\t: Enhances performance on multicore systems.");
            System.err.println("  -XX:+UseNUMA\t\t: Enhances performance on multi-CPU systems.");
            System.err.println(" -------------------------");
            System.err.println(" === EXAMPLES ===");
            System.err.println("   java -XX:+UseParallelGC -Xms2g -Xmx10g -XX:+UseNUMA -XX:NewRatio=9 -jar SequenceAlignmentFixer.jar -i alignment.bam -g references.fasta");
            System.err.println(" -------------------------");
        }
    }
}

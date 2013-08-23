/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of ProfileSim.
 *
 * ProfileSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * ProfileSim is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ProfileSim. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.alignmentfixer.utils;

import ch.ethz.bsse.alignmentfixer.informationholder.Globals;
import ch.ethz.bsse.alignmentfixer.informationholder.Read;
import ch.ethz.bsse.alignmentfixer.informationholder.ReadTMP;
import ch.ethz.bsse.alignmentfixer.informationholder.Threading;
import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Utils extends FastaParser {

    public final static String SAVEPATH = "";

    public static byte[] convertRead(Byte[] readSplit) {
        byte[] rs = new byte[readSplit.length];
        int length = readSplit.length;
        for (int i = 0; i < length; i++) {
            switch ((short) readSplit[i]) {
                case 65:
                    rs[i] = 0;
                    break;
                case 67:
                    rs[i] = 1;
                    break;
                case 71:
                    rs[i] = 2;
                    break;
                case 84:
                    rs[i] = 3;
                    break;
                case 45:
                case 78:
                    rs[i] = 4;
                    break;
                default:
                    System.out.println("Unknown " + (char) ((byte) readSplit[i]) + " " + readSplit[i]);
                    break;
            }
        }
        return rs;
    }

    private static boolean isFastaFormat(String path) {
        try {
            FileInputStream fstream = new FileInputStream(path);
            try (DataInputStream in = new DataInputStream(fstream)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    if (strLine.startsWith(">")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error identifying format of input file: " + e.getMessage());
        }
        return false;
    }

    public static void mkdir(String save) {
        if (!new File(save).exists()) {
            if (!new File(save).mkdirs()) {
                throw new RuntimeException("Cannot create directory: " + save);
            }
        }
    }

    public static void appendFile(String path, String sb) {
        try {
            // Create file 
            FileWriter fstream = new FileWriter(path, true);
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.write(sb);
            }
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error append file: ");
            System.err.println(path);
            System.err.println(sb);
        }
    }

    public static void saveFile(String path, String sb) {
        try {
            // Create file 
            FileWriter fstream = new FileWriter(path);
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.write(sb);
            }
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error save file: ");
            System.err.println(path);
            System.err.println(sb);
        }
    }

    public static String reverse(byte[] packed, int length) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < length; j++) {
            sb.append(reverse(packed[j]));
        }
        return sb.toString();
    }

    public static byte[] splitReadIntoByteArray(String read) {
        byte[] Rs = new byte[read.length()];
        char[] readSplit = read.toCharArray();
        int length = readSplit.length;
        for (int i = 0; i < length; i++) {
            switch ((short) readSplit[i]) {
                case 65:
                    Rs[i] = 0;
                    break;
                case 67:
                    Rs[i] = 1;
                    break;
                case 71:
                    Rs[i] = 2;
                    break;
                case 84:
                    Rs[i] = 3;
                    break;
                case 45:
                    Rs[i] = 4;
                    break;
                default:
                    break;
            }
        }
        return Rs;
    }

    public static byte[] splitQualityIntoByteArray(String quality) {
        byte[] Rs = new byte[quality.length()];
        char[] readSplit = quality.toCharArray();
        int length = readSplit.length;
        for (int i = 0; i < length; i++) {
            Rs[i] = (byte) readSplit[i];
        }
        return Rs;
    }

    public static Read[] parseInput(String path) {
//        if (isFastaGlobalMatePairFormat(path)) {
//            return FastaParser.parseFastaPairedEnd(path);
        if (isFastaFormat(path)) {
            return parseFastaInput(path);
        } else {
            return parseBAMSAM(path);
        }
    }

    public static Map<String, Read> parseBAMSAMPure(String location) {
        Map<String, Read> readMap = new HashMap<>();
        File bam = new File(location);
        SAMFileReader sfr = new SAMFileReader(bam);
        double size = 0;
        for (final SAMRecord samRecord : sfr) {
            size++;
        }
        sfr.close();
        sfr = new SAMFileReader(bam);
        int counter = 0;
        int x = 0;
        List<Callable<List<ReadTMP>>> callables = new ArrayList<>();
        List<SAMRecord> l = new LinkedList<>();
        int slices = 0;
        int max = (int) Math.ceil(size / (Runtime.getRuntime().availableProcessors() - 1));
        for (final SAMRecord samRecord : sfr) {
            if (x > max) {
                x = 0;
                slices++;
                callables.add(new SFRComputing(l));
                l = new LinkedList<>();
            } else {
            }
            StatusUpdate.getINSTANCE().print("Parsing\t\t" + (Math.round((counter++ / size) * 100)) + "%");
            l.add(samRecord);
            x++;
        }
        StatusUpdate.getINSTANCE().print("Parsing\t\tcomputing");
        callables.add(new SFRComputing(l));
        List<Future<List<ReadTMP>>> readFutures = null;
        try {
            readFutures = Threading.getINSTANCE().getExecutor().invokeAll(callables);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        StatusUpdate.getINSTANCE().print("Parsing\t\t           ");
        StatusUpdate.getINSTANCE().print("Parsing\t\tdone");
        sfr.close();
        StringBuilder sb = new StringBuilder();
        for (Future<List<ReadTMP>> future : readFutures) {
            try {
                List<ReadTMP> readList = future.get();
                for (ReadTMP read : readList) {
                    if (read != null) {
                        String name = read.name;
                        int refStart = read.refStart;
                        byte[] readBases = read.readBases;
                        double[] quality = read.quality;
                        boolean hasQuality = read.hasQuality;
                        boolean[] cigar = read.cigar;
                        if (readMap.containsKey(name)) {
                            if (hasQuality) {
                                readMap.get(name).setPairedEnd(readBases, refStart, refStart + readBases.length, quality, cigar);
                            } else {
                                readMap.get(name).setPairedEnd(readBases, refStart, refStart + readBases.length, cigar);
                            }
                            Read r2 = readMap.get(name);
                            if (r2.isPaired()) {
                                sb.append(r2.getCrickEnd() - r2.getWatsonBegin() + "\n");
                                if (Globals.getINSTANCE().isUNPAIRED()) {
                                    readMap.put(name + "_R", r2.unpair());
                                } else {
                                    if ((r2.getCrickBegin() - r2.getWatsonEnd()) > 2000) {
                                        readMap.put(name + "_R", r2.unpair());
                                    }
//                                    if (r2.getCrickBegin() - r2.getWatsonEnd() < 0) {
//                                        System.out.println(name + "\t" + r2.getWatsonBegin() + "\t" + r2.getWatsonEnd() + "\t" + r2.getCrickBegin() + "\t" + r2.getCrickEnd());
//                                    }
                                }
                            }
                        } else {
                            if (hasQuality) {
                                readMap.put(name, new Read(readBases, refStart, refStart + readBases.length, quality, cigar));
                            } else {
                                readMap.put(name, new Read(readBases, refStart, refStart + readBases.length, cigar));
                            }
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException ex) {
                System.err.println(ex);
            }
        }
        readFutures.clear();

        return readMap;
    }

    public static Read[] parseBAMSAM(String location) {
        Map<String, Read> readMap = parseBAMSAMPure(location);

        Map<Integer, Read> hashed = new HashMap<>();
        for (Read r1 : readMap.values()) {
            if (r1 != null) {
                int hash = r1.hashCode();
                if (hashed.containsKey(hash)) {
                    hashed.get(hash).incCount();
                } else {
                    hashed.put(hash, r1);
                }
            }
        }
        return hashed.values().toArray(new Read[hashed.size()]);
    }

    public static Read[] parseFastaInput(String path) {
        List<Read> hashing = new ArrayList<>();
        Map<Integer, Read> hashMap = new HashMap<>();
        String[] parseFarFile = parseFarFile(path);
        for (String s : parseFarFile) {
            byte[] packed = splitReadIntoByteArray(s);
            boolean[] cigar = new boolean[s.length()];
            for (int i = 0; i < s.length(); i++) {
                cigar[i] = true;
            }
            Read r = new Read(packed, 0, s.length(), cigar);
            if (hashMap.containsKey(r.hashCode())) {
                hashMap.get(r.hashCode()).incCount();
            } else {
                hashMap.put(r.hashCode(), new Read(packed, 0, s.length(), cigar));
            }
        }
        hashing.addAll(hashMap.values());
        return hashing.toArray(new Read[hashing.size()]);
    }

    public static String reverse(int[] intArray) {
        StringBuilder sb = new StringBuilder();
        for (int i : intArray) {
            sb.append(reverse(i));
        }
        return sb.toString();
    }

    public static String reverse(byte[] bArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bArray) {
            sb.append(reverse(b));
        }
        return sb.toString();
    }

    public static String reverse(byte i) {
        switch (i) {
            case 0:
                return "A";
            case 1:
                return "C";
            case 2:
                return "G";
            case 3:
                return "T";
            case 4:
                return "N";
        }
        throw new IllegalAccessError();
    }

    public static String reverse(int i) {
        switch (i) {
            case 0:
                return "A";
            case 1:
                return "C";
            case 2:
                return "G";
            case 3:
                return "T";
            case 4:
                return "N";
        }
        throw new IllegalAccessError("" + i);
    }

    public static char reverseChar(int v) {
        switch ((short) v) {
            case 0:
                return 'A';
            case 1:
                return 'C';
            case 2:
                return 'G';
            case 3:
                return 'T';
            case 4:
                return 'N';
            default:
                throw new IllegalStateException("cannot reverse " + v);
        }
    }

    public static void error() {
        System.out.println("    .o oOOOOOOOo                                            OOOo");
        System.out.println("    Ob.OOOOOOOo  OOOo.      oOOo.                      .adOOOOOOO");
        System.out.println("    OboO\"\"\"\"\"\"\"\"\"\"\"\".OOo. .oOOOOOo.    OOOo.oOOOOOo..\"\"\"\"\"\"\"\"\"'OO");
        System.out.println("    OOP.oOOOOOOOOOOO \"POOOOOOOOOOOo.   `\"OOOOOOOOOP,OOOOOOOOOOOB'");
        System.out.println("    `O'OOOO'     `OOOOo\"OOOOOOOOOOO` .adOOOOOOOOO\"oOOO'    `OOOOo");
        System.out.println("    .OOOO'            `OOOOOOOOOOOOOOOOOOOOOOOOOO'            `OO");
        System.out.println("    OOOOO                 '\"OOOOOOOOOOOOOOOO\"`                oOO");
        System.out.println("   oOOOOOba.                .adOOOOOOOOOOba               .adOOOOo.");
        System.out.println("  oOOOOOOOOOOOOOba.    .adOOOOOOOOOO@^OOOOOOOba.     .adOOOOOOOOOOOO");
        System.out.println(" OOOOOOOOOOOOOOOOO.OOOOOOOOOOOOOO\"`  '\"OOOOOOOOOOOOO.OOOOOOOOOOOOOO");
        System.out.println(" \"OOOO\"       \"YOoOOOOMOIONODOO\"`  .   '\"OOROAOPOEOOOoOY\"     \"OOO\"");
        System.out.println("    Y           'OOOOOOOOOOOOOO: .oOOo. :OOOOOOOOOOO?'         :`");
        System.out.println("    :            .oO%OOOOOOOOOOo.OOOOOO.oOOOOOOOOOOOO?         .");
        System.out.println("    .            oOOP\"%OOOOOOOOoOOOOOOO?oOOOOO?OOOO\"OOo");
        System.out.println("                 '%o  OOOO\"%OOOO%\"%OOOOO\"OOOOOO\"OOO':");
        System.out.println("                      `$\"  `OOOO' `O\"Y ' `OOOO'  o             .");
        System.out.println("    .                  .     OP\"          : o     .");
        System.out.println("                              :");
        System.out.println("                              .");
    }
}

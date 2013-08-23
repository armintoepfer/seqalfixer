/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of AlignmentFixer.
 *
 * AlignmentFixer is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * AlignmentFixer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AlignmentFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.alignmentfixer.informationholder;

import java.util.Arrays;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Read {

    private byte[] watsonSequence;
    private double[] watsonQuality;
    private boolean[] watsonCigar;
    private int watsonBegin;
    private int watsonEnd;
    private int count = 1;
    private byte[] crickSequence;
    private double[] crickQuality;
    private boolean[] crickCigar;
    private int crickBegin;
    private int crickEnd = -1;
    private int insertion;
    private boolean merged;

    public Read(Read r) {
        this.watsonSequence = r.watsonSequence;
        this.watsonQuality = r.watsonQuality;
        this.watsonCigar = r.watsonCigar;
        this.watsonBegin = r.watsonBegin;
        this.watsonEnd = r.watsonEnd;
        this.crickSequence = r.crickSequence;
        this.crickQuality = r.crickQuality;
        this.crickCigar = r.crickCigar;
        this.crickBegin = r.crickBegin;
        this.insertion = r.insertion;
        this.merged = r.merged;
    }

    public Read(byte[] sequence, int begin, int end, double[] quality, boolean[] cigar) {
        this.watsonSequence = sequence;
        this.watsonBegin = begin;
        this.watsonEnd = end;
        this.watsonQuality = quality;
        this.watsonCigar = cigar;
    }

    public Read(byte[] sequence, int begin, int end, boolean[] cigar) {
        this.watsonSequence = sequence;
        this.watsonBegin = begin;
        this.watsonEnd = end;
        this.watsonCigar = cigar;
    }

    public Read(byte[] sequence, int begin, int end, boolean[] watsonCigar, byte[] Csequence, int Cbegin, int Cend, boolean[] Ccigar) {
        this.watsonSequence = sequence;
        this.watsonBegin = begin;
        this.watsonEnd = end;
        this.watsonCigar = watsonCigar;
        setPairedEnd(Csequence, Cbegin, Cend, Ccigar);
        if (end - begin != sequence.length) {
            throw new IllegalAccessError("length problen: watson. Suggested length: " + (end - begin) + ". Actual length: " + sequence.length);
        }
        if (Cend - Cbegin != Csequence.length) {
            throw new IllegalAccessError("length problen: crick");
        }
    }

    public Read(byte[] sequence, int begin, int end, double[] quality, boolean[] watsonCigar, byte[] Csequence, int Cbegin, int Cend, double[] Cquality, boolean[] Ccigar) {
        this.watsonSequence = sequence;
        this.watsonBegin = begin;
        this.watsonEnd = end;
        this.watsonQuality = quality;
        this.watsonCigar = watsonCigar;
        setPairedEnd(Csequence, Cbegin, Cend, Cquality, Ccigar);
        if (end - begin != sequence.length) {
            throw new IllegalAccessError("length problen: watson. Suggested length: " + (end - begin) + ". Actual length: " + sequence.length);
        }
        if (Cend - Cbegin != Csequence.length) {
            throw new IllegalAccessError("length problen: crick");
        }
    }

    public void merge() {
        if (this.watsonEnd < this.crickBegin) {
            return;
        }
        final int length = this.crickEnd - this.watsonBegin;
        byte[] seqConsensus = new byte[length];
        double[] qualConsensus = new double[length];
        boolean[] cigarConsensus = new boolean[length];

        for (int i = 0; i < this.getLength(); i++) {
            seqConsensus[i] = this.getBase(i);
            qualConsensus[i] = this.getQuality(i);
            cigarConsensus[i] = this.getCigar(i);
        }
        this.watsonEnd = this.crickEnd;
        this.watsonSequence = seqConsensus;
        this.watsonQuality = qualConsensus;
        this.watsonCigar = cigarConsensus;
        this.crickEnd = -1;
        this.crickBegin = 0;
        this.crickSequence = null;
        this.crickQuality = null;
        this.crickCigar = null;
        this.merged = true;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getBegin() {
        return this.watsonBegin;
    }

    public void incCount() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public int getInsertSize() {
        return this.crickBegin - this.watsonEnd;
    }

    public Position getPosition(int j) {
        if (j == 0) {
            return Position.WATSON_IN;
        } else if (j < this.watsonEnd - this.watsonBegin - 1) {
            return Position.WATSON_HIT;
        } else if (j == this.getWatsonLength() - 1) {
            return Position.WATSON_OUT;
        } else if (this.isPaired()) {
            if (j > this.getWatsonLength() - 1 && j < this.getWatsonLength() + this.getInsertSize()) {
                return Position.INSERTION;
            } else if (j == this.crickBegin - this.watsonBegin) {
                return Position.CRICK_IN;
            } else if (j > this.crickBegin - this.watsonBegin && j < this.crickBegin + this.getCrickLength() - this.watsonBegin - 1) {
                return Position.CRICK_HIT;
            } else if (j == this.crickBegin + this.getCrickLength() - this.watsonBegin - 1) {
                return Position.CRICK_OUT;
            }
        }
        return Position.ERROR;
    }

    public boolean isHit(int j) {
        if (j < this.getWatsonLength()) {
            return true;
        } else if (this.isPaired() && j >= this.getWatsonLength() && j < this.getWatsonLength() + this.getInsertSize()) {
            return false;
        } else if (this.isPaired() && j >= this.crickBegin - this.watsonBegin && j < this.crickBegin + this.getCrickLength() - this.watsonBegin) {
            return true;
        } else {
            throw new IllegalAccessError("No such sequence space for hit. j=" + j + "\tl=" + (this.crickBegin + this.getCrickLength() - this.watsonBegin));
        }
    }

    public int getLength() {
        if (this.crickSequence != null) {
            return this.crickEnd - this.watsonBegin;
        } else {
            return this.watsonEnd - this.watsonBegin;
        }
    }

    public int getEnd() {
        if (this.crickEnd == -1) {
            return watsonEnd;
        } else {
            return this.crickEnd;
        }
    }

    public byte[] getSequence() {
        return this.watsonSequence;
    }

    public double getQuality(int j) {
        if (watsonQuality != null) {
            if (j < this.getWatsonLength()) {
                return this.watsonQuality[j];//BitMagic.getPosition(this.watsonSequence, j);
            } else if (this.isPaired() && j >= this.crickBegin - this.watsonBegin && j <= this.crickBegin + this.getCrickLength() - this.watsonBegin) {
                return this.crickQuality[j - this.getWatsonLength() - this.getInsertSize()];//BitMagic.getPosition(this.crickSequence, j - this.getWatsonLength() - this.getInsertSize());
            } else {
                throw new IllegalAccessError("No such sequence space. j=" + j);
            }
        } else {
            return 1;
        }
    }

    public byte getBase(int j) {
        if (j < this.getWatsonLength()) {
            return this.watsonSequence[j];
        } else if (this.isPaired() && j >= this.crickBegin - this.watsonBegin && j <= this.crickBegin + this.getCrickLength() - this.watsonBegin) {
            return this.crickSequence[j - this.getWatsonLength() - this.getInsertSize()];
        } else {
            throw new IllegalAccessError("No such sequence space. j=" + j);
        }
    }

    public boolean getCigar(int j) {
        if (j < this.getWatsonLength()) {
            return this.watsonCigar[j];
        } else if (this.isPaired() && j >= this.crickBegin - this.watsonBegin && j <= this.crickBegin + this.getCrickLength() - this.watsonBegin) {
            return this.crickCigar[j - this.getWatsonLength() - this.getInsertSize()];
        } else {
            throw new IllegalAccessError("No such sequence space. j=" + j);
        }
    }

    public byte getBaseSilent(int j) {
        if (j < this.getWatsonLength()) {
            return this.watsonSequence[j];
        } else if (this.isPaired() && j >= this.crickBegin - this.watsonBegin && j <= this.crickBegin + this.getCrickLength() - this.watsonBegin) {
            return this.crickSequence[j - this.getWatsonLength() - this.getInsertSize()];
        } else {
            return -1;
        }
    }

    public byte[] getCrickSequence() {
        return crickSequence;
    }

    public int getCrickLength() {
        return this.crickEnd - this.crickBegin;
    }

    public int getWatsonLength() {
        return this.watsonEnd - this.watsonBegin;
    }

    public boolean isPaired() {
        return this.crickSequence != null;
    }

    public final void setPairedEnd(byte[] sequence, int begin, int end, double[] quality, boolean[] cigar) {
        this.crickSequence = sequence;
        this.crickBegin = begin;
        this.crickEnd = end;
        this.crickQuality = quality;
        this.crickCigar = cigar;
        rearrange();
        this.insertion = this.crickBegin - this.watsonEnd;
        if (!Globals.getINSTANCE().isUNPAIRED()) {
            merge();
        }
    }

    public final void setPairedEnd(byte[] sequence, int begin, int end, boolean[] cigar) {
        this.crickSequence = sequence;
        this.crickBegin = begin;
        this.crickEnd = end;
        this.crickCigar = cigar;
        rearrange();
        this.insertion = this.crickBegin - this.watsonEnd;
        if (!Globals.getINSTANCE().isUNPAIRED()) {
            merge();
        }
    }

    public int getCrickEnd() {
        return crickEnd;
    }

    public int getWatsonEnd() {
        return watsonEnd;
    }

    public int getWatsonBegin() {
        return watsonBegin;
    }

    public int getCrickBegin() {
        return crickBegin;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Arrays.hashCode(this.watsonSequence);
        hash = 29 * hash + Arrays.hashCode(this.crickSequence);
        hash = 29 * hash + this.watsonBegin;
        hash = 29 * hash + this.watsonEnd;
        hash = 29 * hash + this.crickBegin;
        hash = 29 * hash + this.crickEnd;
        hash = 29 * hash + Arrays.hashCode(this.crickQuality);
        hash = 29 * hash + Arrays.hashCode(this.watsonQuality);
        hash = 29 * hash + Arrays.hashCode(this.crickCigar);
        hash = 29 * hash + Arrays.hashCode(this.watsonCigar);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && obj.hashCode() == this.hashCode();
    }

    public void rearrange() {
        if (this.watsonBegin > this.crickBegin) {
            int beginTmp = this.watsonBegin;
            int endTmp = this.watsonEnd;
            byte[] seqTmp = this.watsonSequence;
            this.watsonBegin = this.crickBegin;
            this.watsonEnd = this.crickEnd;
            this.watsonSequence = this.crickSequence;
            this.crickBegin = beginTmp;
            this.crickEnd = endTmp;
            this.crickSequence = seqTmp;

            double[] qualTmp = this.watsonQuality;
            this.watsonQuality = this.crickQuality;
            this.crickQuality = qualTmp;

            boolean[] cigarTmp = this.watsonCigar;
            this.watsonCigar = this.crickCigar;
            this.crickCigar = cigarTmp;
        }
    }

    public void shrink() {
        this.watsonBegin -= Globals.getINSTANCE().getALIGNMENT_BEGIN();
        this.watsonEnd -= Globals.getINSTANCE().getALIGNMENT_BEGIN();
        if (this.crickSequence != null) {
            this.crickBegin -= Globals.getINSTANCE().getALIGNMENT_BEGIN();
            this.crickEnd -= Globals.getINSTANCE().getALIGNMENT_BEGIN();
        }
    }

    public Read unpair() {
        Read r = null;
        if (this.crickQuality != null) {
            r = new Read(crickSequence, crickBegin, crickEnd, crickQuality, crickCigar);
        } else {
            r = new Read(crickSequence, crickBegin, crickEnd, crickCigar);
        }
        this.crickBegin = -1;
        this.crickEnd = -1;
        this.crickSequence = null;
        return r;
    }

    public enum Position {

        WATSON_IN,
        WATSON_HIT,
        WATSON_OUT,
        INSERTION,
        CRICK_IN,
        CRICK_HIT,
        CRICK_OUT,
        ERROR;
    }

    public double[] getWatsonQuality() {
        return watsonQuality;
    }

    public double[] getCrickQuality() {
        return crickQuality;
    }

    public int getInsertion() {
        return insertion;
    }

    public void setInsertion(int insertion) {
        this.insertion = insertion;
    }

    public boolean[] getWatsonCigar() {
        return watsonCigar;
    }

    public boolean[] getCrickCigar() {
        return crickCigar;
    }

    public boolean isMerged() {
        return merged;
    }
}

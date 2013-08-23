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
package ch.ethz.bsse.saf.informationholder;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class ReadTMP {
    public String name;
    public double[] quality;
    public byte[] readBases;
    public int refStart;
    public boolean hasQuality;
    public boolean[] cigar;

    public ReadTMP(String name, double[] quality, byte[] readBases, int refStart, boolean hasQuality, boolean[] cigar) {
        this.name = name;
        this.quality = quality;
        this.readBases = readBases;
        this.refStart = refStart;
        this.hasQuality = hasQuality;
        this.cigar = cigar;
    }
}

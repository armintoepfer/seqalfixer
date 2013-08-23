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
package ch.ethz.bsse.saf.utils;

import ch.ethz.bsse.saf.informationholder.Globals;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class StatusUpdate {

    private String oldOut = "";
    private double hammingCount = 0;
    private double PERCENTAGE = 0;
    private long start = System.currentTimeMillis();
    private final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private static final StatusUpdate INSTANCE = new StatusUpdate();

    public static StatusUpdate getINSTANCE() {
        return INSTANCE;
    }

    private StatusUpdate() {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void print(String s) {
        if (!oldOut.equals(s)) {
            this.oldOut = s;
            System.out.print("\r" + time() + " " + s);
        }
    }

    public void println(String s) {
        System.out.print("\n" + time() + " " + s);
    }

    public String time() {
        return df.format(new Date(System.currentTimeMillis() - start));
    }

    public double getPERCENTAGE() {
        return PERCENTAGE;
    }

    public void setPERCENTAGE(double PERCENTAGE) {
        this.PERCENTAGE = PERCENTAGE;
    }
}

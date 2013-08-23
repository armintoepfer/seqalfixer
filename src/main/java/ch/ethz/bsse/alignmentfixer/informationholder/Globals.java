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
package ch.ethz.bsse.alignmentfixer.informationholder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Information holder for all necessary given and inferred parameters.
 *
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Globals {

    private static final Globals INSTANCE = new Globals();

    public static Globals getINSTANCE() {
        return INSTANCE;
    }
    private boolean UNPAIRED;
    private boolean WINDOW;
    private boolean DEBUG;
    private boolean STORAGE;
    private int WINDOW_BEGIN;
    private int WINDOW_END;
    private int ALIGNMENT_BEGIN = Integer.MAX_VALUE;
    private int ALIGNMENT_END = Integer.MIN_VALUE;
    private final int cpus = Runtime.getRuntime().availableProcessors();
    private String GENOME;
    private String SAVEPATH;

    public Globals getInstance() {
        return INSTANCE;
    }

    public synchronized void log(Object o) {
        if (DEBUG) {
            System.out.print(o);
        }
    }

    public int getALIGNMENT_BEGIN() {
        return ALIGNMENT_BEGIN;
    }

    public int getALIGNMENT_END() {
        return ALIGNMENT_END;
    }

    public String getSAVEPATH() {
        return SAVEPATH;
    }

    public void setALIGNMENT_BEGIN(int ALIGNMENT_BEGIN) {
        this.ALIGNMENT_BEGIN = ALIGNMENT_BEGIN;
    }

    public void setALIGNMENT_END(int ALIGNMENT_END) {
        this.ALIGNMENT_END = ALIGNMENT_END;
    }

    public void setSAVEPATH(String SAVEPATH) {
        this.SAVEPATH = SAVEPATH;
    }

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    public int getCpus() {
        return cpus;
    }

    public boolean isSTORAGE() {
        return STORAGE;
    }

    public void setSTORAGE(boolean STORAGE) {
        this.STORAGE = STORAGE;
    }

    public String getGENOME() {
        return GENOME;
    }

    public void setGENOME(String GENOME) {
        this.GENOME = GENOME;
    }

    public boolean isWINDOW() {
        return WINDOW;
    }

    public void setWINDOW(boolean WINDOW) {
        this.WINDOW = WINDOW;
    }

    public int getWINDOW_BEGIN() {
        return WINDOW_BEGIN;
    }

    public void setWINDOW_BEGIN(int WINDOW_BEGIN) {
        this.WINDOW_BEGIN = WINDOW_BEGIN;
    }

    public int getWINDOW_END() {
        return WINDOW_END;
    }

    public void setWINDOW_END(int WINDOW_END) {
        this.WINDOW_END = WINDOW_END;
    }

    public void setUNPAIRED(boolean UNPAIRED) {
        this.UNPAIRED = UNPAIRED;
    }

    public boolean isUNPAIRED() {
        return UNPAIRED;
    }

    public boolean isDEBUG() {
        return DEBUG;
    }
}

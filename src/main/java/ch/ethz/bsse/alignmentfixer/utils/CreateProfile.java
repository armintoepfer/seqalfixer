/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of ProfileSim.
 *
 * ProfileSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * ProfileSim is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ProfileSim. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.alignmentfixer.utils;

import ch.ethz.bsse.alignmentfixer.informationholder.Globals;
import ch.ethz.bsse.alignmentfixer.informationholder.Read;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class CreateProfile {

    private int L;
    private int N = 0;

    public CreateProfile(String genomePath, String alignmentPath) {
        Globals.getINSTANCE().setUNPAIRED(true);
        Read[] alignmentReads = Utils.parseBAMSAM(alignmentPath);
        fixAlignment(alignmentReads);
        StatusUpdate.getINSTANCE().println("Reads\t"+N);
    }
    
    private void fixAlignment(Read[] reads) {
        //fix alignment to position 0
        for (Read r : reads) {
            Globals.getINSTANCE().setALIGNMENT_BEGIN(Math.min(r.getBegin(), Globals.getINSTANCE().getALIGNMENT_BEGIN()));
            Globals.getINSTANCE().setALIGNMENT_END(Math.max(r.getEnd(), Globals.getINSTANCE().getALIGNMENT_END()));
            N += r.getCount();
        }
        L = Globals.getINSTANCE().getALIGNMENT_END() - Globals.getINSTANCE().getALIGNMENT_BEGIN();
        Globals.getINSTANCE().setALIGNMENT_END(L);
        StatusUpdate.getINSTANCE().println("Modifying reads\t");
        
        Parallel.ForEach(Arrays.asList(reads), new LoopBody<Read>() {
            public void run(Read r) {
                r.shrink();
            }
        });
        StatusUpdate.getINSTANCE().print("Modifying reads\t100%");
    }

}

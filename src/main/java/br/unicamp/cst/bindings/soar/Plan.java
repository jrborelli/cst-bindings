/***********************************************************************************************
 * Copyright (c) 2012  DCA-FEEC-UNICAMP
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * K. Raizer, A. L. O. Paraense, E. M. Froes, R. R. Gudwin - initial API and implementation
 ***********************************************************************************************/
package br.unicamp.cst.bindings.soar;

import java.util.ArrayList;
import java.util.List;

public class Plan {

    private boolean isFinished;
    private List<Plan> subPlans;
    private Object content;

    public Plan(List<Plan> subPlans, Object content) {
        this.subPlans = subPlans;
        this.content = content;
        this.setFinished(false);
    }

    public Plan(Object content) {
        this.setFinished(false);
        this.content = content;

        this.subPlans = new ArrayList<>();
    }

    public List<Plan> getSubPlans() {
        return subPlans;
    }

    public void setSubPlans(List<Plan> subPlans) {
        this.subPlans = subPlans;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}

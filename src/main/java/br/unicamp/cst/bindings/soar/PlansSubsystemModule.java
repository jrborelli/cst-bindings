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

/**
 *
 * @author du
 */
public class PlansSubsystemModule {

    // Codelets in System 2;
    private JSoarCodelet jSoarCodelet;
    private PlanSelectionCodelet planSelectionCodelet;

    public PlansSubsystemModule(){

    }

    public boolean verifyExistCodelets() {
        if (jSoarCodelet != null)
            return true;
        else
            return false;
    }

    public PlansSubsystemModule(JSoarCodelet jSoarCodelet){
        this.setjSoarCodelet(jSoarCodelet);
    }

    public JSoarCodelet getjSoarCodelet() {
        return jSoarCodelet;
    }

    public void setjSoarCodelet(JSoarCodelet jSoarCodelet) {
        this.jSoarCodelet = jSoarCodelet;
    }

    public PlanSelectionCodelet getPlanSelectionCodelet() {
        return planSelectionCodelet;
    }

    public void setPlanSelectionCodelet(PlanSelectionCodelet planSelectionCodelet) {
        this.planSelectionCodelet = planSelectionCodelet;
    }
}

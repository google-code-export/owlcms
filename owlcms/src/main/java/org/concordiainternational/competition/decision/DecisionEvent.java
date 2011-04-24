/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Open Software Licence, Version 3.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.opensource.org/licenses/osl-3.0.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.concordiainternational.competition.decision;

import java.util.EventObject;

import org.concordiainternational.competition.data.Lifter;

public class DecisionEvent extends EventObject {
    private static final long serialVersionUID = 2789988074894824591L;

    public enum Type {
        DOWN, // two refs have given the same decision, lifter can put weight
              // down
        WAITING, // two refs have given different decisions, waiting for third
        UPDATE, // any change during 3 second period where refs can change their decision
        SHOW, // all referees have given decisions, public sees decision
        BLOCK, // cannot change decision after 3 seconds
        RESET, // after 5 seconds, or if announced
    }

    private Type type;
    private long when;
    private Decision[] decisions;
	private Lifter lifter;
	private Integer attempt;
	private Boolean accepted;

    public DecisionEvent(IDecisionController source, Type down, long currentTimeMillis, Decision[] refereeDecisions) {
        super(source);
        this.type = down;
        this.when = currentTimeMillis;
        this.decisions = refereeDecisions;
        this.lifter = source.getLifter();
        if (lifter != null) {
        	this.setAttempt(lifter.getAttemptsDone());
        }
        this.setAccepted(computeAccepted());
    }

    @Override
    public String toString() {
        switch (this.type) {
        case DOWN:
            return type + " " + when;
        case RESET:
            return "reset";
        default:
            return type + " " + decisions[0].accepted + " " + decisions[1].accepted + " " + decisions[2].accepted;
        }
    }

    public Type getType() {
        return type;
    }

    public Decision[] getDecisions() {
        return decisions;
    }

    public boolean isDisplayable() {
    	int count = 0;
    	for (Decision decision : decisions){
    		if (decision.accepted != null) count++;
    	}
    	return count == 3;
    }
    
    /**
     * Return true for a good lift, false for a rejected lift, null if decisions are pending.
     * @return null if no decision yet, true if 3 decisions and at least 2 good, false otherwise
     */
    public Boolean computeAccepted() {
    	int count = 0;
    	int countAccepted = 0;
    	for (Decision decision : decisions){
    		final Boolean accepted1 = decision.accepted;
			if (accepted1 != null) {
    			count++;
    			if (accepted1) countAccepted++;
    		}
    	}
    	if (count < 3) {
    		return null;
    	} else {
    		return countAccepted >= 2;
    	}
    }

	public Lifter getLifter() {
		return lifter;
	}

	public void setAttempt(Integer attempt) {
		this.attempt = attempt;
	}

	public Integer getAttempt() {
		return attempt;
	}

	public void setAccepted(Boolean accepted) {
		this.accepted = accepted;
	}

	public Boolean isAccepted() {
		return accepted;
	}
}

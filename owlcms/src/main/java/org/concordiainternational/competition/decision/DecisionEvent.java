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

import org.concordiainternational.competition.decision.DecisionController.Decision;

public class DecisionEvent extends EventObject {
    private static final long serialVersionUID = 2789988074894824591L;

    public enum Type {
        DOWN, // two refs have given the same decision, lifter can put weight
              // down
        WAITING, // two refs have given different decisions, waiting for third
        UPDATE, // during 3 second period where refs can change their decision
        SHOW, // 3 seconds period is over, public sees decision
        RESET, // after 5 seconds, or if announced
    }

    private Type type;
    private long when;
    private Decision[] decisions;;

    public DecisionEvent(DecisionController source, Type down, long currentTimeMillis,
            DecisionController.Decision[] refereeDecisions) {
        super(source);
        this.type = down;
        this.when = currentTimeMillis;
        this.decisions = refereeDecisions;
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

}

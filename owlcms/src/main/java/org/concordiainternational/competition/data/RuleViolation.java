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

package org.concordiainternational.competition.data;

public class RuleViolation {

    // public static RuleViolationException change1ValueTooSmall(Object... objs)
    // {
    //		return new RuleViolationException(("RuleViolation.change1ValueTooSmall"), objs);   //$NON-NLS-1$
    // }
    //	
    // public static RuleViolationException change2ValueTooSmall(Object... objs)
    // {
    //		return new RuleViolationException(("RuleViolation.change2ValueTooSmall"), objs);   //$NON-NLS-1$
    // }
    //	
    public static RuleViolationException declarationValueTooSmall(Object... objs) {
        return new RuleViolationException(("RuleViolation.declarationValueTooSmall"), objs); //$NON-NLS-1$
    }

    //
    // public static RuleViolationException liftValueTooSmall(Object... objs) {
    //		return new RuleViolationException(("RuleViolation.liftValueTooSmall"), objs);   //$NON-NLS-1$
    // }

    public static RuleViolationException liftValueNotWhatWasRequested(Object... objs) {
        return new RuleViolationException(("RuleViolation.liftValueNotWhatWasRequested"), objs); //$NON-NLS-1$
    }

    public static RuleViolationException declaredChangesNotOk(Object... objs) {
        return new RuleViolationException(("RuleViolation.declaredChangesNotOk"), objs); //$NON-NLS-1$
    }

    public static RuleViolationException liftValueBelowProgression(int curLift, String actualLift,
            int automaticProgression) {
        return new RuleViolationException(
                ("RuleViolation.liftValueBelowProgression"), curLift, actualLift, automaticProgression); //$NON-NLS-1$
    }

}

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

import org.concordiainternational.competition.ui.GroupData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionTester implements DecisionController.DecisionEventListener {

    private Logger logger = LoggerFactory.getLogger(DecisionTester.class);

    @Override
    public void updateEvent(DecisionEvent updateEvent) {
        logger.info(updateEvent.toString());
    }

    @Test
    public void runTest() {
        DecisionController decisionController = new DecisionController(new GroupData(null));
        decisionController.addListener(this);
        decisionController.reset();
        try {
            decisionController.decisionMade(0, true);
            decisionController.decisionMade(1, true);
            decisionController.decisionMade(2, true);
            Thread.sleep(1001);
            decisionController.decisionMade(2, false);
            Thread.sleep(2100);
            decisionController.decisionMade(1, false);
        } catch (InterruptedException e) {
        }

    }

}

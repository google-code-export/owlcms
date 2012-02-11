/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.decision;

import org.concordiainternational.competition.ui.SessionData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefereeDecisionTester implements DecisionEventListener {

    private Logger logger = LoggerFactory.getLogger(RefereeDecisionTester.class);

    @Override
    public void updateEvent(DecisionEvent updateEvent) {
        logger.info(updateEvent.toString());
    }

    @Test
    public void runTest() {
        IDecisionController decisionController = new RefereeDecisionController(new SessionData(null));
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

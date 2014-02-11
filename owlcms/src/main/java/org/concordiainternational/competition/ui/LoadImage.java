/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.ui;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.weelayout.WeeLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class LoadImage extends WeeLayout {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(LoadImage.class);
    private static final long serialVersionUID = 8340222363211435843L;

    private int weight;
    private Window parentWindow;

    public LoadImage() {
        super(Direction.HORIZONTAL);
        addStyleName("loadChart");
        setMargin(false);
    }

    public LoadImage(Window parentWindow) {
        this();
        this.parentWindow = parentWindow;
    }

    public void computeImageArea(SessionData masterData, Platform platform, boolean showCaption) {
        if (masterData == null || platform == null)
            return;

        final Lifter currentLifter = masterData.getCurrentLifter();
        final Integer barWeight = computeBarWeight(masterData, platform);
        if (currentLifter == null) {
            setCaption("");
            return;
        }
        weight = currentLifter.getNextAttemptRequestedWeight();
        final String caption = weight + "kg";

        createImageArea(platform, barWeight, (showCaption ? caption : ""));
    }

    /**
     * @param platform
     * @param barWeight
     * @param caption
     */
    private void createImageArea(Platform platform, final Integer barWeight, final String caption) {
        this.removeAllComponents();
        setCaption(caption);

        if (weight == 0)
            return;
        // compute the bar and collar first.

        addPlates(1, "bar", barWeight);
        addPlates(1, "barInner", 0);
        final Integer collarAvailable = platform.getNbC_2_5();
        boolean useCollar = collarAvailable > 0;

        if (weight >= 25) {
            if (useCollar) {
                // we only take off the collar weight because we need to
                // wait before showing the collar.
                weight -= 5;
            }

            // use large plates first
            addPlates(platform.getNbL_25(), "L_25", 2 * 25);
            addPlates(platform.getNbL_20(), "L_20", 2 * 20);
            addPlates(platform.getNbL_15(), "L_15", 2 * 15);
            addPlates(platform.getNbL_10(), "L_10", 2 * 10);
        } else {
            int nonBarWeight = weight;
            // make sure that large 5 and large 2.5 are only used when warranted
            // (must not require manual intervention if they are available)
            if (platform.getNbL_2_5() > 0 && nonBarWeight < 10 ||
                    platform.getNbL_5() > 0 && nonBarWeight < 15) {
                useCollar = false;
            }
            if (useCollar) {
                // we take off the collar weight because we need to
                // wait before showing the collar.
                weight -= 5;
                nonBarWeight -= 5;
            }
            addPlates(platform.getNbL_10(), "L_10", 2 * 10);
            addPlates(platform.getNbL_5(), "L_5", 2 * 5);
            if (nonBarWeight < 10) {
                addPlates(platform.getNbL_2_5(), "L_2_5", 2 * 2.5);
            }

        }

        // add the small plates
        addPlates(platform.getNbS_5(), "S_5", 2 * 5);
        addPlates(platform.getNbS_2_5(), "S_2_5", 2 * 2.5);
        // collar is depicted here
        if (useCollar) {
            // we add back the collar weight we took off above
            weight += 5;
            addPlates(collarAvailable, "C_2_5", 2 * 2.5);
        }
        // remainder of small plates
        addPlates(platform.getNbS_2(), "S_2", 2 * 2);
        addPlates(platform.getNbS_1_5(), "S_1_5", 2 * 1.5);
        addPlates(platform.getNbS_1(), "S_1", 2 * 1);
        addPlates(platform.getNbS_0_5(), "S_0_5", 2 * 0.5);
        addPlates(1, "barOuter", 0);
    }

    /**
     * @param availablePlates
     * @param style
     * @param plateWeight
     * @return
     */
    private int addPlates(Integer availablePlates, String style, double plateWeight) {
        int subtractedWeight = 0;
        while (availablePlates > 0 && weight >= plateWeight) {
            Label plate = new Label();
            plate.setSizeUndefined();
            plate.addStyleName(style);
            if (!style.startsWith("bar")) {
                plate.addStyleName("plate");
            }
            this.addComponent(plate);
            this.setComponentAlignment(plate, Alignment.MIDDLE_CENTER);
            final long delta = Math.round(plateWeight);
            weight -= delta;
            subtractedWeight += delta;
            availablePlates--;
        }
        return subtractedWeight;
    }

    private Integer computeBarWeight(SessionData masterData, Platform platform) {
        if (masterData == null || platform == null)
            return 0;
        if (platform.getLightBar() > 0) {
            return platform.getLightBar();
        } else {
            return computeOfficialBarWeight(masterData, platform);
        }
    }

    /**
     * @return
     */
    private Integer computeOfficialBarWeight(SessionData masterData, Platform platform) {
        if (masterData == null || platform == null)
            return 0;

        final Lifter currentLifter = masterData.getCurrentLifter();
        String gender = "M";
        if (currentLifter != null) {
            gender = currentLifter.getGender();
        }
        final int expectedBarWeight = "M".equals(gender) ? 20 : 15;
        return expectedBarWeight;
    }

    @Override
    public void setCaption(String caption) {
        if (parentWindow == null) {
            super.setCaption(caption);
        } else {
            parentWindow.setCaption(caption);
        }
    }

}

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

    public void computeImageArea(SessionData masterData, Platform platform) {
        final Lifter currentLifter = masterData.getCurrentLifter();
        final Integer barWeight = computeBarWeight(masterData, platform);
        if (currentLifter == null) {
            setCaption("");
            return;
        }
        weight = currentLifter.getNextAttemptRequestedWeight();
        final String caption = weight + "kg";

        createImageArea(platform, barWeight, caption);
    }

    /**
     * @param platform
     * @param barWeight
     * @param caption
     */
    private void createImageArea(Platform platform, final Integer barWeight, final String caption) {
        this.removeAllComponents();
        setCaption(caption);
        
        if (weight == 0) return;
        // compute the bar and collar first.

        addPlates(1, "bar", barWeight);
        addPlates(1, "barInner", 0);
        final Integer collarAvailable = platform.getNbC_2_5();
        logger.warn("collars available = {}",collarAvailable);
        boolean useCollar = false;
        
        final int nonBarWeight = weight;
        if (weight >= 25) {
	        if (collarAvailable > 0) {
	            // we only take off the collar weight because we need to
	            // wait before showing the collar.
	            weight -= 5;
	            useCollar = true;
	        }
	
	        // use large plates first
	        addPlates(platform.getNbL_25(), "L_25", 2 * 25);
	        addPlates(platform.getNbL_20(), "L_20", 2 * 20);
	        addPlates(platform.getNbL_15(), "L_15", 2 * 15);
	        addPlates(platform.getNbL_10(), "L_10", 2 * 10);
        } else {
            // make sure that large 5 and large 2.5 are only used when warranted
            // (must not require manual intervention if they are available)
	        addPlates(platform.getNbL_10(), "L_10", 2 * 10);
            addPlates(platform.getNbL_5(), "L_5", 2 * 5);
            if (nonBarWeight < 10) {
                addPlates(platform.getNbL_2_5(), "L_2_5", 2 * 2.5);      	
            }

        }
        

        // then small plates
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

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

import org.concordiainternational.competition.data.CompetitionSession;

import com.vaadin.terminal.StreamResource;

/**
 * This interface defines the actions that are managed by the application
 * controller. In principle, all actions that affect more than a single
 * application pane should be registered here
 * 
 * @author jflamy
 * 
 */
public interface UserActions {

    public abstract void setPlatformByName(String plaftorm);

    public abstract void setCurrentCompetitionSession(CompetitionSession value);

    /**
     * @param streamSource
     * @param filename
     */
    public abstract void openSpreadsheet(StreamResource.StreamSource streamSource,
            final String filename);

}
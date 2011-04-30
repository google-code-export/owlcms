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

package org.concordiainternational.competition.ui.components;

import com.vaadin.ui.ComponentContainer;

/**
 * In this application the views need to refresh themselves when the user
 * switches groups, or when they receive events.
 * 
 */
public interface ApplicationView extends ComponentContainer {

    public void refresh();
    
    /**
     * @return true if the menu bar is needed
     */
    public boolean needsMenu();

    /**
     */
    public void setParametersFromFragment();
    
    /**
     */
    public String getFragment();

}

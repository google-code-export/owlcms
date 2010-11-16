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

package org.concordiainternational.competition.ui.list;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.ui.CompetitionApplication;

import com.vaadin.Application;

/**
 * This class specializes the List component to use the LifterContainer class.
 * LifterContainer takes into account the currently active filters set in the
 * application.
 * 
 * @author jflamy
 * 
 */
public abstract class LifterHbnList extends GenericHbnList<Lifter> {
    private static final long serialVersionUID = 1L;

    public LifterHbnList(Application app, String caption) {
        super(app, Lifter.class, caption);
    }

    /**
     * Load container content to Table
     */
    @Override
    protected void loadData() {
        final LifterContainer cont = new LifterContainer((CompetitionApplication) app);
        table.setContainerDataSource(cont);
    }

}

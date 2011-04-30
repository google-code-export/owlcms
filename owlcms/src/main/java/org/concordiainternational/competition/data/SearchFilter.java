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

import java.io.Serializable;

@SuppressWarnings("serial")
public class SearchFilter implements Serializable {

    private final String term;
    private final Object propertyId;
    private String searchName;

    public SearchFilter(Object propertyId, String searchTerm, String name) {
        this.propertyId = propertyId;
        this.term = searchTerm;
        this.searchName = name;
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @return the propertyId
     */
    public Object getPropertyId() {
        return propertyId;
    }

    /**
     * @return the name of the search
     */
    public String getSearchName() {
        return searchName;
    }

    @Override
    public String toString() {
        return getSearchName();
    }

}

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

package org.concordiainternational.competition.timer;

public interface CountdownTimerListener {

    void finalWarning(int timeRemaining);

    void initialWarning(int timeRemaining);

    void noTimeLeft(int timeRemaining);

    void normalTick(int timeRemaining);

    /**
     * timer has been stopped, lifter is still associated with timer.
     * 
     * @param timeRemaining
     */
    void pause(int timeRemaining);

    void start(int timeRemaining);

    /**
     * timer has been stopped and associated lifter has been cleared.
     * 
     * @param timeRemaining
     */
    void stop(int timeRemaining);

    /**
     * someone is forcing the amount of time.
     * 
     * @param startTime
     */
    void forceTimeRemaining(int startTime);

}

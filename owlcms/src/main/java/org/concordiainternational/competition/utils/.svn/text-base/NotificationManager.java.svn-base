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

package org.concordiainternational.competition.utils;

import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to manage notifications from domain objects.
 * 
 * This class allows a domain object to have multiple active editors; as long as
 * there is an editor, the listener should be informed. When a domain object no
 * longer has an active editor, it should stop broadcasting events to the
 * listener.
 * 
 * TODO: this manager could manage several listeners.
 * 
 * @author jflamy
 * 
 * @param <Listener>
 *            the listening class, notified by E instances when they change
 * @param <Editable>
 *            editable class, notifies the Listening class, edited by the Editor
 *            class
 * @param <Editor>
 *            Edits the Editable, which notifies the Listener
 */
public class NotificationManager<Listener extends EventListener, Editable extends Notifier, Editor> {
    Logger logger = LoggerFactory.getLogger(NotificationManager.class);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    private Listener listener;

    public NotificationManager(Listener listener) {
        this.listener = listener;
    }

    Map<Editable, Set<Editor>> editorMap = new HashMap<Editable, Set<Editor>>();

    public void addEditor(Editable editable, Editor editor) {
        Set<Editor> set = editorMap.get(editable);
        if (set == null) {
            set = new HashSet<Editor>();
            set.add(editor);
            editorMap.put(editable, set);
        } else {
            set.add(editor);
        }
        editable.addListener(listener);
        logger.debug(dump());
    }

    public void removeEditor(Editable editable, Editor editor) {
        Set<Editor> set = editorMap.get(editable);
        if (set == null) {
            // nothing to do.
        } else {
            set.remove(editor);
            if (set.size() == 0) {
                // no-one is editing editable anymore
                editorMap.remove(editable);
            }
        }
        if (set == null || set.size() == 0) {
            // no-one is editing the editable anymore, listener can
            // stop listening for updates.
            editable.removeListener(listener);
        }
        logger.debug(dump());
    }

    public String dump() {
        Set<Entry<Editable, Set<Editor>>> entrySet = editorMap.entrySet();
        StringBuffer sb = new StringBuffer();
        sb.append("Notifiers and Editors for listener "); //$NON-NLS-1$
        sb.append(listener);
        sb.append(LINE_SEPARATOR);
        for (Entry<Editable, Set<Editor>> entry : entrySet) {
            sb.append("   "); //$NON-NLS-1$
            sb.append(entry.getKey());
            sb.append(": "); //$NON-NLS-1$
            Set<Editor> editors = entry.getValue();
            for (Editor editor : editors) {
                sb.append(editor);
                sb.append("; "); //$NON-NLS-1$
            }
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

}

/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.utils;

import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

    synchronized public void addEditor(Editable editable, Editor editor) {
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

    synchronized public void removeEditor(Editable editable, Editor editor) {
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

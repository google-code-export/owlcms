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

package org.concordiainternational.competition.spreadsheet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class contains utilities to locak a file. The class is currently unused
 * and is kept for reference.
 * 
 * @author jflamy
 * 
 */
@Deprecated
public class Locking {
    /**
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String path = "C:\\Documents and Settings\\jflamy\\Mes documents\\Halt�ro\\Comp�tition\\Current\\Masters.xls";
    private List<LifterReader> allLifters;

    @SuppressWarnings("unused")
    private FileLock lockSheet() throws FileNotFoundException, IOException {
        FileChannel lockChannel;
        FileLock lock;
        File lockFile;
        // we cannot lock the spreadsheet proper (it is always locked).
        // so we create a lock file next to it and lock it (atomically)
        // the spreadsheet does the reverse operation, so lock() waits.
        lockFile = new File(path.replace(".xls", ".lock"));
        lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
        System.err.println("waiting for lock on " + lockFile.getCanonicalPath());
        lock = lockChannel.lock(); // lock() blocks until it can retrieve

        // the lock.
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.err.println("acquired lock at " + timestamp);
        return lock;
    }

    /**
     * @param lock
     * @param sheetLock
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void releaseLock(FileLock lock, FileLock sheetLock) throws IOException {
        if (lock != null) lock.release();
        if (sheetLock != null) sheetLock.release();
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.err.println("released locks at " + timestamp);
    }

    private static long lastReadTimeStamp = 0L;
    private static long refreshInterval = 3500;

    /**
     * @return
     */
    @SuppressWarnings("unused")
    private List<LifterReader> checkIfRecentlyRead() {
        // old method
        long delaySinceLastRead = System.currentTimeMillis() - lastReadTimeStamp;
        if (delaySinceLastRead < refreshInterval && allLifters != null) {
            System.err.println("delaySinceLastRead=" + delaySinceLastRead + " " + allLifters
                + " -- returning previous list");
            return allLifters;
        } else {
            System.err.println("delaySinceLastRead=" + delaySinceLastRead + " " + allLifters
                + " -- returning updated list");
            lastReadTimeStamp = System.currentTimeMillis();
            return null;
        }
    }
}

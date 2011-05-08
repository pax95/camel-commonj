/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.commonj;

import commonj.work.Work;
import commonj.work.WorkManager;

public class WorkManagerThreadWrapper extends Thread {
    private WorkManager wm;
    private Thread thread;
    private Work work;

    /**
     * Every WorkmanagerThreadWrapper has a WorkManager and a RunnableWrapper
     * associated with it.
     */
    public WorkManagerThreadWrapper(WorkManager wm, Thread thread) {
        super();
        this.wm = wm;
        this.thread = thread;
    }

    public void destroy() {
        if (work != null) {
            work.release();
        }
    }

    public ClassLoader getContextClassLoader() {
        return thread.getContextClassLoader();
    }

    public void interrupt() {
        if (work != null) {
            work.release();
        }
        thread.interrupt();
    }

    public boolean isInterrupted() {
        return thread.isInterrupted();
    }

    public void setContextClassLoader(ClassLoader cl) {
        thread.setContextClassLoader(cl);
    }

    /**
     * Start the thread. This will start a WorkManager daemon thread and will
     * immediately submit the WorkItem to it.
     */
    public synchronized void start() {
        work = new WorkItem(thread);
        wm.schedule(work);
    }

}

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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.camel.util.concurrent.ExecutorServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commonj.work.WorkManager;

/**
 * Helper for {@link java.util.concurrent.ExecutorService} to construct
 * executors using a thread factory that create thread names with Camel prefix.
 * <p/>
 * This helper should <b>NOT</b> be used by end users of Camel, as you should
 * use {@link org.apache.camel.spi.ExecutorServiceStrategy} which you obtain
 * from {@link org.apache.camel.CamelContext} to create thread pools.
 * <p/>
 * This helper should only be used internally in Camel.
 * 
 * @version $Revision: 925619 $
 */
public final class WorkManagerExecutorServiceHelper {
    private static Logger LOG = LoggerFactory.getLogger(WorkManagerExecutorServiceHelper.class);
    private static WorkManager workmanager;

    private WorkManagerExecutorServiceHelper() {
    }

    public static void setWorkmanager(WorkManager workmanager) {
        WorkManagerExecutorServiceHelper.workmanager = workmanager;
    }

    /**
     * Creates a new thread name with the given prefix
     * 
     * @param pattern the pattern
     * @param name the name
     * @return the thread name, which is unique
     */
    public static String getThreadName(String pattern, String name) {
        return ExecutorServiceHelper.getThreadName(pattern, name);
    }

    /**
     * Creates a new scheduled thread pool which can schedule threads.
     * 
     * @param poolSize the core pool size
     * @param pattern pattern of the thread name
     * @param name ${name} in the pattern name
     * @param daemon whether the threads is daemon or not
     * @return the created pool
     */
    public static ScheduledExecutorService newScheduledThreadPool(final int poolSize, final String pattern, final String name, final boolean daemon) {
        return Executors.newScheduledThreadPool(poolSize, new WorkmanagerThreadFactory(pattern, name, daemon));
    }

    /**
     * Creates a new fixed thread pool
     * 
     * @param poolSize the fixed pool size
     * @param pattern pattern of the thread name
     * @param name ${name} in the pattern name
     * @param daemon whether the threads is daemon or not
     * @return the created pool
     */
    public static ExecutorService newFixedThreadPool(final int poolSize, final String pattern, final String name, final boolean daemon) {
        return Executors.newFixedThreadPool(poolSize, new WorkmanagerThreadFactory(pattern, name, daemon));
    }

    /**
     * Creates a new single thread pool (usually for background tasks)
     * 
     * @param pattern pattern of the thread name
     * @param name ${name} in the pattern name
     * @param daemon whether the threads is daemon or not
     * @return the created pool
     */
    public static ExecutorService newSingleThreadExecutor(final String pattern, final String name, final boolean daemon) {
        return Executors.newSingleThreadExecutor(new WorkmanagerThreadFactory(pattern, name, daemon));
    }

    /**
     * Creates a new cached thread pool
     * 
     * @param pattern pattern of the thread name
     * @param name ${name} in the pattern name
     * @param daemon whether the threads is daemon or not
     * @return the created pool
     */
    public static ExecutorService newCachedThreadPool(final String pattern, final String name, final boolean daemon) {
        return Executors.newCachedThreadPool(new WorkmanagerThreadFactory(pattern, name, daemon));
    }

    /**
     * Creates a new custom thread pool using 60 seconds as keep alive and with
     * an unbounded queue.
     * 
     * @param pattern pattern of the thread name
     * @param name ${name} in the pattern name
     * @param corePoolSize the core size
     * @param maxPoolSize the maximum pool size
     * @return the created pool
     */
    public static ExecutorService newThreadPool(final String pattern, final String name, int corePoolSize, int maxPoolSize) {
        return WorkManagerExecutorServiceHelper.newThreadPool(pattern, name, corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS, -1, new ThreadPoolExecutor.CallerRunsPolicy(), true);
    }

    /**
     * Creates a new custom thread pool
     * 
     * @param pattern pattern of the thread name
     * @param name ${name} in the pattern name
     * @param corePoolSize the core size
     * @param maxPoolSize the maximum pool size
     * @param keepAliveTime keep alive time
     * @param timeUnit keep alive time unit
     * @param maxQueueSize the maximum number of tasks in the queue, use
     *            <tt>Integer.MAX_VALUE</tt> or <tt>-1</tt> to indicate
     *            unbounded
     * @param rejectedExecutionHandler the handler for tasks which cannot be
     *            executed by the thread pool. If <tt>null</tt> is provided then
     *            {@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy
     *            CallerRunsPolicy} is used.
     * @param daemon whether the threads is daemon or not
     * @return the created pool
     * @throws IllegalArgumentException if parameters is not valid
     */
    public static ExecutorService newThreadPool(final String pattern, final String name, int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit,
                                                int maxQueueSize, RejectedExecutionHandler rejectedExecutionHandler, final boolean daemon) {
        // validate max >= core
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("MaxPoolSize must be >= corePoolSize, was " + maxPoolSize + " >= " + corePoolSize);
        }

        BlockingQueue<Runnable> queue;
        if (corePoolSize == 0 && maxQueueSize <= 0) {
            // use a synchronous so we can act like the cached thread pool
            queue = new SynchronousQueue<Runnable>();
        } else if (maxQueueSize <= 0) {
            // unbounded task queue
            queue = new LinkedBlockingQueue<Runnable>();
        } else {
            // bounded task queue
            queue = new LinkedBlockingQueue<Runnable>(maxQueueSize);
        }
        ThreadPoolExecutor answer = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, queue);
        answer.setThreadFactory(new WorkmanagerThreadFactory(pattern, name, daemon));
        if (rejectedExecutionHandler == null) {
            rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        }
        answer.setRejectedExecutionHandler(rejectedExecutionHandler);
        return answer;
    }

    /**
     * Thread factory which creates threads supporting a naming pattern.
     */
    private static final class WorkmanagerThreadFactory implements ThreadFactory {

        private final String pattern;
        private final String name;
        private final boolean daemon;

        private WorkmanagerThreadFactory(String pattern, String name, boolean daemon) {
            this.pattern = pattern;
            this.name = name;
            this.daemon = daemon;
        }

        public Thread newThread(Runnable runnable) {
            String threadName = getThreadName(pattern, name);
            Thread answer;
            if (workmanager != null) {
                answer = new WorkManagerThreadWrapper(workmanager, new Thread(runnable, threadName));
            } else {
                answer = new Thread(runnable, threadName);
            }
            answer.setDaemon(daemon);
            LOG.trace("Created thread[{}]: {}", name, answer);
            return answer;
        }

        public String toString() {
            return "WorkmanagerThreadFactory[" + name + "]";
        }
    }

}

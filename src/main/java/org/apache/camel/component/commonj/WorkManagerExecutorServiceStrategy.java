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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultExecutorServiceStrategy;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commonj.work.WorkManager;

/**
 */
public class WorkManagerExecutorServiceStrategy extends DefaultExecutorServiceStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(WorkManagerExecutorServiceStrategy.class);

    public WorkManagerExecutorServiceStrategy(CamelContext context) {
        super(context);
    }

    public ExecutorService newCachedThreadPool(Object source, String name) {
        ExecutorService answer = WorkManagerExecutorServiceHelper.newCachedThreadPool(getThreadNamePattern(), name, true);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());
        LOG.debug("Created new cached thread pool for source: " + source + " with name: " + name + ". -> " + answer);
        return answer;
    }

    public ScheduledExecutorService newScheduledThreadPool(Object source, String name, int poolSize) {
        ScheduledExecutorService answer = WorkManagerExecutorServiceHelper.newScheduledThreadPool(poolSize, getThreadNamePattern(), name, true);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());
        LOG.debug("Created new scheduled thread pool for source: " + source + " with name: " + name + ". [poolSize=" + poolSize + "]. -> " + answer);
        return answer;
    }

    public ExecutorService newFixedThreadPool(Object source, String name, int poolSize) {
        ExecutorService answer = WorkManagerExecutorServiceHelper.newFixedThreadPool(poolSize, getThreadNamePattern(), name, true);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());
        LOG.debug("Created new fixed thread pool for source: " + source + " with name: " + name + ". [poolSize=" + poolSize + "]. -> " + answer);
        return answer;
    }

    public ExecutorService newSingleThreadExecutor(Object source, String name) {
        ExecutorService answer = WorkManagerExecutorServiceHelper.newSingleThreadExecutor(getThreadNamePattern(), name, true);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());

        LOG.debug("Created new single thread pool for source: " + source + " with name: " + name + ". -> " + answer);
        return answer;
    }

    public ExecutorService newThreadPool(Object source, String name, int corePoolSize, int maxPoolSize) {
        ExecutorService answer = WorkManagerExecutorServiceHelper.newThreadPool(getThreadNamePattern(), name, corePoolSize, maxPoolSize);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());

        LOG.debug("Created new thread pool for source: " + source + " with name: " + name + ". [poolSize=" + corePoolSize + ", maxPoolSize=" + maxPoolSize + "] -> " + answer);
        return answer;
    }

    public ExecutorService newThreadPool(Object source, String name, int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit, int maxQueueSize,
                                         RejectedExecutionHandler rejectedExecutionHandler, boolean daemon) {
        // the thread name must not be null
        ObjectHelper.notNull(name, "ThreadName");
        ExecutorService answer = WorkManagerExecutorServiceHelper.newThreadPool(getThreadNamePattern(), name, corePoolSize, maxPoolSize, keepAliveTime, timeUnit, maxQueueSize,
                                                                                rejectedExecutionHandler, daemon);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());

        LOG.debug("Created new thread pool for source: " + source + " with name: " + name + ". [poolSize=" + corePoolSize + ", maxPoolSize=" + maxPoolSize + ", keepAliveTime="
                  + keepAliveTime + " " + timeUnit + ", maxQueueSize=" + maxQueueSize + ", rejectedExecutionHandler=" + rejectedExecutionHandler + ", daemon=" + daemon + "] -> "
                  + answer);
        return answer;
    }

    public void setWorkmanager(WorkManager workmanager) {
        WorkManagerExecutorServiceHelper.setWorkmanager(workmanager);
    }

}

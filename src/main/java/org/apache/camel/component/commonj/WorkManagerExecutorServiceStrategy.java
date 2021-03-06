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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultExecutorServiceStrategy;
import org.apache.camel.impl.DefaultShutdownStrategy;
import org.apache.camel.model.OptionalIdentifiedDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ProcessorDefinitionHelper;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.concurrent.ExecutorServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commonj.work.WorkManager;

/**
 */
public class WorkManagerExecutorServiceStrategy extends DefaultExecutorServiceStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(WorkManagerExecutorServiceStrategy.class);

    // TODO Can be removed when camel-core
    // DefaultExecutorServiceStrategy.onThreadPoolCreated changes scope to
    // protected
    private final List<ExecutorService> executorServices = new ArrayList<ExecutorService>();
    private final CamelContext camelContext;

    public WorkManagerExecutorServiceStrategy(CamelContext context) {
        super(context);
        this.camelContext = context;
    }

    @Override
    public ExecutorService newCachedThreadPool(Object source, String name) {
        ExecutorService answer = WorkManagerExecutorServiceHelper.newCachedThreadPool(getThreadNamePattern(), name, true);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());
        LOG.debug("Created new cached thread pool for source: " + source + " with name: " + name + ". -> " + answer);
        return answer;
    }

    @Override
    public ScheduledExecutorService newScheduledThreadPool(Object source, String name, int poolSize) {
        ScheduledExecutorService answer = WorkManagerExecutorServiceHelper.newScheduledThreadPool(poolSize, getThreadNamePattern(), name, true);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());
        LOG.debug("Created new scheduled thread pool for source: " + source + " with name: " + name + ". [poolSize=" + poolSize + "]. -> " + answer);
        return answer;
    }

    @Override
    public ExecutorService newFixedThreadPool(Object source, String name, int poolSize) {
        ExecutorService answer = WorkManagerExecutorServiceHelper.newFixedThreadPool(poolSize, getThreadNamePattern(), name, true);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());
        LOG.debug("Created new fixed thread pool for source: " + source + " with name: " + name + ". [poolSize=" + poolSize + "]. -> " + answer);
        return answer;
    }

    @Override
    public ExecutorService newSingleThreadExecutor(Object source, String name) {
        ExecutorService answer;
        if (source instanceof DefaultShutdownStrategy) {
            answer = ExecutorServiceHelper.newSingleThreadExecutor(getThreadNamePattern(), name, true);
        } else {
            answer = WorkManagerExecutorServiceHelper.newSingleThreadExecutor(getThreadNamePattern(), name, true);
        }
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());

        LOG.debug("Created new single thread pool for source: " + source + " with name: " + name + ". -> " + answer);
        return answer;
    }

    @Override
    public ExecutorService newThreadPool(Object source, String name, int corePoolSize, int maxPoolSize) {
        ExecutorService answer = WorkManagerExecutorServiceHelper.newThreadPool(getThreadNamePattern(), name, corePoolSize, maxPoolSize);
        onThreadPoolCreated(answer, source, getDefaultThreadPoolProfile().getId());

        LOG.debug("Created new thread pool for source: " + source + " with name: " + name + ". [poolSize=" + corePoolSize + ", maxPoolSize=" + maxPoolSize + "] -> " + answer);
        return answer;
    }

    @Override
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

    // TODO Can be removed when camel-core
    // DefaultExecutorServiceStrategy.onThreadPoolCreated changes scope to
    // protected
    private void onThreadPoolCreated(ExecutorService executorService, Object source, String threadPoolProfileId) {
        // add to internal list of thread pools
        executorServices.add(executorService);

        String id;
        String sourceId = null;
        String routeId = null;

        // extract id from source
        if (source instanceof OptionalIdentifiedDefinition) {
            id = ((OptionalIdentifiedDefinition)source).idOrCreate(camelContext.getNodeIdFactory());
            // and let source be the short name of the pattern
            sourceId = ((OptionalIdentifiedDefinition)source).getShortName();
        } else if (source instanceof String) {
            id = (String)source;
        } else if (source != null) {
            // fallback and use the simple class name with hashcode for the id
            // so its unique for this given source
            id = source.getClass().getSimpleName() + "(" + ObjectHelper.getIdentityHashCode(source) + ")";
        } else {
            // no source, so fallback and use the simple class name from thread
            // pool and its hashcode identity so its unique
            id = executorService.getClass().getSimpleName() + "(" + ObjectHelper.getIdentityHashCode(executorService) + ")";
        }

        // id is mandatory
        ObjectHelper.notEmpty(id, "id for thread pool " + executorService);

        // extract route id if possible
        if (source instanceof ProcessorDefinition) {
            RouteDefinition route = ProcessorDefinitionHelper.getRoute((ProcessorDefinition)source);
            if (route != null) {
                routeId = route.idOrCreate(camelContext.getNodeIdFactory());
            }
        }

        // let lifecycle strategy be notified as well which can let it be
        // managed in JMX as well
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor)executorService;
            for (LifecycleStrategy lifecycle : camelContext.getLifecycleStrategies()) {
                lifecycle.onThreadPoolAdd(camelContext, threadPool, id, sourceId, routeId, threadPoolProfileId);
            }
        }

        // now call strategy to allow custom logic
        onNewExecutorService(executorService);
    }

    public void setWorkmanager(WorkManager workmanager) {
        WorkManagerExecutorServiceHelper.setWorkmanager(workmanager);
    }

}

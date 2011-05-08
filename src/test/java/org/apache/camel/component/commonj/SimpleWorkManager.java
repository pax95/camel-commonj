package org.apache.camel.component.commonj;

import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkListener;
import commonj.work.WorkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class SimpleWorkManager implements WorkManager {

    private SimpleWorkQueue workQueue = new SimpleWorkQueue();

    /*
     * public SimpleWorkManager(SimpleWorkQueue workQueue) { this.workQueue =
     * workQueue; }
     */
    public WorkItem schedule(Work work) throws IllegalArgumentException {
        return schedule(work, null);
    }

    public WorkItem schedule(Work work, WorkListener workListener) throws IllegalArgumentException {
        DefaultWorkItem workItem = new DefaultWorkItem(work, null);
        try {
            workQueue.put(workItem);
        } catch (InterruptedException e) {
            workItem.setStatus(WorkEvent.WORK_REJECTED, new WorkException(e));
            Thread.currentThread().interrupt();
        }
        return workItem;
    }

    /**
     * Blocks until all specified WorkItems completed, or until the specified
     * timeout.
     * 
     * @param workItems
     * @param timeout
     * @return true is all items completed within the specified timeout value,
     *         false otherwise
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public boolean waitForAll(Collection workItems, long timeout) throws InterruptedException, IllegalArgumentException {
        long start = System.currentTimeMillis();
        do {
            boolean isAllCompleted = false;
            Iterator<WorkItem> iterator = workItems.iterator();
            while (iterator.hasNext() && !isAllCompleted) {
                int status = iterator.next().getStatus();
                System.out.println("een status: " + status);
                isAllCompleted = (status == WorkEvent.WORK_COMPLETED) || (status == WorkEvent.WORK_REJECTED);
                System.out.println(isAllCompleted);
            }
            if (isAllCompleted) {
                return true;
            }
            if (timeout == IMMEDIATE) {
                return false;
            }
            if (timeout == INDEFINITE) {
                continue;
            }
        } while ((System.currentTimeMillis() - start) < timeout);
        return false;
    }

    /**
     * blocks until any of the specified WorkItems complete until the specified
     * timeout
     * 
     * @param workItems
     * @param timeout
     * @return Collection of completed WorkItems
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public Collection waitForAny(Collection workItems, long timeout) throws InterruptedException, IllegalArgumentException {
        long start = System.currentTimeMillis();
        do {
            synchronized (this) {
                Collection<WorkItem> completed = new ArrayList<WorkItem>();
                Iterator<WorkItem> iterator = workItems.iterator();
                while (iterator.hasNext()) {
                    WorkItem workItem = iterator.next();
                    if (workItem.getStatus() == WorkEvent.WORK_COMPLETED || workItem.getStatus() == WorkEvent.WORK_REJECTED) {
                        completed.add(workItem);
                    }
                }
                if (!completed.isEmpty()) {
                    return completed;
                }
            }
            if (timeout == IMMEDIATE) {
                return Collections.EMPTY_LIST;
            }
            if (timeout == INDEFINITE) {
                continue;
            }
        } while ((System.currentTimeMillis() - start) < timeout);
        return Collections.EMPTY_LIST;
    }

}

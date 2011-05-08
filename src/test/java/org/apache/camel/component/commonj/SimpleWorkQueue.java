package org.apache.camel.component.commonj;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import commonj.work.WorkException;

public class SimpleWorkQueue {

    private BlockingQueue<DefaultWorkItem> workQueue = new LinkedBlockingQueue<DefaultWorkItem>();

    public SimpleWorkQueue() {
    }

    public boolean isEmpty() {
        return workQueue.isEmpty();
    }

    public boolean remove(Object object) {
        return workQueue.remove(object);
    }

    public void put(DefaultWorkItem workItem) throws InterruptedException {
        workQueue.put(workItem);
    }

    public DefaultWorkItem peek() {
        return workQueue.peek();
    }

    public DefaultWorkItem take() throws WorkException {
        try {
            return workQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkException(e);
        }
    }
}

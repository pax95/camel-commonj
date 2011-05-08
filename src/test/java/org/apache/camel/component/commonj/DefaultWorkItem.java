package org.apache.camel.component.commonj;

import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkListener;

public class DefaultWorkItem implements WorkItem {

    private volatile int status;
    private Work work;
    private WorkListener workListener;

    public DefaultWorkItem(Work work, WorkListener workListener) {
        this.work = work;
        this.workListener = workListener;
        this.status = WorkEvent.WORK_ACCEPTED;
    }

    public Work getResult() {
        return work;
    }

    public int getStatus() {
        return status;
    }

    public synchronized void setStatus(int status, WorkException exception) {
        this.status = status;
        if (this.workListener != null) {
            switch (this.status) {
            case WorkEvent.WORK_ACCEPTED:
                this.workListener.workAccepted(new DefaultWorkEvent(WorkEvent.WORK_ACCEPTED, this, exception));
                break;
            case WorkEvent.WORK_REJECTED:
                this.workListener.workRejected(new DefaultWorkEvent(WorkEvent.WORK_REJECTED, this, exception));
                break;
            case WorkEvent.WORK_STARTED:
                this.workListener.workStarted(new DefaultWorkEvent(WorkEvent.WORK_STARTED, this, exception));
                break;
            case WorkEvent.WORK_COMPLETED:
                this.workListener.workCompleted(new DefaultWorkEvent(WorkEvent.WORK_COMPLETED, this, exception));
                break;
            }
        }
    }

    public int compareTo(Object object) {
        Work theWork = ((WorkItem)object).getResult();
        if (this.work instanceof Comparable) {
            Comparable<Comparable> comparableWork1 = (Comparable<Comparable>)this.work;
            if (theWork instanceof Comparable) {
                Comparable comparableWork2 = (Comparable)work;
                return comparableWork1.compareTo(comparableWork2);
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        String temp = null;
        switch (this.status) {
        case WorkEvent.WORK_ACCEPTED:
            temp = "WORK_ACCEPTED";
            break;
        case WorkEvent.WORK_COMPLETED:
            temp = "WORK_COMPLETED";
            break;
        case WorkEvent.WORK_REJECTED:
            temp = "WORK_REJECTED";
            break;
        case WorkEvent.WORK_STARTED:
            temp = "WORK_STARTED";
            break;
        default:
            throw new IllegalArgumentException("unknown status " + this.status);
        }
        return this.work.toString() + ":" + temp;
    }
}

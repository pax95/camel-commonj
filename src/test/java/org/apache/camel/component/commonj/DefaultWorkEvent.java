package org.apache.camel.component.commonj;

import commonj.work.WorkEvent;
import commonj.work.WorkException;
import commonj.work.WorkItem;

public class DefaultWorkEvent implements WorkEvent {

    private final int m_type;

    private final WorkItem m_workItem;

    private final WorkException m_exception;

    public DefaultWorkEvent(final int type, final WorkItem item, final WorkException exception) {
        m_type = type;
        m_workItem = item;
        m_exception = exception;
    }

    public int getType() {
        return m_type;
    }

    public WorkItem getWorkItem() {
        return m_workItem;
    }

    public WorkException getException() {
        return m_exception;
    }
}

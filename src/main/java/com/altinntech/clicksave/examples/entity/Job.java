package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.interfaces.EnumId;

/**
 * The enum Job.
 */
public enum Job implements EnumId {

    /**
     * Three d artist job.
     */
    THREE_D_ARTIST(1L),
    /**
     * Programmer job.
     */
    PROGRAMMER(2L),
    /**
     * Hr job.
     */
    HR(3L),
    /**
     * Qa job.
     */
    QA(4L),
    ;

    private final Long id;

    Job(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}

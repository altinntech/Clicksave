package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.interfaces.EnumId;

public enum Job implements EnumId {

    THREE_D_ARTIST(1L),
    PROGRAMMER(2L),
    HR(3L),
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

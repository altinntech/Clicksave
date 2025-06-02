package com.altinntech.clicksave.interfaces;

import com.altinntech.clicksave.enums.IDTypes;

public interface IdGenerator<ID> {

    Class<ID> getIdType();
    boolean isPrevIdAware();
    ID generateNextId(ID lastId);
    ID generateNextId();
}

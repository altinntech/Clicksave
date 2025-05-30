package com.altinntech.clicksave.interfaces;

public abstract class NetworkBehavior {

    protected final String netId;

    protected NetworkBehavior(String netId) {
        this.netId = netId;
    }
}

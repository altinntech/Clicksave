package com.altinntech.clicksave.core.utils.rpc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.rmi.Remote;

@Getter
@EqualsAndHashCode
public class RemoteServiceData<T extends Remote> {

    public final T connectedService;
    @Setter
    private RemoteServiceStatus serviceStatus;
    private final String host;
    private final int port;
    @Setter
    private String netId;

    public RemoteServiceData(T connectedService, String host, int port) {
        this.connectedService = connectedService;
        this.host = host;
        this.port = port;
    }
}

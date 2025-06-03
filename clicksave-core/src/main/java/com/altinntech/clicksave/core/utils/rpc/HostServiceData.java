package com.altinntech.clicksave.core.utils.rpc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.rmi.Remote;

@Getter
@EqualsAndHashCode
public class HostServiceData<T extends Remote> {

    public final T hostedService;
    @Setter
    private HostedServiceStatus serviceStatus = HostedServiceStatus.OFFLINE;

    public HostServiceData(T hostedService) {
        this.hostedService = hostedService;
    }
}

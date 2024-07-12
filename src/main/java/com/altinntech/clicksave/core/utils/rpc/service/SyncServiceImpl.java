package com.altinntech.clicksave.core.utils.rpc.service;

import com.altinntech.clicksave.core.BatchCollector;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.utils.rpc.RemoteServiceData;
import com.altinntech.clicksave.core.utils.rpc.RemoteServiceStatus;
import com.altinntech.clicksave.log.CSLogger;
import lombok.SneakyThrows;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;

public class SyncServiceImpl extends UnicastRemoteObject implements SyncService {

    private String netId;
    private final List<RemoteServiceData<SyncService>> remoteServices;
    private final BatchCollector batchCollector;

    public SyncServiceImpl(String netId, List<RemoteServiceData<SyncService>> remoteServices, BatchCollector batchCollector) throws RemoteException {
        super();
        this.netId = netId;
        this.remoteServices = remoteServices;
        this.batchCollector = batchCollector;
    }

    @Override
    public String connectionNotification(String netId) throws RemoteException {
        CSLogger.info("SyncManager", "Remote clicksave service " + netId + " connected");
        return this.netId;
    }

    @Override
    public String disconnectionNotification(String netId) throws RemoteException {
        CSLogger.info("SyncManager", netId + " disconnected");
        remoteServices.stream()
                .filter(remoteServiceData -> Objects.equals(remoteServiceData.getNetId(), this.netId))
                .forEach(remoteService -> remoteService.setServiceStatus(RemoteServiceStatus.DISCONNECTED));
        return this.netId;
    }

    @Override
    public boolean healthCheck(String netId) throws RemoteException {
        CSLogger.debug("SyncServiceImpl", "Health check request from: " + netId);
        return true;
    }

    @SneakyThrows
    @Override
    public void saveBatchRequest(String netId) throws RemoteException {
        CSLogger.debug("SyncServiceImpl", "Saving batch request from: " + netId);
        batchCollector.saveAndFlushAll();
    }

    @SneakyThrows
    @Override
    public void saveBatchRequest(ClassDataCache classDataCache, String netId) throws RemoteException {
        CSLogger.debug("SyncServiceImpl", "Saving batch request from: " + netId);
        batchCollector.saveAndFlush(classDataCache);
    }
}

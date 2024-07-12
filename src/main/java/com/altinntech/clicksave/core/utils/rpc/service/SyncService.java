package com.altinntech.clicksave.core.utils.rpc.service;

import com.altinntech.clicksave.core.dto.ClassDataCache;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SyncService extends Remote {

    String connectionNotification(String netId) throws RemoteException;
    String disconnectionNotification(String netId) throws RemoteException;
    boolean healthCheck(String netId) throws RemoteException;
    void saveBatchRequest(String netId) throws RemoteException;
    void saveBatchRequest(ClassDataCache classDataCache, String netId) throws RemoteException;
}

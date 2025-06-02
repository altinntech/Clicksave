package com.altinntech.clicksave.core;

import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.core.utils.DefaultProperties;
import com.altinntech.clicksave.core.utils.rpc.HostServiceData;
import com.altinntech.clicksave.core.utils.rpc.HostedServiceStatus;
import com.altinntech.clicksave.core.utils.rpc.RemoteServiceData;
import com.altinntech.clicksave.core.utils.rpc.RemoteServiceStatus;
import com.altinntech.clicksave.core.utils.rpc.service.SyncService;
import com.altinntech.clicksave.core.utils.rpc.service.SyncServiceImpl;
import com.altinntech.clicksave.exceptions.ServiceHostingException;
import com.altinntech.clicksave.interfaces.Disposable;
import com.altinntech.clicksave.interfaces.NetworkBehavior;
import lombok.SneakyThrows;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.altinntech.clicksave.core.utils.rpc.RMIUtils.*;
import static com.altinntech.clicksave.log.CSLogger.info;

public class SyncManager extends NetworkBehavior implements Disposable {

    private final DefaultProperties properties;
    private final HostServiceData<SyncServiceImpl> sharedSyncService;
    private final List<RemoteServiceData<SyncService>> remoteServices = new ArrayList<>();
    private final int port;
    private final BatchCollector batchCollector;

    private SyncManager(DefaultProperties properties, BatchCollector batchCollector) {
        super(UUID.randomUUID().toString());
        this.batchCollector = batchCollector;
        try {
            this.properties = properties;
            this.port = Integer.parseInt(properties.getSyncHostPort().isEmpty() ? "-1" : properties.getSyncHostPort());
            this.sharedSyncService = shareSyncService();
            connectToRemoteServices();
        } catch (ServiceHostingException | RemoteException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Offline mode
    private SyncManager(BatchCollector batchCollector) {
        super("");
        this.properties = null;
        this.sharedSyncService = new HostServiceData<>(null);
        this.batchCollector = batchCollector;
        this.port = -1;
    }

    public static SyncManager create(DefaultProperties properties, BatchCollector batchCollector) {
        if (!Boolean.parseBoolean(properties.getUseSyncFeatures())) {
            return new SyncManager(batchCollector);
        }
        return new SyncManager(properties, batchCollector);
    }

    private HostServiceData<SyncServiceImpl> shareSyncService() throws RemoteException, ServiceHostingException {
        SyncServiceImpl syncService = new SyncServiceImpl(this.netId, remoteServices, batchCollector);
        HostServiceData<SyncServiceImpl> hostServiceData = new HostServiceData<>(syncService);
        if (properties.getSyncHostPort() == null || properties.getSyncHostPort().isEmpty()) {
            info("Sync manager host status: offline");
            hostServiceData.setServiceStatus(HostedServiceStatus.OFFLINE);
        } else {
            hostService(syncService, port);
            info("Sync manager host status: online on port: " + port + " netId: " + this.netId);
            hostServiceData.setServiceStatus(HostedServiceStatus.ONLINE);
        }
        return hostServiceData;
    }

    private void connectToRemoteServices() throws InterruptedException, RemoteException {
        String hosts = properties.getSyncRemoteHosts();
        if (hosts == null || hosts.isEmpty()) {
            info("SyncManager", "Not found declared remote hosts");
            return;
        }
        info("SyncManager", "Connecting to remote clicksave services...");
        String[] splitHosts = hosts.split(",");
        for (String host : splitHosts) {
            String[] hostAndPort = host.split(":");
            SyncService connectedService = connectToService(SyncService.class, hostAndPort[0], Integer.parseInt(hostAndPort[1]),
                    Long.parseLong(properties.getSyncConnectionRetryTimeout()));
            RemoteServiceData<SyncService> remoteServiceData = new RemoteServiceData<>(
                    connectedService,
                    hostAndPort[0],
                    Integer.parseInt(hostAndPort[1])
            );
            String remoteNetId = connectedService.connectionNotification(this.netId);
            remoteServiceData.setNetId(remoteNetId);
            remoteServiceData.setServiceStatus(RemoteServiceStatus.CONNECTED);
            remoteServices.add(remoteServiceData);
            info("SyncManager", "Connected to " + remoteNetId + " on: " + hostAndPort[0] + ":" + hostAndPort[1]);
        }
    }

    @SneakyThrows
    public void saveBatchRequest() {
        for (RemoteServiceData<SyncService> service : remoteServices) {
            if (service.getServiceStatus() == RemoteServiceStatus.CONNECTED) {
                if (healthCheck(service)) {
                    service.connectedService.saveBatchRequest(this.netId);
                }
            }
        }
    }

    private boolean healthCheck(RemoteServiceData<SyncService> service) {
        try {
            service.connectedService.healthCheck(this.netId);
            return true;
        } catch (RemoteException e) {
            service.setServiceStatus(RemoteServiceStatus.DISCONNECTED);
            info("SyncManager", "Disconnected from " + service.getNetId());
            return false;
        }
    }

    @Override
    public void dispose() {
        remoteServices.clear();
    }

    @SneakyThrows
    public synchronized void shutdown() {
        disconnectFromAll();
        stopHost();
    }

    @SneakyThrows
    private void disconnectFromAll() {
        info("SyncManager", "Disconnect from all remotes services");
        for (RemoteServiceData<SyncService> service : remoteServices) {
            if (service.getServiceStatus() == RemoteServiceStatus.CONNECTED) {
                if (healthCheck(service)) {
                    service.getConnectedService().disconnectionNotification(this.netId);
                    info("SyncManager", "Disconnected from " + service.getNetId());
                    service.setServiceStatus(RemoteServiceStatus.DISCONNECTED);
                }
            }
        }
    }

    @SneakyThrows
    private void stopHost() {
        info("SyncManager", "Stopping host");
        if (sharedSyncService.getServiceStatus() == HostedServiceStatus.ONLINE) {
            unhostService(sharedSyncService.hostedService, port);
            sharedSyncService.setServiceStatus(HostedServiceStatus.OFFLINE);
        }
    }
}

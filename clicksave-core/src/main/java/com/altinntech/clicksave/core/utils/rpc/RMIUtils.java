package com.altinntech.clicksave.core.utils.rpc;

import com.altinntech.clicksave.exceptions.ServiceHostingException;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static com.altinntech.clicksave.log.CSLogger.*;

public class RMIUtils {

    public static <T extends Remote> void hostService(T service, int port) throws ServiceHostingException {
        try {
            Class<T> markerInterface = (Class<T>) service.getClass().getInterfaces()[0];
            LocateRegistry.createRegistry(port).rebind(markerInterface.getSimpleName(), service);
            debug("RPC", "Hosting service " + service.getClass().getSimpleName() + " STARTED on port " + port);
        } catch (RemoteException e) {
            error("Error while hosting service " + service.getClass().getSimpleName() + ". Error message: " + e.getMessage(), RMIUtils.class);
            throw new ServiceHostingException();
        }
    }

    public static <T extends Remote> void unhostService(T service, int port) throws ServiceHostingException {
        try {
            Class<T> markerInterface = (Class<T>) service.getClass().getInterfaces()[0];
            Registry registry = LocateRegistry.getRegistry(port);
            registry.unbind(markerInterface.getSimpleName());
            debug("RPC", "Hosting service " + service.getClass().getSimpleName() + " STOPPED on port " + port);
        } catch (RemoteException | NotBoundException e) {
            error("Error while unstinting service " + service.getClass().getSimpleName() + ". Error message: " + e.getMessage(), RMIUtils.class);
            throw new ServiceHostingException();
        }
    }

    public static <T extends Remote> T connectToService(Class<T> serviceInterface, String host, int port, long timeout) throws InterruptedException {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            debug("RPC", "Connecting to " + serviceInterface.getSimpleName() + " on: " + host + ":" + port);
            T remoteServiceProxy = (T) registry.lookup(serviceInterface.getSimpleName());
            debug("RPC", "Successfully connected to " + serviceInterface.getSimpleName() + " on: " + host + ":" + port);
            return remoteServiceProxy;
        } catch (RemoteException | NotBoundException e) {
            debug("RPC", "Retry connection to: " + host + ":" + port);
            Thread.sleep(500);
            return connectToService(serviceInterface, host, port, timeout);
        }
    }
}

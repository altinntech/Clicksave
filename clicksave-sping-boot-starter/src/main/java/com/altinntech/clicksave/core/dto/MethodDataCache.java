package com.altinntech.clicksave.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodDataCache {

    Set<Method> prePersistedMethods = new HashSet<>();
    Set<Method> preUpdatedMethods = new HashSet<>();
    Set<Method> postLoadedMethods = new HashSet<>();

    public void addPrePersistedMethod(Method method) {
        this.prePersistedMethods.add(method);
    }

    public void addPreUpdatedMethod(Method method) {
        this.preUpdatedMethods.add(method);
    }

    public void addPostLoadedMethod(Method method) {
        this.postLoadedMethods.add(method);
    }
}

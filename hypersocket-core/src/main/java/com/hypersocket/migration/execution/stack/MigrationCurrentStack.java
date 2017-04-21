package com.hypersocket.migration.execution.stack;

import com.hypersocket.realm.Realm;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
public class MigrationCurrentStack {

    ThreadLocal<Stack<MigrationCurrentInfo>> currentState = new ThreadLocal<>();
    ThreadLocal<Realm> currentRealm = new ThreadLocal<>();

    public synchronized void addState(MigrationCurrentInfo migrationCurrentInfo) {
        Stack<MigrationCurrentInfo> infoStack = currentState.get();
        if(infoStack == null) {
            infoStack = new Stack<>();
            currentState.set(infoStack);
        }

        infoStack.push(migrationCurrentInfo);
    }

    public synchronized void addRealm(Realm realm) {
        currentRealm.set(realm);
    }

    public Realm getCurrentRealm() {
        if(currentRealm.get() == null) {
            throw new IllegalStateException("State stack is not initialized with realm, please add realm.");
        }
        return currentRealm.get();
    }

    public MigrationCurrentInfo getState() {
        assertion();
        return currentState.get().peek();
    }

    public MigrationCurrentInfo popState() {
        assertion();
        return currentState.get().pop();
    }

    public void clearState() {
        if(currentState != null) {
            currentState.remove();
        }

        if(currentRealm != null) {
            currentRealm.remove();
        }
    }

    private void assertion() {
        if(currentState == null || currentState.get() == null) {
            throw new IllegalStateException("State stack is not initialized, please add some state.");
        }
    }
}

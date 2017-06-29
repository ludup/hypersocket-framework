package com.hypersocket.migration.execution.stack;

import java.util.Stack;

import org.springframework.stereotype.Component;

@Component
public class MigrationCurrentStack {

    ThreadLocal<Stack<MigrationCurrentInfo>> currentState = new ThreadLocal<>();

    public void addState(MigrationCurrentInfo migrationCurrentInfo) {
        Stack<MigrationCurrentInfo> infoStack = currentState.get();
        if(infoStack == null) {
            infoStack = new Stack<>();
            currentState.set(infoStack);
        }

        infoStack.push(migrationCurrentInfo);
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
    }

    private void assertion() {
        if(currentState == null || currentState.get() == null) {
            throw new IllegalStateException("State stack is not initialized, please add some state.");
        }
    }
}

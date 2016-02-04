package org.march.sync.replica;

/**
 * Created by dli on 28.01.2016.
 */
public enum ReplicaState {

    INACTIVE,
    ACTIVATING,
    ACTIVE,
    DEACTIVATING,
    DEACTIVATED;

    public static boolean isSynchronizing(ReplicaState state){
        return ACTIVE.equals(state) || DEACTIVATING.equals(state);
    }
}

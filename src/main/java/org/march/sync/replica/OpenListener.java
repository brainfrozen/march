package org.march.sync.replica;

/**
 * Created by dli on 31.01.2016.
 */
public interface OpenListener extends Listener {
    void opened(Replica replica);
}

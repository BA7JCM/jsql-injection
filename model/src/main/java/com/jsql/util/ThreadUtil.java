/*******************************************************************************
 * Copyhacked (H) 2012-2025.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.util;

import com.jsql.model.InjectionModel;
import com.jsql.model.bean.database.AbstractElementDatabase;
import com.jsql.model.suspendable.AbstractSuspendable;
import com.jsql.model.suspendable.callable.ThreadFactoryCallable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Utility class managing running threads on which the user can act.
 * It must be noted that as SwingWorker class are used then only 10 jobs can be run
 * at the same time, the 11th will be waiting in the thread pool until one of the 10
 * actives one is freed.
 */
public final class ThreadUtil {
    
    private static final Logger LOGGER = LogManager.getRootLogger();
    
    /**
     * List of running jobs associated to a database injection task.
     * We can interact with those tasks in order to pause/resume and stop the process.
     */
    // Fix #8258: ConcurrentModificationException on java.util.HashMap$ValueIterator.next()
    private final Map<AbstractElementDatabase, AbstractSuspendable> suspendables = new ConcurrentHashMap<>(new HashMap<>());

    private final InjectionModel injectionModel;
    
    public ThreadUtil(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }

    /**
     * Add a job to the list of ongoing tasks. It is used to allow the user to act
     * on the job and stop/pause a running process.
     * @param elementDatabase component associated to the active job
     * @param suspendable active job to act on
     */
    public void put(AbstractElementDatabase elementDatabase, AbstractSuspendable suspendable) {
        this.suspendables.put(elementDatabase, suspendable);
    }
    
    /**
     * Get the task associated to the database component.
     * It's usually done to act on the task like stop/pause the corresponding process, or
     * to check the status of the job.
     * @param elementDatabase component associated to the active job
     * @return job currently running
     */
    public AbstractSuspendable get(AbstractElementDatabase elementDatabase) {
        return this.suspendables.get(elementDatabase);
    }
    
    /**
     * Remove the thread corresponding to the component in order to be
     * garbage collected. The thread should be stopped prior the deletion.
     * @param elementDatabase component associated to thread
     */
    public void remove(AbstractElementDatabase elementDatabase) {
        this.suspendables.remove(elementDatabase);
    }
    
    /**
     * Force to stop every threads still running and empty the list where
     * they were instantiated in order to be garbage collected.
     */
    public void reset() {
        this.suspendables.values().forEach(AbstractSuspendable::stop);
        this.suspendables.clear();
    }
    
    public ExecutorService getExecutor(String nameThread) {
        ExecutorService taskExecutor;
        
        if (this.injectionModel.getMediatorUtils().getPreferencesUtil().isLimitingThreads()) {
            int countThreads = this.injectionModel.getMediatorUtils().getPreferencesUtil().countLimitingThreads();
            taskExecutor = Executors.newFixedThreadPool(countThreads, new ThreadFactoryCallable(nameThread));
        } else {
            taskExecutor = Executors.newCachedThreadPool(new ThreadFactoryCallable(nameThread));
        }
        return taskExecutor;
    }

    public void shutdown(ExecutorService taskExecutor) {
        int timeout = 15;
        if (this.injectionModel.getMediatorUtils().getPreferencesUtil().isConnectionTimeout()) {
            timeout = this.injectionModel.getMediatorUtils().getPreferencesUtil().countConnectionTimeout();
        }

        try {
            taskExecutor.shutdown();
            if (!taskExecutor.awaitTermination(timeout, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.log(LogLevelUtil.IGNORE, e, e);
            Thread.currentThread().interrupt();
        }
    }
}

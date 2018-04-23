/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.java;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * {@link SequentialCompletionService} - Uses a single thread to execute submitted tasks sequentially.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SequentialCompletionService<V> implements CompletionService<V>, Closeable {

    static final FutureTask POISON = new EmptyFutureTask<Object>();

    private static class EmptyCallable<V> implements Callable<V> {

        EmptyCallable() {
            super();
        }

        @Override
        public V call() throws Exception {
            return null;
        }
    }

    private static class EmptyFutureTask<V> extends FutureTask<V> {

        EmptyFutureTask() {
            super(new EmptyCallable<V>());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------

    private static class ExecuterCallable<V> implements Callable<Void> {

        private final BlockingQueue<FutureTask<V>> submittedTasks;
        private final BlockingQueue<Future<V>> requestTaskQueue;

        ExecuterCallable(BlockingQueue<FutureTask<V>> submittedTasks, BlockingQueue<Future<V>> requestTaskQueue) {
            super();
            this.submittedTasks = submittedTasks;
            this.requestTaskQueue = requestTaskQueue;
        }

        void cancel() {
            submittedTasks.add(POISON);
        }

        @Override
        public Void call() throws Exception {
            boolean keepOn = true;
            List<FutureTask<V>> tasks = new ArrayList<FutureTask<V>>();

            while (keepOn) {
                FutureTask<V> first = submittedTasks.take();
                if (POISON == first) {
                    return null;
                }
                // Await its completion
                execute(first);

                tasks.clear();
                submittedTasks.drainTo(tasks);

                for (FutureTask<V> c : tasks) {
                    if (POISON == c) {
                        return null;
                    }
                    // Await its completion
                    execute(c);
                }
            }

            return null;
        }

        private void execute(FutureTask<V> task) {
            task.run();
            requestTaskQueue.offer(task);
        }
    } // End of ConsumerCallable class

    // -----------------------------------------------------------------------------------------------------------------------

    private final BlockingQueue<FutureTask<V>> submittedTasks;
    private final BlockingQueue<Future<V>> requestTaskQueue;
    private final ExecuterCallable<V> consumer;

    /**
     * Initializes a new {@link SequentialCompletionService}.
     *
     * @throws RejectedExecutionException If there is no vacant thread in thread pool
     */
    public SequentialCompletionService(ExecutorService executor) {
        super();
        if (executor == null) {
            throw new NullPointerException();
        }
        BlockingQueue<FutureTask<V>> submittedTasks = new LinkedBlockingQueue<FutureTask<V>>();
        BlockingQueue<Future<V>> requestTaskQueue = new LinkedBlockingQueue<Future<V>>();
        ExecuterCallable<V> consumer = new ExecuterCallable<V>(submittedTasks, requestTaskQueue);
        executor.submit(consumer);
        this.consumer = consumer;
        this.requestTaskQueue = requestTaskQueue;
        this.submittedTasks = submittedTasks;
    }

    /**
     * Shuts down.
     */
    public void shutDown() {
        consumer.cancel();
    }

    @Override
    public Future<V> submit(Callable<V> task) {
        FutureTask<V> ft = new FutureTask<V>(task);
        submittedTasks.offer(ft);
        return ft;
    }

    @Override
    public Future<V> submit(Runnable task, V result) {
        FutureTask<V> ft = new FutureTask<V>(task, result);
        submittedTasks.offer(ft);
        return ft;
    }

    @Override
    public Future<V> take() throws InterruptedException {
        return requestTaskQueue.take();
    }

    @Override
    public Future<V> poll() {
        return requestTaskQueue.poll();
    }

    @Override
    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return requestTaskQueue.poll(timeout, unit);
    }

    @Override
    public void close() {
        shutDown();
    }

}

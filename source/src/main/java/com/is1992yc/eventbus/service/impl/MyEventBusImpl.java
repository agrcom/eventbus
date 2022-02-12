package com.is1992yc.eventbus.service.impl;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.is1992yc.eventbus.events.BaseEvent;
import com.is1992yc.eventbus.service.MyEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class MyEventBusImpl implements MyEventBus {
    private static final Logger log = LoggerFactory.getLogger(MyEventBusImpl.class);


    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("sub-event-pool-%d").build();
    private final ExecutorService executorService = new ThreadPoolExecutor(8, 20, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(1024), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());


    private final EventBus syncBus = new EventBus(MyEventBus.class.getSimpleName());

    private EventBus asyncBus;


    public MyEventBusImpl() {
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("event-pool-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(4, 16, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(1024), tf, new ThreadPoolExecutor.CallerRunsPolicy());
        asyncBus = new AsyncEventBus(pool);
    }


    public MyEventBusImpl(int coreSize, int maxSize, int queue) {
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("event-pool-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(coreSize, maxSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(queue), tf, new ThreadPoolExecutor.CallerRunsPolicy());
        asyncBus = new AsyncEventBus(pool);
    }

    public MyEventBusImpl(int coreSize, int maxSize, int queue, ExecutorService executorService) {
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("event-pool-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(coreSize, maxSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(queue), tf, new ThreadPoolExecutor.CallerRunsPolicy());
        asyncBus = new AsyncEventBus(pool);

    }



    @Override
    public void postSyc(BaseEvent event) {
        syncBus.post(event);
    }

    @Override
    public void postAsync(BaseEvent event) {
        if (event.getLeadTime() > 0L) {
            executorService.execute(() -> {
                try {
                    Thread.sleep(event.getLeadTime());
                    log.info("I will process it! {}", Thread.currentThread().getName());
                    asyncBus.post(event);
                } catch (InterruptedException e) {
                    log.error("Event delay " + e.getMessage());
                }
            });
        } else {
            asyncBus.post(event);
        }
    }

    @Override
    public void registerSyc(Object listener) {
        syncBus.register(listener);
    }

    @Override
    public void registerAsync(Object listener) {
        asyncBus.register(listener);
    }
}

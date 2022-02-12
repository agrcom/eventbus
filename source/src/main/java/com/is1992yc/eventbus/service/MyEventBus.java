package com.is1992yc.eventbus.service;


import com.is1992yc.eventbus.events.BaseEvent;

public interface MyEventBus {

    void postSyc(BaseEvent event);

    void postAsync(BaseEvent event);

    void registerSyc(Object listener);

    void registerAsync(Object listener);

}

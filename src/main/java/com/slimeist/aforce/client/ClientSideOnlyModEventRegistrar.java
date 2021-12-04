package com.slimeist.aforce.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ClientSideOnlyModEventRegistrar {
    private final IEventBus eventBus;

    public ClientSideOnlyModEventRegistrar(IEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void registerClientOnlyEvents() {
        eventBus.register(StartupClient.class);
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
    }
}

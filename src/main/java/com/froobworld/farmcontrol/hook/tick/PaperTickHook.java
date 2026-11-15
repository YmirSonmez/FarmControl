package com.froobworld.farmcontrol.hook.tick;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.froobworld.farmcontrol.FarmControl;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.function.LongConsumer;

public class PaperTickHook implements TickHook, Listener {
    private static final double NANOS_PER_MILLI = 1_000_000.0;
    private static final LongConsumer[] NO_CONSUMERS = new LongConsumer[0];
    private LongConsumer[] tickConsumers = NO_CONSUMERS; // copy-on-write, only touched on the main thread
    private final ServerTickManager tickManager = Bukkit.getServerTickManager();

    @Override
    public void register(FarmControl farmControl) {
        Bukkit.getPluginManager().registerEvents(this, farmControl);
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        if (tickConsumers.length == 0 || !tickManager.isRunningNormally()) {
            return; // ticks while frozen, stepping or sprinting say nothing about the server's performance
        }
        long duration = (long) (event.getTickDuration() * NANOS_PER_MILLI);
        for (LongConsumer consumer : tickConsumers) {
            consumer.accept(duration);
        }
    }

    @Override
    public boolean addTickConsumer(LongConsumer consumer) {
        for (LongConsumer existing : tickConsumers) {
            if (existing == consumer) {
                return false;
            }
        }
        LongConsumer[] newConsumers = new LongConsumer[tickConsumers.length + 1];
        System.arraycopy(tickConsumers, 0, newConsumers, 0, tickConsumers.length);
        newConsumers[tickConsumers.length] = consumer;
        tickConsumers = newConsumers;
        return true;
    }

    @Override
    public boolean removeTickConsumer(LongConsumer consumer) {
        for (int i = 0; i < tickConsumers.length; i++) {
            if (tickConsumers[i] == consumer) {
                LongConsumer[] newConsumers = new LongConsumer[tickConsumers.length - 1];
                System.arraycopy(tickConsumers, 0, newConsumers, 0, i);
                System.arraycopy(tickConsumers, i + 1, newConsumers, i, newConsumers.length - i);
                tickConsumers = newConsumers;
                return true;
            }
        }
        return false;
    }

}

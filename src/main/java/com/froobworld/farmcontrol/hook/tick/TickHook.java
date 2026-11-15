package com.froobworld.farmcontrol.hook.tick;

import com.froobworld.farmcontrol.FarmControl;

import java.util.function.LongConsumer;

public interface TickHook {

    void register(FarmControl farmControl);

    /**
     * @param consumer Accepts the duration of each server tick, in nanoseconds.
     */
    boolean addTickConsumer(LongConsumer consumer);

    boolean removeTickConsumer(LongConsumer consumer);

}

package com.froobworld.farmcontrol.utils;

import com.froobworld.farmcontrol.hook.tick.TickHook;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

/**
 * Tracks the median tick duration over the most recent ticks.
 */
public class MsptTracker {
    private static final double NANOS_PER_MILLI = TimeUnit.MILLISECONDS.toNanos(1);
    private static final double DEFAULT_MSPT = 25.0;
    private final LongConsumer tickConsumer = this::onTickEnd;
    private final TickHook tickHook;
    private final long[] tickDurations; // ring buffer
    private int size;
    private int nextIndex;
    private double cachedMspt = DEFAULT_MSPT;
    private boolean dirty;

    public MsptTracker(int collectionPeriod, TickHook tickHook) {
        this.tickHook = tickHook;
        tickDurations = new long[collectionPeriod <= 0 ? 1200 : collectionPeriod];
    }

    public void register() {
        tickHook.addTickConsumer(tickConsumer);
    }

    public void unregister() {
        tickHook.removeTickConsumer(tickConsumer);
        size = 0;
        nextIndex = 0;
        cachedMspt = DEFAULT_MSPT;
        dirty = false;
    }

    public double getMspt() {
        if (dirty) {
            long[] sorted = Arrays.copyOf(tickDurations, size);
            Arrays.sort(sorted);
            int middle = size / 2;
            if (size % 2 == 0) {
                cachedMspt = 0.5 * (sorted[middle] + sorted[middle - 1]) / NANOS_PER_MILLI;
            } else {
                cachedMspt = sorted[middle] / NANOS_PER_MILLI;
            }
            dirty = false;
        }
        return cachedMspt;
    }

    private void onTickEnd(long tickDuration) {
        tickDurations[nextIndex] = tickDuration;
        if (++nextIndex == tickDurations.length) {
            nextIndex = 0;
        }
        if (size < tickDurations.length) {
            size++;
        }
        dirty = true;
    }

}

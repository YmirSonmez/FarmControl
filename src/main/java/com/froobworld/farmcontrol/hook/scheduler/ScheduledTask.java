package com.froobworld.farmcontrol.hook.scheduler;

public interface ScheduledTask {
    /**
     * A task that has already run to completion.
     */
    ScheduledTask COMPLETED = new ScheduledTask() {
        @Override
        public void cancel() {}

        @Override
        public boolean isCancelled() {
            return false;
        }
    };

    void cancel();

    boolean isCancelled();

}

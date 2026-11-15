package com.froobworld.farmcontrol;

import com.froobworld.farmcontrol.hook.entitygetter.BukkitEntityGetterHook;
import com.froobworld.farmcontrol.hook.entitygetter.EntityGetterHook;
import com.froobworld.farmcontrol.hook.entitygetter.RegionisedEntityGetterHook;
import com.froobworld.farmcontrol.hook.goal.MobGoalHook;
import com.froobworld.farmcontrol.hook.scheduler.BukkitSchedulerHook;
import com.froobworld.farmcontrol.hook.scheduler.RegionisedSchedulerHook;
import com.froobworld.farmcontrol.hook.scheduler.SchedulerHook;
import com.froobworld.farmcontrol.hook.tick.PaperTickHook;
import com.froobworld.farmcontrol.hook.tick.TickHook;
import com.froobworld.farmcontrol.utils.MsptTracker;

public class HookManager {
    private final FarmControl farmControl;
    private final MobGoalHook mobGoalHook;
    private final TickHook tickHook;
    private final SchedulerHook schedulerHook;
    private final EntityGetterHook entityGetterHook;
    private MsptTracker msptTracker;

    public HookManager(FarmControl farmControl) {
        this.farmControl = farmControl;
        mobGoalHook = MobGoalHook.create();
        if (RegionisedSchedulerHook.isCompatible()) {
            // regionised (Folia) servers have no single server tick to measure
            schedulerHook = new RegionisedSchedulerHook(farmControl);
            entityGetterHook = new RegionisedEntityGetterHook(farmControl);
            tickHook = null;
        } else {
            schedulerHook = new BukkitSchedulerHook(farmControl);
            entityGetterHook = new BukkitEntityGetterHook();
            tickHook = new PaperTickHook();
            tickHook.register(farmControl);
        }
    }

    public void load() {
        if (tickHook != null) {
            msptTracker = new MsptTracker(
                    farmControl.getFcConfig().msptTracker.collectionPeriod.get(),
                    tickHook
            );
            msptTracker.register();
        }
    }

    public void reload() {
        if (msptTracker != null) {
            msptTracker.unregister();
        }
        load();
    }

    public MsptTracker getMsptTracker() {
        return msptTracker;
    }

    public TickHook getTickHook() {
        return tickHook;
    }

    public SchedulerHook getSchedulerHook() {
        return schedulerHook;
    }

    public EntityGetterHook getEntityGetterHook() {
        return entityGetterHook;
    }

    public MobGoalHook getMobGoalHook() {
        return mobGoalHook;
    }
}

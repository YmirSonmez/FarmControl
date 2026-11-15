package com.froobworld.farmcontrol.controller.action;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.froobworld.farmcontrol.FarmControl;
import com.google.common.collect.MapMaker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

/**
 * Cancels every path a mob tries to find. The mob keeps its AI (it still looks around, attacks in reach, breeds if it
 * is already next to a partner), but it no longer pays for pathfinding and no longer walks anywhere.
 */
public class DisablePathfindingAction extends Action implements Listener {
    private final Map<Entity, Boolean> pathfindingDisabled = new MapMaker().weakKeys().makeMap();

    public DisablePathfindingAction(FarmControl farmControl) {
        super("disable-pathfinding", Mob.class, false, false, true);
        Bukkit.getPluginManager().registerEvents(this, farmControl);
    }

    @Override
    public void doAction(Entity entity) {
        if (entity instanceof Mob mob) {
            pathfindingDisabled.put(mob, Boolean.TRUE);
            mob.getPathfinder().stopPathfinding();
        }
    }

    @Override
    public void undoAction(Entity entity) {
        pathfindingDisabled.remove(entity);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPathfind(EntityPathfindEvent event) {
        // this event is fired very frequently, so bail out as early as possible
        if (!pathfindingDisabled.isEmpty() && pathfindingDisabled.containsKey(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}

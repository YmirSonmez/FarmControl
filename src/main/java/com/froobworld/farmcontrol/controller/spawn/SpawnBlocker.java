package com.froobworld.farmcontrol.controller.spawn;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.trigger.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stops creatures from spawning in chunks occupied by groups that have the prevent-spawn action, before the creature is
 * even created.
 */
public class SpawnBlocker implements Listener {
    // world -> trigger -> chunk key -> entity types that may not spawn in the chunk
    private final Map<UUID, Map<String, Map<Long, Set<EntityType>>>> blockedChunks = new ConcurrentHashMap<>();

    public SpawnBlocker(FarmControl farmControl) {
        Bukkit.getPluginManager().registerEvents(this, farmControl);
    }

    /**
     * Replaces the chunks blocked by a trigger in a world. The given map must not be modified afterwards.
     */
    public void setBlockedChunks(World world, Trigger trigger, Map<Long, Set<EntityType>> chunkBlockedTypes) {
        if (chunkBlockedTypes == null || chunkBlockedTypes.isEmpty()) {
            clear(world, trigger);
            return;
        }
        blockedChunks.computeIfAbsent(world.getUID(), uuid -> new ConcurrentHashMap<>()).put(trigger.getName(), chunkBlockedTypes);
    }

    public void clear(World world, Trigger trigger) {
        Map<String, Map<Long, Set<EntityType>>> worldBlockedChunks = blockedChunks.get(world.getUID());
        if (worldBlockedChunks != null) {
            worldBlockedChunks.remove(trigger.getName());
        }
    }

    public void clear(World world) {
        blockedChunks.remove(world.getUID());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        if (blockedChunks.isEmpty()) {
            return;
        }
        Location location = event.getSpawnLocation();
        Map<String, Map<Long, Set<EntityType>>> worldBlockedChunks = blockedChunks.get(location.getWorld().getUID());
        if (worldBlockedChunks == null || worldBlockedChunks.isEmpty()) {
            return;
        }
        Long chunkKey = Chunk.getChunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        for (Map<Long, Set<EntityType>> chunkBlockedTypes : worldBlockedChunks.values()) {
            Set<EntityType> blockedTypes = chunkBlockedTypes.get(chunkKey);
            if (blockedTypes != null && blockedTypes.contains(event.getType())) {
                event.setCancelled(true);
                event.setShouldAbortSpawn(true); // don't let the spawner keep retrying this cycle
                return;
            }
        }
    }

}

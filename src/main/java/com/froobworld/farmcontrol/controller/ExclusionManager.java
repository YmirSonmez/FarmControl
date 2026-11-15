package com.froobworld.farmcontrol.controller;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.config.FcConfig;
import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.froobworld.farmcontrol.utils.EntityCategory;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ExclusionManager {
    private final FarmControl farmControl;
    private final Set<String> warnedNames = ConcurrentHashMap.newKeySet(); // only warn once, the predicate is rebuilt every cycle

    public ExclusionManager(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    public Predicate<SnapshotEntity> getExclusionPredicate(World world) {
        FcConfig.WorldSettings.ExclusionSettings exclusionSettings = farmControl.getFcConfig().worldSettings.of(world).exclusionSettings;
        boolean excludeLeashed = exclusionSettings.leashed.get();
        boolean excludeLoveMode = exclusionSettings.loveMode.get();
        String[] excludeMeta = exclusionSettings.metadata.get().toArray(new String[0]);
        boolean excludeNamed = exclusionSettings.named.get();
        boolean excludeTamed = exclusionSettings.tamed.get();
        boolean excludePatrolLeaders = exclusionSettings.patrolLeader.get();
        Set<EntityType> excludeType = EnumSet.noneOf(EntityType.class);
        for (String typeName : exclusionSettings.type.get()) {
            // accepts the same names as profiles do: entity types, categories and legacy type names
            EntityCategory entityCategory = EntityCategory.ofName(typeName);
            if (entityCategory != null) {
                excludeType.addAll(EntityCategory.unionOfMemberTypes(List.of(entityCategory)));
            } else if (warnedNames.add("type:" + typeName)) {
                farmControl.getLogger().warning("Unknown entity type '" + typeName + "' in the exclusion settings for world '" + world.getName() + "'.");
            }
        }
        long excludeTicksLived = exclusionSettings.youngerThan.get();
        boolean excludePickupable = exclusionSettings.pickupable.get();
        boolean excludeMounted = exclusionSettings.mounted.get();
        boolean excludeNotTicking = exclusionSettings.notTicking.get();
        Set<SpawnReason> excludeSpawnReason = EnumSet.noneOf(SpawnReason.class);
        for (String spawnReasonName : exclusionSettings.spawnReason.get()) {
            try {
                excludeSpawnReason.add(SpawnReason.valueOf(spawnReasonName.toUpperCase(Locale.ROOT).replace('-', '_')));
            } catch (IllegalArgumentException e) {
                if (warnedNames.add("spawn-reason:" + spawnReasonName)) {
                    farmControl.getLogger().warning("Unknown spawn reason '" + spawnReasonName + "' in the exclusion settings for world '" + world.getName() + "'.");
                }
            }
        }
        return snapshotEntity -> {
            if (excludeLeashed && snapshotEntity.isLeashed()) {
                return true;
            }
            if (excludeLoveMode && snapshotEntity.isLoveMode()) {
                return true;
            }
            if (excludeNamed && snapshotEntity.hasCustomName()) {
                return true;
            }
            if (excludeTamed && snapshotEntity.isTamed()) {
                return true;
            }
            if (excludePatrolLeaders && snapshotEntity.isPatrolLeader()) {
                return true;
            }
            if (excludePickupable && snapshotEntity.isPickupable()) {
                return true;
            }
            if (excludeNotTicking && !snapshotEntity.isTicking()) {
                return true;
            }
            if (excludeSpawnReason.contains(snapshotEntity.getSpawnReason())) {
                return true;
            }
            if (excludeMounted && snapshotEntity.isMounted()) {
                return true;
            }
            if (snapshotEntity.getTicksLived() < excludeTicksLived) {
                return true;
            }
            if (excludeType.contains(snapshotEntity.getEntityType())) {
                return true;
            }
            for (String meta : excludeMeta) {
                if (snapshotEntity.hasMetadata(meta)) {
                    return true;
                }
            }
            return false;
        };
    }

}

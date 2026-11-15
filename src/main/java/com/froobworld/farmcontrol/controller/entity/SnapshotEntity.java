package com.froobworld.farmcontrol.controller.entity;

import com.froobworld.farmcontrol.data.FcData;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.material.Colorable;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable view of an entity, captured on the thread owning the entity so that it can be inspected asynchronously.
 */
public class SnapshotEntity {
    private final Entity entity;
    private final EntityType entityType;
    private final int entityId;
    private final int blockX, blockY, blockZ;
    private final FcData fcData;
    private final boolean leashed;
    private final boolean loveMode;
    private final boolean customName;
    private final boolean tamed;
    private final boolean isPatrolLeader;
    private final int ticksLived;
    private final boolean pickupable;
    private final boolean mounted;
    private final boolean adult;
    private final boolean ticking;
    private final CreatureSpawnEvent.SpawnReason spawnReason;
    private final List<Object> classifications = new ArrayList<>(2);

    public SnapshotEntity(Entity entity) {
        this.entity = entity;
        this.entityType = entity.getType();
        this.entityId = entity.getEntityId();
        Location location = entity.getLocation();
        this.blockX = location.getBlockX();
        this.blockY = location.getBlockY();
        this.blockZ = location.getBlockZ();
        this.fcData = FcData.get(entity);
        this.leashed = entity instanceof Mob mob && mob.isLeashed();
        this.loveMode = entity instanceof Animals animals && animals.isLoveMode();
        this.customName = entity.customName() != null;
        this.tamed = entity instanceof Tameable tameable && tameable.isTamed();
        this.isPatrolLeader = entity instanceof Raider raider && raider.isPatrolLeader();
        this.pickupable = entity instanceof AbstractArrow arrow && arrow.getPickupStatus() == AbstractArrow.PickupStatus.ALLOWED;
        this.ticksLived = entity.getTicksLived();
        this.mounted = !entity.getPassengers().isEmpty();
        this.adult = !(entity instanceof Ageable ageable) || ageable.isAdult();
        this.ticking = entity.isTicking();
        this.spawnReason = entity.getEntitySpawnReason();
        // boats need no further classification, each wood type is its own entity type
        classifications.add(entityType);
        if (entity instanceof Colorable colorable) {
            DyeColor colour = colorable.getColor();
            if (colour != null) {
                classifications.add(colour);
            }
        }
        if (entity instanceof Villager villager) {
            classifications.add(villager.getProfession());
        }
        if (entity instanceof Item item) {
            classifications.add(item.getItemStack().getType());
        }
    }

    public Class<? extends Entity> getEntityClass() {
        return entity.getClass();
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public int getChunkX() {
        return blockX >> 4;
    }

    public int getChunkZ() {
        return blockZ >> 4;
    }

    public FcData getFcData() {
        return fcData;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean hasMetadata(String key) {
        return entity.hasMetadata(key);
    }

    public boolean isLeashed() {
        return leashed;
    }

    public boolean isLoveMode() {
        return loveMode;
    }

    public boolean hasCustomName() {
        return customName;
    }

    public boolean isTamed() {
        return tamed;
    }

    public boolean isPatrolLeader() {
        return isPatrolLeader;
    }

    public boolean isPickupable() {
        return pickupable;
    }

    public boolean isMounted() {
        return mounted;
    }

    public boolean isAdult() {
        return adult;
    }

    /**
     * @return Whether the entity was being ticked, i.e. it was not outside of simulation distance.
     */
    public boolean isTicking() {
        return ticking;
    }

    public CreatureSpawnEvent.SpawnReason getSpawnReason() {
        return spawnReason;
    }

    public int getTicksLived() {
        return ticksLived;
    }

    public List<Object> getClassifications() {
        return classifications;
    }

}

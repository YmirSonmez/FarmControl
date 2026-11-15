package com.froobworld.farmcontrol.controller.action;

import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import org.bukkit.entity.Entity;

public abstract class Action {
    private final String name;
    private final Class<?> entityClass;
    private final boolean removes;
    private final boolean persistent;
    private final boolean undoOnUnload;

    public Action(String name, Class<?> entityClass, boolean removes, boolean persistent, boolean undoOnUnload) {
        if (!name.matches("[a-z-]+")) {
            throw new IllegalArgumentException("Name must match [a-z-]+");
        }
        this.entityClass = entityClass;
        this.name = name;
        this.removes = removes;
        this.persistent = persistent;
        this.undoOnUnload = undoOnUnload;
    }

    public final String getName() {
        return name;
    }

    public final boolean removes() {
        return removes;
    }

    public final boolean isPersistent() {
        return persistent;
    }

    public boolean undoOnUnload() {
        return undoOnUnload;
    }

    /**
     * @return Whether this action applies to a group as a whole rather than to each of its members, in which case
     * {@link #doAction(Entity)} and {@link #undoAction(Entity)} are never called.
     */
    public boolean isGroupAction() {
        return false;
    }

    /**
     * @return Whether this action can currently be performed on the given entity.
     */
    public boolean isApplicableTo(SnapshotEntity entity) {
        return entityClass.isAssignableFrom(entity.getEntityClass());
    }

    public abstract void doAction(Entity entity);

    public abstract void undoAction(Entity entity);

    public Class<?> getEntityClass() {
        return entityClass;
    }
}

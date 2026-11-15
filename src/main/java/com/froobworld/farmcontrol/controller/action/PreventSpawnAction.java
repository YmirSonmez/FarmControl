package com.froobworld.farmcontrol.controller.action;

import org.bukkit.entity.Entity;

/**
 * A group action: rather than acting on the members of a group, it stops more creatures of the group's types from
 * spawning in the chunks the group occupies. See {@link com.froobworld.farmcontrol.controller.spawn.SpawnBlocker}.
 */
public class PreventSpawnAction extends Action {

    public PreventSpawnAction() {
        super("prevent-spawn", Entity.class, false, false, false);
    }

    @Override
    public boolean isGroupAction() {
        return true;
    }

    @Override
    public void doAction(Entity entity) {}

    @Override
    public void undoAction(Entity entity) {}
}

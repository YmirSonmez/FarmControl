package com.froobworld.farmcontrol.controller;

import com.froobworld.farmcontrol.controller.action.Action;
import com.froobworld.farmcontrol.controller.action.PreventSpawnAction;
import com.froobworld.farmcontrol.group.GroupDefinition;

import java.util.HashSet;
import java.util.Set;

public class ActionProfile {
    private final GroupDefinition groupDefinition;
    private final Set<Action> actions;
    private final Set<Action> entityActions = new HashSet<>();
    private final boolean removes;
    private final boolean preventsSpawn;

    public ActionProfile(GroupDefinition groupDefinition, Set<Action> actions) {
        this.groupDefinition = groupDefinition;
        this.actions = actions;
        removes = actions.stream().anyMatch(Action::removes);
        preventsSpawn = actions.stream().anyMatch(action -> action instanceof PreventSpawnAction);
        for (Action action : actions) {
            if (!action.isGroupAction()) {
                entityActions.add(action);
            }
        }
    }

    public GroupDefinition getGroupDefinition() {
        return groupDefinition;
    }

    public Set<Action> getActions() {
        return actions;
    }

    /**
     * @return The actions that are performed on each member of a group.
     */
    public Set<Action> getEntityActions() {
        return entityActions;
    }

    public boolean preventsSpawn() {
        return preventsSpawn;
    }

    public boolean removes() {
        return removes;
    }

}

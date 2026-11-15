package com.froobworld.farmcontrol.controller;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.action.*;
import com.froobworld.farmcontrol.controller.breeding.BreedingBlocker;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ActionManager {
    private final Map<String, Action> actions = new HashMap<>();
    private final Collection<Action> actionsView = Collections.unmodifiableCollection(actions.values());

    public void addDefaults(FarmControl farmControl) {
        addAction(new DisableCollisionsAction());
        addAction(new KillAction());
        addAction(new RemoveAction());
        addAction(new RemoveAiAction());
        addAction(new RemoveAwarenessAction());
        addAction(new DisableBreedingAction(new BreedingBlocker(farmControl)));
        if (farmControl.getHookManager().getMobGoalHook() != null) {
            addAction(new RemoveRandomMovementAction(farmControl.getHookManager().getMobGoalHook()));
        } else {
            farmControl.getLogger().warning("The remove-random-movement action is not available on this version.");
        }
    }

    public void addAction(Action action) {
        actions.put(action.getName(), action);
    }

    public Action getAction(String actionName) {
        return actions.get(actionName);
    }

    public Collection<Action> getActions() {
        return actionsView;
    }

}

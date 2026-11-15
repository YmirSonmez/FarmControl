package com.froobworld.farmcontrol.controller.action;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.hook.goal.MobGoalHook;
import com.google.common.collect.MapMaker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveRandomMovementAction extends Action {
    private final static Map<Mob, List<MobGoalHook.RemovedGoal>> entityRemovedGoalsMap = new MapMaker().weakKeys().makeMap();
    private final MobGoalHook mobGoalHook;

    public static void cleanUp(FarmControl farmControl) {
        if (entityRemovedGoalsMap.isEmpty()) {
            return;
        }
        for (Mob mob : new ArrayList<>(entityRemovedGoalsMap.keySet())) {
            farmControl.getHookManager().getSchedulerHook().runEntityTaskAsap(() -> {
                if (!mob.isValid()) {
                    entityRemovedGoalsMap.remove(mob);
                }
            }, () -> entityRemovedGoalsMap.remove(mob), mob);
        }
    }

    public RemoveRandomMovementAction(MobGoalHook mobGoalHook) {
        super("remove-random-movement", Mob.class, false, false, true);
        this.mobGoalHook = mobGoalHook;
    }

    @Override
    public void doAction(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }
        List<MobGoalHook.RemovedGoal> removedGoals = mobGoalHook.removeRandomMovementGoals(mob);
        if (removedGoals.isEmpty()) {
            return;
        }
        entityRemovedGoalsMap.merge(mob, removedGoals, (previous, current) -> {
            List<MobGoalHook.RemovedGoal> merged = new ArrayList<>(previous.size() + current.size());
            merged.addAll(previous);
            merged.addAll(current);
            return merged;
        });
    }

    @Override
    public void undoAction(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }
        List<MobGoalHook.RemovedGoal> removedGoals = entityRemovedGoalsMap.remove(mob);
        if (removedGoals != null) {
            mobGoalHook.restoreGoals(removedGoals);
        }
    }

}

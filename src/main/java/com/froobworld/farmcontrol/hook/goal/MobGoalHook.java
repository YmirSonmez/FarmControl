package com.froobworld.farmcontrol.hook.goal;

import org.bukkit.entity.Mob;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gives access to the goal selectors of a mob. Paper's MobGoals API does not expose goal priorities, which are needed
 * to restore a removed goal, so the (Mojang-mapped) server internals are accessed through cached method handles.
 */
public final class MobGoalHook {
    private static final String GOAL_PACKAGE = "net.minecraft.world.entity.ai.goal.";
    private static final List<String> RANDOM_MOVEMENT_GOAL_CLASS_NAMES = List.of(
            GOAL_PACKAGE + "WaterAvoidingRandomFlyingGoal",
            GOAL_PACKAGE + "RandomStrollGoal",
            GOAL_PACKAGE + "WaterAvoidingRandomStrollGoal",
            GOAL_PACKAGE + "RandomSwimmingGoal"
    );

    private final MethodHandle getHandle; // (CraftMob) -> nms Mob
    private final MethodHandle[] goalSelectorGetters; // (nms Mob) -> GoalSelector
    private final MethodHandle getAvailableGoals; // (GoalSelector) -> Set<WrappedGoal>
    private final MethodHandle addGoal; // (GoalSelector, int, Goal) -> void
    private final MethodHandle removeGoal; // (GoalSelector, Goal) -> void
    private final MethodHandle getGoal; // (WrappedGoal) -> Goal
    private final MethodHandle getPriority; // (WrappedGoal) -> int
    private final Set<Class<?>> randomMovementGoalClasses;

    private MobGoalHook() throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> craftMobClass = Class.forName("org.bukkit.craftbukkit.entity.CraftMob");
        Class<?> mobClass = Class.forName("net.minecraft.world.entity.Mob");
        Class<?> goalSelectorClass = Class.forName(GOAL_PACKAGE + "GoalSelector");
        Class<?> wrappedGoalClass = Class.forName(GOAL_PACKAGE + "WrappedGoal");
        Class<?> goalClass = Class.forName(GOAL_PACKAGE + "Goal");

        getHandle = lookup.unreflect(craftMobClass.getMethod("getHandle"))
                .asType(MethodType.methodType(Object.class, Mob.class));

        // mobs have two goal selectors (one for goals, one for targets)
        List<MethodHandle> selectorGetters = new ArrayList<>();
        MethodHandles.Lookup mobLookup = MethodHandles.privateLookupIn(mobClass, lookup);
        for (Field field : mobClass.getDeclaredFields()) {
            if (field.getType().equals(goalSelectorClass)) {
                selectorGetters.add(mobLookup.unreflectGetter(field).asType(MethodType.methodType(Object.class, Object.class)));
            }
        }
        if (selectorGetters.isEmpty()) {
            throw new NoSuchFieldException("No goal selectors found in " + mobClass.getName());
        }
        goalSelectorGetters = selectorGetters.toArray(new MethodHandle[0]);

        getAvailableGoals = lookup.findVirtual(goalSelectorClass, "getAvailableGoals", MethodType.methodType(Set.class))
                .asType(MethodType.methodType(Set.class, Object.class));
        addGoal = lookup.findVirtual(goalSelectorClass, "addGoal", MethodType.methodType(void.class, int.class, goalClass))
                .asType(MethodType.methodType(void.class, Object.class, int.class, Object.class));
        removeGoal = lookup.findVirtual(goalSelectorClass, "removeGoal", MethodType.methodType(void.class, goalClass))
                .asType(MethodType.methodType(void.class, Object.class, Object.class));
        getGoal = lookup.findVirtual(wrappedGoalClass, "getGoal", MethodType.methodType(goalClass))
                .asType(MethodType.methodType(Object.class, Object.class));
        getPriority = lookup.findVirtual(wrappedGoalClass, "getPriority", MethodType.methodType(int.class))
                .asType(MethodType.methodType(int.class, Object.class));

        Set<Class<?>> goalClasses = new HashSet<>();
        for (String className : RANDOM_MOVEMENT_GOAL_CLASS_NAMES) {
            try {
                goalClasses.add(Class.forName(className));
            } catch (ClassNotFoundException ignored) {}
        }
        if (goalClasses.isEmpty()) {
            throw new ClassNotFoundException("No random movement goal classes found");
        }
        randomMovementGoalClasses = Set.copyOf(goalClasses);
    }

    /**
     * @return A hook, or null if the server internals are not laid out as expected.
     */
    public static MobGoalHook create() {
        try {
            return new MobGoalHook();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Removes (and stops) the random movement goals of a mob. Must be called on the thread owning the mob.
     *
     * @return The goals that were removed, to be passed to {@link #restoreGoals(List)}.
     */
    public List<RemovedGoal> removeRandomMovementGoals(Mob mob) {
        try {
            Object handle = (Object) getHandle.invokeExact(mob);
            List<RemovedGoal> removedGoals = null;
            for (MethodHandle goalSelectorGetter : goalSelectorGetters) {
                Object goalSelector = (Object) goalSelectorGetter.invokeExact(handle);
                Set<?> wrappedGoals = (Set<?>) getAvailableGoals.invokeExact(goalSelector);
                int firstRemoved = removedGoals == null ? 0 : removedGoals.size();
                for (Object wrappedGoal : wrappedGoals) {
                    Object goal = (Object) getGoal.invokeExact(wrappedGoal);
                    if (randomMovementGoalClasses.contains(goal.getClass())) {
                        if (removedGoals == null) {
                            removedGoals = new ArrayList<>(2);
                        }
                        removedGoals.add(new RemovedGoal(goalSelector, (int) getPriority.invokeExact(wrappedGoal), goal));
                    }
                }
                if (removedGoals != null) {
                    // removed after iterating, as removing a goal modifies the set of available goals
                    for (int i = firstRemoved; i < removedGoals.size(); i++) {
                        removeGoal.invokeExact(goalSelector, removedGoals.get(i).goal);
                    }
                }
            }
            return removedGoals == null ? List.of() : removedGoals;
        } catch (Throwable throwable) {
            throw rethrow(throwable);
        }
    }

    /**
     * Adds back goals that were previously removed. Must be called on the thread owning the mob.
     */
    public void restoreGoals(List<RemovedGoal> removedGoals) {
        try {
            for (RemovedGoal removedGoal : removedGoals) {
                addGoal.invokeExact(removedGoal.goalSelector, removedGoal.priority, removedGoal.goal);
            }
        } catch (Throwable throwable) {
            throw rethrow(throwable);
        }
    }

    private static RuntimeException rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        return new IllegalStateException(throwable);
    }

    public static final class RemovedGoal {
        private final Object goalSelector;
        private final int priority;
        private final Object goal;

        private RemovedGoal(Object goalSelector, int priority, Object goal) {
            this.goalSelector = goalSelector;
            this.priority = priority;
            this.goal = goal;
        }
    }

}

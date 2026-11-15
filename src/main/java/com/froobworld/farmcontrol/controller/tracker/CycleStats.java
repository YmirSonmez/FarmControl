package com.froobworld.farmcontrol.controller.tracker;

import com.froobworld.farmcontrol.controller.action.Action;
import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.EntityType;

import java.util.*;

public class CycleStats {
    private final long startTime;
    private final long endTime;
    private final int entitiesAffected;
    private final int entitiesRemoved;
    private final Map<Action, Map<EntityType, Integer>> actionCounts;

    private CycleStats(long startTime, long endTime, int entitiesAffected, int entitiesRemoved, Map<Action, Map<EntityType, Integer>> actionCounts) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.entitiesAffected = entitiesAffected;
        this.entitiesRemoved = entitiesRemoved;
        this.actionCounts = actionCounts;
    }

    public long getStartTimeMillis() {
        return startTime;
    }

    public long getDurationMillis() {
        return endTime - startTime;
    }

    public int getAffectedEntityCount() {
        return entitiesAffected;
    }

    public int getEntitiesRemoved() {
        return entitiesRemoved;
    }

    public Component getBreakdown() {
        TextComponent.Builder breakdown = Component.text();
        boolean firstAction = true;

        for (Map.Entry<Action, Map<EntityType, Integer>> actionEntry : actionCounts.entrySet()) {
            int totalCount = 0;
            for (int count : actionEntry.getValue().values()) {
                totalCount += count;
            }
            if (!firstAction) {
                breakdown.append(Component.newline()).append(Component.newline());
            }
            breakdown.append(Component.text("- " + actionEntry.getKey().getName() + ": ", NamedTextColor.GOLD))
                    .append(Component.text(totalCount, NamedTextColor.RED))
                    .append(Component.newline());

            List<Map.Entry<EntityType, Integer>> entityCounts = new ArrayList<>(actionEntry.getValue().entrySet());
            entityCounts.sort(Map.Entry.<EntityType, Integer>comparingByValue().reversed());
            boolean firstEntity = true;
            for (Map.Entry<EntityType, Integer> entityEntry : entityCounts) {
                if (!firstEntity) {
                    breakdown.append(Component.text(", ", NamedTextColor.GRAY));
                }
                breakdown.append(Component.text(entityEntry.getValue() + " ", NamedTextColor.RED))
                        .append(Component.text(entityEntry.getKey().name().toLowerCase(Locale.ROOT), NamedTextColor.GRAY));
                firstEntity = false;
            }
            firstAction = false;
        }
        return breakdown.build();
    }

    public static class Builder {
        private long startTime;
        private final Set<SnapshotEntity> affectedEntities = new HashSet<>();
        private final Set<SnapshotEntity> removedEntities = new HashSet<>();
        private final Map<Action, Map<EntityType, Integer>> actionCounts = new HashMap<>();

        private Builder() {}

        public static Builder start(long startTime) {
            Builder builder = new Builder();
            builder.startTime = startTime;
            return builder;
        }

        public synchronized void action(Action action, SnapshotEntity entity) {
            affectedEntities.add(entity);
            if (action.removes()) {
                removedEntities.add(entity);
            }
            actionCounts.computeIfAbsent(action, a -> new HashMap<>())
                    .compute(entity.getEntityType(), (e, count) -> count != null ? count + 1 : 1);
        }

        public CycleStats end(long endTime) {
            return new CycleStats(startTime, endTime, affectedEntities.size(), removedEntities.size(), actionCounts);
        }

    }

}

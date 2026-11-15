package com.froobworld.farmcontrol.group;

import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class ProtoGroup {
    private final EntityType initialType;
    private final int initialChunkX, initialChunkZ;
    private final GroupDefinition definition;
    private final List<SnapshotEntity> members;

    public ProtoGroup(GroupDefinition definition, SnapshotEntity initialMember) {
        this.definition = definition;
        this.members = new ArrayList<>();
        this.members.add(initialMember);
        initialType = initialMember.getEntityType();
        initialChunkX = initialMember.getChunkX();
        initialChunkZ = initialMember.getChunkZ();
    }

    public List<SnapshotEntity> getMembers() {
        return members;
    }

    public Group toGroup() {
        return new Group(definition, members);
    }

    void merge(ProtoGroup otherProtoGroup) {
        members.addAll(otherProtoGroup.members);
    }

    void add(SnapshotEntity entity) {
        members.add(entity);
    }

    public boolean shouldBeMember(SnapshotEntity entity) {
        if (definition.isPure() && entity.getEntityType() != initialType) {
            return false;
        }
        if (definition.isSameChunk()) {
            return entity.getChunkX() == initialChunkX && entity.getChunkZ() == initialChunkZ;
        }
        final double maxDistance = definition.getDistance();
        final boolean ignoreVerticalDistance = definition.ignoreVerticalDistance();
        final int x = entity.getBlockX(), y = entity.getBlockY(), z = entity.getBlockZ();
        int remainingMembers = members.size();
        int minDistance = -1;
        for (int i = 0, size = members.size(); i < size; i++) {
            SnapshotEntity member = members.get(i);
            remainingMembers--;
            int distance = Math.max(Math.abs(member.getBlockX() - x), Math.abs(member.getBlockZ() - z));
            if (!ignoreVerticalDistance) {
                distance = Math.max(distance, Math.abs(member.getBlockY() - y));
            }
            if (minDistance < 0 || distance < minDistance) {
                minDistance = distance;
            }
            if (distance <= maxDistance) {
                return true;
            } else if (minDistance > (remainingMembers + 1) * maxDistance) {
                return false;
            }
        }
        return false;
    }

}

package com.froobworld.farmcontrol.utils;

import com.froobworld.farmcontrol.controller.action.Action;
import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import org.bukkit.entity.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class EntityCategory {
    private static final Map<String, EntityCategory> entityCategoryMap = new HashMap<>();
    private final String name;
    private final Set<EntityType> memberTypes = EnumSet.noneOf(EntityType.class); // resolved once, membership never changes

    static {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getEntityClass() != null) {
                register(new EntityCategory(entityType.toString().toLowerCase(Locale.ROOT), false, entityType.getEntityClass()));
            }
        }
        register(new EntityCategory("category:animal", true, Animals.class));
        register(new EntityCategory("category:monster", true, Monster.class));
        register(new EntityCategory("category:golem", true, Golem.class));
        register(new EntityCategory("category:ambient", true, Ambient.class));
        register(new EntityCategory("category:fish", true, Fish.class));
        register(new EntityCategory("category:tameable", true, Tameable.class));
        register(new EntityCategory("category:raider", true, Raider.class));
        register(new EntityCategory("category:mob", true, Mob.class));
        register(new EntityCategory("category:vehicle", true, Boat.class, Minecart.class));
        register(new EntityCategory("category:projectile", true, Projectile.class));
    }

    private static void register(EntityCategory entityCategory) {
        entityCategoryMap.put(entityCategory.name, entityCategory);
    }

    private EntityCategory(String name, boolean allowInheritance, Class<?>... memberClasses) {
        this.name = name;
        for (EntityType entityType : EntityType.values()) {
            Class<?> entityClass = entityType.getEntityClass();
            if (entityClass == null) {
                continue;
            }
            for (Class<?> memberClass : memberClasses) {
                if (allowInheritance ? memberClass.isAssignableFrom(entityClass) : memberClass.equals(entityClass)) {
                    memberTypes.add(entityType);
                    break;
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public boolean isMember(SnapshotEntity entity) {
        return memberTypes.contains(entity.getEntityType());
    }

    public boolean isMember(EntityType entityType) {
        return memberTypes.contains(entityType);
    }

    public Predicate<SnapshotEntity> asSnapshotEntityPredicate() {
        return this::isMember;
    }

    public boolean isCompatibleWith(Action action) {
        for (EntityType entityType : memberTypes) {
            Class<?> entityTypeClass = entityType.getEntityClass();
            if (entityTypeClass != null && action.getEntityClass().isAssignableFrom(entityTypeClass)) {
                return true;
            }
        }
        return false;
    }

    public static Set<EntityType> unionOfMemberTypes(Collection<EntityCategory> entityCategories) {
        Set<EntityType> union = EnumSet.noneOf(EntityType.class);
        for (EntityCategory entityCategory : entityCategories) {
            union.addAll(entityCategory.memberTypes);
        }
        return union;
    }

    public static EntityCategory ofName(String categoryName) {
        return EntityCategory.entityCategoryMap.get(categoryName.toLowerCase(Locale.ROOT));
    }

}

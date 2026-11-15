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
        register(new EntityCategory("category:villager", true, AbstractVillager.class));
        register(new EntityCategory("category:water-animal", true, WaterMob.class));
        register(new EntityCategory("category:chest-boat", true, ChestBoat.class));

        // names of entity types from before they were renamed or split up, so that older configurations keep working
        registerLegacy(new EntityCategory("chest_boat", true, ChestBoat.class));
        registerLegacy(new EntityCategory("boat", entityClass -> Boat.class.isAssignableFrom(entityClass) && !ChestBoat.class.isAssignableFrom(entityClass)));
        registerLegacy(new EntityCategory("potion", true, ThrownPotion.class));
        registerLegacyAlias("minecart_chest", "chest_minecart");
        registerLegacyAlias("minecart_hopper", "hopper_minecart");
        registerLegacyAlias("minecart_tnt", "tnt_minecart");
        registerLegacyAlias("minecart_furnace", "furnace_minecart");
        registerLegacyAlias("minecart_command", "command_block_minecart");
        registerLegacyAlias("minecart_mob_spawner", "spawner_minecart");
        registerLegacyAlias("snowman", "snow_golem");
        registerLegacyAlias("mushroom_cow", "mooshroom");
        registerLegacyAlias("dropped_item", "item");
        registerLegacyAlias("primed_tnt", "tnt");
        registerLegacyAlias("firework", "firework_rocket");
        registerLegacyAlias("fishing_hook", "fishing_bobber");
        registerLegacyAlias("ender_crystal", "end_crystal");
        registerLegacyAlias("ender_signal", "eye_of_ender");
        registerLegacyAlias("thrown_exp_bottle", "experience_bottle");
        registerLegacyAlias("leash_hitch", "leash_knot");
        registerLegacyAlias("lightning", "lightning_bolt");
    }

    private static void registerLegacy(EntityCategory entityCategory) {
        if (!entityCategory.memberTypes.isEmpty()) {
            entityCategoryMap.putIfAbsent(entityCategory.name, entityCategory);
        }
    }

    private static void registerLegacyAlias(String legacyName, String currentName) {
        EntityCategory entityCategory = entityCategoryMap.get(currentName);
        if (entityCategory != null) {
            entityCategoryMap.putIfAbsent(legacyName, entityCategory);
        }
    }

    private static void register(EntityCategory entityCategory) {
        entityCategoryMap.put(entityCategory.name, entityCategory);
    }

    private EntityCategory(String name, boolean allowInheritance, Class<?>... memberClasses) {
        this(name, entityClass -> {
            for (Class<?> memberClass : memberClasses) {
                if (allowInheritance ? memberClass.isAssignableFrom(entityClass) : memberClass.equals(entityClass)) {
                    return true;
                }
            }
            return false;
        });
    }

    private EntityCategory(String name, Predicate<Class<?>> memberClassPredicate) {
        this.name = name;
        for (EntityType entityType : EntityType.values()) {
            Class<?> entityClass = entityType.getEntityClass();
            if (entityClass != null && memberClassPredicate.test(entityClass)) {
                memberTypes.add(entityType);
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

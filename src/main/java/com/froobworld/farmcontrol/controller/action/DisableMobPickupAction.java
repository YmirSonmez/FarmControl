package com.froobworld.farmcontrol.controller.action;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

/**
 * Stops mobs from picking up (and searching for) an item. Players and hoppers are unaffected.
 */
public class DisableMobPickupAction extends Action {

    public DisableMobPickupAction() {
        super("disable-mob-pickup", Item.class, false, false, true);
    }

    @Override
    public void doAction(Entity entity) {
        if (entity instanceof Item item) {
            item.setCanMobPickup(false);
        }
    }

    @Override
    public void undoAction(Entity entity) {
        if (entity instanceof Item item) {
            item.setCanMobPickup(true);
        }
    }
}

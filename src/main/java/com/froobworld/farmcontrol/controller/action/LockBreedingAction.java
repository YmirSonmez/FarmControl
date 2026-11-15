package com.froobworld.farmcontrol.controller.action;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

/**
 * Disables breeding on the entity itself, by putting an adult on breeding cooldown and locking its age so that the
 * cooldown never runs out. Unlike disable-breeding nothing has to be intercepted afterwards, and the entity can not
 * even enter love mode. The state is stored by the server, so it stays in effect until this action is undone.
 */
public class LockBreedingAction extends Action {
    private final NamespacedKey lockedKey;

    public LockBreedingAction(FarmControl farmControl) {
        super("lock-breeding", Breedable.class, false, true, false);
        lockedKey = new NamespacedKey(farmControl, "breeding-locked");
    }

    @Override
    public boolean isApplicableTo(SnapshotEntity entity) {
        // babies are left alone as locking their age would stop them from growing up, they are picked up in a later
        // cycle once they are adults
        return entity.isAdult() && super.isApplicableTo(entity);
    }

    @Override
    public void doAction(Entity entity) {
        // entities with an age lock from elsewhere are not ours to change
        if (!(entity instanceof Breedable breedable) || !breedable.isAdult() || breedable.getAgeLock()) {
            return;
        }
        breedable.setBreed(false);
        breedable.setAgeLock(true);
        entity.getPersistentDataContainer().set(lockedKey, PersistentDataType.BYTE, (byte) 1);
    }

    @Override
    public void undoAction(Entity entity) {
        if (!(entity instanceof Breedable breedable) || !entity.getPersistentDataContainer().has(lockedKey)) {
            return;
        }
        entity.getPersistentDataContainer().remove(lockedKey);
        breedable.setAgeLock(false);
        breedable.setBreed(true);
    }
}

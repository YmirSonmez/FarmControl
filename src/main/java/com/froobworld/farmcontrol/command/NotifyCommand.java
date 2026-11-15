package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.tracker.NotificationCentre;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class NotifyCommand {
    private final FarmControl farmControl;

    public NotifyCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    public void execute(CommandSender sender) {
        NotificationCentre notificationCentre = farmControl.getFarmController().getCycleHistoryManager().getNotificationCentre();
        notificationCentre.setNotifiable(sender, !notificationCentre.isNotifiable(sender));
        if (notificationCentre.isNotifiable(sender)) {
            sender.sendMessage(Component.text("FarmControl will now notify you of actions it performs.", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text("FarmControl will no longer notify you of actions it performs.", NamedTextColor.YELLOW));
        }
    }
}

package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.tracker.CycleStats;
import com.froobworld.farmcontrol.utils.DurationDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HistoryCommand {
    private final FarmControl farmControl;

    public HistoryCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    public void execute(CommandSender sender) {
        List<CycleStats> cycleHistory = farmControl.getFarmController().getCycleHistoryManager().getCycleHistory();
        if (cycleHistory.isEmpty()) {
            sender.sendMessage(Component.text("There is no history to display.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Most recent history:", NamedTextColor.GOLD));
        for (CycleStats cycleStats : cycleHistory) {
            sender.sendMessage(Component.empty());
            long timeSinceCycle = System.currentTimeMillis() - cycleStats.getStartTimeMillis();
            sender.sendMessage(Component.text()
                    .append(Component.text("From ", NamedTextColor.GRAY))
                    .append(Component.text(DurationDisplayer.getDurationInMinutesAndSeconds(timeSinceCycle), NamedTextColor.RED))
                    .append(Component.text(" ago:", NamedTextColor.GRAY))
                    .build());
            TextComponent.Builder builder = Component.text()
                    .append(Component.text("- ", NamedTextColor.GOLD))
                    .append(Component.text("Total of ", NamedTextColor.GRAY))
                    .append(Component.text(cycleStats.getAffectedEntityCount(), NamedTextColor.RED))
                    .append(Component.text(" entities affected", NamedTextColor.GRAY));
            if (cycleStats.getEntitiesRemoved() > 0) {
                builder.append(Component.text(", with ", NamedTextColor.GRAY))
                        .append(Component.text(cycleStats.getEntitiesRemoved(), NamedTextColor.RED))
                        .append(Component.text(" removed.", NamedTextColor.GRAY));
            } else {
                builder.append(Component.text(".", NamedTextColor.GRAY));
            }
            sender.sendMessage(builder.hoverEvent(HoverEvent.showText(cycleStats.getBreakdown())).build());
        }
    }
}

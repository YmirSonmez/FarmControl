package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final FarmControl farmControl;

    public ReloadCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        try {
            farmControl.reload();
            sender.sendMessage(Component.text("Plugin reloaded.", NamedTextColor.YELLOW));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Encountered an error reloading the plugin, see console for details.", NamedTextColor.RED));
            farmControl.getSLF4JLogger().error("Failed to reload the plugin", e);
        }
        return true;
    }
}

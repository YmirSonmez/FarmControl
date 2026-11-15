package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.FarmController;
import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import com.froobworld.farmcontrol.data.FcData;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class StatusCommand implements CommandExecutor {
    private final FarmControl farmControl;

    public StatusCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cl, @NotNull String[] args) {
        World world;
        if (args.length == 1) {
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                sender.sendMessage(Component.text("You must specify a world.", NamedTextColor.RED));
                return false;
            }
        } else {
            world = Bukkit.getWorld(args[1]);
        }
        if (world == null) {
            sender.sendMessage(Component.text("Unknown world.", NamedTextColor.RED));
            return true;
        }
        AtomicInteger entityCount = new AtomicInteger(0);
        AtomicInteger affectedCount = new AtomicInteger(0);
        Map<String, AtomicInteger> actionCount = new HashMap<>();

        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        farmControl.getHookManager().getEntityGetterHook().getSnapshotEntities(world, FarmController.ENTITY_CLASSES).thenAccept(entities -> {
            entityCount.set(entities.size());
            for (SnapshotEntity entity : entities) {
                FcData fcData = entity.getFcData();
                if (fcData != null && !fcData.getActions().isEmpty()) {
                    affectedCount.incrementAndGet();
                    for (String action : fcData.getActions()) {
                        actionCount.computeIfAbsent(action, a -> new AtomicInteger(0)).incrementAndGet();
                    }
                }
            }
            completableFuture.complete(null);
        });
        completableFuture.thenRunAsync(() -> {
            sender.sendMessage(Component.text()
                    .append(Component.text("Status for world '", NamedTextColor.GRAY))
                    .append(Component.text(world.getName(), NamedTextColor.RED))
                    .append(Component.text("'", NamedTextColor.GRAY))
                    .build());
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("Total entities: ", NamedTextColor.GOLD).append(Component.text(entityCount.get(), NamedTextColor.RED)));
            sender.sendMessage(Component.text("Total affected entities: ", NamedTextColor.GOLD).append(Component.text(affectedCount.get(), NamedTextColor.RED)));
            if (affectedCount.get() > 0) {
                sender.sendMessage(Component.empty());
                sender.sendMessage(Component.text("Breakdown:", NamedTextColor.GOLD));
                actionCount.forEach((action, count) -> sender.sendMessage(Component.text()
                        .append(Component.text("- ", NamedTextColor.GOLD))
                        .append(Component.text(action + ": ", NamedTextColor.GRAY))
                        .append(Component.text(count.get(), NamedTextColor.RED))
                        .build()));
            }
        });
        return true;
    }

}

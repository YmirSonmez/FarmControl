package com.froobworld.farmcontrol.controller.tracker;

import com.froobworld.farmcontrol.FarmControl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationCentre {
    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private final FarmControl farmControl;
    private final ExecutorService executorService;
    private final Set<UUID> notifiableUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public NotificationCentre(FarmControl farmControl) {
        this.farmControl = farmControl;
        this.executorService = Executors.newSingleThreadExecutor();
        load();
    }

    public void notify(CycleStats cycleStats) {
        if (notifiableUsers.isEmpty()) {
            return;
        }
        TextComponent.Builder notificationBuilder = Component.text()
                .append(Component.text("FarmControl - ", NamedTextColor.GOLD))
                .append(Component.text(cycleStats.getAffectedEntityCount(), NamedTextColor.RED))
                .append(Component.text(" entities affected", NamedTextColor.GRAY));
        if (cycleStats.getEntitiesRemoved() > 0) {
            notificationBuilder
                    .append(Component.text(", including ", NamedTextColor.GRAY))
                    .append(Component.text(cycleStats.getEntitiesRemoved(), NamedTextColor.RED))
                    .append(Component.text(" removed.", NamedTextColor.GRAY));
        } else {
            notificationBuilder.append(Component.text(".", NamedTextColor.GRAY));
        }
        Component message = notificationBuilder.hoverEvent(HoverEvent.showText(cycleStats.getBreakdown())).build();

        for (UUID uuid : notifiableUsers) {
            if (uuid.equals(CONSOLE_UUID)) {
                Bukkit.getConsoleSender().sendMessage(message);
                continue;
            }
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.hasPermission("farmcontrol.command.notify")) {
                player.sendMessage(message);
            }
        }
    }

    public void setNotifiable(CommandSender sender, boolean notifiable) {
        UUID uuid;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        } else {
            uuid = CONSOLE_UUID;
        }
        if (notifiable) {
            notifiableUsers.add(uuid);
        } else {
            notifiableUsers.remove(uuid);
        }
        save();
    }

    public boolean isNotifiable(CommandSender sender) {
        UUID uuid;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        } else {
            uuid = CONSOLE_UUID;
        }
        return notifiableUsers.contains(uuid);
    }

    public synchronized void load() {
        notifiableUsers.clear();
        File userFile = new File(farmControl.getDataFolder(), "notifiable-users.txt");
        if (!userFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            for(String line; (line = reader.readLine()) != null; ) {
                if (!line.isEmpty()) {
                    notifiableUsers.add(UUID.fromString(line));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            farmControl.getSLF4JLogger().warn("Unable to load notifiable-users.txt", e);
        }
    }

    private synchronized void save() {
        final Set<UUID> users = new HashSet<>(notifiableUsers);
        final File userFile = new File(farmControl.getDataFolder(), "notifiable-users.txt");
        executorService.submit(() -> {
            if (!userFile.exists()) {
                try {
                    userFile.createNewFile();
                } catch (IOException e) {
                    farmControl.getSLF4JLogger().warn("Unable to create notifiable-users.txt", e);
                    return;
                }
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(userFile, false))) {
                for (UUID uuid : users) {
                    writer.println(uuid.toString());
                }
            } catch (IOException e) {
                farmControl.getSLF4JLogger().warn("Unable to save notifiable-users.txt", e);
            }
        });
    }

}

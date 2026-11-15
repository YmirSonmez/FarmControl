package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FarmControlCommand {
    private static final String BASE_PERMISSION = "farmcontrol.command.farmcontrol";
    private static final String RELOAD_PERMISSION = "farmcontrol.command.reload";
    private static final String STATUS_PERMISSION = "farmcontrol.command.status";
    private static final String HISTORY_PERMISSION = "farmcontrol.command.history";
    private static final String NOTIFY_PERMISSION = "farmcontrol.command.notify";
    private final FarmControl farmControl;

    private final ReloadCommand reloadCommand;
    private final StatusCommand statusCommand;
    private final HistoryCommand historyCommand;
    private final NotifyCommand notifyCommand;

    public FarmControlCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
        reloadCommand = new ReloadCommand(farmControl);
        statusCommand = new StatusCommand(farmControl);
        historyCommand = new HistoryCommand(farmControl);
        notifyCommand = new NotifyCommand(farmControl);
    }

    public void register() {
        farmControl.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(buildNode(), "Base command for FarmControl.", List.of("fc"))
        );
    }

    private LiteralCommandNode<CommandSourceStack> buildNode() {
        return Commands.literal("farmcontrol")
                .requires(hasPermission(BASE_PERMISSION))
                .executes(context -> {
                    String input = context.getInput();
                    int labelEnd = input.indexOf(' ');
                    String label = labelEnd < 0 ? input : input.substring(0, labelEnd);
                    sendHelp(context.getSource().getSender(), label.startsWith("/") ? label.substring(1) : label);
                    return Command.SINGLE_SUCCESS;
                })
                .then(simple("reload", RELOAD_PERMISSION, reloadCommand::execute))
                .then(simple("rl", RELOAD_PERMISSION, reloadCommand::execute))
                .then(status("status"))
                .then(status("stats"))
                .then(simple("history", HISTORY_PERMISSION, historyCommand::execute))
                .then(simple("notify", NOTIFY_PERMISSION, notifyCommand::execute))
                .build();
    }

    private LiteralArgumentBuilder<CommandSourceStack> simple(String name, String permission, Consumer<CommandSender> executor) {
        return Commands.literal(name)
                .requires(hasPermission(permission))
                .executes(context -> {
                    executor.accept(context.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> status(String name) {
        return Commands.literal(name)
                .requires(hasPermission(STATUS_PERMISSION))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    if (sender instanceof Player player) {
                        statusCommand.execute(sender, player.getWorld());
                    } else {
                        sender.sendMessage(Component.text("You must specify a world.", NamedTextColor.RED));
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("world", ArgumentTypes.world())
                        .executes(context -> {
                            statusCommand.execute(context.getSource().getSender(), context.getArgument("world", World.class));
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private static Predicate<CommandSourceStack> hasPermission(String permission) {
        return source -> source.getSender().hasPermission(permission);
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Component.text("FarmControl v" + farmControl.getPluginMeta().getVersion(), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("GitHub: ", NamedTextColor.YELLOW).append(Component.text("https://github.com/froobynooby/FarmControl/", NamedTextColor.WHITE)));
        sender.sendMessage(Component.empty());
        if (sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(Component.text("/" + label + " reload"));
        }
        if (sender.hasPermission(STATUS_PERMISSION)) {
            sender.sendMessage(Component.text("/" + label + " status " + (sender instanceof Player ? "[world]" : "<world>")));
        }
        if (sender.hasPermission(HISTORY_PERMISSION)) {
            sender.sendMessage(Component.text("/" + label + " history"));
        }
        if (sender.hasPermission(NOTIFY_PERMISSION)) {
            sender.sendMessage(Component.text("/" + label + " notify"));
        }
    }

}

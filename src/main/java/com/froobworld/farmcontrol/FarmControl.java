package com.froobworld.farmcontrol;

import com.froobworld.farmcontrol.command.FarmControlCommand;
import com.froobworld.farmcontrol.config.FcConfig;
import com.froobworld.farmcontrol.controller.*;
import com.froobworld.farmcontrol.controller.action.RemoveRandomMovementAction;
import com.froobworld.farmcontrol.message.MessageManager;
import com.froobworld.farmcontrol.metrics.FcMetrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;

public class FarmControl extends JavaPlugin {
    private FcConfig fcConfig;
    private HookManager hookManager;
    private ActionManager actionManager;
    private TriggerManager triggerManager;
    private ProfileManager profileManager;
    private ExclusionManager exclusionManager;
    private FarmController farmController;
    private MessageManager messageManager;

    public void onEnable() {
        this.fcConfig = new FcConfig(this);
        try {
            fcConfig.load();
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to load config.yml", e);
        }
        hookManager = new HookManager(this);
        hookManager.load();
        actionManager = new ActionManager();
        actionManager.addDefaults(this);
        triggerManager = new TriggerManager();
        triggerManager.addDefaults(this);
        profileManager = new ProfileManager(this);
        try {
            profileManager.load();
        } catch (IOException e) {
            getSLF4JLogger().error("Failed to load profiles.yml", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        exclusionManager = new ExclusionManager(this);
        messageManager = new MessageManager(this);
        try {
            messageManager.reload();
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to load messages.yml", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        farmController = new FarmController(this);
        farmController.load();
        farmController.register();

        registerCommands();

        new FcMetrics(this, 9692);
        hookManager.getSchedulerHook().runRepeatingTask(() -> RemoveRandomMovementAction.cleanUp(this), 1200, 1200);
    }

    public void reload() throws Exception {
        fcConfig.load();
        hookManager.reload();
        farmController.unRegister();
        profileManager.reload();
        messageManager.reload();
        farmController.reload();
        farmController.register();

    }

    public void onDisable() {
        if (farmController != null) {
            farmController.unRegister();
            farmController.unload();
        }
        RemoveRandomMovementAction.cleanUp(this);
    }

    public FcConfig getFcConfig() {
        return fcConfig;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public FarmController getFarmController() {
        return farmController;
    }

    public ExclusionManager getExclusionManager() {
        return exclusionManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void registerCommands() {
        FarmControlCommand farmControlCommand = new FarmControlCommand(this);
        PluginCommand command = Objects.requireNonNull(getCommand("farmcontrol"));
        command.setExecutor(farmControlCommand);
        command.setTabCompleter(farmControlCommand.getTabCompleter());
        command.setPermission("farmcontrol.command.farmcontrol");
    }
}

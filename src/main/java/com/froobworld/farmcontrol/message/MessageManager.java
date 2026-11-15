package com.froobworld.farmcontrol.message;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.message.config.MessagesConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MessageManager {
    private final FarmControl farmControl;
    private MessagesConfig messagesConfig;

    public MessageManager(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    public void reload() throws Exception {
        MessagesConfig newMessagesConfig = new MessagesConfig(farmControl);
        newMessagesConfig.load();
        messagesConfig = newMessagesConfig;
    }

    public void sendMessage(Player player, String messageKey) {
        if (messagesConfig == null) {
            throw new IllegalStateException("Messages have not been loaded");
        }
        player.sendMessage(getMessage(messageKey));
    }

    private Component getMessage(String messageKey) {
        if (messageKey.equalsIgnoreCase("breeding-disabled")) {
            return messagesConfig.breedingDisabled.get();
        }
        throw new IllegalArgumentException("No message for key '" + messageKey + "'");
    }

}

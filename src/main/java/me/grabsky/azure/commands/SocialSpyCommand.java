package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.AzureKeys;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.azure.storage.objects.JsonPlayer;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;

public class SocialSpyCommand extends BaseCommand {
    private final PlayerDataManager data;

    public SocialSpyCommand(Azure instance) {
        super("socialspy", List.of("ss"), "azure.command.socialspy", ExecutorType.PLAYER);
        this.data = instance.getDataManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.onDefault(sender);
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        final Player player = (Player) sender;
        final JsonPlayer jsonPlayer = data.getOnlineData((Player) sender);
        if (!jsonPlayer.getSocialSpy()) {
            // Enabling social spy
            player.getPersistentDataContainer().set(AzureKeys.SOCIAL_SPY, PersistentDataType.BYTE, (byte) 1);
            jsonPlayer.setSocialSpy(true);
            AzureLang.send(sender, AzureLang.SOCIAL_SPY_ON);
            return;
        }
        // Disabling social spy
        player.getPersistentDataContainer().set(AzureKeys.SOCIAL_SPY, PersistentDataType.BYTE, (byte) 0);
        jsonPlayer.setSocialSpy(false);
        AzureLang.send(sender, AzureLang.SOCIAL_SPY_OFF);
    }
}
package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.utils.Numbers;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SpeedCommand extends BaseCommand {
    private final Azure instance;

    public SpeedCommand(Azure instance) {
        super("speed", null, "azure.command.speed", ExecutorType.PLAYER);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (index == 0) return List.of("1");
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else {
            this.onSpeed(sender, args[0]);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        final Player executor = (Player) sender;
        executor.setFlySpeed(1);
    }

    @SubCommand
    public void onSpeed(CommandSender sender, String val) {
        final Player player = (Player) sender;
        final Float speed = Numbers.parseFloat(val);
        if (speed != null && speed >= 0 && speed <= 1) {
            if (player.isFlying()) {
                player.setFlySpeed(speed);
                AzureLang.send(sender, AzureLang.FLY_SPEED_SET);
                return;
            }
            player.setWalkSpeed(speed);
            AzureLang.send(sender, AzureLang.FLY_SPEED_SET);
            return;
        }
        AzureLang.send(sender, Global.INVALID_NUMBER);
    }
}

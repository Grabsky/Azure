package cloud.grabsky.azure;

import net.pl3x.guithium.api.Key;
import net.pl3x.guithium.api.gui.Screen;
import net.pl3x.guithium.api.gui.element.Button;
import net.pl3x.guithium.api.gui.element.Textbox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class GuithiumCommand extends Command {

    private static final Key TEST_KEY = Key.of("plugin:test_screen");
    public GuithiumCommand(final Plugin plugin) {
        super("guithium", "DESCRIPTION", "USAGE", new ArrayList<>());
        // Registering command to the server.
        plugin.getServer().getCommandMap().register("guithium", this);
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String[] args) {
        // Escaping the command block if command is not executed by a player.
        if (sender instanceof Player == false)
            return false;

        // Creating new screen and adding contents...
        final Screen screen = Screen.builder(TEST_KEY).build();

        screen.addElement(Screen.GRADIENT_BACKGROUND);

        screen.addElement(Textbox.builder(TEST_KEY + "/textbox_0")
                .setOffset(0.5F, 0.5F)
                .setAnchor(0.5F, 0.35F)
                .setSize(160, 20)
                // Line below is part of the mentioned PR, it allows us to verify that textboxes work even when rendered inproperly.
                .onChange((aScreen, aTextbox, aPlayer, value) -> aPlayer.<Player>unwrap().sendPlainMessage(aTextbox.getKey() + ": " + value))
                .build()
        );

        screen.addElement(Textbox.builder(TEST_KEY + "/textbox_1")
                .setOffset(0.5F, 0.5F)
                .setAnchor(0.5F, 0.45F)
                .setSize(160, 20)
                // Line below is part of the mentioned PR, it allows us to verify that textboxes work even when rendered inproperly.
                .onChange((aScreen, aTextbox, aPlayer, value) -> aPlayer.<Player>unwrap().sendPlainMessage(aTextbox.getKey() + ": " + value))
                .build()
        );

        screen.addElement(Textbox.builder(TEST_KEY + "/textbox_2")
                .setOffset(0.5F, 0.5F)
                .setAnchor(0.5F, 0.55F)
                .setSize(160, 20)
                // Line below is part of the mentioned PR, it allows us to verify that textboxes work even when rendered inproperly.
                .onChange((aScreen, aTextbox, aPlayer, value) -> aPlayer.<Player>unwrap().sendPlainMessage(aTextbox.getKey() + ": " + value))
                .build()
        );

        screen.addElement(Button.builder(TEST_KEY + "/button_0")
                .setOffset(0.5F, 0.5F)
                .setAnchor(0.5F, 0.65F)
                .setLabel("Button")
                .onClick((aScreen, button, player) -> {
                    button.setLabel("Modified Button");
                    button.send(player);
                })
                .build()
        );

        // Opening screen to the sender.
        screen.open(sender);

        // Escaping the command block.
        return true;
    }

}
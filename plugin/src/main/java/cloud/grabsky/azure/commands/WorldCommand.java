package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.RootCommandInput;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static java.lang.String.valueOf;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

public final class WorldCommand extends RootCommand {

    public WorldCommand() {
        super("world", null, "azure.command.world", "/world [args]", "Manage your worlds in-game.");
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        final RootCommandInput input = context.getInput();
        // ...
        if (index == 0) return CompletionsProvider.of(
                Stream.of("create", "delete", "gamerule", "info", "teleport").filter(literal -> sender.hasPermission(this.getPermission() + "." + literal) == true).toList()
        );
        // ...
        final String literal = input.at(1).toLowerCase();
        if (sender.hasPermission(this.getPermission() + "." + literal) == false)
            return CompletionsProvider.EMPTY;
        // ...
        return switch (literal) {
            case "create" -> switch (index) {
                case 2 -> CompletionsProvider.of(World.Environment.class);
                case 3 -> CompletionsProvider.of(WorldType.class);
                case 4 -> CompletionsProvider.of("natural");
                case 5 -> (context.getExecutor().isPlayer() == true)
                        ? CompletionsProvider.of(valueOf(context.getExecutor().asPlayer().getWorld().getSeed()))
                        : CompletionsProvider.EMPTY;
                default -> CompletionsProvider.EMPTY;
            };
            case "delete" -> (index == 1)
                    ? CompletionsProvider.of(World.class)
                    : CompletionsProvider.of("--confirm");
            case "gamerule" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of(GameRule.class);
                case 3 -> {
                    final GameRule<?> gameRule = GameRule.getByName(input.at(3));
                    yield (gameRule != null)
                            ? CompletionsProvider.of(gameRule.getType())
                            : CompletionsProvider.EMPTY;
                }
                default -> CompletionsProvider.EMPTY;
            };
            case "info" -> (index == 1)
                    ? CompletionsProvider.of(World.class)
                    : CompletionsProvider.EMPTY;
            case "teleport" -> switch (index) {
                case 1 -> CompletionsProvider.of(Player.class);
                case 2 -> CompletionsProvider.of(World.class);
                default -> CompletionsProvider.EMPTY;
            };
            // ...
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false) {
            sendMessage(context.getExecutor().asCommandSender(), PluginLocale.COMMAND_WORLD_HELP);
        } else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            default -> sendMessage(context.getExecutor().asCommandSender(), PluginLocale.COMMAND_WORLD_HELP);
            case "create" -> this.onWorldCreate(context, arguments);
            case "teleport" -> this.onWorldTeleport(context, arguments);
            case "delete" -> this.onWorldDelete(context, arguments);
            case "info" -> this.onWorldInfo(context, arguments);
            case "gamerule" -> this.onWorldGamerule(context, arguments);
        }
    }

    private void onWorldCreate(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + "." + "create") == true) {
            final NamespacedKey key = arguments.next(NamespacedKey.class).asRequired();
            final World.Environment environment = arguments.next(World.Environment.class).asRequired();
            final WorldType type = arguments.next(WorldType.class).asRequired();
            final String generator = arguments.next(String.class).asRequired();
            final Long seed = arguments.next(Long.class).asOptional(null);
            // ...
            if (new File(Bukkit.getWorldContainer(), key.getKey()).exists() == false && Bukkit.getWorld(key) == null) {
                final WorldCreator creator = new WorldCreator(key);
                // ...
                creator.environment(environment);
                creator.type(type);
                creator.generator(generator);
                if (seed != null)
                    creator.seed(seed);
                // ...
                final World world = Bukkit.createWorld(creator);
                // ...
                sendMessage(sender, PluginLocale.COMMAND_WORLD_CREATE_SUCCESS, unparsed("world", world.key().asString()));
                return;
            }
            sendMessage(sender, PluginLocale.COMMAND_WORLD_CREATE_FAILURE_ALREADY_EXISTS, unparsed("world", key.asString()));
            return;
        }
        sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
    }

    private void onWorldTeleport(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + "." + "teleport") == true) {
            final Player target = arguments.next(Player.class).asRequired();
            final World world = arguments.next(World.class).asRequired();
            // ...
            target.teleportAsync(world.getSpawnLocation());
            sendMessage(sender, PluginLocale.COMMAND_WORLD_TELEPORT,
                    unparsed("player", target.getName()),
                    unparsed("world", world.key().asString())
            );
            return;
        }
        sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
    }

    private void onWorldDelete(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + "." + "delete") == true) {
            final World world = arguments.next(World.class).asRequired();
            final String[] flags = arguments.next(String.class, StringArgument.GREEDY).asOptional("").split(" ");
            // ...
            if (containsIgnoreCase(flags, "--confirm") == true) {
                // teleporting players away from the world
                for (final Player player : world.getPlayers()) {
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }
                // unloading the world (no save action is performed)
                Bukkit.unloadWorld(world, false);
                // deleting world directory
                if (world != Bukkit.getWorlds().get(0) && deleteDirectory(world.getWorldFolder()) == true) {
                    sendMessage(sender, PluginLocale.COMMAND_WORLD_DELETE_SUCCESS, unparsed("world", world.key().asString()));
                    return;
                }
                sendMessage(sender, PluginLocale.COMMAND_WORLD_DELETE_FAILURE, unparsed("world", world.key().asString()));
                return;
            }
            sendMessage(sender, PluginLocale.COMMAND_WORLD_DELETE_CONFIRM,
                    unparsed("input", context.getInput().toString()),
                    unparsed("world", world.key().asString())
            );
            return;
        }
        sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
    }

    private void onWorldInfo(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + "." + "info") == true) {
            final World world = arguments.next(World.class).asOptional(context.getExecutor().asPlayer().getWorld());
            // ...
            final Location spawnLoc = world.getSpawnLocation();
            // ...
            sendMessage(sender, PluginLocale.COMMAND_WORLD_INFO,
                    unparsed("world_name", world.getName()),
                    unparsed("world_key", world.getKey().asString()),
                    unparsed("world_environment", world.getEnvironment().name()),
                    unparsed("world_online", valueOf(world.getPlayerCount())),
                    unparsed("spawn_x", valueOf(spawnLoc.x())),
                    unparsed("spawn_y", valueOf(spawnLoc.y())),
                    unparsed("spawn_z", valueOf(spawnLoc.z()))
            );
            return;
        }
        sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
    }

    private void onWorldGamerule(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + "." + "gamerule") == true) {
            final World world = arguments.next(World.class).asRequired();
            final GameRule<Object> gameRule = arguments.next(GameRule.class).asRequired();
            final Object value = arguments.next(gameRule.getType()).asOptional();
            // ...
            if (value == null) {
                sendMessage(sender, PluginLocale.COMMAND_WORLD_GAMERULE_VIEW, unparsed("rule", gameRule.getName()), unparsed("value", world.getGameRuleValue(gameRule).toString()));
                return;
            }
            // ...
            if (world.setGameRule(gameRule, value) == true) {
                sendMessage(sender, PluginLocale.COMMAND_WORLD_GAMERULE_SUCCESS, unparsed("rule", gameRule.getName()), unparsed("value", value.toString()));
                return;
            }
            sendMessage(sender, PluginLocale.COMMAND_WORLD_GAMERULE_FAILURE, unparsed("rule", gameRule.getName()));
            return;
        }
        sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
    }

    private static boolean containsIgnoreCase(final String[] arr, final String search) {
        for (final String element : arr) {
            if (search.equalsIgnoreCase(element) == true)
                return true;
        }
        return false;
    }

    private static boolean deleteDirectory(final File file) {
        try (final Stream<Path> walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

}

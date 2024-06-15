/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.commands.arguments.DirectionArgument;
import cloud.grabsky.azure.commands.arguments.WorldSeedArgument;
import cloud.grabsky.azure.commands.arguments.WorldTimeArgument;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.world.AzureWorldManager;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.RootCommandInput;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import io.papermc.paper.math.Position;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import static cloud.grabsky.azure.world.AzureWorldManager.WorldOperationException;
import static cloud.grabsky.azure.world.AzureWorldManager.WorldOperationException.Reason;
import static cloud.grabsky.bedrock.helpers.Conditions.inRange;
import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static java.lang.String.format;

@Command(name = "world", permission = "azure.command.world", usage = "/world (...)")
public final class WorldCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;

    @Dependency
    private @UnknownNullability AzureWorldManager worlds;

    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("#,###.##");


    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        final RootCommandInput input = context.getInput();
        // Returning list of sub-commands when no argument was specified in the input.
        if (index == 0) return CompletionsProvider.of(
                Stream.of("autoload", "border", "create", "delete", "description", "gamerule", "import", "info", "list", "load", "spawnpoint", "teleport", "time", "unload", "weather")
                        .filter(literal -> sender.hasPermission(this.getPermission() + "." + literal) == true)
                        .toList()
        );
        // Getting the first literal (argument) of user input.
        final String literal = input.at(1, "").toLowerCase();
        // Returning empty completions provider when missing permission for that literal.
        if (sender.hasPermission(this.getPermission() + "." + literal) == false)
            return CompletionsProvider.EMPTY;
        // Returning sub-command-aware completions provider.
        return switch (literal) {
            case "autoload" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of(Boolean.class);
                default -> CompletionsProvider.EMPTY;
            };
            case "border" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of("@x @y @z");
                case 3 -> CompletionsProvider.of("@y @z");
                case 4 -> CompletionsProvider.of("@z");
                case 5 -> CompletionsProvider.of("-1", "1000", "2500", "5000");
                default -> CompletionsProvider.EMPTY;
            };
            case "create" -> switch (index) {
                case 2 -> CompletionsProvider.of(World.Environment.class);
                case 3 -> CompletionsProvider.of(WorldType.class);
                case 4 -> WorldSeedArgument.INSTANCE;
                default -> CompletionsProvider.EMPTY;
            };
            case "delete" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of("--confirm");
                default -> CompletionsProvider.EMPTY;
            };
            case "description" -> (index == 1) ? CompletionsProvider.of(World.class) : CompletionsProvider.EMPTY;
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
            case "import" -> switch (index) {
                case 1 -> {
                    final File[] files = plugin.getServer().getWorldContainer().listFiles();
                    // ...
                    if (files == null)
                        yield CompletionsProvider.EMPTY;
                    // ...
                    yield CompletionsProvider.of(Stream.of(files)
                            .filter(dir -> dir.isDirectory() == true && new File(dir, "level.dat").exists() == true && plugin.getServer().getWorld(dir.getName()) == null)
                            .map(File::getName)
                            .filter(name -> plugin.getServer().getWorld(name) == null)
                            .toList()
                    );
                }
                case 2 -> CompletionsProvider.of(World.Environment.class);
                default -> CompletionsProvider.EMPTY;
            };
            case "info" -> (index == 1) ? CompletionsProvider.of(World.class) : CompletionsProvider.EMPTY;
            case "load" -> switch (index) {
                case 1 -> {
                    final File[] files = plugin.getServer().getWorldContainer().listFiles();
                    // ...
                    if (files == null)
                        yield CompletionsProvider.EMPTY;
                    // ...
                    yield CompletionsProvider.of(Stream.of(files)
                            .filter(dir -> dir.isDirectory() == true && new File(dir, "level.dat").exists() == true)
                            .map(File::getName)
                            .filter(name -> plugin.getServer().getWorld(name) == null)
                            .toList()
                    );
                }
                default -> CompletionsProvider.EMPTY;
            };
            case "spawnpoint" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of("@x @y @z");
                case 3 -> CompletionsProvider.of("@y @z");
                case 4 -> CompletionsProvider.of("@z");
                case 5 -> CompletionsProvider.of("@yaw @pitch");
                case 6 -> CompletionsProvider.of("@pitch");
                default -> CompletionsProvider.EMPTY;
            };
            case "teleport" -> switch (index) {
                case 1 -> CompletionsProvider.of(Player.class);
                case 2 -> CompletionsProvider.of(World.class);
                default -> CompletionsProvider.EMPTY;
            };
            case "time" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> WorldTimeArgument.INSTANCE;
                default -> CompletionsProvider.EMPTY;
            };
            case "unload" -> (index == 1) ? CompletionsProvider.of(World.class) : CompletionsProvider.EMPTY;
            case "weather" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 ->
                        CompletionsProvider.of("clear", "rain", "thunder"); // There is no weather "registry" or anything like that.
                default -> CompletionsProvider.EMPTY;
            };
            // No completions by default.
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false) {
            Message.of(PluginLocale.COMMAND_WORLD_HELP).send(context.getExecutor());
        } else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            case "autoload"     -> this.onWorldAutoload(context, arguments);
            case "border"       -> this.onWorldBorder(context, arguments);
            case "create"       -> this.onWorldCreate(context, arguments);
            case "delete"       -> this.onWorldDelete(context, arguments);
            case "description"  -> this.onWorldDescription(context, arguments);
            case "gamerule"     -> this.onWorldGamerule(context, arguments);
            case "import"       -> this.onWorldImport(context, arguments);
            case "info"         -> this.onWorldInfo(context, arguments);
            case "list"         -> this.onWorldList(context, arguments);
            case "load"         -> this.onWorldLoad(context, arguments);
            case "spawnpoint"   -> this.onWorldSpawnPoint(context, arguments);
            case "teleport"     -> this.onWorldTeleport(context, arguments);
            case "time"         -> this.onWorldTime(context, arguments);
            case "unload"       -> this.onWorldUnload(context, arguments);
            case "weather"      -> this.onWorldWeather(context, arguments);
            // Showing help page when invalid argument is provided.
            default -> Message.of(PluginLocale.COMMAND_WORLD_HELP).send(context.getExecutor());
        }
    }


    /* WORLD AUTOLOAD */

    private static final ExceptionHandler.Factory WORLD_AUTOLOAD_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_AUTOLOAD_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldAutoload(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".autoload") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_AUTOLOAD_USAGE);
            final boolean state = arguments.next(Boolean.class).asRequired(WORLD_AUTOLOAD_USAGE);
            // Trying to unload the world...
            worlds.setAutoLoad(world, state);
            Message.of(state == true ? PluginLocale.COMMAND_WORLD_AUTOLOAD_SUCCESS_ON : PluginLocale.COMMAND_WORLD_AUTOLOAD_SUCCESS_OFF).placeholder("world", world).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD BORDER */

    private static final ExceptionHandler.Factory WORLD_BORDER_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_BORDER_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    @SuppressWarnings("UnstableApiUsage")
    private void onWorldBorder(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".border") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_BORDER_USAGE);
            final Position position = arguments.next(Position.class).asRequired(WORLD_BORDER_USAGE);
            final float radius = arguments.next(Float.class).asRequired(WORLD_BORDER_USAGE);
            // When radius is lower than 0, border is removed.
            if (radius < 0) {
                // Resetting the world border.
                world.getWorldBorder().reset();
                // Sending message to the sender.
                Message.of(PluginLocale.COMMANDS_WORLD_BORDER_RESET).placeholder("world", world).send(sender);
                return;
            }
            // Setting the new border.
            world.getWorldBorder().setCenter(position.blockX() + 0.5, position.blockZ() + 0.5);
            world.getWorldBorder().setSize(radius * 2);
            System.out.println(radius * 2);
            // Sending message to the sender.
            Message.of(PluginLocale.COMMAND_WORLD_BORDER_SET_SUCCESS).placeholder("world", world).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD CREATE */

    private static final ExceptionHandler.Factory WORLD_CREATE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_CREATE_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldCreate(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".create") == true) {
            final NamespacedKey key = arguments.next(NamespacedKey.class).asRequired(WORLD_CREATE_USAGE);
            final World.Environment environment = arguments.next(World.Environment.class).asRequired(WORLD_CREATE_USAGE);
            final WorldType type = arguments.next(WorldType.class).asRequired(WORLD_CREATE_USAGE);
            final long seed = arguments.next(Long.class, WorldSeedArgument.INSTANCE).asRequired(WORLD_CREATE_USAGE);
            // Trying to create a new world...
            try {
                final World world = plugin.getWorldManager().createWorld(key, environment, type, seed);
                // Sending success message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_CREATE_SUCCESS).placeholder("world", world).send(sender);
            } catch (final WorldOperationException e) {
                // Sending reason-specific error message to command sender.
                if (e.getReason() == Reason.ALREADY_EXISTS)
                    Message.of(PluginLocale.COMMAND_WORLD_CREATE_FAILURE_ALREADY_EXISTS).placeholder("world", key).send(sender);
                else if (e.getReason() == Reason.OTHER)
                    Message.of(PluginLocale.COMMAND_WORLD_CREATE_FAILURE_OTHER).placeholder("world", key).send(sender);
                // Catch any RuntimeException, I'm tired of guessing what exceptions Bukkit can throw...
            } catch (final RuntimeException e) {
                e.printStackTrace();
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_CREATE_FAILURE_OTHER).placeholder("world", key).send(sender);
            }
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD DELETE */

    private static final ExceptionHandler.Factory WORLD_DELETE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_DELETE_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldDelete(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".delete") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_DELETE_USAGE);
            final boolean isConfirm = arguments.next(String.class).asOptional("--no-confirm").equalsIgnoreCase("--confirm");
            // Checking if --confirm flag is present.
            if (isConfirm == true) {
                // Trying to delete the world...
                try {
                    // Deleting the world. This method disallow deleting primary world.
                    if (worlds.deleteWorld(world) == true) {
                        // Sending success message to command sender.
                        Message.of(PluginLocale.COMMAND_WORLD_DELETE_SUCCESS).placeholder("world", world).send(sender);
                        return;
                    }
                    // Sending error message to command sender.
                    Message.of(PluginLocale.COMMAND_WORLD_DELETE_FAILURE_OTHER).placeholder("world", world).send(sender);
                } catch (final WorldOperationException e) {
                    // Sending reason-specific error message to command sender.
                    if (e.getReason() == Reason.PRIMARY_WORLD)
                        Message.of(PluginLocale.COMMAND_WORLD_DELETE_FAILURE_PRIMARY_WORLD).send(sender);
                }
                return;
            }
            // Sending confirmation message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_DELETE_CONFIRM).replace("<input>", context.getInput().toString()).placeholder("world", world).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD DESCRIPTION */

    private static final ExceptionHandler.Factory WORLD_DESCRIPTION_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_DESCRIPTION_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldDescription(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".description") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_DESCRIPTION_USAGE);
            final @Nullable String description = arguments.next(String.class, StringArgument.GREEDY).asOptional(null);
            // Making sure description length is between 3 and 32 characters.
            if (description != null && inRange(description.length(), 3, 32) == false) {
                // Sending failure message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_DESCRIPTION_SET_FAILURE_NOT_IN_RANGE).send(sender);
                return;
            }
            // Setting the description.
            worlds.setDescription(world, description);
            // Sending success message to command sender.
            Message.of(description != null ? PluginLocale.COMMAND_WORLD_DESCRIPTION_SET_SUCCESS : PluginLocale.COMMAND_WORLD_DESCRIPTION_RESET_SUCCESS).placeholder("world", world).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD GAMERULE */

    private static final ExceptionHandler.Factory WORLD_GAMERULE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_GAMERULE_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    @SuppressWarnings("unchecked")
    private void onWorldGamerule(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".gamerule") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_GAMERULE_USAGE);
            final GameRule<Object> gameRule = arguments.next(GameRule.class).asRequired(WORLD_GAMERULE_USAGE);
            final Object value = arguments.next(gameRule.getType()).asOptional();
            // ...
            if (value == null) {
                // Sending message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_GAMERULE_INFO).placeholder("rule", gameRule.getName()).placeholder("value", world.getGameRuleValue(gameRule)).send(sender);
                return;
            } else if (world.setGameRule(gameRule, value) == true) {
                // Sending success message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_GAMERULE_SET_SUCCESS).placeholder("rule", gameRule.getName()).placeholder("value", value).send(sender);
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_GAMERULE_SET_FAILURE).placeholder("rule", gameRule.getName()).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD IMPORT */

    private static final ExceptionHandler.Factory WORLD_IMPORT_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_IMPORT_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldImport(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".import") == true) {
            final NamespacedKey key = arguments.next(NamespacedKey.class).asRequired(WORLD_IMPORT_USAGE);
            final World.Environment environment = arguments.next(World.Environment.class).asRequired(WORLD_IMPORT_USAGE);
            try {
                // Trying to load the world...
                plugin.getWorldManager().importWorld(key, environment);
                // Sending success message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_IMPORT_SUCCESS).placeholder("world", key).send(sender);
            } catch (final WorldOperationException e) {
                // Sending reason-specific error message to command sender.
                if (e.getReason() == Reason.DOES_NOT_EXIST)
                    Message.of(PluginLocale.COMMAND_WORLD_IMPORT_FAILURE_NOT_FOUND).placeholder("world", key).send(sender);
                else if (e.getReason() == Reason.OTHER)
                    Message.of(PluginLocale.COMMAND_WORLD_IMPORT_FAILURE_OTHER).placeholder("world", key).send(sender);
                // Catch any RuntimeException, I'm tired of guessing what exceptions Bukkit can throw...
            } catch (final RuntimeException e) {
                Message.of(PluginLocale.COMMAND_WORLD_IMPORT_FAILURE_OTHER).placeholder("world", key).send(sender);
            }
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD INFO */

    private void onWorldInfo(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".info") == true) {
            final World world = arguments.next(World.class).asOptional(context.getExecutor().asPlayer().getWorld());
            // Getting spawn location of specified world.
            final Location spawnLoc = worlds.getSpawnPoint(world);
            final WorldBorder border = world.getWorldBorder();
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_INFO)
                    .placeholder("world_name", world.getName())
                    .placeholder("world_key", world)
                    .placeholder("world_autoload", PluginLocale.getBooleanShort(worlds.getAutoLoad(world)))
                    .placeholder("world_seed", world.getSeed())
                    .placeholder("world_environment", world.getEnvironment().name())
                    .placeholder("world_online", world.getPlayerCount())
                    .placeholder("spawn_x", COORD_FORMAT.format(spawnLoc.x()))
                    .placeholder("spawn_y", COORD_FORMAT.format(spawnLoc.y()))
                    .placeholder("spawn_z", COORD_FORMAT.format(spawnLoc.z()))
                    .placeholder("border_x", COORD_FORMAT.format(border.getCenter().x()))
                    .placeholder("border_z", COORD_FORMAT.format(border.getCenter().z()))
                    .placeholder("border_size", COORD_FORMAT.format(border.getSize()))
                    .placeholder("border_radius", COORD_FORMAT.format(border.getSize() / 2))
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD LIST */

    private void onWorldList(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".list") == true) {
            Message.of(PluginLocale.COMMAND_WORLD_LIST_HEADER).placeholder("worlds_count", plugin.getServer().getWorlds().size()).send(sender);
            // ...
            plugin.getServer().getWorlds().forEach(world -> {
                final @Nullable String description = worlds.getDescription(world);
                // ...
                Message.of(description != null ? PluginLocale.COMMAND_WORLD_LIST_ENTRY : PluginLocale.COMMAND_WORLD_LIST_ENTRY_NO_DESCRIPTION)
                        .replace("<world_key>", world.key().asString())
                        .placeholder("description", requirePresent(description, "N/A"))
                        .send(sender);
            });
            Message.of(PluginLocale.COMMAND_WORLD_LIST_FOOTER).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD LOAD */

    private static final ExceptionHandler.Factory WORLD_LOAD_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_LOAD_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldLoad(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".load") == true) {
            final NamespacedKey key = arguments.next(NamespacedKey.class).asRequired(WORLD_LOAD_USAGE);
            try {
                // Trying to load the world...
                plugin.getWorldManager().loadWorld(key);
                // Sending success message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_LOAD_SUCCESS).placeholder("world", key).send(sender);
            } catch (final WorldOperationException e) {
                // Sending reason-specific error message to command sender.
                if (e.getReason() == Reason.DOES_NOT_EXIST)
                    Message.of(PluginLocale.COMMAND_WORLD_LOAD_FAILURE_NOT_FOUND).placeholder("world", key).send(sender);
                else if (e.getReason() == Reason.OTHER)
                    Message.of(PluginLocale.COMMAND_WORLD_LOAD_FAILURE_OTHER).placeholder("world", key).send(sender);
            } catch (final IOException e) {
                e.printStackTrace();
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_LOAD_FAILURE_OTHER).placeholder("world", key).send(sender);
                // Catch any RuntimeException, I'm tired of guessing what exceptions Bukkit can throw...
            } catch (final RuntimeException e) {
                Message.of(PluginLocale.COMMAND_WORLD_LOAD_FAILURE_OTHER).placeholder("world", key).send(sender);
            }

            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD SPAWNPOINT */

    private static final ExceptionHandler.Factory WORLD_SPAWNPOINT_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_SPAWNPOINT_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    @SuppressWarnings("UnstableApiUsage")
    private void onWorldSpawnPoint(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".spawnpoint") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_SPAWNPOINT_USAGE);
            final Position position = arguments.next(Position.class).asRequired(WORLD_SPAWNPOINT_USAGE);
            final DirectionArgument.Direction direction = arguments.next(DirectionArgument.Direction.class, DirectionArgument.INSTANCE).asOptional(null);
            // Creating location from specified position.
            final Location location = (direction == null)
                    ? new Location(world, position.x(), position.y(), position.z())
                    : new Location(world, position.x(), position.y(), position.z(), direction.getYaw(), direction.getPitch());
            // Updating spawn location.
            worlds.setSpawnPoint(world, location);
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_SPAWNPOINT_SET_SUCCESS)
                    .placeholder("x", format("%.2f", position.x()))
                    .placeholder("y", format("%.2f", position.y()))
                    .placeholder("z", format("%.2f", position.z()))
                    .send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD TELEPORT */

    private static final ExceptionHandler.Factory WORLD_TELEPORT_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_TELEPORT_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldTeleport(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".teleport") == true) {
            final Player target = arguments.next(Player.class).asRequired(WORLD_TELEPORT_USAGE);
            final World world = arguments.next(World.class).asRequired(WORLD_TELEPORT_USAGE);
            // Teleporting...
            target.teleportAsync(worlds.getSpawnPoint(world), TeleportCause.PLUGIN);
            // Sending success message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_TELEPORT_SUCCESS).placeholder("player", target).placeholder("world", world).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD TIME */

    private static final ExceptionHandler.Factory WORLD_TIME_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_TIME_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldTime(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".time") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_TIME_USAGE);
            final Long time = arguments.next(Long.class, WorldTimeArgument.INSTANCE).asOptional(null);
            // Sending current time message if new one is not specified.
            if (time == null) {
                // Sending (status) message to command sender.
                Message.of(PluginLocale.COMMAND_WORLD_TIME_INFO).placeholder("world", world).placeholder("time", world.getTime()).send(sender);
                return;
            }
            // Setting the time.
            world.setTime(time);
            // Sending success message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_TIME_SET_SUCCESS).placeholder("world", world).placeholder("time", world.getTime()).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD UNLOAD */

    private static final ExceptionHandler.Factory WORLD_UNLOAD_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_UNLOAD_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldUnload(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".unload") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_UNLOAD_USAGE);
            // Trying to unload the world...
            try {
                // Unloading the world. This method teleports all players to spawn of the primary world. Primary worlds cannot be unloaded.
                if (worlds.unloadWorld(world) == true) {
                    // Sending success message to command sender.
                    Message.of(PluginLocale.COMMAND_WORLD_UNLOAD_SUCCESS).placeholder("world", world).send(sender);
                    return;
                }
                Message.of(PluginLocale.COMMAND_WORLD_UNLOAD_FAILURE_OTHER).placeholder("world", world).send(sender);
                return;
            } catch (final WorldOperationException e) {
                // Sending reason-specific error message to command sender.
                if (e.getReason() == Reason.PRIMARY_WORLD)
                    Message.of(PluginLocale.COMMAND_WORLD_UNLOAD_FAILURE_PRIMARY_WORLD).send(sender);
            }
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WORLD WEATHER */

    private static final ExceptionHandler.Factory WORLD_WEATHER_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WORLD_WEATHER_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWorldWeather(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".weather") == true) {
            final World world = arguments.next(World.class).asRequired(WORLD_WEATHER_USAGE);
            final String weather = arguments.next(String.class).asRequired(WORLD_WEATHER_USAGE);
            // Changing the weather based on input...
            switch (weather.toLowerCase()) {
                case "clear" -> {
                    world.setStorm(false);
                    world.setThundering(false);
                }
                case "rain" -> {
                    world.setStorm(true);
                    world.setThundering(false);
                }
                case "thunder" -> {
                    world.setStorm(true);
                    world.setThundering(true);
                }
                default -> {
                    // Sending error message to command sender.
                    Message.of(PluginLocale.COMMAND_WORLD_WEATHER_SET_FAILURE_INVALID_TYPE).placeholder("input", weather).send(sender);
                    return;
                }
            }
            // Sending success message to command sender.
            Message.of(PluginLocale.COMMAND_WORLD_WEATHER_SET_SUCCESS).placeholder("world", world).placeholder("weather", weather).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}

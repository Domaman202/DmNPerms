package ru.DmN.perms;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.LiteralText;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer {
    public static Set<Permission> permissions = new LinkedHashSet<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            load();

            dispatcher.register(literal("permission_add").then(argument("name", StringArgumentType.word()).then(argument("parent", StringArgumentType.greedyString()).executes(context -> {
                addPermission(context.getArgument("name", String.class), context.getArgument("parent", String.class));
                save();
                return 1;
            }))));

            dispatcher.register(literal("permission_del").then(argument("name", StringArgumentType.word()).executes(context -> {
                for (var permission : permissions)
                    if (permission.name.equals(context.getArgument("name", String.class))) {
                        permissions.remove(permission);
                        break;
                    }
                save();
                return 1;
            })));

            dispatcher.register(literal("permission_addusr").then(argument("name", StringArgumentType.word()).then(argument("user", StringArgumentType.string()).executes(context -> {
                for (var permission : permissions)
                    if (permission.name.equals(context.getArgument("name", String.class))) {
                        permission.players.add(context.getArgument("user", String.class));
                        break;
                    }
                save();
                return 1;
            }))));

            dispatcher.register(literal("permission_delusr").then(argument("name", StringArgumentType.word()).then(argument("user", StringArgumentType.string()).executes(context -> {
                for (var permission : permissions)
                    if (permission.name.equals(context.getArgument("name", String.class))) {
                        permission.players.remove(context.getArgument("user", String.class));
                        break;
                    }
                save();
                return 1;
            }))));

            dispatcher.register(literal("permission_addcmd").then(argument("name", StringArgumentType.word()).then(argument("command", StringArgumentType.greedyString()).executes(context -> {
                for (var permission : permissions)
                    if (permission.name.equals(context.getArgument("name", String.class))) {
                        permission.commands.add(context.getArgument("command", String.class));
                        break;
                    }
                save();
                return 1;
            }))));

            dispatcher.register(literal("permission_delcmd").then(argument("name", StringArgumentType.word()).then(argument("command", StringArgumentType.greedyString()).executes(context -> {
                for (var permission : permissions)
                    if (permission.name.equals(context.getArgument("name", String.class))) {
                        permission.commands.remove(context.getArgument("command", String.class));
                        break;
                    }
                save();
                return 1;
            }))));

            dispatcher.register(literal("permission_list").executes(context -> {
                var src = context.getSource();
                src.sendFeedback(new LiteralText("??CPerms list:"), false);
                for (var permission : permissions) {
                    src.sendFeedback(new LiteralText("??1>").append("??2" + permission.name + "\n??3<parent>??5" + permission.parent), false);
                    var sb = new StringBuilder();
                    for (var user : permission.players)
                        sb.append("??3<usr>??5").append(user).append('\n');
                    for (var cmd : permission.commands)
                        sb.append("??3<cmd>??6").append(cmd).append('\n');
                    src.sendFeedback(new LiteralText(sb.toString()), false);
                }
                return 1;
            }));
        });
    }

    public static void addPermission(String name, String parent) {
        for (var permission : permissions)
            if (permission.name.equals(name))
                return;
        permissions.add(new Permission(name, parent));
    }

    public static boolean checkAccess(String command, Permission permission) {
        for (var cmd : permission.commands)
            if (command.startsWith(cmd))
                return true;
        return false;
    }

    public static boolean checkAccess(String user, String command, Permission permission, Set<Permission> permissions, ArrayList<String> blacklist) {
        if (permission.players.contains(user) && checkAccess(command, permission))
            return true;
        if (!Objects.equals(permission.parent, "#"))
            for (var parent : permissions)
                if (Objects.equals(permission.parent, parent.name) && !blacklist.contains(parent.name)) {
                    blacklist.add(parent.name);
                    return checkAccess(user, command, parent, permissions, blacklist);
                }
        return false;
    }

    public static void save() {
        try (var file = new ObjectOutputStream(new FileOutputStream("perms_hash.data"))) {
            file.writeObject(permissions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try (var file = new ObjectInputStream(new FileInputStream("perms_hash.data"))) {
            permissions = (LinkedHashSet<Permission>) file.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
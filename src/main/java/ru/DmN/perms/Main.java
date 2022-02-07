package ru.DmN.perms;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.LiteralText;

import java.io.*;
import java.util.ArrayList;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer {
    public static ArrayList<Permission> permissions = new ArrayList<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            load();

            dispatcher.register(literal("permission_add").then(argument("name", StringArgumentType.word()).then(argument("parent", StringArgumentType.greedyString()).executes(context -> {
                if (context.getArgument("parent", String.class).equals("#"))
                    addPermission(context.getArgument("name", String.class), null);
                else
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
                src.sendFeedback(new LiteralText("§CPerms list:"), false);
                for (var permission : permissions) {
                    src.sendFeedback(new LiteralText("§1>").append("§2" + permission.name), false);
                    var sb = new StringBuilder();
                    for (var user : permission.players)
                        sb.append("§3<usr>§5").append(user).append('\n');
                    for (var cmd : permission.commands)
                        sb.append("§3<cmd>§6").append(cmd).append('\n');
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

    public static boolean checkContains(String command, Permission permission) {
        if (permission.commands.contains(command))
            return true;
        for (var cmd : permission.commands)
            if (command.startsWith(cmd))
                return true;
        return false;
    }

    public static boolean checkAccess(String name, Permission permission, ArrayList<Permission> permissions) {
        if (permission.players.contains(name))
            return true;
        if (permission.parent != null)
            for (var parent : permissions)
                if (permission.parent.equals(parent.name))
                    return checkAccess(name, parent, permissions);
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
            permissions = (ArrayList<Permission>) file.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
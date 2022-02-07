package ru.DmN.perms;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer {
    public static ArrayList<Permission> permissions = new ArrayList<>();

    @Override
    public void onInitialize() {
        permissions.add(new Permission("_", null));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            load();

            dispatcher.register(literal("permission_add").then(argument("name", StringArgumentType.word()).then(argument("parent", StringArgumentType.word()).executes(context -> {
                permissions.add(new Permission(context.getArgument("name", String.class), context.getArgument("parent", String.class)));
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
        });
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
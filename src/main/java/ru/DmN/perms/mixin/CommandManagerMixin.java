package ru.DmN.perms.mixin;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

import static ru.DmN.perms.Main.checkAccess;
import static ru.DmN.perms.Main.permissions;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    public void executeInject(ServerCommandSource commandSource, String command, CallbackInfoReturnable<Integer> cir) {
        try {
            var user = commandSource.getPlayer().getName().asString();
            for (var permission : permissions)
                if (checkAccess(user, command, permission, permissions, new ArrayList<>()))
                    return;
            if (commandSource.getServer().isSingleplayer())
                return;
            commandSource.getPlayer().sendMessage(new LiteralText("§CPermissions error!"), false);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        cir.setReturnValue(0);
        cir.cancel();
    }
}

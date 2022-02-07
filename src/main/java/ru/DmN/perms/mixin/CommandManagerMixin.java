package ru.DmN.perms.mixin;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ru.DmN.perms.Main.checkAccess;
import static ru.DmN.perms.Main.permissions;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    public void executeInject(ServerCommandSource commandSource, String command, CallbackInfoReturnable<Integer> cir) {
        if (commandSource.hasPermissionLevel(4))
            return;
        try {
            if (commandSource.getPlayer() != null) {
                var user = commandSource.getPlayer().getName().asString();
                var cmd = true;
                for (var permission : permissions) {
                    if (permission.commands.contains(command))
                        cmd = false;
                    if (permission.players.contains(user) || checkAccess(user, permission, permissions))
                        return;
                }
                if (cmd)
                    return;
                commandSource.getPlayer().sendMessage(new LiteralText("§CPermissions error!"), false);
            } else return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        cir.setReturnValue(0);
        cir.cancel();
    }
}

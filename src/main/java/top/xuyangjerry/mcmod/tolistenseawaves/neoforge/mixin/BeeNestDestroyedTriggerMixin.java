package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.mixin;

import net.minecraft.advancements.criterion.PlayerHurtEntityTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;

@Mixin(PlayerHurtEntityTrigger.class)
public class PlayerHurtEntityTriggerMixin {
    @Inject(method = "trigger", at = @At(value = "HEAD"))
    private void trigger(ServerPlayer player, Entity entity, DamageSource source, float amountDealt, float amountTaken, boolean blocked, CallbackInfo ci) {
        PrescriptCriteriaTriggers.PLAYER_HURT_ENTITY.get().trigger(player, entity, source, amountDealt, amountTaken, blocked);
    }
}

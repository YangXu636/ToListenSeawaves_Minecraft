package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.mixin;

import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.advancements.criterion.PlayerHurtEntityTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;

@Mixin(BredAnimalsTrigger.class)
public class BredAnimalsTriggerMixin {
    @Inject(method = "trigger", at = @At(value = "HEAD"))
    private void trigger(ServerPlayer player, Animal parent, Animal partner, AgeableMob child, CallbackInfo ci) {
        PrescriptCriteriaTriggers.BRED_ANIMALS.get().trigger(player, parent, partner, child);
    }
}

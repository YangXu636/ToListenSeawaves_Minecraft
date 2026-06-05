package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.mixin;

import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.advancements.criterion.BrewedPotionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;

@Mixin(BrewedPotionTrigger.class)
public class BrewedPotionTriggerMixin {
    @Inject(method = "trigger", at = @At(value = "HEAD"))
    private void trigger(ServerPlayer player, Holder<Potion> potion, CallbackInfo ci) {
        PrescriptCriteriaTriggers.BREWED_POTION.get().trigger(player, potion);
    }
}

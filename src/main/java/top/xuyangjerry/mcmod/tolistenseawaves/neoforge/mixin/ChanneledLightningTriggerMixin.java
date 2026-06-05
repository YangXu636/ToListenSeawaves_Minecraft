package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.mixin;

import net.minecraft.advancements.criterion.BrewedPotionTrigger;
import net.minecraft.advancements.criterion.ChanneledLightningTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;

import java.util.Collection;

@Mixin(ChanneledLightningTrigger.class)
public class ChanneledLightningTriggerMixin {
    @Inject(method = "trigger", at = @At(value = "HEAD"))
    private void trigger(ServerPlayer player, Collection<? extends Entity> entityTriggered, CallbackInfo ci) {
        PrescriptCriteriaTriggers.CHANNELED_LIGHTNING.get().trigger(player, entityTriggered);
    }
}

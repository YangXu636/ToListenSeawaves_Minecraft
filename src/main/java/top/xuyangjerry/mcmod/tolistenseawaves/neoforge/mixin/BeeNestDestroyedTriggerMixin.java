package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.mixin;

import net.minecraft.advancements.criterion.BeeNestDestroyedTrigger;
import net.minecraft.advancements.criterion.PlayerHurtEntityTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;

@Mixin(BeeNestDestroyedTrigger.class)
public class BeeNestDestroyedTriggerMixin {
    @Inject(method = "trigger", at = @At(value = "HEAD"))
    private void trigger(ServerPlayer player, BlockState state, ItemStack stack, int numBees, CallbackInfo ci) {
        PrescriptCriteriaTriggers.BEE_NEST_DESTROYED.get().trigger(player, state, stack, numBees);
    }
}

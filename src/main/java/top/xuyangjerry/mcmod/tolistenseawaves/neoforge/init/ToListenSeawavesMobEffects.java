package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.potion.KarmaObstacleMobEffect;

public class ToListenSeawavesMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ToListenSeawaves.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> KARMA_OBSTACLE = MOB_EFFECTS.register("karma_obstacle", KarmaObstacleMobEffect::new);

}

package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AreaAttackBonusDataComponent(int bonus) {
    public static final AreaAttackBonusDataComponent EMPTY = new AreaAttackBonusDataComponent(0);

    public static final Codec<AreaAttackBonusDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("add_bonus").forGetter(AreaAttackBonusDataComponent::bonus)
            ).apply(instance, AreaAttackBonusDataComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AreaAttackBonusDataComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, AreaAttackBonusDataComponent::bonus,
                    AreaAttackBonusDataComponent::new
            );
}

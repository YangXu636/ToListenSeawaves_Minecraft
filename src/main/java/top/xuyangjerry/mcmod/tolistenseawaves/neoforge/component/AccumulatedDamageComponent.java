package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AccumulatedDamageComponent(float damage) {
    public static final AccumulatedDamageComponent EMPTY = new AccumulatedDamageComponent(0f);

    public static final Codec<AccumulatedDamageComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("damage").forGetter(AccumulatedDamageComponent::damage)
            ).apply(instance, AccumulatedDamageComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AccumulatedDamageComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, AccumulatedDamageComponent::damage,
                    AccumulatedDamageComponent::new
            );
}

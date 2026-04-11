package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptProgress;

import java.util.Optional;

public record PlayerPrescriptDataComponent(long complete, Optional<Identifier> currentId, PrescriptProgress currentProgress, long remainingCdTicks){
    public static final PlayerPrescriptDataComponent EMPTY = new PlayerPrescriptDataComponent(0, Optional.empty(), new PrescriptProgress(), 0);

    public static final Codec<PlayerPrescriptDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("complete_count").forGetter(PlayerPrescriptDataComponent::complete),
                    Identifier.CODEC.optionalFieldOf("current_id").forGetter(PlayerPrescriptDataComponent::currentId),
                    PrescriptProgress.CODEC.fieldOf("current_progress").forGetter(PlayerPrescriptDataComponent::currentProgress),
                    Codec.LONG.fieldOf("complete_count").forGetter(PlayerPrescriptDataComponent::remainingCdTicks)
            ).apply(instance, PlayerPrescriptDataComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerPrescriptDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG, PlayerPrescriptDataComponent::complete,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), PlayerPrescriptDataComponent::currentId,
            PrescriptProgress.STREAM_CODEC, PlayerPrescriptDataComponent::currentProgress,
            ByteBufCodecs.LONG, PlayerPrescriptDataComponent::remainingCdTicks,
            PlayerPrescriptDataComponent::new
    );
}

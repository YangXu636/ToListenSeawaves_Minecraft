package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public record PrescriptDisplay(Component description, boolean showTime) {
    public static final Codec<PrescriptDisplay> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, PrescriptDisplay> STREAM_CODEC;

    private void serializeToNetwork(RegistryFriendlyByteBuf buffer) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, this.description);
        buffer.writeBoolean(this.showTime);
    }

    private static PrescriptDisplay fromNetwork(RegistryFriendlyByteBuf buffer) {
        Component description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
        boolean showTime = buffer.readBoolean();
        return new PrescriptDisplay(description, showTime);
    }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("description").forGetter(PrescriptDisplay::description),
                Codec.BOOL.optionalFieldOf("show_time", true).forGetter(PrescriptDisplay::showTime)
        ).apply(instance, PrescriptDisplay::new));

        STREAM_CODEC = StreamCodec.ofMember(PrescriptDisplay::serializeToNetwork, PrescriptDisplay::fromNetwork);
    }
}

package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;

public record PrescriptHolder(Identifier id, Prescript value) {

    public static final StreamCodec<RegistryFriendlyByteBuf, PrescriptHolder> STREAM_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, List<PrescriptHolder>> LIST_STREAM_CODEC;

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof PrescriptHolder prescriptholder) {
            return this.id.equals(prescriptholder.id);
        }
        return false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return this.id.toString();
    }

    static {
        STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, PrescriptHolder::id, Prescript.STREAM_CODEC, PrescriptHolder::value, PrescriptHolder::new);
        LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());
    }
}

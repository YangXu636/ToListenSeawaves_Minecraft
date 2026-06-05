package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;

import java.util.Optional;

public record PrescriptSyncPacket(Optional<String> prescriptId, Optional<String> prescriptDesc,
                                  long timeLimit, long remainingTicks,
                                  int totalCount, int completedCount,
                                  boolean animationPlaying)
        implements CustomPacketPayload {
    public static final Type<@NotNull PrescriptSyncPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ToListenSeawaves.MOD_ID, "prescript_sync"));
    public static final StreamCodec<FriendlyByteBuf, PrescriptSyncPacket> STREAM_CODEC = StreamCodec.of(
            PrescriptSyncPacket::write,
            PrescriptSyncPacket::read
    );
    public static final PrescriptSyncPacket EMPTY = new PrescriptSyncPacket(Optional.empty(), Optional.empty(), 0L, 0L, 0, 0, false);

    private static void write(FriendlyByteBuf buf, PrescriptSyncPacket packet) {
        buf.writeOptional(packet.prescriptId, FriendlyByteBuf::writeUtf);
        buf.writeOptional(packet.prescriptDesc, FriendlyByteBuf::writeUtf);
        buf.writeLong(packet.timeLimit);
        buf.writeLong(packet.remainingTicks);
        buf.writeInt(packet.totalCount);
        buf.writeInt(packet.completedCount);
        buf.writeBoolean(packet.animationPlaying);
    }

    private static PrescriptSyncPacket read(FriendlyByteBuf buf) {
        return new PrescriptSyncPacket(
                buf.readOptional(FriendlyByteBuf::readUtf),
                buf.readOptional(FriendlyByteBuf::readUtf),
                buf.readLong(),
                buf.readLong(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean()
        );
    }

    public static void sendToPlayer(ServerPlayer player, PrescriptSyncPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public @NotNull Type<@NotNull PrescriptSyncPacket> type() {
        return TYPE;
    }
}

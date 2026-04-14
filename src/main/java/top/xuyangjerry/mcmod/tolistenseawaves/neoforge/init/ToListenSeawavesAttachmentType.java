package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.PlayerPrescriptDataComponent;

import java.util.function.Supplier;

public class ToListenSeawavesAttachmentType {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ToListenSeawaves.MOD_ID);

    public static final Supplier<AttachmentType<PlayerPrescriptDataComponent>> PRESCRIPT = ATTACHMENT_TYPES.register(
            "player_prescript_data", () -> AttachmentType.builder(() -> PlayerPrescriptDataComponent.EMPTY).serialize(PlayerPrescriptDataComponent.CODEC.optionalFieldOf("player_prescript_data", PlayerPrescriptDataComponent.EMPTY)).copyOnDeath().sync(PlayerPrescriptDataComponent.STREAM_CODEC).build()
    );
}

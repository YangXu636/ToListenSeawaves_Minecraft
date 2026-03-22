package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesRegistries;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.Prescript;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerPrescriptManager  extends SimpleJsonResourceReloadListener<Prescript> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Map<Identifier, PrescriptHolder> prescripts = Map.of();
    private final HolderLookup.Provider registries;

    public ServerPrescriptManager(HolderLookup.Provider registries) {
        super(registries, Prescript.CODEC, ToListenSeawavesRegistries.PRESCRIPT);
        this.registries = registries;
    }

    @Override
    protected @NotNull Map<Identifier, Prescript> prepare(ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<Identifier, Prescript> loadedPrescripts = new HashMap<>();
        Map<Identifier, Resource> raw = resourceManager.listResources("prescripts", (id) -> true);
        for (Identifier rl : raw.keySet()) {
            Identifier prescriptId = Identifier.fromNamespaceAndPath(rl.getNamespace(),
                    rl.getPath().replace("prescripts/", "").replace(".json", ""));
            try {
                Resource resource = raw.get(rl);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8));
                Prescript prescript = Prescript.CODEC.decode(JsonOps.INSTANCE, com.google.gson.JsonParser.parseReader(reader))
                        .getOrThrow()
                        .getFirst();
                loadedPrescripts.put(prescriptId, prescript);
                LOGGER.info("加载预设任务：{}（时长：{} Tick）", prescriptId, prescript.timeLimitTicks());
            } catch (IOException e) {
                LOGGER.warn("加载任务失败：{}，错误：{}", rl, e.getMessage());
            }
        }
        return loadedPrescripts;
    }

    protected void apply(Map<Identifier, Prescript> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        ImmutableMap.Builder<Identifier, PrescriptHolder> builder = ImmutableMap.builder();
        object.forEach((id, prescript) -> {
            this.validate(id, prescript);
            builder.put(id, new PrescriptHolder(id, prescript));
        });
        prescripts = builder.buildOrThrow();
    }

    private void validate(Identifier location, Prescript prescript) {
        ProblemReporter.Collector problemreporter$collector = new ProblemReporter.Collector();
        prescript.validate(problemreporter$collector, this.registries);
        if (!problemreporter$collector.isEmpty()) {
            LOGGER.warn("Found validation problems in prescript {}: \n{}", location, problemreporter$collector.getReport());
        }

    }

    public static @Nullable PrescriptHolder get(Identifier location) {
        return prescripts.get(location);
    }

    public static Collection<PrescriptHolder> getAllPrescripts() {
        return prescripts.values();
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(ToListenSeawaves.MOD_ID, "prescripts"), new ServerPrescriptManager(event.getRegistryAccess()));
    }
}

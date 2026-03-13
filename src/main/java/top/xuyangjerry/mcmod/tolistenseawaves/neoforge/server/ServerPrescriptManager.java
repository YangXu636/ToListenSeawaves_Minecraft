package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesRegistries;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.Prescript;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptHolder;

import java.util.Collection;
import java.util.Map;

public class ServerPrescriptManager  extends SimpleJsonResourceReloadListener<Prescript> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<Identifier, PrescriptHolder> prescripts = Map.of();
    private final HolderLookup.Provider registries;

    public ServerPrescriptManager(HolderLookup.Provider registries) {
        super(registries, Prescript.CODEC, ToListenSeawavesRegistries.PRESCRIPT);
        this.registries = registries;
    }

    protected void apply(Map<Identifier, Prescript> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        ImmutableMap.Builder<Identifier, PrescriptHolder> builder = ImmutableMap.builder();
        object.forEach((id, prescript) -> {
            this.validate(id, prescript);
            builder.put(id, new PrescriptHolder(id, prescript));
        });
        this.prescripts = builder.buildOrThrow();
    }

    private void validate(Identifier location, Prescript prescript) {
        ProblemReporter.Collector problemreporter$collector = new ProblemReporter.Collector();
        prescript.validate(problemreporter$collector, this.registries);
        if (!problemreporter$collector.isEmpty()) {
            LOGGER.warn("Found validation problems in prescript {}: \n{}", location, problemreporter$collector.getReport());
        }

    }

    public @Nullable PrescriptHolder get(Identifier location) {
        return this.prescripts.get(location);
    }

    public Collection<PrescriptHolder> getAllPrescripts() {
        return this.prescripts.values();
    }
}

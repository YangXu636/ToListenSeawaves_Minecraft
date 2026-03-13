package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server;

import com.google.gson.*;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.*;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesBuiltInRegistries;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesDataFixTypes;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.Prescript;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriterionTrigger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptHolder;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptProgress;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class PlayerPrescripts {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final PlayerList playerList;
    private final Path playerSavePath;
    private final Map<PrescriptHolder, PrescriptProgress> progress = new LinkedHashMap<>();
    private final Set<PrescriptHolder> visible = new HashSet<>();
    private final Set<PrescriptHolder> progressChanged = new HashSet<>();
    private ServerPlayer player;
    private @Nullable PrescriptHolder lastSelectedTab;
    private boolean isFirstPacket = true;
    private final Codec<PlayerPrescripts.Data> codec;

    public PlayerPrescripts(DataFixer dataFixer, PlayerList playerList, ServerPrescriptManager manager, Path playerSavePath, ServerPlayer player) {
        this.playerList = playerList;
        this.playerSavePath = playerSavePath;
        this.player = player;
        this.codec = ToListenSeawavesDataFixTypes.PRESCRIPTS.wrapCodec(PlayerPrescripts.Data.CODEC, dataFixer, 1343);
        this.load(manager);
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public void stopListening() {
        for (PrescriptCriterionTrigger<?> prescriptCriterionTrigger : ToListenSeawavesBuiltInRegistries.PRESCRIPT_TRIGGER_TYPES) {
            prescriptCriterionTrigger.removePlayerListeners(this);
        }
    }

    public void reload(ServerPrescriptManager manager) {
        this.stopListening();
        this.progress.clear();
        this.visible.clear();
        this.progressChanged.clear();
        this.isFirstPacket = true;
        this.lastSelectedTab = null;
        this.load(manager);
    }

    private void registerListeners(ServerPrescriptManager manager) {
        for (PrescriptHolder prescriptHolder : manager.getAllPrescripts()) {
            this.registerListeners(prescriptHolder);
        }
    }

    private void checkForAutomaticTriggers(ServerPrescriptManager manager) {
        for (PrescriptHolder prescriptholder : manager.getAllPrescripts()) {
            Prescript prescript = prescriptholder.value();
            if (prescript.criteria().isEmpty()) {
                this.award(prescriptholder, "");
                prescript.rewards().grant(this.player);
            }
        }
    }

    private void load(ServerPrescriptManager manager) {
        if (Files.isRegularFile(this.playerSavePath)) {
            try {
                Reader reader = Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8);
                try {
                    JsonElement jsonelement = StrictJsonParser.parse(reader);
                    PlayerPrescripts.Data data = this.codec.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(JsonParseException::new);
                    this.applyFrom(manager, data);
                } catch (Throwable e) {
                    try {
                        reader.close();
                    } catch (Throwable var5) {
                        e.addSuppressed(var5);
                    }
                    throw e;
                }
                reader.close();
            } catch (IOException | JsonIOException e) {
                LOGGER.error("Couldn't access player advancements in {}", this.playerSavePath, e);
            } catch (JsonParseException e) {
                LOGGER.error("Couldn't parse player advancements in {}", this.playerSavePath, e);
            }
        }
        this.checkForAutomaticTriggers(manager);
        this.registerListeners(manager);
    }

    public void save() {
        JsonElement jsonelement = (JsonElement)this.codec.encodeStart(JsonOps.INSTANCE, this.asData()).getOrThrow();
        try {
            FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());
            Writer writer = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8);
            try {
                GSON.toJson(jsonelement, GSON.newJsonWriter(writer));
            } catch (Throwable e) {
                try {
                    writer.close();
                } catch (Throwable var5) {
                    e.addSuppressed(var5);
                }
                throw e;
            }
            writer.close();
        } catch (IOException | JsonIOException e) {
            LOGGER.error("Couldn't save player advancements to {}", this.playerSavePath, e);
        }
    }

    private void applyFrom(ServerPrescriptManager prescriptManager, PlayerPrescripts.Data data) {
        data.forEach((id, progress) -> {
            PrescriptHolder prescriptholder = prescriptManager.get(id);
            if (prescriptholder == null) {
                LOGGER.warn("Ignored prescript '{}' in progress file {} - it doesn't exist anymore?", id, this.playerSavePath);
                return;
            }
            this.startProgress(prescriptholder, progress);
            this.progressChanged.add(prescriptholder);
            this.markForVisibilityUpdate(prescriptholder);
        });
    }

    private PlayerPrescripts.Data asData() {
        Map<Identifier, PrescriptProgress> map = new LinkedHashMap<>();
        this.progress.forEach((holder, progress) -> {
            if (progress.hasProgress()) {
                map.put(holder.id(), progress);
            }
        });
        return new PlayerPrescripts.Data(map);
    }

    public boolean award(PrescriptHolder prescript, String criterionKey) {
        if (this.player instanceof FakePlayer) {
            return false;
        }
        boolean flag = false;
        PrescriptProgress prescriptprogress = this.getOrStartProgress(prescript);
        boolean flag1 = prescriptprogress.isDone();
        if (prescriptprogress.grantProgress(criterionKey)) {
            this.unregisterListeners(prescript);
            this.progressChanged.add(prescript);
            flag = true;
            EventHooks.onAdvancementProgressedEvent(this.player, prescript, prescriptprogress, criterionKey, AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT);
            if (!flag1 && prescriptprogress.isDone()) {
                prescript.value().rewards().grant(this.player);
                prescript.value().display().ifPresent((p_460321_) -> {
                    if (p_460321_.shouldAnnounceChat() && (Boolean)this.player.level().getGameRules().get(GameRules.SHOW_ADVANCEMENT_MESSAGES)) {
                        this.playerList.broadcastSystemMessage(p_460321_.getType().createAnnouncement(prescript, this.player), false);
                    }

                    EventHooks.onAdvancementEarnedEvent(this.player, prescript);
                });
            }
        }

        if (!flag1 && prescriptprogress.isDone()) {
            this.markForVisibilityUpdate(prescript);
        }
        return flag;
    }

    public boolean revoke(PrescriptHolder prescript, String criterionKey) {
        boolean flag = false;
        PrescriptProgress prescriptprogress = this.getOrStartProgress(prescript);
        boolean flag1 = prescriptprogress.isDone();
        if (prescriptprogress.revokeProgress(criterionKey)) {
            this.registerListeners(prescript);
            this.progressChanged.add(prescript);
            flag = true;
            EventHooks.onAdvancementProgressedEvent(this.player, prescript, prescriptprogress, criterionKey, AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE);
        }

        if (flag1 && !prescriptprogress.isDone()) {
            this.markForVisibilityUpdate(prescript);
        }

        return flag;
    }

    private void markForVisibilityUpdate(AdvancementHolder advancement) {
        AdvancementNode advancementnode = this.tree.get(advancement);
        if (advancementnode != null) {
            this.rootsToUpdate.add(advancementnode.root());
        }

    }

    private void registerListeners(AdvancementHolder advancement) {
        AdvancementProgress advancementprogress = this.getOrStartProgress(advancement);
        if (!advancementprogress.isDone()) {
            Iterator var3 = advancement.value().criteria().entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<String, Criterion<?>> entry = (Map.Entry)var3.next();
                CriterionProgress criterionprogress = advancementprogress.getCriterion((String)entry.getKey());
                if (criterionprogress != null && !criterionprogress.isDone()) {
                    this.registerListener(advancement, (String)entry.getKey(), (Criterion)entry.getValue());
                }
            }
        }

    }

    private <T extends CriterionTriggerInstance> void registerListener(AdvancementHolder advancement, String criterionKey, Criterion<T> criterion) {
        criterion.trigger().addPlayerListener(this, new CriterionTrigger.Listener(criterion.triggerInstance(), advancement, criterionKey));
    }

    private void unregisterListeners(AdvancementHolder advancement) {
        AdvancementProgress advancementprogress = this.getOrStartProgress(advancement);
        Iterator var3 = advancement.value().criteria().entrySet().iterator();

        while(true) {
            Map.Entry entry;
            CriterionProgress criterionprogress;
            do {
                do {
                    if (!var3.hasNext()) {
                        return;
                    }

                    entry = (Map.Entry)var3.next();
                    criterionprogress = advancementprogress.getCriterion((String)entry.getKey());
                } while(criterionprogress == null);
            } while(!criterionprogress.isDone() && !advancementprogress.isDone());

            this.removeListener(advancement, (String)entry.getKey(), (Criterion)entry.getValue());
        }
    }

    private <T extends CriterionTriggerInstance> void removeListener(AdvancementHolder advancement, String criterionKey, Criterion<T> criterion) {
        criterion.trigger().removePlayerListener(this, new CriterionTrigger.Listener(criterion.triggerInstance(), advancement, criterionKey));
    }

    public void flushDirty(ServerPlayer player, boolean showAdvancements) {
        if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
            Map<Identifier, AdvancementProgress> map = new HashMap();
            Set<AdvancementHolder> set = new HashSet();
            Set<Identifier> set1 = new HashSet();
            Iterator var6 = this.rootsToUpdate.iterator();

            while(var6.hasNext()) {
                AdvancementNode advancementnode = (AdvancementNode)var6.next();
                this.updateTreeVisibility(advancementnode, set, set1);
            }

            this.rootsToUpdate.clear();
            var6 = this.progressChanged.iterator();

            while(var6.hasNext()) {
                AdvancementHolder advancementholder = (AdvancementHolder)var6.next();
                if (this.visible.contains(advancementholder)) {
                    map.put(advancementholder.id(), (AdvancementProgress)this.progress.get(advancementholder));
                }
            }

            this.progressChanged.clear();
            if (!map.isEmpty() || !set.isEmpty() || !set1.isEmpty()) {
                player.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, set, set1, map, showAdvancements));
            }
        }

        this.isFirstPacket = false;
    }

    public PrescriptProgress getOrStartProgress(PrescriptHolder prescript) {
        PrescriptProgress prescriptprogress = this.progress.get(prescript);
        if (prescriptprogress == null) {
            prescriptprogress = new PrescriptProgress();
            this.startProgress(prescript, prescriptprogress);
        }
        return prescriptprogress;
    }

    private void startProgress(PrescriptHolder prescript, PrescriptProgress progress) {
        progress.update(prescript.value().requirements());
        this.progress.put(prescript, progress);
    }

    record Data(Map<Identifier, PrescriptProgress> map) {
        public static final Codec<PlayerPrescripts.Data> CODEC;

        public void forEach(BiConsumer<Identifier, PrescriptProgress> action) {
            this.map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach((kvp) -> {
                action.accept(kvp.getKey(), kvp.getValue());
            });
        }

        public Map<Identifier, PrescriptProgress> map() {
            return this.map;
        }

        static {
            CODEC = Codec.unboundedMap(Identifier.CODEC, PrescriptProgress.CODEC).xmap(PlayerPrescripts.Data::new, PlayerPrescripts.Data::map);
        }
    }
}

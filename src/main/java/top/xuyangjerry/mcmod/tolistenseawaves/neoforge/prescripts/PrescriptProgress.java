package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class PrescriptProgress implements Comparable<PrescriptProgress> {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Codec<Map<String, CriterionProgress>> CRITERIA_CODEC;
    private static final StreamCodec<RegistryFriendlyByteBuf, CriterionProgress> CRITERION_PROGRESS_STREAM_CODEC;
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<String, CriterionProgress>> CRITERIA_STREAM_CODEC;
    public static final Codec<PrescriptProgress> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, PrescriptProgress> STREAM_CODEC;
    private final Map<String, CriterionProgress> criteria;
    private PrescriptRequirements requirements;
    private int ticks;

    private PrescriptProgress(Map<String, CriterionProgress> criteria, int ticks) {
        this.requirements = PrescriptRequirements.EMPTY;
        this.criteria = new LinkedHashMap<>(criteria);
        this.ticks = ticks;
    }

    private PrescriptProgress(Map<String, CriterionProgress> criteria) {
        this.requirements = PrescriptRequirements.EMPTY;
        this.criteria = new LinkedHashMap<>(criteria);
        this.ticks = 0;
    }

    public PrescriptProgress() {
        this.requirements = PrescriptRequirements.EMPTY;
        this.criteria = new LinkedHashMap<>();
        this.ticks = 0;
    }

    public void updateTick() {
        this.ticks++;
    }

    public void update(PrescriptRequirements requirements) {
        Set<String> set = requirements.names();
        this.criteria.entrySet().removeIf((map) -> !set.contains(map.getKey()));
        for (String s : set) {
            this.criteria.putIfAbsent(s, new CriterionProgress());
        }
        this.requirements = requirements;
    }

    public boolean isDone() {
        return this.requirements.test(this::isCriterionDone);
    }

    public boolean hasProgress() {
        Iterator<CriterionProgress> var1 = this.criteria.values().iterator();
        CriterionProgress criterionprogress;
        do {
            if (!var1.hasNext()) { return false; }
            criterionprogress = var1.next();
        } while(!criterionprogress.isDone());
        return true;
    }

    private OptionalInt getRequirementGroupIndex(String criterionName) {
        List<List<String>> requirementGroups = this.requirements.requirements();
        for (int i = 0; i < requirementGroups.size(); i++) {
            List<String> group = requirementGroups.get(i);
            if (group.contains(criterionName)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    private boolean areAllPreviousGroupsCompleted(int targetGroupIndex) {
        List<List<String>> requirementGroups = this.requirements.requirements();
        for (int i = 0; i < targetGroupIndex; i++) {
            List<String> prevGroup = requirementGroups.get(i);
            boolean isPrevGroupDone = prevGroup.stream().anyMatch(this::isCriterionDone);
            if (!isPrevGroupDone) {
                return false;
            }
        }
        return true;
    }

    private boolean isCriterionInValidOrder(String criterionName) {
        OptionalInt groupIndexOpt = getRequirementGroupIndex(criterionName);
        if (groupIndexOpt.isEmpty()) {
            return false;
        }
        int targetGroupIndex = groupIndexOpt.getAsInt();
        return areAllPreviousGroupsCompleted(targetGroupIndex);
    }

    public boolean grantProgress(String criterionName, boolean isOrdered) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if (criterionprogress != null && !criterionprogress.isDone() && (!isOrdered || this.isCriterionInValidOrder(criterionName))) {
            criterionprogress.grant();
            return true;
        }
        return false;
    }

    public boolean revokeProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if (criterionprogress != null && criterionprogress.isDone()) {
            criterionprogress.revoke();
            return true;
        }
        return false;
    }

    public String toString() {
        String str = String.valueOf(this.criteria);
        return "PrescriptProgress{criteria=" + str + ", requirements=" + this.requirements + ", ticks=" + this.ticks + "}";
    }

    public void serializeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (buf, progress) -> {
            progress.serializeToNetwork(buf);
        });
    }

    public static PrescriptProgress fromNetwork(FriendlyByteBuf buffer) {
        Map<String, CriterionProgress> map = buffer.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        return new PrescriptProgress(map);
    }

    public @Nullable CriterionProgress getCriterion(String criterionName) {
        return this.criteria.get(criterionName);
    }

    public Map<String, CriterionProgress> getCriteria() {
        return this.criteria;
    }

    private boolean isCriterionDone(String criterionName) {
        CriterionProgress criterionprogress = this.getCriterion(criterionName);
        return criterionprogress != null && criterionprogress.isDone();
    }

    public int getTotalCount() {
        return this.requirements.size();
    }

    public int countCompletedRequirements() {
        return StreamSupport.stream(this.getCompletedCriteria().spliterator(), false).toList().size();
    }

    public Iterable<String> getRemainingCriteria() {
        List<String> list = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> stringCriterionProgressEntry : this.criteria.entrySet()) {
            if (!stringCriterionProgressEntry.getValue().isDone()) {
                list.add(stringCriterionProgressEntry.getKey());
            }
        }
        return list;
    }

    public Iterable<String> getCompletedCriteria() {
        List<String> list = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> stringCriterionProgressEntry : this.criteria.entrySet()) {
            if (stringCriterionProgressEntry.getValue().isDone()) {
                list.add(stringCriterionProgressEntry.getKey());
            }
        }
        return list;
    }

    public @Nullable Instant getFirstProgressDate() {
        return this.criteria.values().stream().map(CriterionProgress::getObtained).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }

    public int compareTo(PrescriptProgress other) {
        Instant instant = this.getFirstProgressDate();
        Instant instant1 = other.getFirstProgressDate();
        if (instant == null && instant1 != null) {
            return 1;
        } else if (instant != null && instant1 == null) {
            return -1;
        } else {
            return instant == null ? 0 : instant.compareTo(instant1);
        }
    }

    static {
        /*CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, ).xmap((p_465324_) -> {
            return Util.mapValues(p_465324_, CriterionProgress::new);
        }, (p_300663_) -> {
            return (Map)p_300663_.entrySet().stream().filter((p_300656_) -> {
                return ((CriterionProgress)p_300656_.getValue()).isDone();
            }).collect(Collectors.toMap(Map.Entry::getKey, (p_300655_) -> {
                return (Instant)Objects.requireNonNull(((CriterionProgress)p_300655_.getValue()).getObtained());
            }));
        });*/
        CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, Codec.BOOL).xmap(
                // 正向转换：Map<String, Boolean> → Map<String, CriterionProgress>
                (boolMap) -> {
                    Map<String, CriterionProgress> criterionMap = new HashMap<>();
                    for (Map.Entry<String, Boolean> entry : boolMap.entrySet()) {
                        CriterionProgress progress = new CriterionProgress();
                        // 根据布尔值设置完成状态（true=授予进度，false=撤销）
                        if (entry.getValue()) {
                            progress.grant(); // 模拟完成（注：若需要清空时间，可修改CriterionProgress的grant方法）
                        } else {
                            progress.revoke();
                        }
                        criterionMap.put(entry.getKey(), progress);
                    }
                    return criterionMap;
                },
                // 反向转换：Map<String, CriterionProgress> → Map<String, Boolean>
                (criterionMap) -> {
                    return criterionMap.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().isDone()
                            ));
                }
        );

        CRITERION_PROGRESS_STREAM_CODEC = StreamCodec.of(
                (buf, progress) -> progress.serializeToNetwork(buf),
                CriterionProgress::fromNetwork
        );

        CRITERIA_STREAM_CODEC = ByteBufCodecs.map(
                HashMap::new,
                ByteBufCodecs.STRING_UTF8.cast(), // .cast() 把 StreamCodec<ByteBuf, String> → StreamCodec<RegistryFriendlyByteBuf, String>
                CRITERION_PROGRESS_STREAM_CODEC
        );

        CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                CRITERIA_CODEC.optionalFieldOf("criteria", Map.of()).forGetter((progress) -> progress.criteria),
                Codec.INT.fieldOf("ticks").forGetter(PrescriptProgress::getTicks)
        ).apply(instance, (map, ticks) -> new PrescriptProgress(new HashMap<>(map), ticks)));

        STREAM_CODEC = StreamCodec.composite(
                CRITERIA_STREAM_CODEC,
                PrescriptProgress::getCriteria,
                ByteBufCodecs.INT,
                PrescriptProgress::getTicks,
                PrescriptProgress::new
        );
    }
}

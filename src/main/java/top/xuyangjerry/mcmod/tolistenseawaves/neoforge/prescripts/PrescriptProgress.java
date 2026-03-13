package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class PrescriptProgress implements Comparable<PrescriptProgress> {
    private static final Codec<Map<String, CriterionProgress>> CRITERIA_CODEC;
    public static final Codec<PrescriptProgress> CODEC;
    private final Map<String, CriterionProgress> criteria;
    private PrescriptRequirements requirements;

    private PrescriptProgress(Map<String, CriterionProgress> criteria) {
        this.requirements = PrescriptRequirements.EMPTY;
        this.criteria = criteria;
    }

    public PrescriptProgress() {
        this.requirements = PrescriptRequirements.EMPTY;
        this.criteria = Maps.newHashMap();
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

    public boolean grantProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if (criterionprogress != null && !criterionprogress.isDone()) {
            criterionprogress.grant();
            return true;
        } else {
            return false;
        }
    }

    public boolean revokeProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if (criterionprogress != null && criterionprogress.isDone()) {
            criterionprogress.revoke();
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        String str = String.valueOf(this.criteria);
        return "PrescriptProgress{criteria=" + str + ", requirements=" + this.requirements + "}";
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

    private boolean isCriterionDone(String criterionName) {
        CriterionProgress criterionprogress = this.getCriterion(criterionName);
        return criterionprogress != null && criterionprogress.isDone();
    }

    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0F;
        } else {
            float f = (float)this.requirements.size();
            float f1 = (float)this.countCompletedRequirements();
            return f1 / f;
        }
    }

    public @Nullable Component getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        } else {
            int i = this.requirements.size();
            if (i <= 1) {
                return null;
            } else {
                int j = this.countCompletedRequirements();
                return Component.translatable("prescript.progress", new Object[]{j, i});
            }
        }
    }

    private int countCompletedRequirements() {
        return this.requirements.count(this::isCriterionDone);
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
        CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    CRITERIA_CODEC.optionalFieldOf("criteria", Map.of()).forGetter((progress) -> {
                        return progress.criteria;
                    }),
                    Codec.BOOL.fieldOf("done").orElse(true).forGetter(PrescriptProgress::isDone)
            ).apply(instance, (map, bool) -> {
                return new PrescriptProgress(new HashMap<>(map));
            });
        });
    }
}

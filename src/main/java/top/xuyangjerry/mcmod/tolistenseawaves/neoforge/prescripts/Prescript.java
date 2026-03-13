package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record Prescript(long timeLimitTicks, Map<String, Criterion<?>> criteria,
                        PrescriptRequirements requirements, PrescriptRewards rewards,
                        PrescriptPublishConditions publishConditions)
{
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC;
    public static final Codec<Prescript> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, Prescript> STREAM_CODEC;
    public static final Codec<Optional<WithConditions<Prescript>>> CONDITIONAL_CODEC;

    public static DataResult<Prescript> validate(Prescript prescript) {
        Set<String> allCriteria = prescript.criteria().keySet();
        return prescript.requirements().validate(allCriteria)
                .mapError(error -> error)
                .flatMap(ignored -> prescript.publishConditions().validate(allCriteria))
                .mapError(error -> error)
                .map(ignored -> prescript);
    }

    private void write(RegistryFriendlyByteBuf buffer) {    //传出必要数据
        buffer.writeLong(this.timeLimitTicks);
        this.requirements.write(buffer);
        this.publishConditions.write(buffer);
    }

    private static Prescript read(RegistryFriendlyByteBuf buffer) { //按照write的顺序读取数据
        return new Prescript(buffer.readLong(), Map.of(), new PrescriptRequirements(buffer), PrescriptRewards.EMPTY, new PrescriptPublishConditions(buffer));
    }

    public void validate(ProblemReporter reporter, HolderGetter.Provider lootData) {    //校验 子条件本身的触发逻辑 是否合法
        this.criteria.forEach((name, criterion) -> {
            CriterionValidator criterionvalidator = new CriterionValidator(reporter.forChild(new ProblemReporter.RootFieldPathElement(name)), lootData);
            criterion.triggerInstance().validate(criterionvalidator);
        });
    }

    static {
        CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, Criterion.CODEC).validate((criterionMap) -> criterionMap.isEmpty() ? DataResult.error(() -> "Prescript criteria cannot be empty") : DataResult.success(criterionMap));

        Codec<Prescript> codec1;
        codec1 = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.LONG.fieldOf("timeLimitTicks").forGetter(Prescript::timeLimitTicks),
                CRITERIA_CODEC.fieldOf("criteria").forGetter(Prescript::criteria),
                PrescriptRequirements.CODEC.fieldOf("requirements").forGetter(Prescript::requirements),
                PrescriptRewards.CODEC.fieldOf("rewards").forGetter(Prescript::rewards),
                PrescriptPublishConditions.CODEC.fieldOf("publishConditions").forGetter(Prescript::publishConditions)
        ).apply(instance, Prescript::new));
        CODEC = codec1.validate(Prescript::validate);

        STREAM_CODEC = StreamCodec.ofMember(Prescript::write, Prescript::read);
        CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);
    }
}

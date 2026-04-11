package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record Prescript(PrescriptDisplay display, long timeLimitTicks, MinMaxBounds.Ints cd,
                        Map<String, PrescriptCriterion<?>> criteria,
                        PrescriptRequirements requirements, PrescriptRewards rewards,
                        PrescriptPublishConditions publishConditions)
{
    private static final Codec<Map<String, PrescriptCriterion<?>>> CRITERIA_CODEC;
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
        PrescriptDisplay.STREAM_CODEC.encode(buffer, this.display);
        buffer.writeLong(this.timeLimitTicks);
        MinMaxBounds.Ints.STREAM_CODEC.encode(buffer, this.cd);
        this.requirements.write(buffer);
        this.publishConditions.write(buffer);
    }

    private static Prescript read(RegistryFriendlyByteBuf buffer) { //按照write的顺序读取数据
        return new Prescript(PrescriptDisplay.STREAM_CODEC.decode(buffer), buffer.readLong(), MinMaxBounds.Ints.STREAM_CODEC.decode(buffer), Map.of(), new PrescriptRequirements(buffer), PrescriptRewards.EMPTY, new PrescriptPublishConditions(buffer));
    }

    public void validate(ProblemReporter reporter, HolderGetter.Provider lootData) {    //校验 子条件本身的触发逻辑 是否合法
        this.criteria.forEach((name, criterion) -> {
            CriterionValidator criterionvalidator = new CriterionValidator(reporter.forChild(new ProblemReporter.RootFieldPathElement(name)), lootData);
            criterion.triggerInstance().validate(criterionvalidator);
        });
    }

    static {
        CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, PrescriptCriterion.CODEC).validate((criterionMap) -> criterionMap.isEmpty() ? DataResult.error(() -> "Prescript criteria cannot be empty") : DataResult.success(criterionMap));

        Codec<Prescript> codec1;
        codec1 = RecordCodecBuilder.create((instance) -> instance.group(
                PrescriptDisplay.CODEC.fieldOf("display").forGetter(Prescript::display),
                Codec.LONG.fieldOf("time_limit_ticks").forGetter(Prescript::timeLimitTicks),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("cd", MinMaxBounds.Ints.exactly(0)).forGetter(Prescript::cd),
                CRITERIA_CODEC.fieldOf("criteria").forGetter(Prescript::criteria),
                PrescriptRequirements.CODEC.fieldOf("requirements").forGetter(Prescript::requirements),
                PrescriptRewards.CODEC.fieldOf("rewards").forGetter(Prescript::rewards),
                PrescriptPublishConditions.CODEC.optionalFieldOf("publish_conditions", PrescriptPublishConditions.EMPTY).forGetter(Prescript::publishConditions)
        ).apply(instance, Prescript::new));
        CODEC = codec1.validate(Prescript::validate);

        STREAM_CODEC = StreamCodec.ofMember(Prescript::write, Prescript::read);
        CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);
    }
}

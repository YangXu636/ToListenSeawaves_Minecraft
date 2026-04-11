package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.network.FriendlyByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public record PrescriptPublishConditions(List<List<String>> conditions) {
    public static final Codec<PrescriptPublishConditions> CODEC = Codec.STRING.listOf().listOf().xmap(PrescriptPublishConditions::new, PrescriptPublishConditions::conditions);
    public static final PrescriptPublishConditions EMPTY = new PrescriptPublishConditions(List.of());

    public PrescriptPublishConditions(FriendlyByteBuf buffer) {
        this(buffer.readList((instance) -> instance.readList(FriendlyByteBuf::readUtf)));
    }

    public PrescriptPublishConditions(List<List<String>> conditions) {
        this.conditions = conditions;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(this.conditions, (buf, strList) -> {
            buf.writeCollection(strList, FriendlyByteBuf::writeUtf);
        });
    }

    public static PrescriptPublishConditions allOf(Collection<String> conditions) {
        return new PrescriptPublishConditions(conditions.stream().map(List::of).toList());
    }

    public static PrescriptPublishConditions anyOf(Collection<String> conditions) {
        return new PrescriptPublishConditions(List.of(List.copyOf(conditions)));
    }

    public int size() {
        return this.conditions.size();
    }

    private static boolean anyMatch(List<String> requirements, Predicate<String> predicate) {
        Iterator<String> var2 = requirements.iterator();
        String s;
        do {
            if (!var2.hasNext()) { return false; }
            s = var2.next();
        } while(!predicate.test(s) && !s.isEmpty());
        return true;
    }

    public boolean test(Predicate<String> predicate) {
        if (this.conditions.isEmpty()) {
            return true;
        }
        Iterator<List<String>> var2 = this.conditions.iterator();
        List<String> list;
        do {
            if (!var2.hasNext()) { return true; }
            list = var2.next();
        } while(anyMatch(list, predicate));
        return false;
    }

    public int count(Predicate<String> filter) {
        if (this.conditions.isEmpty()) {
            return 1; // 空规则默认完成数为1
        }
        int i = 0;
        for (List<String> list : this.conditions) {
            if (anyMatch(list, filter)) {
                ++i;
            }
        }
        return i;
    }

    public boolean canIPublish() {
        if (this.conditions.isEmpty() || this.conditions.stream().allMatch(List::isEmpty)) { return true; }
        Map<String, CriterionProgress> criteria = new HashMap<>();
        Set<String> set = this.names();
        for (String s : set) {
            criteria.putIfAbsent(s, new CriterionProgress());
        }
        return this.test((conditionName) -> this.isConditionOk(criteria, conditionName));
    }

    private boolean isConditionOk(Map<String, CriterionProgress> criteria, String conditionName) {
        CriterionProgress criterionprogress = criteria.get(conditionName);
        return criterionprogress != null && criterionprogress.isDone();
    }

    public DataResult<PrescriptPublishConditions> validate(Set<String> criteria) {
        if (this.conditions.isEmpty()) {
            return DataResult.success(this);
        }
        Set<String> set = new ObjectOpenHashSet<>();
        for (List<String> condition : this.conditions) {
            /*if (condition.isEmpty() && !criteria.isEmpty()) {
                return DataResult.error(() -> "PrescriptPublishCondition entry cannot be empty");
            }*/
            set.addAll(condition);
        }
        if (!Sets.difference(set, criteria).isEmpty()) {
            Set<String> set1 = Sets.difference(set, criteria);
            return DataResult.error(() -> {
                return "Prescript completion conditions did not exactly match specified criteria. Unknown: " + set1;
            });
        }
        return DataResult.success(this);
    }

    public boolean isEmpty() {
        return this.conditions.isEmpty();
    }

    public String toString() {
        return this.conditions.toString();
    }

    public Set<String> names() {
        Set<String> set = new ObjectOpenHashSet<>();
        for (List<String> requirement : this.conditions) {
            set.addAll(requirement);
        }
        return set;
    }

    public List<List<String>> conditions() {
        return this.conditions;
    }

    public interface Strategy {
        PrescriptPublishConditions.Strategy AND = PrescriptPublishConditions::allOf;
        PrescriptPublishConditions.Strategy OR = PrescriptPublishConditions::anyOf;
        PrescriptPublishConditions create(Collection<String> var1);
    }
}

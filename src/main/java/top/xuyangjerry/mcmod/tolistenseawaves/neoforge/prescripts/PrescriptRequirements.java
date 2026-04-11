package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public record PrescriptRequirements(List<List<String>> requirements) {
    public static final Codec<PrescriptRequirements> CODEC = Codec.STRING.listOf().listOf().xmap(PrescriptRequirements::new, PrescriptRequirements::requirements);
    public static final PrescriptRequirements EMPTY = new PrescriptRequirements(List.of());

    public PrescriptRequirements(FriendlyByteBuf buffer) {
        this(buffer.readList((instance) -> instance.readList(FriendlyByteBuf::readUtf)));
    }

    public PrescriptRequirements(List<List<String>> requirements) {
        this.requirements = requirements;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(this.requirements, (buf, strList) -> {
            buf.writeCollection(strList, FriendlyByteBuf::writeUtf);
        });
    }

    public static PrescriptRequirements allOf(Collection<String> requirements) {
        return new PrescriptRequirements(requirements.stream().map(List::of).toList());
    }

    public static PrescriptRequirements anyOf(Collection<String> criteria) {
        return new PrescriptRequirements(List.of(List.copyOf(criteria)));
    }

    public int size() {
        return this.requirements.size();
    }

    private static boolean anyMatch(List<String> requirements, Predicate<String> predicate) {
        Iterator<String> var2 = requirements.iterator();
        String s;
        do {
            if (!var2.hasNext()) { return false; }
            s = var2.next();
        } while(!predicate.test(s));
        return true;
    }

    public boolean test(Predicate<String> predicate) {
        if (this.requirements.isEmpty()) {
            return false;
        } else {
            Iterator<List<String>> var2 = this.requirements.iterator();
            List<String> list;
            do {
                if (!var2.hasNext()) { return true; }
                list = var2.next();
            } while(anyMatch(list, predicate));
            return false;
        }
    }

    public int count(Predicate<String> filter) {
        int i = 0;
        for (List<String> list : this.requirements) {
            if (anyMatch(list, filter)) {
                ++i;
            }
        }
        return i;
    }

    public DataResult<PrescriptRequirements> validate(Set<String> criteria) {
        Set<String> set = new ObjectOpenHashSet<>();

        for (List<String> requirement : this.requirements) {
            if (requirement.isEmpty() && criteria.isEmpty()) {
                return DataResult.error(() -> "PrescriptRequirement entry cannot be empty");
            }
            set.addAll(requirement);
        }
        if (!Sets.difference(criteria, set).isEmpty()) {
            Set<String> set1 = Sets.difference(set, criteria);
            return DataResult.error(() -> {
                return "Prescript completion requirements did not exactly match specified criteria. Unknown: " + set1;
            });
        }
        return DataResult.success(this);
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> names() {
        Set<String> set = new ObjectOpenHashSet<>();
        for (List<String> requirement : this.requirements) {
            set.addAll(requirement);
        }
        return set;
    }

    public List<List<String>> requirements() {
        return this.requirements;
    }

    public interface Strategy {
        PrescriptRequirements.Strategy AND = PrescriptRequirements::allOf;
        PrescriptRequirements.Strategy OR = PrescriptRequirements::anyOf;
        PrescriptRequirements create(Collection<String> var1);
    }
}

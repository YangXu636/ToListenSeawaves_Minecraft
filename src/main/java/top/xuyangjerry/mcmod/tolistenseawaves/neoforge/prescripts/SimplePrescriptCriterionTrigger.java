package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server.PlayerPrescripts;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server.PrescriptPublisher;

import java.util.*;
import java.util.function.Predicate;

public abstract class SimplePrescriptCriterionTrigger<T extends SimplePrescriptCriterionTrigger.SimplePrescriptInstance> implements PrescriptCriterionTrigger<T> {
    private final Map<PlayerPrescripts, Set<PrescriptCriterionTrigger.Listener<T>>> players = Maps.newIdentityHashMap();

    public SimplePrescriptCriterionTrigger() {
    }

    public final void addPlayerListener(PlayerPrescripts playerPrescripts, PrescriptCriterionTrigger.Listener<T> listener) {
        this.players.computeIfAbsent(playerPrescripts, (p_466907_) -> Sets.newHashSet()).add(listener);
    }

    public final void removePlayerListener(PlayerPrescripts playerPrescripts, PrescriptCriterionTrigger.Listener<T> listener) {
        Set<PrescriptCriterionTrigger.Listener<T>> set = this.players.get(playerPrescripts);
        if (set == null) { return; }
        set.remove(listener);
        if (set.isEmpty()) {
            this.players.remove(playerPrescripts);
        }
    }

    public final void removePlayerListeners(PlayerPrescripts playerPrescripts) {
        this.players.remove(playerPrescripts);
    }

    protected void trigger(ServerPlayer player, Predicate<T> testTrigger) {
        PlayerPrescripts playerPrescripts = PrescriptPublisher.getPrescript(player);
        Set<PrescriptCriterionTrigger.Listener<T>> listenerSet = this.players.get(playerPrescripts);
        if (listenerSet == null || listenerSet.isEmpty()) {
            return;
        }
        LootContext lootcontext = EntityPredicate.createContext(player, player);
        listenerSet.stream()
                .filter(listener -> {
                    T t = listener.trigger();
                    return testTrigger.test(t) && (t.player().isEmpty() || t.player().get().matches(lootcontext));
                })
                .forEach(listener -> listener.run(playerPrescripts));

        /*Iterator<Listener<T>> var7 = listenerSet.iterator();
        while(true) {
            PrescriptCriterionTrigger.Listener<T> listener;
            Optional<ContextAwarePredicate> optional;
            do {
                T t;
                do {
                    if (!var7.hasNext()) {
                        if (list != null) {
                            var7 = list.iterator();
                            while(var7.hasNext()) {
                                listener = var7.next();
                                listener.run(playerPrescripts);
                            }
                        }
                        return;
                    }
                    listener = var7.next();
                    t = listener.trigger();
                } while(!testTrigger.test(t));
                optional = t.player();
            } while(optional.isPresent() && !(optional.get()).matches(lootcontext));
            if (list == null) {
                list = Lists.newArrayList();
            }

            list.add(listener);
        }*/
    }

    public interface SimplePrescriptInstance extends CriterionTriggerInstance {
        default void validate(CriterionValidator p_469858_) {
            p_469858_.validateEntity(this.player(), "player");
        }

        Optional<ContextAwarePredicate> player();
    }
}

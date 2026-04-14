package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
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
                .toList()
                .forEach(listener -> listener.run(playerPrescripts));
    }

    public interface SimplePrescriptInstance extends CriterionTriggerInstance {
        default void validate(CriterionValidator p_469858_) {
            p_469858_.validateEntity(this.player(), "player");
        }

        Optional<ContextAwarePredicate> player();
    }
}

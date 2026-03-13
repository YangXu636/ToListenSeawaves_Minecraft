package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server.PlayerPrescripts;

public interface PrescriptCriterionTrigger <T extends CriterionTriggerInstance> {
    void addPlayerListener(PlayerPrescripts var1, CriterionTrigger.Listener<T> var2);

    void removePlayerListener(PlayerPrescripts var1, CriterionTrigger.Listener<T> var2);

    void removePlayerListeners(PlayerPrescripts var1);

    Codec<T> codec();

    default PrescriptCriterion<T> createCriterion(T triggerInstance) {
        return new PrescriptCriterion(this, triggerInstance);
    }

    public static record Listener<T extends CriterionTriggerInstance>(T trigger, PrescriptHolder prescript,
                                                                      String criterion) {
        public Listener(T trigger, PrescriptHolder prescript, String criterion) {
            this.trigger = trigger;
            this.prescript = prescript;
            this.criterion = criterion;
        }

        public void run(PlayerPrescripts playerPrescripts) {
            playerPrescripts.award(this.prescript, this.criterion);
        }

        public T trigger() {
            return this.trigger;
        }

        public PrescriptHolder prescript() {
            return this.prescript;
        }

        public String criterion() {
            return this.criterion;
        }
    }
}
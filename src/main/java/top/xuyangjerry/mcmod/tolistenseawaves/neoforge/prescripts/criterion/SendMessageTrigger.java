package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.SimplePrescriptCriterionTrigger;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@EventBusSubscriber(modid = ToListenSeawaves.MOD_ID)
public class SendMessageTrigger extends SimplePrescriptCriterionTrigger<SendMessageTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String sentMessage) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(sentMessage));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<List<String>> messages) implements SimplePrescriptCriterionTrigger.SimplePrescriptInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Codec.list(Codec.STRING).optionalFieldOf("messages").forGetter(TriggerInstance::messages)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(String sentMessage) {
            if (this.messages.isEmpty()) {
                return true;
            }
            List<String> patterns = this.messages.get();
            for (String regex : patterns) {
                try {
                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    if (pattern.matcher(sentMessage).matches()) {
                        return true;
                    }
                } catch (PatternSyntaxException e) {
                    continue;
                }
            }
            return false;
        }

        @Override
        public void validate(CriterionValidator validator) {
            SimplePrescriptCriterionTrigger.SimplePrescriptInstance.super.validate(validator);
        }
    }

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();
        if (message.startsWith("/")) {
            return;
        }
        PrescriptCriteriaTriggers.SEND_MESSAGE.get().trigger(player, message);
    }
}

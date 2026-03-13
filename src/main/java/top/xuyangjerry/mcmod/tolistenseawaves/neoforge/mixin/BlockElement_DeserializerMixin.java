package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.mixin;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockElement;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockElement.Deserializer.class)
public class BlockElement_DeserializerMixin {
    @Shadow
    private static Vector3f getVector3f(JsonObject json, String field) { return null; }

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private static void injectGetPosition(JsonObject json, String field, CallbackInfoReturnable<Vector3f> cir) {
        cir.setReturnValue(getVector3f(json, field));
    }
}

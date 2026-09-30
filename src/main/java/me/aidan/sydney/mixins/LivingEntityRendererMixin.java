package me.aidan.sydney.mixins;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.modules.impl.visuals.EntityModifierModule;
import me.aidan.sydney.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        NoRenderModule module = Sydney.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (module.isToggled() && module.corpses.getValue() && entity.deathTime > 0) info.cancel();
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 1))
    private void render$scale(Args args, T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        EntityModifierModule module = Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class);
        if (!module.isToggled() || !module.players.getValue() || !((Object) this instanceof PlayerEntityRenderer)) return;

        float scale = module.playerScale.getValue().floatValue();
        args.set(0, -scale);
        args.set(1, -scale);
        args.set(2, scale);
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void render$model(Args args, T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        EntityModifierModule module = Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class);
        if (!module.isToggled() || !module.players.getValue() || !((Object) this instanceof PlayerEntityRenderer)) return;

        args.set(4, module.playerColor.getColor().getRGB());
    }
}
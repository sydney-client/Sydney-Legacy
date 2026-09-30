package me.aidan.sydney.mixins;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.modules.impl.visuals.EntityModifierModule;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EndCrystalEntityRenderer.class)
public class EndCrystalEntityRendererMixin {
    @Mutable @Shadow @Final private static RenderLayer END_CRYSTAL;

    @Shadow @Final private static Identifier TEXTURE;

    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void render$HEAD(EndCrystalEntity entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo info) {
        if (Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class).crystals.getValue()) {
            END_CRYSTAL = RenderLayer.getEntityTranslucent(TEXTURE);
            return;
        }

        END_CRYSTAL = RenderLayer.getEntityCutoutNoCull(TEXTURE);
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 0))
    private void render$scale(Args args, EndCrystalEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        EntityModifierModule module = Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class);
        if (module.isToggled() && module.crystals.getValue()) {
            float scale = 2.0F * module.crystalScale.getValue().floatValue();
            args.set(0, scale);
            args.set(1, scale);
            args.set(2, scale);
        }
    }

    @Inject(method = "getYOffset(Lnet/minecraft/entity/decoration/EndCrystalEntity;F)F", at = @At("HEAD"), cancellable = true)
    private static void getYOffset(EndCrystalEntity entity, float tickDelta, CallbackInfoReturnable<Float> info) {
        EntityModifierModule module = Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class);
        if (module.isToggled() && module.crystals.getValue()) {
            float age = entity.endCrystalAge + tickDelta;
            float bounce = MathHelper.sin(age * 0.2F) / 2.0F + 0.5F;
            bounce = (bounce * bounce + bounce) * 0.4F * module.crystalBounce.getValue().floatValue();

            info.setReturnValue(bounce - 1.4F);
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"), index = 0)
    private float render$crystalSpeed(float angle) {
        EntityModifierModule module = Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class);
        return module.isToggled() && module.crystals.getValue() ? angle * module.crystalSpeed.getValue().floatValue() : angle;
    }
}

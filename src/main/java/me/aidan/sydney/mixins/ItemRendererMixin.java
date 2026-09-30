package me.aidan.sydney.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.aidan.sydney.Sydney;
import me.aidan.sydney.modules.impl.visuals.EntityModifierModule;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.Color;
import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow @Final private ItemColors colors;

    @WrapOperation(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V"))
    private void renderItem$renderBakedItemModel(ItemRenderer renderer, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertexConsumer, Operation<Void> original, @Local(argsOnly = true) ModelTransformationMode renderMode) {
        EntityModifierModule module = Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class);
        Color color = module.isToggled() && module.items.getValue() && (renderMode != ModelTransformationMode.GUI || module.itemGlobal.getValue()) ? module.itemColor.getColor() : null;
        if (color == null) {
            original.call(renderer, model, stack, light, overlay, matrices, vertexConsumer);
            return;
        }

        Random random = Random.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            sydney$renderBakedItemQuads(matrices, vertexConsumer, model.getQuads(null, direction, random), stack, color, light, overlay);
        }

        random.setSeed(42L);
        sydney$renderBakedItemQuads(matrices, vertexConsumer, model.getQuads(null, null, random), stack, color, light, overlay);
    }

    @Unique
    private void sydney$renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, ItemStack stack, Color color, int light, int overlay) {
        MatrixStack.Entry entry = matrices.peek();
        float alpha = color.getAlpha() / 255.0F;
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;

        for (BakedQuad bakedQuad : quads) {
            if (bakedQuad.hasColor()) {
                int tint = colors.getColor(stack, bakedQuad.getColorIndex());
                vertexConsumer.quad(entry, bakedQuad,
                        ColorHelper.Argb.getRed(tint) / 255.0F * red,
                        ColorHelper.Argb.getGreen(tint) / 255.0F * green,
                        ColorHelper.Argb.getBlue(tint) / 255.0F * blue,
                        ColorHelper.Argb.getAlpha(tint) / 255.0F * alpha, light, overlay);
            } else {
                vertexConsumer.quad(entry, bakedQuad, red, green, blue, alpha, light, overlay);
            }
        }
    }
}
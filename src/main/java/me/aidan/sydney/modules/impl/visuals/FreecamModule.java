package me.aidan.sydney.modules.impl.visuals;

import lombok.Getter;
import lombok.Setter;
import me.aidan.sydney.events.SubscribeEvent;
import me.aidan.sydney.events.impl.KeyboardTickEvent;
import me.aidan.sydney.events.impl.TickEvent;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.modules.RegisterModule;
import me.aidan.sydney.settings.impl.NumberSetting;
import me.aidan.sydney.utils.minecraft.MovementUtils;
import me.aidan.sydney.utils.system.MathUtils;
import org.joml.Vector2d;

@Getter @Setter
@RegisterModule(name = "Freecam", description = "Allows you to move your camera anywhere you want without restriction.", category = Module.Category.VISUALS)
public class FreecamModule extends Module {
    public NumberSetting horizontalSpeed = new NumberSetting("HorizontalSpeed", "The speed at which your camera will move horizontally.", 1.0f, 0.1f, 3.0f);
    public NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", "The speed at which your camera will move vertically.", 0.5f, 0.1f, 3.0f);

    private float freeYaw, freePitch;
    private float prevFreeYaw, prevFreePitch;

    private double freeX, freeY, freeZ;
    private double prevFreeX, prevFreeY, prevFreeZ;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        prevFreeYaw = freeYaw;
        prevFreePitch = freePitch;

        freeYaw = mc.player.getYaw();
        freePitch = mc.player.getPitch();
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent event) {
        if (mc.player == null) return;

        Vector2d motion = MovementUtils.forward(horizontalSpeed.getValue().doubleValue());

        prevFreeX = freeX;
        prevFreeY = freeY;
        prevFreeZ = freeZ;

        freeX += motion.x;
        freeZ += motion.y;

        if (mc.options.jumpKey.isPressed()) freeY += verticalSpeed.getValue().doubleValue();
        if (mc.options.sneakKey.isPressed()) freeY -= verticalSpeed.getValue().doubleValue();

        mc.player.input.pressingForward = false;
        mc.player.input.pressingBack = false;
        mc.player.input.pressingLeft = false;
        mc.player.input.pressingRight = false;
        mc.player.input.jumping = false;
        mc.player.input.sneaking = false;
        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            setToggled(false);
            return;
        }

        mc.chunkCullingEnabled = false;

        freeYaw = prevFreeYaw = mc.player.getYaw();
        freePitch = prevFreePitch = mc.player.getPitch();

        freeX = prevFreeX = mc.player.getX();
        freeY = prevFreeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        freeZ = prevFreeZ = mc.player.getZ();
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) return;

        mc.chunkCullingEnabled = true;
    }

    public float getFreeYaw() {
        return (float) MathUtils.interpolate(prevFreeYaw, freeYaw, mc.getRenderTickCounter().getTickDelta(true));
    }

    public float getFreePitch() {
        return (float) MathUtils.interpolate(prevFreePitch, freePitch, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFreeX() {
        return MathUtils.interpolate(prevFreeX, freeX, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFreeY() {
        return MathUtils.interpolate(prevFreeY, freeY, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFreeZ() {
        return MathUtils.interpolate(prevFreeZ, freeZ, mc.getRenderTickCounter().getTickDelta(true));
    }
}

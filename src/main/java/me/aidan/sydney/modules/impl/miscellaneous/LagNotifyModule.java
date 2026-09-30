package me.aidan.sydney.modules.impl.miscellaneous;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.events.SubscribeEvent;
import me.aidan.sydney.events.impl.PacketReceiveEvent;
import me.aidan.sydney.events.impl.RenderOverlayEvent;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.modules.RegisterModule;
import me.aidan.sydney.settings.impl.BooleanSetting;
import me.aidan.sydney.settings.impl.ColorSetting;
import me.aidan.sydney.utils.color.ColorUtils;
import me.aidan.sydney.utils.system.MathUtils;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

@RegisterModule(name = "LagNotify", description = "Notifies you when you lag.", category = Module.Category.MISCELLANEOUS)
public class LagNotifyModule extends Module {
    BooleanSetting server = new BooleanSetting("Server", "Notifies you when the server stops responding.", true);
    BooleanSetting lagback = new BooleanSetting("Lagback", "Notifies you when you lagback.", true);
    ColorSetting color = new ColorSetting("Color", "The color of the notification text.", ColorUtils.getDefaultOutlineColor());

    Vec3d lagPos = null;
    double lagDistance;
    long lagTime = System.currentTimeMillis();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if(getNull()) return;

        if(event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            lagPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            lagDistance = mc.player.getPos().distanceTo(lagPos);
            lagTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {
        if(getNull()) return;

        int width = mc.getWindow().getScaledWidth() / 2, height = mc.getWindow().getScaledHeight() / 4;
        boolean flag = false;

        if(server.getValue() && Sydney.SERVER_MANAGER.getResponseTimer().hasTimeElapsed(1000)) {
            String text = "Detected server not responding for " + MathUtils.round(Sydney.SERVER_MANAGER.getResponseTimer().timeElapsed()/1000f, 1) + "s.";
            Sydney.FONT_MANAGER.drawTextWithShadow(event.getContext(),text, width - Sydney.FONT_MANAGER.getWidth(text) / 2, height - Sydney.FONT_MANAGER.getHeight(), color.getColor());
            flag = true;
        }

        if(lagback.getValue() && System.currentTimeMillis() - lagTime < 3000) {
            String text = "Detected lagback of " + MathUtils.round(lagDistance, 1) + " blocks " + MathUtils.round((System.currentTimeMillis() - lagTime) / 1000f, 1) + "s.";
            Sydney.FONT_MANAGER.drawTextWithShadow(event.getContext(),text, width - Sydney.FONT_MANAGER.getWidth(text) / 2, height - Sydney.FONT_MANAGER.getHeight() + (flag ? Sydney.FONT_MANAGER.getHeight() : 0), color.getColor());
        }
    }
}

package me.aidan.sydney.modules.impl.combat;

import me.aidan.sydney.events.SubscribeEvent;
import me.aidan.sydney.events.impl.AttackEntityEvent;
import me.aidan.sydney.mixins.accessors.ClientPlayerEntityAccessor;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.modules.RegisterModule;
import me.aidan.sydney.settings.impl.ModeSetting;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@RegisterModule(name = "Criticals", description = "Makes every one of your hits a critical.", category = Module.Category.COMBAT)
public class CriticalsModule extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to achieve the critical hits.", "Packet", new String[]{"Packet", "Strict", "Grim"});

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getTarget() == null || event.getTarget() instanceof EndCrystalEntity) return;

        if (mc.player.isOnGround() || mc.player.getAbilities().flying || mode.getValue().equalsIgnoreCase("Grim") && !mc.player.isInLava() && !mc.player.isSubmergedInWater()) {
            switch (mode.getValue()) {
                case "Packet" -> {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.05, mc.player.getZ(), false));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.03, mc.player.getZ(), false));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
                }
                case "Strict" -> {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.11, mc.player.getZ(), false));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.1100013579, mc.player.getZ(), false));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0000013579, mc.player.getZ(), false));
                }
                case "Grim" -> {
                    if (!mc.player.isOnGround()) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.000001, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false));
                    }
                }
            }

            ((ClientPlayerEntityAccessor) mc.player).setLastOnGround(false);
            mc.player.addCritParticles(event.getTarget());
        }
    }

    @Override
    public String getMetaData() {
        return mode.getValue();
    }
}

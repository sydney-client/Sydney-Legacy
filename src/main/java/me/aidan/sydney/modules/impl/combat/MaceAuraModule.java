package me.aidan.sydney.modules.impl.combat;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.events.SubscribeEvent;
import me.aidan.sydney.events.impl.PacketReceiveEvent;
import me.aidan.sydney.events.impl.PlayerMoveEvent;
import me.aidan.sydney.events.impl.PlayerUpdateEvent;
import me.aidan.sydney.events.impl.UpdateMovementEvent;
import me.aidan.sydney.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.modules.RegisterModule;
import me.aidan.sydney.settings.impl.BooleanSetting;
import me.aidan.sydney.settings.impl.CategorySetting;
import me.aidan.sydney.settings.impl.ModeSetting;
import me.aidan.sydney.settings.impl.NumberSetting;
import me.aidan.sydney.utils.minecraft.InventoryUtils;
import me.aidan.sydney.utils.minecraft.MovementUtils;
import me.aidan.sydney.utils.minecraft.PositionUtils;
import me.aidan.sydney.utils.rotations.RotationUtils;
import me.aidan.sydney.utils.system.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@RegisterModule(name = "MaceAura", description = "Automatically maces people after falling.", category = Module.Category.COMBAT)
public class MaceAuraModule extends Module {
    public BooleanSetting silent = new BooleanSetting("Silent", "Silently switch to the mace so others can't see you holding it.", false);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Whether or not to rotate when attacking the target.", true);
    public NumberSetting range = new NumberSetting("Range", "Max distance to consider a player valid target.", 20.0, 10.0, 30.0);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Max distance to attack with the mace.", 6.0, 0.0, 12.0);

    // ← New setting: delay between mace swings (ms)
    public NumberSetting delay = new NumberSetting(  "Delay",     "Minimum delay between mace hits in milliseconds.", 1000.0, 0.0, 5000.0);

    public BooleanSetting rubberband = new BooleanSetting("Rubberband", "Whether to rubberband back after macing.", false);
    public BooleanSetting movementAssist = new BooleanSetting("MovementAssist", "Moves you towards your target while falling.", true);
    public NumberSetting extrapolation = new NumberSetting( "Extrapolation",  "Extrapolates target position for movement assist.",  new BooleanSetting.Visibility(movementAssist, true), 0, 0, 20);

    private PlayerEntity target = null;
    private final Timer timer = new Timer();
    private boolean attacking = false;
    private boolean shouldAttack = false;
    private boolean reset = false;

    @Override
    public void onEnable() {
        if (getNull()) return;
        if (mc.player.isGliding()) mc.player.stopGliding();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && reset) {
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(
                    mc.player.isOnGround(),
                    mc.player.horizontalCollision
                )
            );
            reset = false;
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!movementAssist.getValue() || mc.player.isOnGround() || target == null) return;
        if (mc.player.distanceTo(target) <= attackRange.getValue().floatValue()) return;
        if (!timer.hasTimeElapsed(delay.getValue().longValue())) return;  // :contentReference[oaicite:0]{index=0}

        Vec3d vec3d = PositionUtils.extrapolate(target, extrapolation.getValue().intValue()).getCenter();
        MovementUtils.moveTowards(event, vec3d, MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        target = getTarget();
        attacking = false;
        shouldAttack = false;
        if (target == null || mc.player.distanceTo(target) > attackRange.getValue().floatValue()) return;
        if (!silent.getValue() && !mc.player.getMainHandStack().getItem().equals(Items.MACE)) return;

        int slot = InventoryUtils.find(Items.MACE, 0, 8);
        if (slot == -1) return;

        if (rotate.getValue()) Sydney.ROTATION_MANAGER.rotate(RotationUtils.getRotations(target), this);
        attacking = true;

        // Use custom delay instead of hard-coded 1000 ms
        if (!timer.hasTimeElapsed(delay.getValue().longValue())) return;
        shouldAttack = true;
    }

    @SubscribeEvent
    public void onUpdateMovement$POST(UpdateMovementEvent.Post event) {
        if (mc.player == null || mc.world == null || !shouldAttack || !attacking || target == null) {
            shouldAttack = false;
            return;
        }

        int slot = InventoryUtils.find(Items.MACE, 0, 8);
        int previousSlot = mc.player.getInventory().selectedSlot;
        Vec3d previous = mc.player.getPos();

        if (silent.getValue()) switchSlot(slot);
        mc.interactionManager.attackEntity(mc.player, target);
        if (silent.getValue()) switchSlot(previousSlot);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (rubberband.getValue()) doRubberband(previous);
        shouldAttack = false;
        timer.reset();  // reset timer after each swing :contentReference[oaicite:2]{index=2}
    }

    private void switchSlot(int slot) {
        mc.player.getInventory().selectedSlot = slot;
        ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).invokeSyncSelectedSlot();
    }

    private void doRubberband(Vec3d previous) {
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision)
        );
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), mc.player.getY() + 1, mc.player.getZ(),
                false, mc.player.horizontalCollision
            )
        );
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.PositionAndOnGround(
                previous.x, previous.y, previous.z,
                false, mc.player.horizontalCollision
            )
        );
        reset = true;
    }

    private PlayerEntity getTarget() {
        PlayerEntity optimalTarget = null;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (!player.isAlive() || player.getHealth() <= 0.0f) continue;
            if (mc.player.squaredDistanceTo(player) > MathHelper.square(range.getValue().doubleValue())) continue;
            if (Sydney.FRIEND_MANAGER.contains(player.getName().getString())) continue;

            if (optimalTarget == null ||
                mc.player.squaredDistanceTo(player) < mc.player.squaredDistanceTo(optimalTarget)) {
                optimalTarget = player;
            }
        }
        return optimalTarget;
    }
                }
            

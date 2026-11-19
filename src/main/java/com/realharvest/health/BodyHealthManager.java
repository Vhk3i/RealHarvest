package com.realharvest.health;

import com.realharvest.RealHarvest;
import com.realharvest.network.ModNetwork;
import com.realharvest.network.packet.S2CSyncBodyHealthPacket;
import com.realharvest.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BodyHealthManager {
    private static final String NBT_KEY = "RealHarvestBodyHealth";

    private static CompoundTag getTag(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(NBT_KEY)) {
            root.put(NBT_KEY, new CompoundTag());
        }
        return root.getCompound(NBT_KEY);
    }

    public static InjuryType getInjury(Player player, BodyPart part) {
        int ord = getTag(player).getInt(part.name());
        InjuryType[] vals = InjuryType.values();
        return (ord >= 0 && ord < vals.length) ? vals[ord] : InjuryType.NONE;
    }

    public static void setInjury(Player player, BodyPart part, InjuryType type) {
        getTag(player).putInt(part.name(), type.ordinal());
        if (player instanceof ServerPlayer sp) {
            syncToClient(sp);
        }
    }

    public static void clearAll(Player player) {
        for (BodyPart p : BodyPart.values()) {
            getTag(player).putInt(p.name(), InjuryType.NONE.ordinal());
        }
        if (player instanceof ServerPlayer sp) {
            syncToClient(sp);
        }
    }

    public static void syncToClient(ServerPlayer player) {
        BodyPart[] parts = BodyPart.values();
        int[] states = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            states[i] = getInjury(player, parts[i]).ordinal();
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncBodyHealthPacket(states));
    }

    public static void tryHealLimb(ServerPlayer player, BodyPart part) {
        InjuryType injury = getInjury(player, part);
        if (injury == InjuryType.NONE) return;

        Item required = (injury == InjuryType.CUT) ? ModItems.BANDAGE.get() : ModItems.SPLINT.get();
        String itemName = (injury == InjuryType.CUT) ? "Бинт" : "Шина";

        if (!player.isCreative() && !consumeItem(player, required)) {
            player.displayClientMessage(Component.literal("Нужен предмет: " + itemName), true);
            return;
        }

        setInjury(player, part, InjuryType.NONE);
        player.heal(3.0F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0F, 1.1F);
        player.displayClientMessage(Component.literal("Вылечено: " + part.getDisplayName()), true);
    }

    private static boolean consumeItem(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(item)) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || event.getAmount() < 2.0F) return;

        if (event.getSource().is(DamageTypes.FALL)) {
            BodyPart leg = player.getRandom().nextBoolean() ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
            InjuryType type = event.getAmount() >= 5.0F ? InjuryType.FRACTURE : InjuryType.CUT;
            setInjury(player, leg, type);
            player.displayClientMessage(Component.literal("Травма: " + leg.getDisplayName() + " — " + type.getLabel()), true);
        } else if (event.getSource().getEntity() != null && player.getRandom().nextFloat() < 0.45F) {
            BodyPart[] parts = BodyPart.values();
            BodyPart hit = parts[player.getRandom().nextInt(parts.length)];
            InjuryType type = (player.getRandom().nextFloat() < 0.25F) ? InjuryType.FRACTURE : InjuryType.CUT;
            setInjury(player, hit, type);
            player.displayClientMessage(Component.literal("Травма: " + hit.getDisplayName() + " — " + type.getLabel()), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.tickCount % 40 != 0) return;

        boolean bleeding = false;
        boolean legHurt = false;
        boolean armHurt = false;

        for (BodyPart p : BodyPart.values()) {
            InjuryType t = getInjury(player, p);
            if (t == InjuryType.CUT) bleeding = true;
            if ((p == BodyPart.LEFT_LEG || p == BodyPart.RIGHT_LEG) && t != InjuryType.NONE) legHurt = true;
            if ((p == BodyPart.LEFT_ARM || p == BodyPart.RIGHT_ARM) && t != InjuryType.NONE) armHurt = true;
        }

        if (bleeding && player.tickCount % 120 == 0 && player.getHealth() > 2.0F) {
            player.hurt(player.damageSources().generic(), 1.0F);
        }
        if (legHurt) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
        }
        if (armHurt) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) syncToClient(sp);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) clearAll(sp);
    }
}

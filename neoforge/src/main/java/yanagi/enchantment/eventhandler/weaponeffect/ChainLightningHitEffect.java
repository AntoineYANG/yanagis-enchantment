package yanagi.enchantment.eventhandler.weaponeffect;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.entity.magic.ChainLightningEntity;
import yanagi.enchantment.entry.YEEnchantments;

public abstract class ChainLightningHitEffect {

    private static final String CD_ROOT = YanagisEnchantment.of("data:chain_lightning_enchantment");
    private static final String CD_WEAPON_NEXT = "cooldown_off_after";

    public static int resolveCooldownTicks(int level) {
        return Math.max(1, 100 - 10 * (level - 1));
    }
    public static int resolveChainLightningLevel(int level) {
        return level;
    }

    public static float apply(LivingIncomingDamageEvent event) {
        DamageSource entitySource = event.getSource();
        Entity target = event.getEntity();
        Entity attacker = entitySource.getEntity();
        float originalDamage = event.getAmount();
        if (originalDamage < 1 || entitySource.is(DamageTypeTags.IS_PROJECTILE)) {
            return 0;
        }
        if (attacker != null && target != null) {
            if (attacker instanceof LivingEntity atk && target instanceof LivingEntity tar) {
                ItemStack weapon = atk.getMainHandItem();
                if (!atk.getMainHandItem().isEmpty() && weapon != null) {
                    Registry<Enchantment> enchantments = atk.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                    int level = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.CHAIN_LIGHTNING), atk);
                    if (level > 0) {
                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                        long now = server.overworld().getGameTime();
                        if (isWeaponOffCooldown(weapon, now)) {
                            markWeaponUsed(weapon, now + resolveCooldownTicks(level));
                            ChainLightningEntity.generateChainLightningEntityOnTarget(atk, attacker.level(), new ChainLightningEntity.StrikeTarget(tar), resolveChainLightningLevel(level) - 1);
                        }
                    }
                }
            }
        }
        return 0;
    }

    private static boolean isWeaponOffCooldown(ItemStack stack, long now) {
        long time = readWeaponCooldownOffTick(stack);
        return now >= time;
    }

    private static long readWeaponCooldownOffTick(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 0;
        }
        CompoundTag root = data.copyTag();
        CompoundTag tag = root.getCompound(CD_ROOT);
        return tag.getLong(CD_WEAPON_NEXT);
    }

    private static void markWeaponUsed(ItemStack stack, long reusableAfter) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = (data != null) ? data.copyTag() : new CompoundTag();
        CompoundTag tag = root.getCompound(CD_ROOT);
        tag.putLong(CD_WEAPON_NEXT, reusableAfter);
        root.put(CD_ROOT, tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }


}

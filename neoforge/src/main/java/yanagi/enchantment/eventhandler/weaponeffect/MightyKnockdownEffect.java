package yanagi.enchantment.eventhandler.weaponeffect;

import java.util.Random;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.entry.YEEnchantments;

public abstract class MightyKnockdownEffect {

    public static float resolveBbSizeThreshold(int level) {
        return 1.33f + 0.67f * (level - 1);
    }
    public static float resolveExtraDmgMultiplier(int level, double bbSizeRatio) {
        float basicDmg = 0.2f + 0.1f * (level - 1);
        float extraDmg = (float)(basicDmg / Math.min(0.5, bbSizeRatio));
        return basicDmg + extraDmg;
    }
    public static float resolveStunChance(int level) {
        return 0.25f + 0.05f * (level - 1);
    }
    public static int resolveStunTicks(int level) {
        return 16 + (int)(3.6 * (level - 1));
    }

    public static final Random random = new Random();

    public static float apply(LivingIncomingDamageEvent event) {
        DamageSource entitySource = event.getSource();
        Entity target = event.getEntity();
        Entity attacker = entitySource.getEntity();
        float originalDamage = event.getAmount();
        if (originalDamage < 1) {
            return 0;
        }
        if (attacker != null && target != null) {
            if (attacker instanceof LivingEntity atk && target instanceof LivingEntity tar) {
                if (!atk.getMainHandItem().isEmpty()) {
                    Registry<Enchantment> enchantments = atk.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                    int level = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.MIGHTY_KNOCKDOWN), atk);
                    if (level > 0) {
                        double atkSize = bbSize(atk);
                        double tarSize = bbSize(tar);
                        double ratio = tarSize / atkSize;
                        if (ratio <= resolveBbSizeThreshold(level)) {
                            float dmg = resolveExtraDmgMultiplier(level, ratio) * originalDamage;
                            event.setAmount(dmg);
                            // System.out.println("!!!! attack " + attacker.getName().getString() + " -> " + target.getName().getString() + " (" + originalDamage + "->" + dmg + ")");
                            if (random.nextDouble() <= resolveStunChance(level)) {
                                // stun
                                int ticks = resolveStunTicks(level);
                                tar.addEffect(new MobEffectInstance(YEEffects.STUN_EFFECT, ticks, 1));
                                // System.out.println("!!!! stun " + ticks + " ticks");
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    private static double bbSize(LivingEntity e) {
        AABB box = e.getBoundingBox();
        return (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
    }

}

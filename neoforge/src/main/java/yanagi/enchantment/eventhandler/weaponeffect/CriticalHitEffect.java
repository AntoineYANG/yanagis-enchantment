package yanagi.enchantment.eventhandler.weaponeffect;

import java.util.Random;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import yanagi.enchantment.entry.YEEnchantments;

public abstract class CriticalHitEffect {

    public static float resolveCriticalRate(int level) {
        return level == 0 ? 0.05f : level * 0.1f;
    }
    public static float resolveCriticalDamageMultiplier(int level) {
        return 1.50f + 0.25f * level;
    }

    protected static Random random = new Random();

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
                if (!atk.getMainHandItem().isEmpty()) {
                    Registry<Enchantment> enchantments = atk.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                    int cRateLvl = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.CRITICAL_RATE), atk);
                    int cDmgLvl = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.CRITICAL_DAMAGE), atk);
                    if (cRateLvl + cDmgLvl > 0) {
                        float cRate = resolveCriticalRate(cRateLvl);
                        float cDmgM = resolveCriticalDamageMultiplier(cDmgLvl);
                        double r = random.nextDouble();
                        if (r <= cRate) {
                            float dmg = originalDamage * cDmgM;
                            event.setAmount(dmg);
                            tar.level().addAlwaysVisibleParticle(ParticleTypes.CRIT, tar.getX(), tar.getY() + tar.getEyeHeight(), tar.getZ(), 0.0D, 0.0D, 0.0D);
                        }
                    }
                }
            }
        }
        return 0;
    }

}

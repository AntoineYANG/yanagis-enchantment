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

public abstract class LifeStealHitEffect {

    public static float resolveLifeStealRate(int level) {
        return (level + 1) * 0.05f;
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
            if (attacker instanceof LivingEntity atk && target instanceof LivingEntity) {
                if (!atk.getMainHandItem().isEmpty()) {
                    Registry<Enchantment> enchantments = atk.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                    int level = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.LIFE_STEAL), atk);
                    if (level > 0) {
                        float stealRate = resolveLifeStealRate(level);
                        float steal = stealRate * originalDamage;
                        if (steal >= 1) {
                            atk.heal(steal);
                            atk.level().addAlwaysVisibleParticle(ParticleTypes.HEART, atk.getX(), atk.getY() + atk.getEyeHeight(), atk.getZ(), 0.0D, 0.0D, 0.0D);
                        }
                    }
                }
            }
        }
        return 0;
    }

}

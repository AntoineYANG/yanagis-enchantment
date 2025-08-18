package yanagi.enchantment.eventhandler.weaponeffect;

import java.util.Random;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import yanagi.enchantment.effect.CrescendoEffect;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.entry.YEEnchantments;

public abstract class BattleRhythmHitEffect {

    public static final int MAX_STACK = 8;
    public static final int DURATION = 6 * 20;

    public static final Random random = new Random();

    public static float apply(LivingIncomingDamageEvent event) {
        DamageSource entitySource = event.getSource();
        Entity target = event.getEntity();
        Entity attacker = entitySource.getEntity();
        float originalDamage = event.getAmount();
        int stacks = YEEffects.getCrescendoStacks(attacker);
        if (entitySource.is(DamageTypeTags.IS_PROJECTILE)) {
            return 0;
        }
        if (attacker != null) {
            if (attacker instanceof LivingEntity atk) {
                if (!atk.getMainHandItem().isEmpty()) {
                    Registry<Enchantment> enchantments = atk.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                    int level = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.BATTLE_RHYTHM), atk);
                    if (originalDamage >= 1 && target != null) {
                        if (level > 0) {
                            stacks = Math.min(stacks + 1, MAX_STACK);
                            atk.addEffect(new MobEffectInstance(YEEffects.CRESCENDO_EFFECT, DURATION, stacks - 1));
                        }
                        if (stacks >= 1) {
                            float dmg = CrescendoEffect.resolveExtraDmgMultiplier(level, stacks) * originalDamage;
                            event.setAmount(originalDamage + dmg);
                        }
                    } else {
                        if (level > 0) {
                            stacks -= 1;
                            if (stacks < 1) {
                                float dmg = CrescendoEffect.resolveExtraDmgMultiplier(level, stacks) * originalDamage;
                                event.setAmount(originalDamage + dmg);
                                atk.removeEffect(YEEffects.CRESCENDO_EFFECT);
                            } else {
                                atk.addEffect(new MobEffectInstance(YEEffects.CRESCENDO_EFFECT, DURATION, stacks - 1));
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

}

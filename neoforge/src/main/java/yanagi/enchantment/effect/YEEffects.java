package yanagi.enchantment.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredRegister;
import yanagi.enchantment.YanagisEnchantment;

public class YEEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, YanagisEnchantment.MOD_ID);

    // beneficial
    public static final Holder<MobEffect> CRESCENDO_EFFECT = MOB_EFFECTS.register(CrescendoEffect.name, CrescendoEffect::new);

    // harmful
    public static final Holder<MobEffect> SHOCKED_EFFECT = MOB_EFFECTS.register(ShockedEffect.name, ShockedEffect::new);
    public static final Holder<MobEffect> STUN_EFFECT = MOB_EFFECTS.register(StunnedEffect.name, StunnedEffect::new);

    public static boolean isInterrupted(Entity e) {
        return isShocked(e) || isStunned(e);
    }

    public static boolean isShocked(Entity e) {
        if (e instanceof LivingEntity le) {
            return le.hasEffect(SHOCKED_EFFECT);
        }
        return false;
    }

    public static boolean isStunned(Entity e) {
        if (e instanceof LivingEntity le) {
            return le.hasEffect(STUN_EFFECT);
        }
        return false;
    }

    public static int getCrescendoStacks(Entity e) {
        if (e instanceof LivingEntity le) {
            if (le.hasEffect(CRESCENDO_EFFECT)) {
                MobEffectInstance ei = le.getEffect(CRESCENDO_EFFECT);
                return ei == null ? 0 : (ei.getAmplifier() + 1);
            }
        }
        return 0;
    }

}

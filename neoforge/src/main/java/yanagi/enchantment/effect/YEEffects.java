package yanagi.enchantment.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredRegister;
import yanagi.enchantment.YanagisEnchantment;

public class YEEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, YanagisEnchantment.MODID);

    public static final Holder<MobEffect> STUN_EFFECT = MOB_EFFECTS.register(StunnedEffect.name, StunnedEffect::new);

    public static boolean isStunned(Entity e) {
        if (e instanceof LivingEntity le) {
            return le.hasEffect(STUN_EFFECT);
        }
        return false;
    }

}

package yanagi.enchantment.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import yanagi.enchantment.YanagisEnchantment;

public class StunnedEffect extends MobEffect {

    public static final String name = "stunned";

    public static final ResourceLocation ZERO_MV_SPD_MODIFIER = YanagisEnchantment.prefix("zero_move_spd");
    public static final ResourceLocation ZERO_ATK_SPD_MODIFIER = YanagisEnchantment.prefix("zero_attack_spd");

    public StunnedEffect() {
        super(MobEffectCategory.HARMFUL, 0xF9BE32);
    }
    
    // If this returns false when shouldApplyEffectTickThisTick returns true, the effect will immediately be removed
    @Override
    public boolean applyEffectTick(@SuppressWarnings("null") LivingEntity entity, int amplifier) {
        if (entity == null) {
            return false;
        }

        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }

        // clear horizontal speed
        Vec3 v = entity.getDeltaMovement();
        if (v.x != 0 || v.z != 0) {
            entity.setDeltaMovement(0.0, v.y, 0.0);
            entity.hurtMarked = true; // synchronize to client
        }

        // clear AI goals
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            if (mob.getTarget() != null) {
                mob.setTarget(null);
            }
            mob.getLookControl().setLookAt(mob.getX(), mob.getEyeY(), mob.getZ());
        }

        return true;
    }
    
    // Whether the effect should apply this tick. Used e.g. by the Regeneration effect that only applies
    // once every x ticks, depending on the tick count and amplifier.
    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }
    
    // Utility method that is called when the effect is first added to the entity.
    // This does not get called again until all instances of this effect have been removed from the entity.
    @Override
    public void onEffectAdded(@SuppressWarnings("null") LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setAggressive(false);
        }
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }
        entity.setSprinting(false);
    }

    // Utility method that is called when the effect is added to the entity.
    // This gets called every time this effect is added to the entity.
    @Override
    public void onEffectStarted(@SuppressWarnings("null") LivingEntity entity, int amplifier) {
        // clear movement speed & attack speed
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED, ZERO_MV_SPD_MODIFIER,
            -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED, ZERO_ATK_SPD_MODIFIER,
            -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }
    }

}

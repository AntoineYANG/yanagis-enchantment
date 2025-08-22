package yanagi.enchantment.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yanagi.enchantment.YanagisEnchantment;

public class ShockedEffect extends MobEffect {

    public static final String name = "shocked";

    public static final ResourceLocation ZERO_MV_SPD_MODIFIER = YanagisEnchantment.prefix("shock_zero_move_spd");
    public static final ResourceLocation ZERO_ATK_SPD_MODIFIER = YanagisEnchantment.prefix("shock_zero_attack_spd");

    public ShockedEffect() {
        super(MobEffectCategory.HARMFUL, 0xD46FED);
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

        // clear speed
        entity.setDeltaMovement(0.0, 0.0, 0.0);
        entity.hurtMarked = true; // synchronize to client

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

        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }
    }

}

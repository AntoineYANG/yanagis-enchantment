package yanagi.enchantment.effect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.entry.YEEnchantments;

public class CrescendoEffect extends MobEffect {

    public static final String name = "crescendo";

    public static final ResourceLocation EXTRA_MV_SPD_MODIFIER = YanagisEnchantment.prefix("crescendo_move_spd");
    public static final ResourceLocation EXTRA_ATK_SPD_MODIFIER = YanagisEnchantment.prefix("crescendo_attack_spd");

    public static float resolveExtraDmgMultiplier(int level, int stacks) {
        return (0.03f + 0.005f * (level - 1)) * stacks;
    }
    public static float resolveMvSpdMultiplier(int level, int stacks) {
        return (0.018f + 0.001f * (level - 1)) * stacks;
    }
    public static float resolveAtkSpdMultiplier(int level, int stacks) {
        return (0.02f + 0.003f * (level - 1)) * stacks;
    }
    public static int getEntityBattleRhythmLevel(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        if (entity instanceof LivingEntity e) {
            if (e.getMainHandItem().isEmpty()) {
                return 0;
            }
            Registry<Enchantment> enchantments = e.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            int level = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.BATTLE_RHYTHM), e);
            return level;
        }
        return 0;
    }

    public CrescendoEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC89C3D);
    }
    
    // If this returns false when shouldApplyEffectTickThisTick returns true, the effect will immediately be removed
    @Override
    public boolean applyEffectTick(@SuppressWarnings("null") LivingEntity entity, int amplifier) {
        int level = getEntityBattleRhythmLevel(entity);
        return level > 0;
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
    }

    // Utility method that is called when the effect is added to the entity.
    // This gets called every time this effect is added to the entity.
    @Override
    public void onEffectStarted(@SuppressWarnings("null") LivingEntity entity, int amplifier) {
        int level = getEntityBattleRhythmLevel(entity);
        if (level == 0) {
            return;
        }
        int stacks = amplifier + 1;
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, EXTRA_MV_SPD_MODIFIER, resolveMvSpdMultiplier(level, stacks), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, EXTRA_ATK_SPD_MODIFIER, resolveAtkSpdMultiplier(level, stacks), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

}

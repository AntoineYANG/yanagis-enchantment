package yanagi.enchantment.effect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yanagi.enchantment.entry.YEEnchantments;
import yanagi.enchantment.eventhandler.weaponeffect.BetTheFarmShootEffect;

public class BetTheFarmEffect extends MobEffect {

    public static final String name = "bet_the_farm";

    public static int getEntityBTFLevel(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        if (entity instanceof LivingEntity e) {
            if (e.getMainHandItem().isEmpty()) {
                return 0;
            }
            Registry<Enchantment> enchantments = e.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            // int level = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.BET_THE_FARM), e);
            // return level;
            return 0;
        }
        return 0;
    }

    public BetTheFarmEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC89C3D);
    }
    
    // If this returns false when shouldApplyEffectTickThisTick returns true, the effect will immediately be removed
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity == null || !entity.isUsingItem()) {
            return false;
        }
        ItemStack is = entity.getUseItem();
        Item item = is.getItem();
        if (!(item instanceof BowItem)) {
            return false;
        }
        int lvl = getEntityBTFLevel(entity);
        if (lvl <= 0) {
            return false;
        }
        ItemStack off = entity.getOffhandItem();
        if (off.isEmpty() || !off.is(ItemTags.ARROWS)) {
            return false;
        }
        int arrowCount = off.getCount();
        return BetTheFarmShootEffect.isOffhandArrowCountValid(arrowCount);
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
        int level = getEntityBTFLevel(entity);
        if (level == 0) {
            return;
        }
    }

}

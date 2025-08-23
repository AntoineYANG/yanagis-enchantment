package yanagi.enchantment.entry;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import yanagi.enchantment.YanagisEnchantment;

public class YETags {

    public static final class Items {

        public static final TagKey<Item> MELEE_WEAPONS = tag("melee_weapons");

        private static @NotNull TagKey<Item> tag(@NotNull ResourceLocation resourceLocation) {
            return ItemTags.create(resourceLocation);
        }

        private static @NotNull TagKey<Item> tag(@NotNull String name) {
            return tag(YanagisEnchantment.prefix(name));
        }
        
    }

    public static final class DamageTypes {

        public static final TagKey<DamageType> ELEMENTAL = tag("elemental");

        private static @NotNull TagKey<DamageType> tag(@NotNull String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, YanagisEnchantment.prefix(name));
        }
        
    }

    public static final class Enchantments {

        public static final TagKey<Enchantment> ATTACK_EFFECT = tag("attack_effect");

        private static @NotNull TagKey<Enchantment> tag(@NotNull String name) {
            return TagKey.create(Registries.ENCHANTMENT, YanagisEnchantment.prefix(name));
        }
        
    }

}

package yanagi.enchantment.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import yanagi.enchantment.YanagisEnchantment;

public class YEEnchantments {

    public static ResourceKey<Enchantment> MIGHTY_KNOCKDOWN = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MODID, "mighty_knockdown"));
    public static ResourceKey<Enchantment> BATTLE_RHYTHM = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MODID, "battle_rhythm"));

}

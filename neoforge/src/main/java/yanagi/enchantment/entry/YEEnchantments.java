package yanagi.enchantment.entry;

// import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
// import net.neoforged.neoforge.registries.DeferredRegister;
import yanagi.enchantment.YanagisEnchantment;

public class YEEnchantments {

    // public static final DeferredRegister<Enchantment> MOB_EFFECTS = DeferredRegister.create(Registries.ENCHANTMENT, YanagisEnchantment.MOD_ID);

    // // beneficial
    // public static final Holder<Enchantment> CRESCENDO_EFFECT = MOB_EFFECTS.register(CrescendoEffect.name, CrescendoEffect::new);

    // // harmful
    // public static final Holder<MobEffect> SHOCKED_EFFECT = MOB_EFFECTS.register(ShockedEffect.name, ShockedEffect::new);
    // public static final Holder<MobEffect> STUN_EFFECT = MOB_EFFECTS.register(StunnedEffect.name, StunnedEffect::new);

    public static ResourceKey<Enchantment> BATTLE_RHYTHM = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "battle_rhythm"));
    public static ResourceKey<Enchantment> CHAIN_LIGHTNING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "chain_lightning"));
    public static ResourceKey<Enchantment> MIGHTY_KNOCKDOWN = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "mighty_knockdown"));

}

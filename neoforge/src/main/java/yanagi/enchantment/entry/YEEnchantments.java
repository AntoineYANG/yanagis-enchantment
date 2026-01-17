package yanagi.enchantment.entry;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import yanagi.enchantment.YanagisEnchantment;

public class YEEnchantments {

    public static class Tags {
        
        // melee enchantments
        public static final TagKey<Enchantment> BATTLE_RHYTHM = tag("battle_rhythm");
        public static final TagKey<Enchantment> CHAIN_LIGHTNING = tag("chain_lightning");
        // public static final TagKey<Enchantment> MIGHTY_KNOCKDOWN = tag("mighty_knockdown");
        public static final TagKey<Enchantment> CRITICAL_RATE = tag("critical_rate");
        public static final TagKey<Enchantment> CRITICAL_DAMAGE = tag("critical_damage");
        public static final TagKey<Enchantment> LIFE_STEAL = tag("life_steal");
        
        // bow enchantments
        // public static final TagKey<Enchantment> BET_THE_FARM = tag("bet_the_farm");

        public static void setup(TagsProvider<Enchantment> provider, HolderLookup.@NotNull Provider pProvider) {
            return;
        }

        private static @NotNull TagKey<Enchantment> tag(@NotNull String name) {
            return TagKey.create(Registries.ENCHANTMENT, YanagisEnchantment.prefix(name));
        }

    }

    public static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_DATA_COMPONENTS = DeferredRegister.create(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, YanagisEnchantment.MOD_ID);

    // melee enchantments
    public static final ResourceKey<Enchantment> BATTLE_RHYTHM = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "battle_rhythm"));
    public static final ResourceKey<Enchantment> CHAIN_LIGHTNING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "chain_lightning"));
    // public static final ResourceKey<Enchantment> MIGHTY_KNOCKDOWN = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "mighty_knockdown"));
    public static final ResourceKey<Enchantment> CRITICAL_RATE = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "critical_rate"));
    public static final ResourceKey<Enchantment> CRITICAL_DAMAGE = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "critical_damage"));
    public static final ResourceKey<Enchantment> LIFE_STEAL = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "life_steal"));
    
    // // bow enchantments
    // public static final ResourceKey<Enchantment> BET_THE_FARM = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "bet_the_farm"));
    
    public static void register(IEventBus eventBus) {
        ENCHANTMENT_DATA_COMPONENTS.register(eventBus);
    }

    public static void createEnchantments(BootstrapContext<Enchantment> context) {
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        context.register(BATTLE_RHYTHM,
            new Enchantment.Builder(
                Enchantment.definition(
                    items.getOrThrow(YETags.Items.MELEE_WEAPONS),
                    2,
                    5,
                    Enchantment.dynamicCost(10, 4),
                    Enchantment.dynamicCost(20, 5),
                    10,
                    EquipmentSlotGroup.MAINHAND
                )
            )
            .exclusiveWith(enchantments.getOrThrow(YETags.Enchantments.ATTACK_EFFECT))
            .build(BATTLE_RHYTHM.location())
        );
        context.register(CHAIN_LIGHTNING,
            new Enchantment.Builder(
                Enchantment.definition(
                    items.getOrThrow(YETags.Items.MELEE_WEAPONS),
                    2,
                    5,
                    Enchantment.dynamicCost(12, 3),
                    Enchantment.dynamicCost(24, 6),
                    15,
                    EquipmentSlotGroup.MAINHAND
                )
            )
            .exclusiveWith(enchantments.getOrThrow(YETags.Enchantments.ATTACK_EFFECT))
            .build(CHAIN_LIGHTNING.location())
        );
        // context.register(MIGHTY_KNOCKDOWN,
        //     new Enchantment.Builder(
        //         Enchantment.definition(
        //             items.getOrThrow(YETags.Items.MELEE_WEAPONS),
        //             3,
        //             5,
        //             Enchantment.dynamicCost(5, 6),
        //             Enchantment.dynamicCost(20, 8),
        //             8,
        //             EquipmentSlotGroup.MAINHAND
        //         )
        //     )
        //     .exclusiveWith(enchantments.getOrThrow(YETags.Enchantments.ATTACK_EFFECT))
        //     .build(MIGHTY_KNOCKDOWN.location())
        // );
        // context.register(BET_THE_FARM,
        //     new Enchantment.Builder(
        //         Enchantment.definition(
        //             items.getOrThrow(ItemTags.BOW_ENCHANTABLE),
        //             3,
        //             5,
        //             Enchantment.dynamicCost(10, 4),
        //             Enchantment.dynamicCost(20, 8),
        //             8,
        //             EquipmentSlotGroup.MAINHAND
        //         )
        //     )
        //     .exclusiveWith(HolderSet.direct(enchantments.getOrThrow((Enchantments.INFINITY))))
        //     .build(BET_THE_FARM.location())
        // );
        context.register(CRITICAL_RATE,
            new Enchantment.Builder(
                Enchantment.definition(
                    items.getOrThrow(YETags.Items.MELEE_WEAPONS),
                    2,
                    5,
                    Enchantment.dynamicCost(4, 5),
                    Enchantment.dynamicCost(16, 7),
                    8,
                    EquipmentSlotGroup.MAINHAND
                )
            )
            .exclusiveWith(enchantments.getOrThrow(YETags.Enchantments.ATTACK_EFFECT))
            .build(CRITICAL_RATE.location())
        );
        context.register(CRITICAL_DAMAGE,
            new Enchantment.Builder(
                Enchantment.definition(
                    items.getOrThrow(YETags.Items.MELEE_WEAPONS),
                    2,
                    5,
                    Enchantment.dynamicCost(4, 5),
                    Enchantment.dynamicCost(16, 7),
                    8,
                    EquipmentSlotGroup.MAINHAND
                )
            )
            .exclusiveWith(enchantments.getOrThrow(YETags.Enchantments.ATTACK_EFFECT))
            .build(CRITICAL_DAMAGE.location())
        );
        context.register(LIFE_STEAL,
            new Enchantment.Builder(
                Enchantment.definition(
                    items.getOrThrow(YETags.Items.MELEE_WEAPONS),
                    3,
                    5,
                    Enchantment.dynamicCost(5, 6),
                    Enchantment.dynamicCost(20, 8),
                    10,
                    EquipmentSlotGroup.MAINHAND
                )
            )
            .exclusiveWith(enchantments.getOrThrow(YETags.Enchantments.ATTACK_EFFECT))
            .build(LIFE_STEAL.location())
        );
    }

}

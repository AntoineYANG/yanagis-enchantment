package yanagi.enchantment.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.*;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.entry.YEDamageTypes;
import yanagi.enchantment.entry.YEEnchantments;
import yanagi.enchantment.entry.YETags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TagProvider {

    public static void register(DataGenerator gen, @NotNull GatherDataEvent event, PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper existingFileHelper) {
        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(output, future, existingFileHelper);
        gen.addProvider(event.includeServer(), blockTagsProvider);
        gen.addProvider(event.includeServer(), new ModItemTagsProvider(output, future, blockTagsProvider.contentsGetter(), existingFileHelper));
        gen.addProvider(event.includeServer(), new ModEntityTypeTagsProvider(output, future, existingFileHelper));
        gen.addProvider(event.includeServer(), new ModDamageTypeProvider(output, future, existingFileHelper));
        gen.addProvider(event.includeServer(), new ModEffectTypeProvider(output, future, existingFileHelper));
        gen.addProvider(event.includeServer(), new ModEnchantmentProvider(output, future, existingFileHelper));
    }

    public static class ModBlockTagsProvider extends BlockTagsProvider {

        public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, YanagisEnchantment.MOD_ID, existingFileHelper);
        }

        @NotNull
        @Override
        public String getName() {
            return YanagisEnchantment.MOD_ID + " " + super.getName();
        }

        @Override
        protected void addTags(HolderLookup.Provider holderLookup) {
            return;
        }

    }

    public static class ModItemTagsProvider extends ItemTagsProvider {

        public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagsProvider, ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, blockTagsProvider, YanagisEnchantment.MOD_ID, existingFileHelper);
        }

        @NotNull
        @Override
        public String getName() {
            return YanagisEnchantment.MOD_ID + " " + super.getName();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void addTags(HolderLookup.@NotNull Provider holderProvider) {
            // enchantable
            tag(YETags.Items.MELEE_WEAPONS).addTags(ItemTags.SWORDS, ItemTags.AXES).add(Items.TRIDENT.asItem(), Items.MACE.asItem());
        }

    }

    public static class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {

        public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, YanagisEnchantment.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider holderLookup) {
            return;
        }

    }

    public static class ModDamageTypeProvider extends TagsProvider<DamageType> {

        public ModDamageTypeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, Registries.DAMAGE_TYPE, provider, YanagisEnchantment.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.@NotNull Provider pProvider) {
            this.tag(YETags.DamageTypes.ELEMENTAL).add(YEDamageTypes.ELEMENT_ELECTRICITY);
            this.tag(DamageTypeTags.IS_LIGHTNING).add(YEDamageTypes.ELEMENT_ELECTRICITY);
        }

    }

    public static class ModEffectTypeProvider extends TagsProvider<MobEffect> {

        public ModEffectTypeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, Registries.MOB_EFFECT, provider, YanagisEnchantment.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.@NotNull Provider pProvider) {
            YEEffects.Tags.setup(this, pProvider);
        }

    }

    public static class ModEnchantmentProvider extends TagsProvider<Enchantment> {

        public ModEnchantmentProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, Registries.ENCHANTMENT, provider, YanagisEnchantment.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.@NotNull Provider pProvider) {
            this.tag(Tags.Enchantments.WEAPON_DAMAGE_ENHANCEMENTS).add(YEEnchantments.BATTLE_RHYTHM, YEEnchantments.MIGHTY_KNOCKDOWN);
            this.tag(Tags.Enchantments.ENTITY_SPEED_ENHANCEMENTS).add(YEEnchantments.BATTLE_RHYTHM);
            this.tag(YETags.Enchantments.ATTACK_EFFECT).add(YEEnchantments.CHAIN_LIGHTNING);
            this.tag(EnchantmentTags.NON_TREASURE).add(YEEnchantments.BATTLE_RHYTHM, YEEnchantments.CHAIN_LIGHTNING, YEEnchantments.MIGHTY_KNOCKDOWN);
            this.tag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT).add(YEEnchantments.BATTLE_RHYTHM, YEEnchantments.CHAIN_LIGHTNING);
        }

    }

}

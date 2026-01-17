package yanagi.enchantment.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.entity.magic.ChainLightningEntity;
import yanagi.enchantment.entity.projectile.GuidingArrow;

public class YEEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, YanagisEnchantment.MOD_ID);
    // public static final DeferredRegister<Item> SPAWN_EGGS =
    // DeferredRegister.create(Registries.ITEM, YanagisEnchantment.MODID);
    // public static final Map<Holder<EntityType<?>>,
    // Supplier<AttributeSupplier.Builder>> ATTRIBUTES = new HashMap<>();
    // public static final Map<Holder<EntityType<?>>,
    // SpawnPlacements.SpawnPredicate<?>> SPAWN_PREDICATES = new HashMap<>();

    public static final DeferredHolder<EntityType<?>, EntityType<ChainLightningEntity>> CHAIN_LIGHTNING = registerMisc(
        ChainLightningEntity.name,
        EntityType.Builder.<ChainLightningEntity>of(ChainLightningEntity::new, MobCategory.MISC)
        .sized(1.0F, 1.0F)
        .clientTrackingRange(4)
        .updateInterval(10)
        .fireImmune()
    );

    public static final DeferredHolder<EntityType<?>, EntityType<GuidingArrow>> GUIDING_ARROW = registerMisc(
        GuidingArrow.name,
        EntityType.Builder.<GuidingArrow>of(GuidingArrow::new, MobCategory.MISC)
        .sized(0.5F, 0.5F)
        .clientTrackingRange(32)
        .updateInterval(5)
    );

    public static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerMisc(String name, EntityType.Builder<E> builder) {
        return ENTITY_TYPES.register(name, () -> builder.build(idOf(name)));
    }

    // public static <E extends LivingEntity> DeferredHolder<EntityType<?>,
    // EntityType<E>> registerWithAttributes(String name, EntityType.Builder<E>
    // builder, Supplier<AttributeSupplier.Builder> attributes) {
    // DeferredHolder<EntityType<?>, EntityType<E>> ret =
    // ENTITY_TYPES.register(name, () -> builder.build(idOf(name)));
    // ATTRIBUTES.put(ret, attributes);
    // return ret;
    // }

    // public static <E extends LivingEntity> DeferredHolder<EntityType<?>,
    // EntityType<E>> registerWithPlacement(String name, EntityType.Builder<E>
    // builder, Supplier<AttributeSupplier.Builder> attributes, @Nullable
    // SpawnPlacements.SpawnPredicate<E> predicate) {
    // DeferredHolder<EntityType<?>, EntityType<E>> ret =
    // ENTITY_TYPES.register(name, () -> builder.build(idOf(name)));
    // ATTRIBUTES.put(ret, attributes);
    // if (predicate != null) {
    // SPAWN_PREDICATES.put(ret, predicate);
    // }
    // return ret;
    // }

    // public static <E extends Mob> DeferredHolder<EntityType<?>, EntityType<E>>
    // registerWithEgg(String name, EntityType.Builder<E> builder,
    // Supplier<AttributeSupplier.Builder> attributes, @Nullable
    // SpawnPlacements.SpawnPredicate<E> predicate) {
    // DeferredHolder<EntityType<?>, EntityType<E>> ret =
    // ENTITY_TYPES.register(name, () -> builder.build(idOf(name)));
    // SPAWN_EGGS.register(name + "_spawn_egg", () -> new SpawnEggItem(ret.get(),
    // new Item.Properties().setId(idOf(name + "_spawn_egg"))));
    // ATTRIBUTES.put(ret, attributes);
    // if (predicate != null) {
    // SPAWN_PREDICATES.put(ret, predicate);
    // }
    // return ret;
    // }

    private static String idOf(String name) {
        return YanagisEnchantment.of(name);
    }

}

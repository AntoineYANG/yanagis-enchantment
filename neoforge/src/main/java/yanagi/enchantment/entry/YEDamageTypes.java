package yanagi.enchantment.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import yanagi.enchantment.YanagisEnchantment;

public class YEDamageTypes {

	public static final ResourceKey<DamageType> ELEMENT_ELECTRICITY = create("element_electricity");

	public static ResourceKey<DamageType> create(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, YanagisEnchantment.prefix(name));
	}

	public static void createDamageTypes(BootstrapContext<DamageType> context) {
        register(context, ELEMENT_ELECTRICITY, "element_electricity", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.HURT, DeathMessageType.DEFAULT);
	}

    private static void register(BootstrapContext<DamageType> context, ResourceKey<DamageType> key, String name, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
        context.register(key, new DamageType(YanagisEnchantment.dot(name), scaling, exhaustion, effects, deathMessageType));
    }

}

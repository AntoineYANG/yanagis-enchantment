package yanagi.enchantment.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import yanagi.enchantment.YanagisEnchantment;

public abstract class YEModelLayers {

    public static final ModelLayerLocation CHAIN_THUNDER_PART = register("chain_thunder_part");

    private static ModelLayerLocation register(String path) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, path), "main");
    }

}

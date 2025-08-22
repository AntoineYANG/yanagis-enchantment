package yanagi.enchantment.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.client.model.ChainThunderPartModel;
import yanagi.enchantment.client.model.YEModelLayers;
import yanagi.enchantment.client.renderer.ChainLightningRenderer;
import yanagi.enchantment.entity.YEEntities;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = YanagisEnchantment.MOD_ID, value = Dist.CLIENT)
public class ClientRenderingRegistry {

	@SubscribeEvent
	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(YEEntities.CHAIN_LIGHTNING.get(), ChainLightningRenderer::new);
    }

	@SubscribeEvent
	public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(YEModelLayers.CHAIN_THUNDER_PART, ChainThunderPartModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void clientExtensionRegistry(RegisterClientExtensionsEvent event) {
        return;
	}

    @SubscribeEvent
    public static void modelBake(ModelEvent.ModifyBakingResult event) {
        return;
	}

}

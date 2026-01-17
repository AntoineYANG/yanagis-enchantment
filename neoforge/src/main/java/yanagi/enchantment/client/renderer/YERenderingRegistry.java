package yanagi.enchantment.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.entity.YEEntities;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = YanagisEnchantment.MOD_ID, value = Dist.CLIENT/*, bus = EventBusSubscriber.Bus.MOD */)
public class YERenderingRegistry {

    @SubscribeEvent
	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(YEEntities.CHAIN_LIGHTNING.get(), ChainLightningRenderer::new);
		event.registerEntityRenderer(YEEntities.GUIDING_ARROW.get(), GuidingArrowRenderer::new);
    }

}

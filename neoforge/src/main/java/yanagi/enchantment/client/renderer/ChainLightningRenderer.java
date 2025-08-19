package yanagi.enchantment.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yanagi.enchantment.entity.magic.ChainLightningEntity;

@OnlyIn(Dist.CLIENT)
public class ChainLightningRenderer<T extends ChainLightningEntity> extends EntityRenderer<T> {

	public ChainLightningRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public void render(T p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
	}

	@Override
	public ResourceLocation getTextureLocation(T p_110775_1_) {
		return null;
	}
}

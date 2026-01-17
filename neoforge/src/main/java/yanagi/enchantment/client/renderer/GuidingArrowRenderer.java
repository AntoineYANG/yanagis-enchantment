package yanagi.enchantment.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yanagi.enchantment.entity.projectile.GuidingArrow;

@OnlyIn(Dist.CLIENT)
public class GuidingArrowRenderer<T extends GuidingArrow> extends EntityRenderer<T> {

    protected static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }

	public GuidingArrowRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
	}

}

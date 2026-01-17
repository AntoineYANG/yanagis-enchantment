package yanagi.enchantment.client.renderer;

import java.util.Random;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.client.model.ChainThunderPartModel;
import yanagi.enchantment.client.model.YEModelLayers;
import yanagi.enchantment.entity.magic.ChainLightningEntity;

@OnlyIn(Dist.CLIENT)
public class ChainLightningRenderer<T extends ChainLightningEntity> extends EntityRenderer<T> {

    protected static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YanagisEnchantment.MOD_ID, "textures/entity/magic/chain_thunder_part.png");

    protected ChainThunderPartModel<T> model;
    protected EntityRenderDispatcher entityRenderer;
    protected Random random = new Random();

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }

	public ChainLightningRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
        this.model = new ChainThunderPartModel<>(renderManagerIn.bakeLayer(YEModelLayers.CHAIN_THUNDER_PART));
        this.entityRenderer = renderManagerIn.getEntityRenderDispatcher();
	}

	@Override
	public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity == null || poseStack == null) {
            return;
        }
        
        Vec3 tar = entity.getTargetPosSync();

        if (tar == null) {
            return;
        }

        Vec3 src = entity.getSourcePosSync();
        Vec3 rel = tar.subtract(src);

        float len = (float)rel.length();
        if (len <= 1.0e-4f || src.length() <= 1.0e-4f || tar.length() <= 1.0e-4f) {
            return;
        }

        Vec3 dir = rel.normalize();
        Vec3 up = new Vec3(0, 1, 0);

        Vec3 axis = up.cross(dir);
        double axisLen = axis.length();
        float angleRad;
        Quaternionf q;

        if (axisLen < 1.0e-6) {
            double dot = up.dot(dir);
            angleRad = (float) (dot > 0 ? 0.0 : Math.PI);
            q = new org.joml.Quaternionf().rotationAxis(angleRad, 1f, 0f, 0f);
        } else {
            axis = axis.scale(1.0 / axisLen);
            double dot = Mth.clamp(up.dot(dir), -1.0, 1.0);
            angleRad = (float) Math.acos(dot);
            q = new org.joml.Quaternionf().rotationAxis(angleRad, (float) axis.x, (float) axis.y, (float) axis.z);
        }

        poseStack.pushPose();

        float thickness = 0.24f;

        poseStack.translate(
            thickness * 0.6f * (random.nextDouble() - 0.5f) - rel.x * 0.5f,
            thickness * 0.6f * (random.nextDouble() - 0.5f) - rel.y * 0.5f,
            thickness * 0.6f * (random.nextDouble() - 0.5f) - rel.z * 0.5f
        );
        poseStack.mulPose(q);

        poseStack.scale(thickness, len, thickness);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        this.model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF);

        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
	}

}

package yanagi.enchantment.client.model;

// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class ChainThunderPartModel<T extends Entity> extends EntityModel<T> {

	private final ModelPart bb_main;

	public ChainThunderPartModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();

		@SuppressWarnings("unused")
        PartDefinition main = partDefinition.addOrReplaceChild(
            "bb_main",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );

		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public void setupAnim(@SuppressWarnings("null") Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        return;
	}

    @Override
    public void renderToBuffer(@SuppressWarnings("null") PoseStack poseStack, @SuppressWarnings("null") VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        // if (young) {
        //     poseStack.scale(0.5f, 0.5f, 0.5f);
        //     poseStack.translate(0, 1.5f, 0);
        // }
        bb_main.render(poseStack, buffer, packedLight, packedOverlay);
    }

}
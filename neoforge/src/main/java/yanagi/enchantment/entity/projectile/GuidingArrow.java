package yanagi.enchantment.entity.projectile;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.scores.PlayerTeam;

public class GuidingArrow extends AbstractArrow {

    public static final String name = "guiding_arrow";

    protected static final int LOCK_ON_TIME = 8;
    protected static final int SEARCH_INTERVAL = 3;
    protected static final int DIRECT_TIME = 6;
    protected static final int MAX_SEARCH_TIME = 32;
    protected static final float TURN_SOFTEN = 0.1F;
    protected static final int MAX_DIST = 32;

    protected static float getMaxSpeed(int level) {
        return 3.0F + 0.33F * (level - 1);
    }
    
    @Nullable ItemStack originalArrowStack;
    int enchantLvl = 1;
    int time = 0;
    int lockOnAt = 0;
    @Nullable protected LivingEntity target = null;

    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(GuidingArrow.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> TAR_ID = SynchedEntityData.defineId(GuidingArrow.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(GuidingArrow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(GuidingArrow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(GuidingArrow.class, EntityDataSerializers.FLOAT);

    public GuidingArrow(EntityType<? extends GuidingArrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public GuidingArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(EntityType.ARROW, x, y, z, level, pickupItemStack, firedFromWeapon);
        this.setNoGravity(true);
        this.updateColor();
    }

    public GuidingArrow(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(EntityType.ARROW, owner, level, pickupItemStack, firedFromWeapon);
        this.setNoGravity(true);
        this.updateColor();
    }

    public void setOriginalArrowStack(ItemStack itemStack) {
        this.originalArrowStack = itemStack;
    }

    public void setEnchantmentLevel(int enchantLvl) {
        this.enchantLvl = enchantLvl;
    }

    private PotionContents getPotionContents() {
        return this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    private void setPotionContents(PotionContents potionContents) {
        this.getPickupItemStackOrigin().set(DataComponents.POTION_CONTENTS, potionContents);
        this.updateColor();
    }

    @Override
    protected void setPickupItemStack(ItemStack pickupItemStack) {
        super.setPickupItemStack(pickupItemStack);
        this.updateColor();
    }

    private void updateColor() {
        PotionContents potioncontents = this.getPotionContents();
        this.entityData.set(ID_EFFECT_COLOR, potioncontents.equals(PotionContents.EMPTY) ? -1 : potioncontents.getColor());
    }

    public void addEffect(MobEffectInstance effectInstance) {
        this.setPotionContents(this.getPotionContents().withEffectAdded(effectInstance));
    }

    private Vec3 lockOn(LivingEntity e) {
        // this.entityData.set(TAR_ID, e.getId());
        Vec3 from = this.position();
        Vec3 to = getDirectPosOf(e);
        Vec3 rel = to.subtract(from);
        double len = rel.length();
        if (len < 1e-3) {
            // this.entityData.set(DIR_X, (float)rel.x);
            // this.entityData.set(DIR_Y, (float)rel.y);
            // this.entityData.set(DIR_Z, (float)rel.z);
            return rel;
        }
        Vec3 unit = rel.normalize();
        // this.entityData.set(DIR_X, (float)unit.x);
        // this.entityData.set(DIR_Y, (float)unit.y);
        // this.entityData.set(DIR_Z, (float)unit.z);
        return unit;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_EFFECT_COLOR, -1);
        builder.define(TAR_ID, -1);
        builder.define(DIR_X, 0F);
        builder.define(DIR_Y, 0F);
        builder.define(DIR_Z, 0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.time = compound.getInt("Time");
        this.lockOnAt = compound.getInt("LockOnAt");
        this.enchantLvl = compound.getInt("Level");
        if (compound.contains("TargetID")) {
            Entity e = this.level().getEntity(compound.getInt("TargetID"));
            if (e != null && e instanceof LivingEntity e0) {
                setTarget(e0);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Time", this.time);
        compound.putInt("LockOnAt", this.lockOnAt);
        compound.putInt("Level", this.enchantLvl);
        if (this.target != null) {
            compound.putInt("TargetID", this.target.getId());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity() || this.inGround) {
            return;
        }
        if (this.level().isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }
        } else {
            if (this.inGround && this.inGroundTime != 0 && !this.getPotionContents().equals(PotionContents.EMPTY) && this.inGroundTime >= 600) {
                this.level().broadcastEntityEvent(this, (byte)0);
                this.setPickupItemStack(new ItemStack(Items.ARROW));
            }
            this.time++;
            if (!this.inGround) {
                @Nullable LivingEntity tar = this.target;
                if (tar == null) {
                    if (this.time >= LOCK_ON_TIME && this.time - this.lockOnAt >= SEARCH_INTERVAL) {
                        @Nullable LivingEntity next = searchForTarget();
                        if (next == null) {
                            if (this.time > this.lockOnAt + MAX_SEARCH_TIME) {
                                this.setNoGravity(false);
                                return;
                            }
                        } else {
                            this.setTarget(next);
                            this.lockOnAt = this.time;
                        }
                    }
                } else {
                    if (!tar.isAlive()) {
                        this.setTarget(null);
                    }
                }
                this.updateVelo();
            }
        }
    }

    private void makeParticle(int particleAmount) {
        int i = this.getColor();
        if (i != -1 && particleAmount > 0) {
            for (int j = 0; j < particleAmount; j++) {
                this.level()
                    .addParticle(
                        ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, i),
                        this.getRandomX(0.5),
                        this.getRandomY(),
                        this.getRandomZ(0.5),
                        0.0,
                        0.0,
                        0.0
                    );
            }
        }
    }

    public int getColor() {
        return this.entityData.get(ID_EFFECT_COLOR);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        Entity entity = this.getEffectSource();
        PotionContents potioncontents = this.getPotionContents();
        if (potioncontents.potion().isPresent()) {
            for (MobEffectInstance mobeffectinstance : potioncontents.potion().get().value().getEffects()) {
                living.addEffect(
                    new MobEffectInstance(
                        mobeffectinstance.getEffect(),
                        Math.max(mobeffectinstance.mapDuration(p_268168_ -> p_268168_ / 8), 1),
                        mobeffectinstance.getAmplifier(),
                        mobeffectinstance.isAmbient(),
                        mobeffectinstance.isVisible()
                    ),
                    entity
                );
            }
        }

        for (MobEffectInstance mobeffectinstance1 : potioncontents.customEffects()) {
            living.addEffect(mobeffectinstance1, entity);
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getPickupItem() {
        ItemStack stack = this.originalArrowStack;
        return stack == null || stack.isEmpty() ? new ItemStack(Items.ARROW) : stack.copy();
    }

    /**
     * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 0) {
            int i = this.getColor();
            if (i != -1) {
                float f = (float)(i >> 16 & 0xFF) / 255.0F;
                float f1 = (float)(i >> 8 & 0xFF) / 255.0F;
                float f2 = (float)(i >> 0 & 0xFF) / 255.0F;

                for (int j = 0; j < 20; j++) {
                    this.level()
                        .addParticle(
                            ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, f, f1, f2),
                            this.getRandomX(0.5),
                            this.getRandomY(),
                            this.getRandomZ(0.5),
                            0.0,
                            0.0,
                            0.0
                        );
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    protected void updateVelo() {
        @Nullable LivingEntity tar = this.target;
        Vec3 curVelo = this.getDeltaMovement();
        if (tar == null || !tar.isAlive()) {
            // reduce velocity
            this.setDeltaMovement(curVelo.scale(0.2D));
        } else {
            // direct
            Vec3 goalVelo = lockOn(tar).normalize().scale(getMaxSpeed(this.enchantLvl));
            if (this.time >= this.lockOnAt + DIRECT_TIME + 999999   ) {
                // fully directed
                this.setDeltaMovement(goalVelo);
            } else {
                Vec3 blended = curVelo.scale(1.0 - TURN_SOFTEN).add(goalVelo.scale(TURN_SOFTEN));
                this.setDeltaMovement(blended);
            }
        }
    }

    protected @Nullable LivingEntity searchForTarget() {
        Entity owner = this.getOwner();
        @Nullable PlayerTeam ownerTeam = owner == null ? null : owner.getTeam();
        Vec3 startPos = this.position();
        AABB searchBox = new AABB(
            startPos.x - MAX_DIST, startPos.y - MAX_DIST, startPos.z - MAX_DIST,
            startPos.x + MAX_DIST, startPos.y + MAX_DIST, startPos.z + MAX_DIST
        );
        // search for entities
        @Nullable LivingEntity t = null;
        float weight = 0F;
        List<Entity> list = this.level().getEntities(this, searchBox, EntitySelector.NO_SPECTATORS);
        for (Entity entity : list) {
            if (entity instanceof LivingEntity e) {
                if (!e.isAlive()) {
                    continue;
                }
                if (owner != null && owner.getId() == e.getId()) {
                    continue;
                }
                if (ownerTeam != null) {
                    @Nullable PlayerTeam team = e.getTeam();
                    if (team != null && team.isAlliedTo(ownerTeam)) {
                        continue;
                    }
                }
                float w = weightForDirect(startPos, e);
                if (t == null || w > weight) {
                    t = e;
                    weight = w;
                }
            }
        }
        if (weight > 0F && t != null) {
            return t;
        }
        return null;
    }

    protected float weightForDirect(Vec3 startPos, LivingEntity e) {
        if (!reachable(startPos, getDirectPosOf(e))) {
            return 0F;
        }
        return e.getMaxHealth();
    }

    protected boolean reachable(Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 1.0e-6) {
            return false;
        }

        Vec3 dir = d.scale(1.0 / len);
        double eps = 1.0e-3;
        Vec3 f = from.add(dir.scale(eps));
        Vec3 t = to.add(dir.scale(-eps));

        BlockHitResult hit = this.level().clip(new ClipContext(
            f, t,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            CollisionContext.empty()
        ));

        return hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK;
    }

    protected void setTarget(@Nullable LivingEntity tar) {
        this.target = tar;
        if (tar == null) {
            this.entityData.set(TAR_ID, -1);
        } else {
            this.lockOn(tar);
        }
    }

    protected static Vec3 getDirectPosOf(LivingEntity e) {
        return e.getEyePosition();
    }

}

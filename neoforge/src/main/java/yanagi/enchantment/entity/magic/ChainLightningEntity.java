package yanagi.enchantment.entity.magic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.scores.PlayerTeam;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.entity.YEEntities;
import yanagi.enchantment.entry.YEDamageTypes;
import yanagi.enchantment.utils.RandomHelper;

import java.util.*;

import javax.annotation.Nullable;

public class ChainLightningEntity extends Entity implements OwnableEntity {

    public static final String name = "chain_lightning";

    public static final int ENERGY_COST_PER_BLOCK = 16;
    public static final int ENERGY_COST_TO_LIGHTNING_ROD_PER_BLOCK = 3;
    public static final int ENERGY_BONUS_WHEN_HIT = 5;
    public static final int ENERGY_LOSS_PCT_WHEN_HIT_LIGHTNING_ROD = 80;
    public static final int CONTINUE_INTERVAL = 1;
    public static final int ANI_TIME = 5;
    public static final int MAX_LIFE = Math.max(ANI_TIME, CONTINUE_INTERVAL);

    public static final int resolveInitEnergy(int amplifier) {
        return 150 + 50 * amplifier;
    }
    public static final int resolveBasicDamage(int amplifier) {
        int minDmg = 1;
        int maxDmg = 4 + amplifier * 2;
        double mean = (minDmg + maxDmg) * 0.5d;
        double sigma = (maxDmg - minDmg) / 6.0d;
        return (int)RandomHelper.clampGaussian(mean, sigma, minDmg, maxDmg);
    }
    public static final int resolvePctDamage(LivingEntity e, int energy) {
        double pct = e.getHealth() * 0.01d * (energy / 80.0d);
        return (int)Math.ceil(pct);
    }
    
    @Nullable
    protected LivingEntity owner;
    @Nullable
    protected UUID ownerUuid;
    @Nullable
    protected ChainLightningEntity parent = null;

    protected boolean initialized = false;

    protected int amplifier = 0;
    protected int initEnergy;
    protected int nextEnergy;
    protected int time = 0;
    @Nullable protected StrikeTarget source = null;
    @Nullable protected StrikeTarget target = null;
    protected final List<String> uuidExcludes = new ArrayList<>();

    private static final EntityDataAccessor<Integer> SRC_ID = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SRC_X = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SRC_Y = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SRC_Z = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.FLOAT);
    
    private static final EntityDataAccessor<Integer> TGT_ID = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TGT_X = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TGT_Y = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TGT_Z = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.FLOAT);

    public ChainLightningEntity(EntityType<? extends ChainLightningEntity> entityType, Level worldLevel) {
        super(entityType, worldLevel);
        this.setNoGravity(true);
    }

    protected ChainLightningEntity(Level worldLevel, double x, double y, double z, LivingEntity owner, int amplifier) {
        this(YEEntities.CHAIN_LIGHTNING.get(), worldLevel);
        this.setOwner(owner);
        this.setPos(x, y, z);
        this.amplifier = amplifier;
    }

    protected void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        this.setInitEnergy(resolveInitEnergy(this.amplifier));
        this.time = 0;

        StrikeTarget tar = this.findTarget();
        if (tar != null) {
            setTarget(tar);
            LivingEntity e = tar.entity;
            if (e != null) {
                this.damage(e);
                e.addEffect(new MobEffectInstance(YEEffects.SHOCKED_EFFECT, 4, 0));
            }
            float vol = Math.min(3.0f, 0.1f + 2.9f * this.initEnergy / resolveInitEnergy(4));
            this.playSound(SoundEvents.FIRECHARGE_USE, vol, 0.6F);
        } else {
            // int count = 0;
            // @Nullable ChainLightningEntity p = this.parent;
            // while (p != null) {
            //     p = p.parent;
            //     count++;
            // }
            // System.out.println("[ChainLightning] " + count + "hits");
            this.discard();
        }
    }

    public static ChainLightningEntity generateChainLightningEntityOnTarget(
        @Nullable LivingEntity owner, Level level, StrikeTarget target, int amplifier
    ) {
        Vec3 pos = getStrikePosOf(target);
        ChainLightningEntity chainLightning = new ChainLightningEntity(level, pos.x, pos.y, pos.z, owner, amplifier);
        chainLightning.setSource(target);
		level.addFreshEntity(chainLightning);
        return chainLightning;
    }

    public static ChainLightningEntity generateChainLightningEntity(
        LivingEntity owner, double x, double y, double z, int amplifier
    ) {
        Level level = owner.level();
        ChainLightningEntity chainLightning = new ChainLightningEntity(level, x, y, z, owner, amplifier);
		level.addFreshEntity(chainLightning);
        return chainLightning;
    }

    public void updateUUIDExcludes(Collection<? extends String> c) {
        this.uuidExcludes.clear();
        this.uuidExcludes.addAll(c);
    }

    public void setInitEnergy(int energy) {
        if (this.initEnergy > 0) {
            return;
        }
        this.initEnergy = energy;
        this.nextEnergy = energy;
    }

    public void setParent(ChainLightningEntity parent) {
        this.parent = parent;
    }

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

    @Override
    protected void defineSynchedData(@SuppressWarnings("null") Builder builder) {
        if (builder == null) {
            return;
        }
        builder.define(SRC_ID, -1); builder.define(SRC_X, 0f); builder.define(SRC_Y, 0f); builder.define(SRC_Z, 0f);
        builder.define(TGT_ID, -1); builder.define(TGT_X, 0f); builder.define(TGT_Y, 0f); builder.define(TGT_Z, 0f);
        return;
    }

    @Override
    protected void readAdditionalSaveData(@SuppressWarnings("null") CompoundTag compound) {
        this.amplifier = compound.getInt("Amplifier");
        this.initEnergy = compound.getInt("InitEnergy");
        this.nextEnergy = compound.getInt("NextEnergy");
        this.time = compound.getInt("Time");
        if (compound.contains("SourceID")) {
            Entity e = this.level().getEntity(compound.getInt("SourceID"));
            if (e != null && e instanceof LivingEntity e0) {
                setSource(new StrikeTarget(e0));
            }
        }
        if (compound.contains("TargetID")) {
            Entity e = this.level().getEntity(compound.getInt("TargetID"));
            if (e != null && e instanceof LivingEntity e0) {
                setTarget(new StrikeTarget(e0));
            }
        }
        this.uuidExcludes.clear();
        this.uuidExcludes.addAll(List.of(compound.getString("UUIDExcludes").split(",")));
    }

    @Override
    protected void addAdditionalSaveData(@SuppressWarnings("null") CompoundTag compound) {
        compound.putInt("Amplifier", this.amplifier);
        compound.putInt("InitEnergy", this.initEnergy);
        compound.putInt("NextEnergy", this.nextEnergy);
		compound.putInt("Time", this.time);
        if (this.source != null && this.source.entity != null) {
            compound.putInt("SourceID", this.source.entity.getId());
        }
        if (this.target != null && this.target.entity != null) {
            compound.putInt("TargetID", this.target.entity.getId());
        }
        compound.putString("UUIDExcludes", String.join(",", this.uuidExcludes));
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUuid = owner == null ? null : owner.getUUID();
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUuid;
    }

    @Override
    public void tick() {
        if (this.isInLava()) {
            this.discard();
            this.firstTick = false;
            return;
        }

        this.entityData.set(SRC_X, (float)this.getX());
        this.entityData.set(SRC_Y, (float)this.getY());
        this.entityData.set(SRC_Z, (float)this.getZ());
        if (this.level().isClientSide()) {
            this.makeParticles();
        } else {
            if (!this.initialized) {
                this.init();
                this.initialized = true;
            }
            this.time++;
            if (this.time == ChainLightningEntity.CONTINUE_INTERVAL) {
                StrikeTarget tar = this.target;
                if (tar != null) {
                    if (this.nextEnergy > 0) {
                        ChainLightningEntity child = generateChainLightningEntityOnTarget(this.owner, this.level(), tar, amplifier);
                        List<String> nextUUIDExcludes = new ArrayList<>();
                        nextUUIDExcludes.addAll(this.uuidExcludes);
                        if (tar.entity != null) {
                            nextUUIDExcludes.add(tar.entity.getStringUUID());
                        } else if (tar.pos != null) {
                            nextUUIDExcludes.add("" + tar.pos.asLong());
                        }
                        child.updateUUIDExcludes(nextUUIDExcludes);
                        child.setInitEnergy(this.nextEnergy);
                        child.setParent(this);
                    }
                }
            }
            if (this.time > ChainLightningEntity.MAX_LIFE) {
                this.discard();
            }
        }

        this.firstTick = false;
    }

    private void damage(LivingEntity target) {
        LivingEntity owner = this.getOwner();
        if (!target.isAlive() || target.isInvulnerable() || target == owner) {
            return;
        }
        @Nullable PlayerTeam targetTeam = target.getTeam();
        @Nullable PlayerTeam ownerTeam = owner == null ? null : owner.getTeam();
        if (targetTeam != null && target != null && targetTeam.isAlliedTo(ownerTeam)) {
            return;
        }
        target.hurt(this.damageSources().source(YEDamageTypes.ELEMENT_ELECTRICITY), resolveBasicDamage(amplifier));
        target.hurt(this.damageSources().source(YEDamageTypes.ELEMENT_ELECTRICITY), resolvePctDamage(target, this.nextEnergy));
    }

	@Override
	public boolean displayFireAnimation() {
		return false;
	}
    
    protected void makeParticles() {
        for (int i = 0; i < 4; i++) {
			double dx = this.getX() - 0.2F + 0.4F * (this.random.nextFloat() - this.random.nextFloat());
			double dy = this.getY() - 0.5F + 1.0F * (this.random.nextFloat() - this.random.nextFloat());
			double dz = this.getZ() - 0.2F + 0.4F * (this.random.nextFloat() - this.random.nextFloat());

			this.level().addAlwaysVisibleParticle(ParticleTypes.ELECTRIC_SPARK, dx, dy, dz, 0.0D, 0.0D, 0.0D);
		}
    }

    protected static Vec3 getStrikePosOf(LivingEntity e) {
        AABB bb = e.getBoundingBox();
        Vec3 bc = bb.getBottomCenter();
        return new Vec3(bc.x, bc.y + (bb.maxY - bb.minY) * (e.isAlive() ? 0.6f : 0.1f), bc.z);
    }

    @SuppressWarnings("null")
    protected static Vec3 getStrikePosOf(ChainLightningEntity.StrikeTarget tar) {
        @Nullable LivingEntity e = tar.entity;
        if (e != null) {
            return getStrikePosOf(e);
        }
        return tar.pos.getCenter();
    }

    protected Vec3 getSourcePos() {
        @Nullable Vec3 sourcePos = this.source == null ? null : getStrikePosOf(this.source);
        Vec3 pos = sourcePos == null ? this.position() : sourcePos;
        return pos;
    }

    @SuppressWarnings("null")
    protected void setSource(StrikeTarget tar) {
        this.source = tar;
        Vec3 pos = getStrikePosOf(tar);
        if (tar.entity == null) {
            this.entityData.set(SRC_ID, -1);
        } else {
            this.entityData.set(SRC_ID, tar.entity.getId());
        }
        this.entityData.set(SRC_X, (float)pos.x);
        this.entityData.set(SRC_Y, (float)pos.y);
        this.entityData.set(SRC_Z, (float)pos.z);
    }

    @SuppressWarnings("null")
    protected void setTarget(StrikeTarget tar) {
        this.target = tar;
        Vec3 pos = getStrikePosOf(tar);
        if (tar.entity == null) {
            this.entityData.set(TGT_ID, -1);
        } else {
            this.entityData.set(TGT_ID, tar.entity.getId());
        }
        this.entityData.set(TGT_X, (float)pos.x);
        this.entityData.set(TGT_Y, (float)pos.y);
        this.entityData.set(TGT_Z, (float)pos.z);
    }

    public static class StrikeTarget {

        final @Nullable LivingEntity entity;
        final @Nullable BlockPos pos;

        public StrikeTarget(LivingEntity entity) {
            this.entity = entity;
            this.pos = null;
        }

        public StrikeTarget(BlockPos pos) {
            this.entity = null;
            this.pos = pos;
        }

    }

    protected StrikeTarget findTarget() {
        @Nullable PlayerTeam ownerTeam = owner == null ? null : owner.getTeam();
        Vec3 startPos = getSourcePos();
        int radius = (int)(1.5F * this.initEnergy / ENERGY_COST_PER_BLOCK);
        AABB searchBox = new AABB(
            startPos.x - radius, startPos.y - radius, startPos.z - radius,
            startPos.x + radius, startPos.y + radius, startPos.z + radius
        );
        int cost = Integer.MAX_VALUE;
        // search for entities
        @Nullable LivingEntity t = null;
        List<Entity> list = this.level().getEntities(this, searchBox, EntitySelector.NO_SPECTATORS);
        for (Entity entity : list) {
            if (entity instanceof LivingEntity e) {
                if (owner != null && owner.getId() == e.getId()) {
                    continue;
                }
                boolean repeated = false;
                for (String uuid : this.uuidExcludes) {
                    if (uuid.compareTo(e.getStringUUID()) == 0) {
                        repeated = true;
                        break;
                    }
                }
                if (repeated) {
                    continue;
                }
                if (ownerTeam != null) {
                    @Nullable PlayerTeam team = e.getTeam();
                    if (team != null && team.isAlliedTo(ownerTeam)) {
                        continue;
                    }
                }
                // TODO: reachable?
                int c = Math.max(costForStriking(startPos, e) - ENERGY_BONUS_WHEN_HIT, 1);
                if (t == null || c < cost) {
                    t = e;
                    cost = c;
                }
            }
        }
        // search for lightning rods
        @Nullable BlockPos bp = null;
        BlockPos base = BlockPos.containing(startPos);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = base.offset(dx, dy, dz);
                    BlockState bs = this.level().getBlockState(p);
                    if (!bs.is(Blocks.LIGHTNING_ROD)) {
                        continue;
                    }
                    boolean repeated = false;
                    for (String uuid : this.uuidExcludes) {
                        if (uuid.compareTo("" + p.asLong()) == 0) {
                            repeated = true;
                            break;
                        }
                    }
                    if (repeated) {
                        continue;
                    }
                    int c = Math.max(costForStrikingLightningRod(startPos, p) - ENERGY_BONUS_WHEN_HIT, 1);
                    if ((t == null && bp == null) || c < cost) {
                        bp = p;
                        cost = c;
                    }
                }
            }
        }
        if (cost <= this.initEnergy) {
            if (bp != null) {
                this.nextEnergy = (int)((1.0F - ENERGY_LOSS_PCT_WHEN_HIT_LIGHTNING_ROD * 0.01F) * (this.initEnergy - cost));
                return new StrikeTarget(bp);
            } else if (t != null) {
                this.nextEnergy = this.initEnergy - cost;
                return new StrikeTarget(t);
            }
        }
        return null;
    }

    protected int costForStriking(Vec3 startPos, LivingEntity e) {
        if (!reachable(startPos, getStrikePosOf(e))) {
            return Integer.MAX_VALUE;
        }
        double dist = Math.sqrt(e.getBoundingBox().distanceToSqr(startPos));
        return (int)(dist * ENERGY_COST_PER_BLOCK);
    }

    protected int costForStrikingLightningRod(Vec3 startPos, BlockPos bp) {
        if (!reachable(startPos, bp.getCenter())) {
            return Integer.MAX_VALUE;
        }
        double dist = Math.sqrt(bp.getCenter().distanceToSqr(startPos));
        return (int)(dist * ENERGY_COST_TO_LIGHTNING_ROD_PER_BLOCK);
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

        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            return this.level().getBlockState(pos).is(Blocks.LIGHTNING_ROD);
        }
        return true;
    }

    public Vec3 getSourcePosSync() {
        int id = this.entityData.get(SRC_ID);
        if (id != -1 && level() != null) {
            Entity e = level().getEntity(id);
            if (e instanceof LivingEntity le) {
                return getStrikePosOf(le);
            }
        }
        return new Vec3(this.entityData.get(SRC_X), this.entityData.get(SRC_Y), this.entityData.get(SRC_Z));
    }

    public Vec3 getTargetPosSync() {
        int id = this.entityData.get(TGT_ID);
        if (id != -1 && level() != null) {
            Entity e = level().getEntity(id);
            if (e instanceof LivingEntity le) {
                return getStrikePosOf(le);
            }
        }
        return new Vec3(this.entityData.get(TGT_X), this.entityData.get(TGT_Y), this.entityData.get(TGT_Z));
    }

}

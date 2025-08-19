package yanagi.enchantment.entity.magic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.entity.YEEntities;
import yanagi.enchantment.utils.RandomHelper;

import java.util.*;

import javax.annotation.Nullable;

public class ChainLightningEntity extends Entity implements OwnableEntity {

    public static final String name = "chain_lightning";

    public static final int ENERGY_COST_PER_BLOCK = 16;
    public static final int ENERGY_COST_PER_BLOCK_IN_WATER = 3;
    public static final int ENERGY_BONUS_WHEN_HIT = 5;
    public static final int WARMUP_TIME = 1;
    public static final int CONTINUE_INTERVAL = 2;
    public static final int ANI_TIME = 6;
    public static final int MAX_LIFE = Math.max(WARMUP_TIME + ANI_TIME, WARMUP_TIME + CONTINUE_INTERVAL);

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
        double pct = e.getHealth() * 0.01d * (energy / 50.0d);
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
    @Nullable protected LivingEntity source = null;
    @Nullable protected LivingEntity target = null;
    protected final List<String> uuidExcludes = new ArrayList<>();

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
    }

    public static ChainLightningEntity generateChainLightningEntityOnLivingEntity(
        @Nullable LivingEntity owner, LivingEntity entity, int amplifier
    ) {
        Level level = entity.level();
        Vec3 pos = entity.getBoundingBox().getCenter();
        ChainLightningEntity chainLightning = new ChainLightningEntity(level, pos.x, pos.y, pos.z, owner, amplifier);
        chainLightning.source = entity;
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
                this.source = e0;
            }
        }
        if (compound.contains("TargetID")) {
            Entity e = this.level().getEntity(compound.getInt("TargetID"));
            if (e != null && e instanceof LivingEntity e0) {
                this.target = e0;
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
        if (this.source != null) {
            compound.putInt("SourceID", this.source.getId());
        }
        if (this.target != null) {
            compound.putInt("TargetID", this.target.getId());
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

        if (this.level().isClientSide()) {
            this.makeParticles();
        } else {
            if (!this.initialized) {
                this.init();
                this.initialized = true;
            }
            this.time++;
            if (this.time == ChainLightningEntity.WARMUP_TIME) {
                LivingEntity tar = this.findTarget();
                if (tar != null) {
                    this.target = tar;
                    this.damage(this.target);
                    // TODO: new effect
                    tar.addEffect(new MobEffectInstance(YEEffects.STUN_EFFECT, 4, 0));
                    this.playSound(SoundType.GLASS.getBreakSound(), 3.0F, 0.5F);
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
            } else if (this.time == ChainLightningEntity.WARMUP_TIME + ChainLightningEntity.CONTINUE_INTERVAL) {
                LivingEntity tar = this.target;
                if (tar != null) {
                    if (this.nextEnergy > 0) {
                        ChainLightningEntity child = generateChainLightningEntityOnLivingEntity(this.owner, tar, amplifier);
                        List<String> nextUUIDExcludes = new ArrayList<>();
                        nextUUIDExcludes.addAll(this.uuidExcludes);
                        nextUUIDExcludes.add(tar.getStringUUID());
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
        LivingEntity livingEntity = this.getOwner();
        if (!target.isAlive() || target.isInvulnerable() || target == livingEntity) {
            return;
        }
        @Nullable PlayerTeam targetTeam = target.getTeam();
        @Nullable PlayerTeam ownerTeam = owner == null ? null : owner.getTeam();
        if (targetTeam != null && target != null && targetTeam.isAlliedTo(ownerTeam)) {
            return;
        }
        if (livingEntity == null) {
            // FIXME: damage types
            target.hurt(this.damageSources().source(DamageTypes.LIGHTNING_BOLT), resolveBasicDamage(amplifier));
            target.hurt(this.damageSources().source(DamageTypes.MAGIC), resolvePctDamage(target, this.nextEnergy));
        }
    }

	@Override
	public boolean displayFireAnimation() {
		return false;
	}
    
    protected void makeParticles() {
        // FIXME:
        for (int i = 0; i < 10; i++) {
			double dx = this.getX() + 1.5F * (this.random.nextFloat() - this.random.nextFloat());
			double dy = this.getY() - 4.0F * (this.random.nextFloat() - this.random.nextFloat()) - 3.0F;
			double dz = this.getZ() + 1.5F * (this.random.nextFloat() - this.random.nextFloat());

			this.level().addAlwaysVisibleParticle(ParticleTypes.FLAME, dx, dy, dz, 0.0D, -1.0D, 0.0D);
		}
        if (this.target != null) {
            // TODO:
        }
    }

    @Nullable
    protected LivingEntity findTarget() {
        @Nullable PlayerTeam ownerTeam = owner == null ? null : owner.getTeam();
        @Nullable Vec3 sourcePos = this.source == null ? null : this.source.getBoundingBox().getCenter();
        Vec3 startPos = sourcePos == null ? this.position() : sourcePos;
        int radius = 8;// FIXME:
        AABB searchBox = new AABB(
            startPos.x - radius, startPos.y - radius, startPos.z - radius,
            startPos.x + radius, startPos.y + radius, startPos.z + radius
        );
        @Nullable LivingEntity t = null;
        int cost = Integer.MAX_VALUE;
        List<Entity> list = this.level().getEntities(this, searchBox, EntitySelector.NO_SPECTATORS);
        for (Entity entity : list) {
            if (entity instanceof LivingEntity e) {
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
        if (t != null && cost <= this.initEnergy) {
            this.nextEnergy = this.initEnergy - cost;
            return t;
        }
        return null;
    }

    protected int costForStriking(Vec3 startPos, LivingEntity e) {
        // TODO: check water
        double dist = e.getBoundingBox().distanceToSqr(startPos);
        return (int)(dist * ENERGY_COST_PER_BLOCK);
    }

}

package yanagi.enchantment.eventhandler.weaponeffect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ArrowNockEvent;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.entity.projectile.GuidingArrow;
import yanagi.enchantment.entry.YEEnchantments;

public abstract class BetTheFarmShootEffect {

    public static final int DURATION = 80;

    public static boolean isOffhandArrowCountValid(int count) {
        return count > 0 && count % 8 == 0;
    }
    public static int resolveDurabilityUse(int count) {
        return Mth.ceil(Math.sqrt(count));
    }

    public static boolean handleArrowNock(ArrowNockEvent event) {
        // LivingEntity shooter = event.getEntity();
        // ItemStack bow = event.getBow();
        // if (!(shooter instanceof Player) || !(bow.getItem() instanceof BowItem)) {
        //     return false;
        // }
        // Registry<Enchantment> enchantments = shooter.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        // int level = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.BET_THE_FARM), shooter);
        // if (level <= 0) {
        //     return false;
        // }
        // ItemStack off = shooter.getOffhandItem();
        // if (off.isEmpty() || !off.is(ItemTags.ARROWS)) {
        //     return false;
        // }
        // int arrowCount = off.getCount();
        // if (!isOffhandArrowCountValid(arrowCount)) {
        //     return false;
        // }
        // shooter.addEffect(new MobEffectInstance(YEEffects.BET_THE_FARM, DURATION, 0));
        return true;
    }

    public static boolean handleArrowLoose(ArrowLooseEvent event) {
        // LivingEntity shooter = event.getEntity();
        // ItemStack bow = event.getBow();

        // if (!shooter.hasEffect(YEEffects.BET_THE_FARM)) {
        //     return false;
        // }
        // Registry<Enchantment> enchantments = shooter.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        // int enchantLvl = EnchantmentHelper.getEnchantmentLevel(enchantments.getHolderOrThrow(YEEnchantments.BET_THE_FARM), shooter);
        // if (enchantLvl <= 0) {
        //     return false;
        // }

        // if (shooter instanceof Player player) {
        //     // Check if fully charged
        //     int charge = event.getCharge();
        //     float power = BowItem.getPowerForTime(charge);
        //     if (power < 1.0f) {
        //         return false;
        //     }
    
        //     Level level = player.level();
    
        //     event.setCanceled(true);   // cancel the original behavior
    
        //     float f = BowItem.getPowerForTime(charge);
        //     if (!((double)f < 0.1)) {
        //         boolean ok = true;
        //         if (level instanceof ServerLevel serverLevel) {
        //             ok = shoot(serverLevel, player, player.getUsedItemHand(), bow, f * 3.0F, f == 1.0F, enchantLvl);
        //         }
    
        //         level.playSound(
        //             null,
        //             player.getX(),
        //             player.getY(),
        //             player.getZ(),
        //             SoundEvents.ARROW_SHOOT,
        //             SoundSource.PLAYERS,
        //             1.0F,
        //             0.5F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 1.0F
        //         );
        //         player.awardStat(Stats.ITEM_USED.get(bow.getItem()));
        //         return ok;
        //     }
        //     return false;
        // }
        return false;
    }

    protected static boolean shoot(
        ServerLevel level,
        LivingEntity shooter,
        InteractionHand hand,
        ItemStack weapon,
        float velocity,
        boolean isCrit,
        int enchantLvl
    ) {
        ItemStack off = shooter.getOffhandItem();
        if (off.isEmpty() || !off.is(ItemTags.ARROWS)) {
            return false;
        }
        int arrowCount = off.getCount();
        if (!isOffhandArrowCountValid(arrowCount)) {
            return false;
        }

        Vec3 look = shooter.getLookAngle().normalize();
        Vec3[] dirs = DiscSpread.uniformCone(look, 45.0f, arrowCount);
        for (int i = 0; i < arrowCount; i++) {
            ItemStack arrowProto = off.copy();
            arrowProto.setCount(1);
            if (!arrowProto.isEmpty()) {
                Projectile projectile = createProjectile(level, shooter, weapon, arrowProto, isCrit, enchantLvl);
                shootProjectile(shooter, projectile, velocity, dirs[i % dirs.length]);
                level.addFreshEntity(projectile);
                if (weapon.isEmpty()) {
                    break;
                }
            }
        }
        if (shooter instanceof Player player) {
            if (!player.isCreative()) {
                shooter.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
        weapon.hurtAndBreak(resolveDurabilityUse(arrowCount), shooter, LivingEntity.getSlotForHand(hand));
        return true;
    }

    protected static Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit, int enchantLvl) {
        ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem arrowItem1 ? arrowItem1 : (ArrowItem)Items.ARROW;
        ItemStack itemStack = new ItemStack(arrowItem);
        GuidingArrow abstractArrow = new GuidingArrow(level, shooter, itemStack, weapon);
        abstractArrow.setOwner(shooter);
        abstractArrow.setOriginalArrowStack(itemStack.copy());
        abstractArrow.setEnchantmentLevel(enchantLvl);
        if (isCrit) {
            abstractArrow.setCritArrow(true);
        }
        return abstractArrow;
    }

    protected static void shootProjectile(
        LivingEntity shooter, Projectile projectile, float velocity, Vec3 dir
    ) {

        Vec3 eye = shooter.getEyePosition();
        Vec3 spawn = eye.add(dir.scale(0.2));
        projectile.moveTo(spawn.x, spawn.y, spawn.z, shooter.getYRot(), shooter.getXRot());
        projectile.shoot(dir.x, dir.y, dir.z, velocity, 0.0f);
    }

    public static final class DiscSpread {
        
        public static Vec3[] uniformCone(Vec3 axis, float thetaDeg, int n) {
            Vec3[] out = new Vec3[n];
            axis = axis.normalize();
            // 构造与 axis 正交的基
            Vec3 u = axis.y != 1 && axis.y != -1 ? axis.cross(new Vec3(0,1,0)).normalize() : axis.cross(new Vec3(1,0,0)).normalize();
            Vec3 v = axis.cross(u).normalize();

            double thetaMax = Math.toRadians(thetaDeg);
            double rMax = Math.sin(thetaMax); // 球面投影半径
            double golden = Math.PI * (3 - Math.sqrt(5));
            for (int i = 0; i < n; i++) {
                double t = (i + 0.5) / n;              // 0..1
                double r = rMax * Math.sqrt(t);        // 面积均匀
                double phi = i * golden;

                // 球面坐标到方向向量（小角近似：径向分量 ~ r）
                Vec3 dir = axis.scale(Math.sqrt(1 - r*r))
                        .add(u.scale(r * Math.cos(phi)))
                        .add(v.scale(r * Math.sin(phi)))
                        .normalize();
                out[i] = dir;
            }
            return out;
        }

    }

}

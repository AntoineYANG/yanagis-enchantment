package yanagi.enchantment.eventhandler;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ArrowNockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.eventhandler.weaponeffect.BattleRhythmHitEffect;
import yanagi.enchantment.eventhandler.weaponeffect.BetTheFarmShootEffect;
import yanagi.enchantment.eventhandler.weaponeffect.ChainLightningHitEffect;
import yanagi.enchantment.eventhandler.weaponeffect.CriticalHitEffect;
import yanagi.enchantment.eventhandler.weaponeffect.LifeStealHitEffect;

public class YEEventHandler {
    
    private boolean isAttacking = false;

    @SubscribeEvent
    public void onEntityAttack(LivingIncomingDamageEvent event) {
        DamageSource entitySource = event.getSource();
        Entity entity = entitySource.getEntity();
        if (entity == null) {
            return;
        }
        if (entity instanceof Player p) {
            if (!p.isCreative() && YEEffects.isInterrupted(p)) {
                event.setCanceled(true);
                return;
            }
        }
        if (YEEffects.isInterrupted(entity)) {
            event.setCanceled(true);
            return;
        }
        // this event will be trigger recursively by EntityLiving#hurt
        if (isAttacking) {
            return;
        }
        isAttacking = true;
        // Enchantment [Critical Hit]
        CriticalHitEffect.apply(event);
        // Enchantment [Battle Rhythm]
        BattleRhythmHitEffect.apply(event);
        // Enchantment [Chain Lightning]
        ChainLightningHitEffect.apply(event);
        // // Enchantment [Mighty Knockdown]
        // MightyKnockdownHitEffect.apply(event);
        // Enchantment [Life Steal]
        LifeStealHitEffect.apply(event);
        isAttacking = false;
    }

    // @SubscribeEvent
    // public void onArrowNock(ArrowNockEvent event) {
    //     // Enchantment [Bet the Farm]
    //     BetTheFarmShootEffect.handleArrowNock(event);
    // }

    // @SubscribeEvent
    // public void onArrowLoose(ArrowLooseEvent event) {
    //     // Enchantment [Bet the Farm]
    //     BetTheFarmShootEffect.handleArrowLoose(event);
    // }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
        Player entity = event.getEntity();
        if (entity != null && !entity.isCreative() && YEEffects.isStunned(entity)) {
            event.setCanceled(true);
        }
    }

}

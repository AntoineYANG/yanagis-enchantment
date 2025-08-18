package yanagi.enchantment.eventhandler;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yanagi.enchantment.effect.YEEffects;
import yanagi.enchantment.eventhandler.weaponeffect.MightyKnockdownEffect;

public class YEEventHandler {

    private boolean isAttacking = false;

    @SubscribeEvent
    public void onEntityAttack(LivingIncomingDamageEvent event) {
        DamageSource entitySource = event.getSource();
        Entity entity = entitySource.getEntity();
        if (entity == null) {
            event.setCanceled(true);
            return;
        }
        if (entity instanceof Player p) {
            if (!p.isCreative() && YEEffects.isStunned(p)) {
                event.setCanceled(true);
                return;
            }
        }
        if (YEEffects.isStunned(entity)) {
            event.setCanceled(true);
            return;
        }
        // this event will be trigger recursively by EntityLiving#hurt
        if (isAttacking) {
            return;
        }
        isAttacking = true;
        // Enchantment [Mighty Knockdown]
        MightyKnockdownEffect.apply(event);
        isAttacking = false;
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
        Player entity = event.getEntity();
        if (entity != null && !entity.isCreative() && YEEffects.isStunned(entity)) {
            event.setCanceled(true);
        }
    }

}

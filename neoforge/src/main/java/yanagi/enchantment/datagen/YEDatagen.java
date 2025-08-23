package yanagi.enchantment.datagen;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import yanagi.enchantment.YanagisEnchantment;
import yanagi.enchantment.entry.YEDamageTypes;
import yanagi.enchantment.entry.YEEnchantments;

@EventBusSubscriber(modid = YanagisEnchantment.MOD_ID)
public class YEDatagen {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        DatapackBuiltinEntriesProvider provider = new DatapackBuiltinEntriesProvider(
            packOutput,
            lookupProvider,
            new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, YEDamageTypes::createDamageTypes)
                .add(Registries.ENCHANTMENT, YEEnchantments::createEnchantments),
            Set.of(YanagisEnchantment.MOD_ID)
        );
        lookupProvider = provider.getRegistryProvider();

        gen.addProvider(event.includeServer(), provider);
        TagProvider.register(gen, event, packOutput, lookupProvider, existingFileHelper);
    }

}

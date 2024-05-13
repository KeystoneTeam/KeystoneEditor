package keystone.core.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;

public final class RegistryLookups
{
    private static DynamicRegistryManager dynamicRegistryManager = DynamicRegistryManager.of(Registries.REGISTRIES);
    private static DataPackContents.ConfigurableWrapperLookup registryLookup = new DataPackContents.ConfigurableWrapperLookup(dynamicRegistryManager);
    private static CommandRegistryAccess commandRegistryLookup = CommandRegistryAccess.of(registryLookup, FeatureSet.empty());
    
    public static void configureLookups(DynamicRegistryManager.Immutable dynamicRegistryManager, DataPackContents.ConfigurableWrapperLookup registryLookup, FeatureSet enabledFeatures)
    {
        RegistryLookups.dynamicRegistryManager = dynamicRegistryManager;
        RegistryLookups.registryLookup = registryLookup;
        RegistryLookups.commandRegistryLookup = CommandRegistryAccess.of(registryLookup, enabledFeatures);
    }
    
    public static RegistryWrapper.WrapperLookup registryLookup() { return registryLookup; }
    public static CommandRegistryAccess commandRegistryLookup() { return commandRegistryLookup; }
    
    public static <T> Registry<T> registry(RegistryKey<? extends Registry<T>> registryKey) { return dynamicRegistryManager.get(registryKey); }
    public static <T> RegistryWrapper<T> registryLookup(RegistryKey<Registry<T>> registryKey) { return commandRegistryLookup.getWrapperOrThrow(registryKey); }
    
    public static <T> RegistryEntry<T> parseRegistryEntry(RegistryKey<? extends Registry<T>> registryKey, String str)
    {
        try { return RegistryEntryReferenceArgumentType.registryEntry(commandRegistryLookup, registryKey).parse(new StringReader(str)); }
        catch (CommandSyntaxException e) { throw new IllegalStateException("RegistryLookups.parseRegistryEntry failed on '" + str + "'!", e); }
    }
}
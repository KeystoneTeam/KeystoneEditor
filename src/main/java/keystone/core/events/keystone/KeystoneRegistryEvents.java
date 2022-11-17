package keystone.core.events.keystone;

import keystone.api.Keystone;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.schematic.SchematicLoader;
import keystone.core.schematic.extensions.ISchematicExtension;
import keystone.core.schematic.formats.ISchematicFormat;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import keystone.core.serialization.VariableSerializer;
import keystone.core.serialization.VariablesSerializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;

public final class KeystoneRegistryEvents
{
    //region Events
    public static final Event<RegisterModulesListener> MODULES = EventFactory.createArrayBacked(RegisterModulesListener.class, listeners -> registry ->
    {
        for (final RegisterModulesListener listener : listeners) listener.register(registry);
    });
    public static final Event<RegisterSchematicFormatsListener> SCHEMATIC_FORMATS = EventFactory.createArrayBacked(RegisterSchematicFormatsListener.class, listeners -> registry ->
    {
        for (final RegisterSchematicFormatsListener listener : listeners) listener.register(registry);
    });
    public static final Event<RegisterSchematicExtensionsListener> SCHEMATIC_EXTENSIONS = EventFactory.createArrayBacked(RegisterSchematicExtensionsListener.class, listeners -> registry ->
    {
        for (final RegisterSchematicExtensionsListener listener : listeners) listener.register(registry);
    });
    public static final Event<RegisterHistoryEntriesListener> HISTORY_ENTRIES = EventFactory.createArrayBacked(RegisterHistoryEntriesListener.class, listeners -> () ->
    {
        for (final RegisterHistoryEntriesListener listener : listeners) listener.onRegister();
    });
    public static final Event<RegisterVariableSerializersListener> VARIABLE_SERIALIZERS = EventFactory.createArrayBacked(RegisterVariableSerializersListener.class, listeners -> () ->
    {
        for (final RegisterVariableSerializersListener listener : listeners) listener.onRegister();
    });
    //endregion
    //region Event Interfaces
    public interface RegisterModulesListener
    {
        void register(Consumer<IKeystoneModule> registry);
    }
    public interface RegisterSchematicFormatsListener
    {
        void register(Consumer<ISchematicFormat> registry);
    }
    public interface RegisterSchematicExtensionsListener
    {
        void register(Consumer<ISchematicExtension> registry);
    }
    public interface RegisterHistoryEntriesListener
    {
        interface HistoryEntryDeserializer
        {
            IHistoryEntry deserialize(NbtCompound nbt);
        }

        void onRegister();

        default <T extends IHistoryEntry> void register(String id, HistoryEntryDeserializer deserializer)
        {
            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.registerDeserializer(id, deserializer);
        }
    }
    public interface RegisterVariableSerializersListener
    {
        void onRegister();

        default <T> void register(Class<T> clazz, VariableSerializer<T> serializer)
        {
            VariablesSerializer.registerSerializer(clazz, serializer);
        }
    }
    //endregion
    //region Static Helpers
    public static void registerModules()
    {
        MODULES.invoker().register(Keystone::registerModule);
    }
    public static void registerSchematicFormats()
    {
        SCHEMATIC_FORMATS.invoker().register(SchematicLoader::registerFormat);
    }
    public static void registerSchematicExtensions()
    {
        SCHEMATIC_EXTENSIONS.invoker().register(KeystoneSchematicFormat::registerExtension);
    }
    public static void registerHistoryEntries()
    {
        HISTORY_ENTRIES.invoker().onRegister();
    }
    public static void registerVariableSerializers()
    {
        VariablesSerializer.registerDefaultSerializers();
        VARIABLE_SERIALIZERS.invoker().onRegister();
    }
    //endregion
}

package keystone.core;

import keystone.core.events.KeystoneEvent;
import keystone.core.renderer.config.KeystoneConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KeystoneMod.MODID)
public class KeystoneMod
{
    public static final String MODID = "keystone";

    public KeystoneMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        Keystone.LOGGER.info("Triggering Keystone initialization events");

        Keystone.registerDefaultBoxes(new KeystoneEvent.RegisterBoundingBoxTypes());
        Keystone.registerDefaultModules(new KeystoneEvent.RegisterModules());

//        TODO: Figure out why events aren't working
//        MinecraftForge.EVENT_BUS.post(new KeystoneEvent.RegisterBoundingBoxTypes());
//        MinecraftForge.EVENT_BUS.post(new KeystoneEvent.RegisterModules());
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        KeystoneKeybinds.register();
    }
}

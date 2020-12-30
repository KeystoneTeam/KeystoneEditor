package keystone.core;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(KeystoneMod.MODID)
public class KeystoneMod
{
    public static final String MODID = "keystone";
    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean KeystoneActive = false;

    public KeystoneMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }
    public static void toggleKeystone()
    {
        if (KeystoneActive) KeystoneActive = false;
        else KeystoneActive = true;
    }

    private void setup(final FMLCommonSetupEvent event)
    {

    }
    private void clientSetup(final FMLClientSetupEvent event)
    {
        KeystoneKeybinds.register();
    }
}

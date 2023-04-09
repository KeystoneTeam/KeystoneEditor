package keystone.api.enums;

import keystone.api.Keystone;
import keystone.core.FeatureSupportFlags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WorldType
{
    public static final WorldType SINGLEPLAYER = supported(FeatureSupportFlags.ALL_FEATURES);
    public static final WorldType HOSTING_LAN = supported(FeatureSupportFlags.ALL_FEATURES);
    public static final WorldType NONE = unsupported("keystone.not_supported.none");
    public static final WorldType CONNECTED_TO_LAN = unsupported("keystone.not_supported.multiplayer");
    public static final WorldType SERVER = unsupported("keystone.not_supported.multiplayer");
    public static final WorldType REALMS = unsupported("keystone.not_supported.multiplayer");
    
    public final boolean supportsKeystone;
    public final FeatureSupportFlags supportedFeatures;
    private final String supportErrorKey;
    
    private static WorldType supported(FeatureSupportFlags supportedFeatures) { return new WorldType(true, supportedFeatures, null); }
    private static WorldType unsupported(String errorKey) { return new WorldType(false, FeatureSupportFlags.NO_FEATURES, errorKey); }
    private WorldType (boolean supportsKeystone, FeatureSupportFlags supportedFeatures, String supportErrorKey)
    {
        this.supportsKeystone = supportsKeystone;
        this.supportedFeatures = supportedFeatures;
        this.supportErrorKey = supportErrorKey;
    }
    
    public static WorldType get()
    {
        // Get client and server info
        MinecraftClient client = MinecraftClient.getInstance();
        MinecraftServer server = client.getServer();
        ServerInfo serverInfo = client.getCurrentServerEntry();
        
        // Check what type of world the player is connected to
        if (client.isConnectedToRealms()) return REALMS;
        else if (server != null)
        {
            if (!server.isRemote()) return SINGLEPLAYER;
            else return HOSTING_LAN;
        }
        else if (serverInfo != null)
        {
            if (serverInfo.isLocal()) return CONNECTED_TO_LAN;
            else return SERVER;
        }
        else return NONE;
    }
    
    public boolean canEnableKeystone(boolean logError)
    {
        PlayerEntity player = MinecraftClient.getInstance().player;
        
        // Check the 'supportsKeystone' flag
        if (!supportsKeystone)
        {
            if (logError)
            {
                Text error = Text.translatable(supportErrorKey).styled(style -> style.withColor(Formatting.RED));
                Keystone.LOGGER.error(error.getString());
                if (player != null) player.sendMessage(error);
            }
            return false;
        }
        
        // Check player permission level
        if (player != null && !player.hasPermissionLevel(4))
        {
            if (logError)
            {
                Text error = Text.translatable("keystone.not_supported.noPermission").styled(style -> style.withColor(Formatting.RED));
                Keystone.LOGGER.error(error.getString());
                player.sendMessage(error);
            }
            return false;
        }
        
        // Return true if all tests passed
        return true;
    }
}

import keystone.api.filters.KeystoneFilter;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class VanillaTest extends KeystoneFilter
{
    @Override
    public void finished()
    {
        ChestType chestType = ChestType.LEFT;
        
        MinecraftClient client = MinecraftClient.getInstance();
        client.player.sendMessage(Text.literal("Success!!!"));    //.styled(style -> style.withColor(Formatting.GREEN)));
    }
}
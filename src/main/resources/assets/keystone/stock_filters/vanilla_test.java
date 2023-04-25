import keystone.api.filters.KeystoneFilter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class VanillaTest extends KeystoneFilter
{
    @Override
    public void finished()
    {
        MinecraftClient client = MinecraftClient.getInstance();
        MinecraftClient client2 = net.minecraft.client.MinecraftClient.getInstance();
        client.player.sendMessage(Text.of("Success!!!"));
    }
}
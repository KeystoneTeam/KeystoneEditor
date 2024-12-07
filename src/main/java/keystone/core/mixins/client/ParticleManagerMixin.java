package keystone.core.mixins.client;

import keystone.core.mixins.interfaces.KeystoneParticleManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin implements KeystoneParticleManager
{
    @Shadow protected ClientWorld world;
    
    @Override
    public ClientWorld getWorld()
    {
        return world;
    }
    
    @Override
    public void setWorld(ClientWorld newWorld)
    {
        world = newWorld;
    }
}

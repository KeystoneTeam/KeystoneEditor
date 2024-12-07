package keystone.core.mixins.interfaces;

import net.minecraft.client.world.ClientWorld;

public interface KeystoneParticleManager
{
    ClientWorld getWorld();
    void setWorld(ClientWorld world);
}

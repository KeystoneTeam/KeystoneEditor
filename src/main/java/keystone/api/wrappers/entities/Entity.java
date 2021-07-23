package keystone.api.wrappers.entities;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.core.math.BlockPosMath;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.arguments.NBTTagArgument;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.*;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;
import java.util.UUID;

/**
 * A wrapper for a Minecraft entity. Contains information about the entity's NBT data
 */
public class Entity
{
    private CompoundNBT entityData;
    private UUID uuid;
    private Vector3d position;
    private float pitch;
    private float yaw;
    private boolean killed;

    //region INTERNAL USE
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for a given entity type ID
     * @param id The entity type ID
     */
    public Entity(String id)
    {
        this.entityData = new CompoundNBT();
        this.entityData.putString("id", id);
        this.uuid = null;
        this.position = Vector3d.ZERO;
        this.pitch = 0;
        this.yaw = 0;
        this.killed = false;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for a Minecraft entity
     * @param minecraftEntity The Minecraft entity
     */
    public Entity(net.minecraft.entity.Entity minecraftEntity)
    {
        this(minecraftEntity.serializeNBT(), true);
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for an NBT compound
     * @param nbt The Minecraft entity data
     * @param copyUUID If true, the UUID stored in the NBT will be copied to the wrapper
     */
    public Entity(CompoundNBT nbt, boolean copyUUID)
    {
        this.entityData = nbt.copy();
        if (copyUUID)
        {
            if (this.entityData.hasUUID("UUID")) this.uuid = this.entityData.getUUID("UUID");
        }
        else this.entityData.remove("UUID");

        if (this.entityData.contains("Pos"))
        {
            ListNBT posNBT = this.entityData.getList("Pos", Constants.NBT.TAG_DOUBLE);
            this.position = new Vector3d(posNBT.getDouble(0), posNBT.getDouble(1), posNBT.getDouble(2));
        }
        if (this.entityData.contains("Rotation"))
        {
            ListNBT rotationNBT = this.entityData.getList("Rotation", Constants.NBT.TAG_FLOAT);
            this.yaw = rotationNBT.getFloat(0);
            this.pitch = rotationNBT.getFloat(1);
        }
        this.killed = false;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft CompoundNBT that represents this entity
     */
    public CompoundNBT getMinecraftEntityData() { return this.entityData; }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Clears the wrapper's UUID, breaking its connection with the Minecraft entity
     */
    public void clearUUID()
    {
        this.uuid = null;
        this.entityData.remove("UUID");
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Spawn a new instance of this entity into a server world
     * @param world The IServerWorld to spawn the entity in
     * @return The Minecraft entity that was spawned into the server world
     */
    public net.minecraft.entity.Entity spawnInWorld(IServerWorld world)
    {
        return spawnInWorld(world, null, Rotation.NONE, Mirror.NONE, Vector3i.ZERO, 1);
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Spawn a new instance of this entity into a server world
     * @param world The IServerWorld to spawn the entity in
     * @param anchor The anchor of the spawned entity. Used to determine the pivot for rotations and mirrors
     * @param rotation The Rotation to place the entity with. Used by schematics
     * @param mirror The Mirror to place the entity with. Used by schematics
     * @param size The size of the area to perform the orientation logic with
     * @param scale The scale of the area to perform the orientation logic with
     * @return The Minecraft entity that was spawned into the server world
     */
    public net.minecraft.entity.Entity spawnInWorld(IServerWorld world, Vector3d anchor, Rotation rotation, Mirror mirror, Vector3i size, int scale)
    {
        if (anchor == null) anchor = Vector3d.ZERO;
        CompoundNBT entityNBT = this.entityData.copy();

        Vector3d oriented = BlockPosMath.getOrientedVector3d(this.position, size, rotation, mirror, scale);
        double x = anchor.x + oriented.x;
        double y = anchor.y + oriented.y;
        double z = anchor.z + oriented.z;

        // Spawning
        Optional<net.minecraft.entity.Entity> entityOptional = EntityType.create(entityNBT, world.getLevel());
        if (entityOptional.isPresent())
        {
            net.minecraft.entity.Entity minecraftEntity = entityOptional.get();

            float rotatedYaw = minecraftEntity.mirror(mirror);
            rotatedYaw = rotatedYaw + (minecraftEntity.yRot - minecraftEntity.rotate(rotation));
            minecraftEntity.moveTo(x, y, z, rotatedYaw, minecraftEntity.xRot);

            world.addFreshEntityWithPassengers(minecraftEntity);
            return minecraftEntity;
        }
        else return null;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Update this wrapper's Minecraft entity with all modifications made to the wrapper,
     * or spawn the entity if none exist
     * @param world The IServerWorld to search for the entity in
     */
    public void updateMinecraftEntity(IServerWorld world)
    {
        if (this.uuid != null)
        {
            net.minecraft.entity.Entity mcEntity = world.getLevel().getEntity(this.uuid);
            if (!this.killed) mcEntity.deserializeNBT(this.entityData);
            else mcEntity.remove();
        }
        else spawnInWorld(world);
    }
    //endregion
    //region API
    /**
     * Create an identical copy of this entity, except for the UUID
     * @return The cloned entity
     */
    public Entity clone()
    {
        Entity clone = new Entity(this.entityData.copy(), false);
        clone.uuid = null;
        clone.position = position;
        clone.pitch = pitch;
        clone.yaw = yaw;
        clone.killed = killed;
        return clone;
    }
    /**
     * @return The x-coordinate of the entity
     */
    public double x() { return this.position.x; }
    /**
     * @return The y-coordinate of the entity
     */
    public double y() { return this.position.y; }
    /**
     * @return The z-coordinate of the entity
     */
    public double z() { return this.position.z; }
    /**
     * @return The yaw angle of the entity, in degrees
     */
    public float yaw() { return this.yaw; }
    /**
     * @return The pitch angle of the entity, in degrees
     */
    public float pitch() { return this.pitch; }
    /**
     * @return If this entity is in the world, the UUID of the Minecraft
     * entity it represents, otherwise null
     */
    public UUID uuid() { return this.uuid; }

    /**
     * @return Whether the entity has been marked as killed
     */
    public boolean killed() { return this.killed; }

    /**
     * Set the entity's position
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @return The modified entity instance
     */
    public Entity position(double x, double y, double z)
    {
        ListNBT posNBT = new ListNBT();
        posNBT.add(DoubleNBT.valueOf(x));
        posNBT.add(DoubleNBT.valueOf(y));
        posNBT.add(DoubleNBT.valueOf(z));
        this.entityData.put("Pos", posNBT);
        this.position = new Vector3d(x, y, z);

        return this;
    }

    /**
     * Move the entity's position by a given offset
     * @param x The x-offset
     * @param y The y-offset
     * @param z The z-offset
     * @return The modified entity instance
     */
    public Entity move(double x, double y, double z)
    {
        if (this.position == null) return position(x, y, z);
        else
        {
            this.position = new Vector3d(this.position.x + x, this.position.y + y, this.position.z + z);

            ListNBT posNBT = new ListNBT();
            posNBT.add(DoubleNBT.valueOf(this.position.x));
            posNBT.add(DoubleNBT.valueOf(this.position.y));
            posNBT.add(DoubleNBT.valueOf(this.position.z));
            this.entityData.put("Pos", posNBT);

            return this;
        }
    }
    /**
     * Set the entity's yaw
     * @param yaw The yaw
     * @return The modified entity instance
     */
    public Entity yaw(float yaw)
    {
        ListNBT rotationNBT = new ListNBT();
        rotationNBT.add(FloatNBT.valueOf(yaw));
        if (this.entityData.contains("Rotation")) rotationNBT.add(this.entityData.getList("Rotation", Constants.NBT.TAG_FLOAT).get(1));
        else rotationNBT.add(FloatNBT.ZERO);
        this.entityData.put("Rotation", rotationNBT);
        this.yaw = yaw;

        return this;
    }
    /**
     * Set the entity's pitch
     * @param pitch The pitch
     * @return The modified entity instance
     */
    public Entity pitch(float pitch)
    {
        ListNBT rotationNBT = new ListNBT();
        if (this.entityData.contains("Rotation")) rotationNBT.add(this.entityData.getList("Rotation", Constants.NBT.TAG_FLOAT).get(0));
        else rotationNBT.add(FloatNBT.ZERO);
        rotationNBT.add(FloatNBT.valueOf(pitch));
        this.entityData.put("Rotation", rotationNBT);
        this.pitch = pitch;

        return this;
    }
    /**
     * Mark the entity as killed. Will be removed from the world when updated
     */
    public void kill() { this.killed = true; }
    /**
     * Set whether the entity is killed. When updated, if true, the entity will be
     * removed from the world
     */
    public void setKilled(boolean killed) { this.killed = killed; }

    /**
     * Set NBT data at a given path to a given value
     * @param path The NBT path. [e.g. "Items[0].Count", "Items[{Slot:0b}]"]
     * @param data The value to set. [e.g. "32b", "{id:"minecraft:diamond",Count:2b}"]
     * @return The modified entity instance
     */
    public Entity data(String path, String data)
    {
        if (path.equals("id"))
        {
            Keystone.abortFilter("Modifying an entity's type ID is not allowed!");
            return this;
        }

        try
        {
            NBTPathArgument.NBTPath nbtPath = NBTPathArgument.nbtPath().parse(new StringReader(path));
            INBT nbt = NBTTagArgument.nbtTag().parse(new StringReader(data));
            nbtPath.set(this.entityData, () -> nbt);

            return this;
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
            return this;
        }
    }
    //endregion
}

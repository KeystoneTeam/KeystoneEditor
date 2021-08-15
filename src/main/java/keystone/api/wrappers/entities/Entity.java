package keystone.api.wrappers.entities;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.client.Player;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.arguments.NBTTagArgument;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.*;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;
import java.util.UUID;

/**
 * A wrapper for a Minecraft entity. Contains information about the entity's NBT data
 */
public class Entity
{
    private CompoundNBT entityData;
    private UUID keystoneUUID;
    private UUID minecraftUUID;
    private Vector3d position;
    private float pitch;
    private float yaw;
    private boolean killed;
    private BoundingBox boundingBox;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for a given entity type ID
     * @param id The entity type ID
     */
    public Entity(String id)
    {
        this.entityData = new CompoundNBT();
        this.entityData.putString("id", id);
        this.keystoneUUID = UUID.randomUUID();
        this.minecraftUUID = null;
        this.position = Vector3d.ZERO;
        this.pitch = 0;
        this.yaw = 0;
        this.killed = false;
        updateBoundingBox();
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
     * @param copyMinecraftUUID If true, the UUID stored in the NBT will be copied to the wrapper
     */
    public Entity(CompoundNBT nbt, boolean copyMinecraftUUID)
    {
        this(nbt, copyMinecraftUUID, UUID.randomUUID());
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for an NBT compound
     * @param nbt The Minecraft entity data
     * @param copyMinecraftUUID If true, the UUID stored in the NBT will be copied to the wrapper
     * @param keystoneUUID The Keystone UUID
     */
    public Entity(CompoundNBT nbt, boolean copyMinecraftUUID, UUID keystoneUUID)
    {
        this.entityData = nbt.copy();
        this.keystoneUUID = keystoneUUID;

        if (copyMinecraftUUID)
        {
            if (this.entityData.hasUUID("UUID")) this.minecraftUUID = this.entityData.getUUID("UUID");
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

        updateBoundingBox();
    }
    private Entity() {}

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return The serialized CompoundNBT
     */
    public CompoundNBT serialize()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("keystone_uuid", keystoneUUID);
        if (minecraftUUID != null) nbt.putUUID("minecraft_uuid", minecraftUUID);

        ListNBT posNBT = new ListNBT();
        posNBT.add(DoubleNBT.valueOf(position.x));
        posNBT.add(DoubleNBT.valueOf(position.y));
        posNBT.add(DoubleNBT.valueOf(position.z));
        nbt.put("pos", posNBT);

        nbt.putFloat("pitch", pitch);
        nbt.putFloat("yaw", yaw);
        nbt.putBoolean("killed", killed);

        ListNBT boundingBoxNBT = new ListNBT();
        boundingBoxNBT.add(DoubleNBT.valueOf(boundingBox.minX));
        boundingBoxNBT.add(DoubleNBT.valueOf(boundingBox.minY));
        boundingBoxNBT.add(DoubleNBT.valueOf(boundingBox.minZ));
        boundingBoxNBT.add(DoubleNBT.valueOf(boundingBox.maxX));
        boundingBoxNBT.add(DoubleNBT.valueOf(boundingBox.maxY));
        boundingBoxNBT.add(DoubleNBT.valueOf(boundingBox.maxZ));
        nbt.put("bounding_box", boundingBoxNBT);

        nbt.put("nbt", entityData);
        return nbt;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param nbt The serialized CompoundNBT
     * @return The deserialized Entity
     */
    public static Entity deserialize(CompoundNBT nbt)
    {
        Entity entity = new Entity();
        entity.entityData = nbt.getCompound("nbt");
        entity.keystoneUUID = nbt.getUUID("keystone_uuid");
        if (nbt.contains("minecraft_uuid")) entity.minecraftUUID = nbt.getUUID("minecraft_uuid");

        ListNBT posNBT = nbt.getList("pos", Constants.NBT.TAG_DOUBLE);
        entity.position = new Vector3d(posNBT.getDouble(0), posNBT.getDouble(1), posNBT.getDouble(2));

        entity.pitch = nbt.getFloat("pitch");
        entity.yaw = nbt.getFloat("yaw");
        entity.killed = nbt.getBoolean("killed");

        ListNBT bb = nbt.getList("bounding_box", Constants.NBT.TAG_DOUBLE);
        entity.boundingBox = new BoundingBox(bb.getDouble(0), bb.getDouble(1), bb.getDouble(2), bb.getDouble(3), bb.getDouble(4), bb.getDouble(5));

        return entity;
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
    public void breakMinecraftEntityConnection()
    {
        this.minecraftUUID = null;
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
        CompoundNBT entityNBT = this.entityData.copy();

        // Spawning
        Optional<net.minecraft.entity.Entity> entityOptional = EntityType.create(entityNBT, world.getLevel());
        if (entityOptional.isPresent())
        {
            net.minecraft.entity.Entity minecraftEntity = entityOptional.get();
            if (!(world instanceof GhostBlocksWorld)) this.minecraftUUID = minecraftEntity.getUUID();
            minecraftEntity.moveTo(position.x, position.y, position.z, yaw, pitch);
            world.addFreshEntityWithPassengers(minecraftEntity);
            return minecraftEntity;
        }
        else return null;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param anchor The Vector3d to use as an anchor for rotation
     * @param rotation The Rotation
     * @param mirror The Mirror
     * @param size The size of the region that is being oriented
     * @param scale The scale
     * @return A copy of this entity with an applied orientation
     */
    public Entity getOrientedEntity(Vector3d anchor, Rotation rotation, Mirror mirror, Vector3i size, int scale)
    {
        if (anchor == null) anchor = Vector3d.ZERO;
        CompoundNBT entityNBT = this.entityData.copy();

        Vector3d oriented = BlockPosMath.getOrientedVector3d(this.position, size, rotation, mirror, scale);
        double x = anchor.x + oriented.x;
        double y = anchor.y + oriented.y;
        double z = anchor.z + oriented.z;

        float yaw = 0;
        if (entityNBT.contains("Rotation", Constants.NBT.TAG_LIST))
        {
            ListNBT rotationNBT = entityNBT.getList("Rotation", Constants.NBT.TAG_FLOAT);
            yaw = rotationNBT.getFloat(0);
        }
        switch (mirror)
        {
            case LEFT_RIGHT: yaw = 180.0F - yaw; break;
            case FRONT_BACK: yaw = -yaw; break;
        }
        switch (rotation)
        {
            case CLOCKWISE_90: yaw += 90; break;
            case CLOCKWISE_180: yaw += 180.0F; break;
            case COUNTERCLOCKWISE_90: yaw -= 90.0F; break;
        }

        Entity ret = clone();
        ret.position(x, y, z);
        ret.yaw(yaw);
        ret.breakMinecraftEntityConnection();
        return ret;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Update this wrapper's Minecraft entity with all modifications made to the wrapper,
     * or spawn the entity if none exist
     * @param world The IServerWorld to search for the entity in
     */
    public void updateMinecraftEntity(IServerWorld world)
    {
        if (this.minecraftUUID != null)
        {
            net.minecraft.entity.Entity mcEntity = world.getLevel().getEntity(this.minecraftUUID);
            if (mcEntity == null)
            {
                breakMinecraftEntityConnection();
                spawnInWorld(world);
            }

            if (!this.killed) mcEntity.deserializeNBT(this.entityData);
            else
            {
                mcEntity.remove();
                breakMinecraftEntityConnection();
            }
        }
        else spawnInWorld(world);
    }

    public void updateBoundingBox()
    {
        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimensionId());
        net.minecraft.entity.Entity mcEntity = EntityType.create(this.entityData.copy(), world).orElse(null);
        if (mcEntity == null) Keystone.LOGGER.error("Cannot update bounding box of Entity " + this);
        else boundingBox = new BoundingBox(mcEntity.getBoundingBox());
    }
    //endregion
    //region API
    /**
     * Create an identical copy of this entity, except for the UUID
     * @return The cloned entity
     */
    public Entity clone()
    {
        Entity clone = new Entity(this.entityData.copy(), false, this.keystoneUUID);
        clone.minecraftUUID = null;
        clone.position = position;
        clone.pitch = pitch;
        clone.yaw = yaw;
        clone.killed = killed;
        clone.boundingBox = boundingBox;
        return clone;
    }
    public String type() { return this.entityData.getString("id"); }
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
     * @return This Entity's {@link BoundingBox}
     */
    public BoundingBox boundingBox() { return this.boundingBox; }
    /**
     * @return The yaw angle of the entity, in degrees
     */
    public float yaw() { return this.yaw; }
    /**
     * @return The pitch angle of the entity, in degrees
     */
    public float pitch() { return this.pitch; }
    /**
     * @return An {@link NBTCompound} representing this entity's data. Note that modifying
     * this NBT Compound will not modify the entity unless you call {@link Entity#data(NBTCompound)}
     * once finished
     */
    public NBTCompound data()
    {
        return new NBTCompound(this.entityData.copy());
    }
    /**
     * @return If this entity is in the world, the UUID of the Minecraft
     * entity it represents, otherwise null
     */
    public UUID minecraftUUID() { return this.minecraftUUID; }
    /**
     * @return The UUID of this entity in Keystone. This is not the same
     * as {@link Entity#minecraftUUID}
     */
    public UUID keystoneUUID() { return this.keystoneUUID; }

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
     * @param killed Whether this entity is killed
     */
    public void setKilled(boolean killed) { this.killed = killed; }

    /**
     * Set NBT data at a given path to a given value. This cannot be used to change
     * the entity's type
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
            updateBoundingBox();
            return this;
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
            return this;
        }
    }

    /**
     * Set this entity's NBT data compound. This cannot be used to change the entity's type
     * @param data The {@link NBTCompound} to set this entity's data to
     * @return The modified entity instance
     */
    public Entity data(NBTCompound data)
    {
        if (data.getString("id") != this.entityData.getString("id"))
        {
            Keystone.abortFilter("Modifying an entity's type ID is not allowed!");
            return this;
        }

        CompoundNBT newEntityData = data.getMinecraftNBT().copy();
        newEntityData.remove("UUID");
        this.entityData = newEntityData;

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

        updateBoundingBox();
        return this;
    }
    //endregion
    //region Object Overrides
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return toString().equals(entity.toString());
    }
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder(this.entityData.getString("id"));
        stringBuilder.append(this.entityData.toString());
        stringBuilder.append("<");
        stringBuilder.append(keystoneUUID.toString());
        stringBuilder.append("|");
        stringBuilder.append(minecraftUUID == null ? "NULL" : minecraftUUID.toString());
        stringBuilder.append(">");
        return stringBuilder.toString();
    }
    //endregion
}

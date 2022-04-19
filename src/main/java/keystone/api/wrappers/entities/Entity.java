package keystone.api.wrappers.entities;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.client.Player;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.*;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

/**
 * A wrapper for a Minecraft entity. Contains information about the entity's NBT data
 */
public class Entity
{
    private NbtCompound entityData;
    private UUID keystoneUUID;
    private UUID minecraftUUID;
    private Vec3d position;
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
        this.entityData = new NbtCompound();
        this.entityData.putString("id", id);
        this.keystoneUUID = UUID.randomUUID();
        this.minecraftUUID = null;
        this.position = Vec3d.ZERO;
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
        this(minecraftEntity.writeNbt(new NbtCompound()), true);
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for an NBT compound
     * @param nbt The Minecraft entity data
     * @param copyMinecraftUUID If true, the UUID stored in the NBT will be copied to the wrapper
     */
    public Entity(NbtCompound nbt, boolean copyMinecraftUUID)
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
    public Entity(NbtCompound nbt, boolean copyMinecraftUUID, UUID keystoneUUID)
    {
        this.entityData = nbt.copy();
        this.keystoneUUID = keystoneUUID;

        if (copyMinecraftUUID)
        {
            if (this.entityData.containsUuid("UUID")) this.minecraftUUID = this.entityData.getUuid("UUID");
        }
        else this.entityData.remove("UUID");

        if (this.entityData.contains("Pos"))
        {
            NbtList posNBT = this.entityData.getList("Pos", NbtElement.DOUBLE_TYPE);
            this.position = new Vec3d(posNBT.getDouble(0), posNBT.getDouble(1), posNBT.getDouble(2));
        }
        if (this.entityData.contains("Rotation"))
        {
            NbtList rotationNBT = this.entityData.getList("Rotation", NbtElement.FLOAT_TYPE);
            this.yaw = rotationNBT.getFloat(0);
            this.pitch = rotationNBT.getFloat(1);
        }
        this.killed = false;

        updateBoundingBox();
    }
    private Entity() {}

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return The serialized NbtCompound
     */
    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("keystone_uuid", keystoneUUID);
        if (minecraftUUID != null) nbt.putUuid("minecraft_uuid", minecraftUUID);

        NbtList posNBT = new NbtList();
        posNBT.add(NbtDouble.of(position.x));
        posNBT.add(NbtDouble.of(position.y));
        posNBT.add(NbtDouble.of(position.z));
        nbt.put("pos", posNBT);

        nbt.putFloat("pitch", pitch);
        nbt.putFloat("yaw", yaw);
        nbt.putBoolean("killed", killed);

        NbtList boundingBoxNBT = new NbtList();
        boundingBoxNBT.add(NbtDouble.of(boundingBox.minX));
        boundingBoxNBT.add(NbtDouble.of(boundingBox.minY));
        boundingBoxNBT.add(NbtDouble.of(boundingBox.minZ));
        boundingBoxNBT.add(NbtDouble.of(boundingBox.maxX));
        boundingBoxNBT.add(NbtDouble.of(boundingBox.maxY));
        boundingBoxNBT.add(NbtDouble.of(boundingBox.maxZ));
        nbt.put("bounding_box", boundingBoxNBT);

        nbt.put("nbt", entityData);
        return nbt;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param nbt The serialized NbtCompound
     * @return The deserialized Entity
     */
    public static Entity deserialize(NbtCompound nbt)
    {
        Entity entity = new Entity();
        entity.entityData = nbt.getCompound("nbt");
        entity.keystoneUUID = nbt.getUuid("keystone_uuid");
        if (nbt.contains("minecraft_uuid")) entity.minecraftUUID = nbt.getUuid("minecraft_uuid");

        NbtList posNBT = nbt.getList("pos", NbtElement.DOUBLE_TYPE);
        entity.position = new Vec3d(posNBT.getDouble(0), posNBT.getDouble(1), posNBT.getDouble(2));

        entity.pitch = nbt.getFloat("pitch");
        entity.yaw = nbt.getFloat("yaw");
        entity.killed = nbt.getBoolean("killed");

        NbtList bb = nbt.getList("bounding_box", NbtElement.DOUBLE_TYPE);
        entity.boundingBox = new BoundingBox(bb.getDouble(0), bb.getDouble(1), bb.getDouble(2), bb.getDouble(3), bb.getDouble(4), bb.getDouble(5));

        return entity;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft NbtCompound that represents this entity
     */
    public NbtCompound getMinecraftEntityData() { return this.entityData; }

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
     * @param world The ServerWorldAccess to spawn the entity in
     * @return The Minecraft entity that was spawned into the server world
     */
    public net.minecraft.entity.Entity spawnInWorld(ServerWorldAccess world)
    {
        NbtCompound entityNBT = this.entityData.copy();

        // Spawning
        Optional<net.minecraft.entity.Entity> entityOptional = EntityType.getEntityFromNbt(entityNBT, world.toServerWorld());
        if (entityOptional.isPresent())
        {
            net.minecraft.entity.Entity minecraftEntity = entityOptional.get();
            if (!(world instanceof GhostBlocksWorld)) this.minecraftUUID = minecraftEntity.getUuid();
            minecraftEntity.setPos(position.x, position.y, position.z);
            minecraftEntity.setYaw(yaw);
            minecraftEntity.setPitch(pitch);
            world.spawnEntityAndPassengers(minecraftEntity);
            return minecraftEntity;
        }
        else return null;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param anchor The Vec3d to use as an anchor for rotation
     * @param rotation The Rotation
     * @param mirror The Mirror
     * @param size The size of the region that is being oriented
     * @param scale The scale
     * @return A copy of this entity with an applied orientation
     */
    public Entity getOrientedEntity(Vec3d anchor, BlockRotation rotation, BlockMirror mirror, Vec3i size, int scale)
    {
        if (anchor == null) anchor = Vec3d.ZERO;
        NbtCompound entityNBT = this.entityData.copy();

        Vec3d oriented = BlockPosMath.getOrientedVec3d(this.position, size, rotation, mirror, scale);
        double x = anchor.x + oriented.x;
        double y = anchor.y + oriented.y;
        double z = anchor.z + oriented.z;

        float yaw = 0;
        if (entityNBT.contains("Rotation", NbtElement.LIST_TYPE))
        {
            NbtList rotationNBT = entityNBT.getList("Rotation", NbtElement.FLOAT_TYPE);
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
     * @param world The ServerWorldAccess to search for the entity in
     */
    public void updateMinecraftEntity(ServerWorldAccess world)
    {
        if (this.minecraftUUID != null)
        {
            net.minecraft.entity.Entity mcEntity = world.toServerWorld().getEntity(this.minecraftUUID);

            try
            {
                if (!this.killed) mcEntity.readNbt(this.entityData);
                else
                {
                    mcEntity.discard();
                    breakMinecraftEntityConnection();
                }
            }
            catch (Exception e)
            {
                breakMinecraftEntityConnection();
                spawnInWorld(world);
            }
        }
        else spawnInWorld(world);
    }

    public void updateBoundingBox()
    {
        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimension());
        net.minecraft.entity.Entity mcEntity = EntityType.getEntityFromNbt(this.entityData.copy(), world).orElse(null);
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
        NbtList posNBT = new NbtList();
        posNBT.add(NbtDouble.of(x));
        posNBT.add(NbtDouble.of(y));
        posNBT.add(NbtDouble.of(z));
        this.entityData.put("Pos", posNBT);
        this.position = new Vec3d(x, y, z);

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
            this.position = new Vec3d(this.position.x + x, this.position.y + y, this.position.z + z);

            NbtList posNBT = new NbtList();
            posNBT.add(NbtDouble.of(this.position.x));
            posNBT.add(NbtDouble.of(this.position.y));
            posNBT.add(NbtDouble.of(this.position.z));
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
        NbtList rotationNBT = new NbtList();
        rotationNBT.add(NbtFloat.of(yaw));
        if (this.entityData.contains("Rotation")) rotationNBT.add(this.entityData.getList("Rotation", NbtElement.FLOAT_TYPE).get(1));
        else rotationNBT.add(NbtFloat.ZERO);
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
        NbtList rotationNBT = new NbtList();
        if (this.entityData.contains("Rotation")) rotationNBT.add(this.entityData.getList("Rotation", NbtElement.FLOAT_TYPE).get(0));
        else rotationNBT.add(NbtFloat.ZERO);
        rotationNBT.add(NbtFloat.of(pitch));
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
            NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.nbtPath().parse(new StringReader(path));
            NbtElement nbt = NbtElementArgumentType.nbtElement().parse(new StringReader(data));
            nbtPath.put(this.entityData, () -> nbt);
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

        NbtCompound newEntityData = data.getMinecraftNBT().copy();
        newEntityData.remove("UUID");
        this.entityData = newEntityData;

        if (this.entityData.contains("Pos"))
        {
            NbtList posNBT = this.entityData.getList("Pos", NbtElement.DOUBLE_TYPE);
            this.position = new Vec3d(posNBT.getDouble(0), posNBT.getDouble(1), posNBT.getDouble(2));
        }
        if (this.entityData.contains("Rotation"))
        {
            NbtList rotationNBT = this.entityData.getList("Rotation", NbtElement.FLOAT_TYPE);
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

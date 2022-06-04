package keystone.api.wrappers.entities;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3d;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.world.EntitiesModule;
import keystone.core.utils.EntityUtils;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ServerWorldAccess;

import java.util.Optional;
import java.util.UUID;

/**
 * A wrapper for a Minecraft entity. Contains information about the entity's NBT data
 */
public class Entity
{
    private net.minecraft.entity.Entity previewEntity;
    private net.minecraft.entity.Entity minecraftEntity;
    private String entityType;

    private UUID minecraftUUID;
    private boolean killed;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    private static EntitiesModule entitiesModule;

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Set the {@link EntitiesModule} used by the Entity wrapper
     * @param entitiesModule The {@link EntitiesModule} the use
     */
    public static void setEntitiesModule(EntitiesModule entitiesModule) { Entity.entitiesModule = entitiesModule; }

    private Entity(net.minecraft.entity.Entity previewEntity, net.minecraft.entity.Entity minecraftEntity, String entityType, UUID minecraftUUID, boolean killed)
    {
        this.previewEntity = previewEntity;
        this.minecraftEntity = minecraftEntity;
        this.entityType = entityType;
        this.minecraftUUID = minecraftUUID;
        this.killed = killed;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for a given entity type ID
     * @param id The entity type ID
     */
    public Entity(String id)
    {
        Optional<EntityType<?>> type = Registry.ENTITY_TYPE.getOrEmpty(new Identifier(id));
        if (type.isEmpty())
        {
            Keystone.tryCancelFilter("Invalid entity type '" + id + "'!");
            return;
        }

        this.previewEntity = entitiesModule.createPreviewEntity(type.get(), new NbtCompound());
        this.minecraftEntity = null;
        this.minecraftUUID = UUID.randomUUID();
        this.entityType = id;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for a Minecraft entity
     * @param minecraftEntity The Minecraft entity
     */
    public Entity(net.minecraft.entity.Entity minecraftEntity)
    {
        this(EntityUtils.getEntityData(minecraftEntity), true);
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create an entity wrapper for an NBT compound
     * @param nbt The Minecraft entity data
     * @param useMinecraftEntity If true, Keystone will wrap an existing entity if one exists
     */
    public Entity(NbtCompound nbt, boolean useMinecraftEntity)
    {
        String id = nbt.getString(net.minecraft.entity.Entity.ID_KEY);
        Optional<EntityType<?>> type = Registry.ENTITY_TYPE.getOrEmpty(new Identifier(id));
        if (type.isEmpty())
        {
            Keystone.tryCancelFilter("Invalid entity type '" + id + "'!");
            return;
        }

        if (useMinecraftEntity)
        {
            this.minecraftEntity = entitiesModule.getMinecraftEntity(nbt);
            if (this.minecraftEntity == null)
            {
                this.previewEntity = entitiesModule.createPreviewEntity(type.get(), new NbtCompound());
                this.minecraftUUID = UUID.randomUUID();
            }
            else
            {
                NbtCompound previewNBT = EntityUtils.getEntityDataNoUuid(this.minecraftEntity);
                this.previewEntity = entitiesModule.createPreviewEntity(type.get(), previewNBT);
                this.minecraftUUID = this.minecraftEntity.getUuid();
            }
        }
        else
        {
            this.previewEntity = entitiesModule.createPreviewEntity(type.get(), nbt);
            this.minecraftEntity = null;
            this.minecraftUUID = UUID.randomUUID();
        }
        this.entityType = id;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The serialized NbtCompound
     */
    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();

        nbt.put("PreviewEntity", EntityUtils.getEntityDataNoUuid(this.previewEntity));
        if (this.minecraftEntity != null) nbt.put("MinecraftEntity", EntityUtils.getEntityData(this.minecraftEntity));

        nbt.putUuid("MinecraftUUID", this.minecraftUUID);
        nbt.putBoolean("Killed", this.killed);

        return nbt;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param nbt The serialized NbtCompound
     * @return The deserialized Entity
     */
    public static Entity deserialize(NbtCompound nbt)
    {
        NbtCompound previewNBT = nbt.getCompound("PreviewEntity");
        NbtCompound minecraftNBT = nbt.contains("MinecraftEntity", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("MinecraftEntity") : null;

        Entity entity;
        if (minecraftNBT != null)
        {
            entity = new Entity(minecraftNBT, true);
            entity.previewEntity.readNbt(previewNBT);
        }
        else entity = new Entity(previewNBT, false);

        entity.minecraftUUID = nbt.getUuid("MinecraftUUID");
        entity.killed = nbt.getBoolean("Killed");

        return entity;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft entity that exists for storing changes made to the {@link Entity} that have not been finalized yet
     */
    public net.minecraft.entity.Entity getPreviewEntity()
    {
        return this.previewEntity;
    }
    public net.minecraft.entity.Entity getMinecraftEntity()
    {
        return this.minecraftEntity;
    }
    
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Clears the wrapper's UUID, breaking its connection with the Minecraft entity
     */
    public void breakMinecraftEntityConnection()
    {
        this.minecraftEntity = null;
        this.minecraftUUID = UUID.randomUUID();
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Spawn this entity into a ServerWorld
     * @param world The ServerWorldAccess to spawn the entity in
     */
    public void spawn(ServerWorldAccess world)
    {
        if (this.minecraftEntity == null)
        {
            this.minecraftEntity = this.previewEntity.getType().create(world.toServerWorld());
            this.minecraftEntity.readNbt(EntityUtils.getEntityDataNoUuid(this.previewEntity));
            this.minecraftEntity.setUuid(this.minecraftUUID);
            world.spawnEntityAndPassengers(this.minecraftEntity);
        }
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
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
        NbtCompound entityNBT = EntityUtils.getEntityDataNoUuid(this.previewEntity);

        Vec3d oriented = BlockPosMath.getOrientedVec3d(this.previewEntity.getPos(), size, rotation, mirror, scale);
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
        if (this.minecraftEntity == null) this.minecraftEntity = world.toServerWorld().getEntity(this.minecraftUUID);
        if (this.minecraftEntity != null)
        {
            if (this.killed)
            {
                this.minecraftEntity.discard();
                breakMinecraftEntityConnection();
            }
            else this.minecraftEntity.readNbt(EntityUtils.getEntityDataNoUuid(this.previewEntity));
        }
        else if (!this.killed) spawn(world);
    }
    //endregion
    //region API

    /**
     * Create an identical copy of this entity, including the UUID and Minecraft connection
     * @return The duplicated entity
     */
    public Entity duplicate()
    {
        return new Entity(this.previewEntity, this.minecraftEntity, this.entityType, this.minecraftUUID, this.killed);
    }
    /**
     * Create an identical copy of this entity, except for the UUID and Minecraft connection
     * @return The cloned entity
     */
    public Entity clone()
    {
        return new Entity(EntityUtils.getEntityDataNoUuid(this.previewEntity), false);
    }
    public String type() { return this.entityType; }
    /**
     * @return The x-coordinate of the entity
     */
    public double x() { return this.previewEntity.getPos().x; }
    /**
     * @return The y-coordinate of the entity
     */
    public double y() { return this.previewEntity.getPos().y; }
    /**
     * @return The z-coordinate of the entity
     */
    public double z() { return this.previewEntity.getPos().z; }
    /**
     * @return The position of the entity
     */
    public Vector3d pos() { return new Vector3d(this.previewEntity.getPos()); }
    /**
     * @return The {@link BlockPos} of the entity
     */
    public BlockPos blockPos() { return new BlockPos(this.previewEntity.getBlockPos()); }
    /**
     * @return This Entity's {@link BoundingBox}
     */
    public BoundingBox boundingBox() { return new BoundingBox(this.previewEntity.getBoundingBox()); }
    /**
     * @return The yaw angle of the entity, in degrees
     */
    public float yaw() { return this.previewEntity.getYaw(); }
    /**
     * @return The pitch angle of the entity, in degrees
     */
    public float pitch() { return this.previewEntity.getPitch(); }
    /**
     * @return An {@link NBTCompound} representing this entity's data. Note that modifying
     * this NBT Compound will not modify the entity unless you call {@link Entity#data(NBTCompound)}
     * once finished
     */
    public NBTCompound data()
    {
        return new NBTCompound(EntityUtils.getEntityDataNoUuid(this.previewEntity));
    }
    /**
     * @return The UUID of this entity in Minecraft. This is not the same as {@link Entity#keystoneUUID()},
     * and might not correspond to an entity that is actually in the Minecraft world. If the entity is in
     * the world, this will always be it's UUID. If the entity is not in the Minecraft world, this will be
     * the UUID of the entity when it is spawned by calling {@link Entity#spawn(ServerWorldAccess)}
     */
    public UUID minecraftUUID() { return this.minecraftUUID; }
    /**
     * @return The UUID of this entity in Keystone. This is not the same as {@link Entity#minecraftUUID()}
     */
    public UUID keystoneUUID() { return this.previewEntity.getUuid(); }
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
        this.previewEntity.setPos(x, y, z);
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
        this.previewEntity.setPos(x() + x, y() + y, z() + z);
        return this;
    }
    /**
     * Set the entity's yaw
     * @param yaw The yaw
     * @return The modified entity instance
     */
    public Entity yaw(float yaw)
    {
        this.previewEntity.setYaw(yaw);
        return this;
    }
    /**
     * Set the entity's pitch
     * @param pitch The pitch
     * @return The modified entity instance
     */
    public Entity pitch(float pitch)
    {
        this.previewEntity.setPitch(pitch);
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
        if (path.equals(net.minecraft.entity.Entity.ID_KEY))
        {
            Keystone.tryCancelFilter("Modifying an entity's type ID is not allowed!");
            return this;
        }

        try
        {
            NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.nbtPath().parse(new StringReader(path));
            NbtElement nbt = NbtElementArgumentType.nbtElement().parse(new StringReader(data));

            NbtCompound previewNBT = EntityUtils.getEntityData(this.previewEntity);
            nbtPath.put(previewNBT, () -> nbt);
            this.previewEntity.readNbt(previewNBT);
            return this;
        }
        catch (CommandSyntaxException e)
        {
            Keystone.tryCancelFilter(e.getLocalizedMessage());
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
        if (data.getString(net.minecraft.entity.Entity.ID_KEY) != this.entityType)
        {
            Keystone.tryCancelFilter("Modifying an entity's type ID is not allowed!");
            return this;
        }

        NbtCompound newEntityData = data.getMinecraftNBT().copy();
        newEntityData.remove(net.minecraft.entity.Entity.UUID_KEY);
        this.previewEntity.readNbt(newEntityData);
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
        StringBuilder stringBuilder = new StringBuilder(this.entityType);
        stringBuilder.append(EntityUtils.getEntityDataNoUuid(this.previewEntity));
        stringBuilder.append("<");
        stringBuilder.append(this.previewEntity.getUuid().toString());
        stringBuilder.append(" | ");
        if (this.minecraftEntity != null) stringBuilder.append(this.minecraftEntity.getUuid().toString());
        else stringBuilder.append("NULL");
        stringBuilder.append(">");
        return stringBuilder.toString();
    }
    //endregion
}

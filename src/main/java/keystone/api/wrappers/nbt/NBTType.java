package keystone.api.wrappers.nbt;

import net.minecraft.nbt.NbtElement;

public enum NBTType
{
    END(NbtElement.END_TYPE),
    BYTE(NbtElement.BYTE_TYPE),
    SHORT(NbtElement.SHORT_TYPE),
    INT(NbtElement.INT_TYPE),
    LONG(NbtElement.LONG_TYPE),
    FLOAT(NbtElement.FLOAT_TYPE),
    DOUBLE(NbtElement.DOUBLE_TYPE),
    BYTE_ARRAY(NbtElement.BYTE_ARRAY_TYPE),
    STRING(NbtElement.STRING_TYPE),
    LIST(NbtElement.LIST_TYPE),
    COMPOUND(NbtElement.COMPOUND_TYPE),
    INT_ARRAY(NbtElement.INT_ARRAY_TYPE),
    LONG_ARRAY(NbtElement.LONG_ARRAY_TYPE),
    ANY_NUMBER(NbtElement.NUMBER_TYPE);

    public final int minecraftID;

    NBTType(int minecraftID) { this.minecraftID = minecraftID; }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param minecraftID The Minecraft ID, found in Constants.NBT
     * @return The NBTType representing the Minecraft ID
     */
    public static NBTType fromMinecraftID(int minecraftID)
    {
        switch (minecraftID)
        {
            case NbtElement.BYTE_TYPE: return BYTE;
            case NbtElement.SHORT_TYPE: return SHORT;
            case NbtElement.INT_TYPE: return INT;
            case NbtElement.LONG_TYPE: return LONG;
            case NbtElement.FLOAT_TYPE: return FLOAT;
            case NbtElement.DOUBLE_TYPE: return DOUBLE;
            case NbtElement.BYTE_ARRAY_TYPE: return BYTE_ARRAY;
            case NbtElement.STRING_TYPE: return STRING;
            case NbtElement.LIST_TYPE: return LIST;
            case NbtElement.COMPOUND_TYPE: return COMPOUND;
            case NbtElement.INT_ARRAY_TYPE: return INT_ARRAY;
            case NbtElement.LONG_ARRAY_TYPE: return LONG_ARRAY;
            case NbtElement.NUMBER_TYPE: return ANY_NUMBER;
            default: return END;
        }
    }
}

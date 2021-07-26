package keystone.api.wrappers.nbt;

import net.minecraftforge.common.util.Constants;

public enum NBTType
{
    TAG_END(Constants.NBT.TAG_END),
    TAG_BYTE(Constants.NBT.TAG_BYTE),
    TAG_SHORT(Constants.NBT.TAG_SHORT),
    TAG_INT(Constants.NBT.TAG_INT),
    TAG_LONG(Constants.NBT.TAG_LONG),
    TAG_FLOAT(Constants.NBT.TAG_FLOAT),
    TAG_DOUBLE(Constants.NBT.TAG_DOUBLE),
    TAG_BYTE_ARRAY(Constants.NBT.TAG_BYTE_ARRAY),
    TAG_STRING(Constants.NBT.TAG_STRING),
    TAG_LIST(Constants.NBT.TAG_LIST),
    TAG_COMPOUND(Constants.NBT.TAG_COMPOUND),
    TAG_INT_ARRAY(Constants.NBT.TAG_INT_ARRAY),
    TAG_LONG_ARRAY(Constants.NBT.TAG_LONG_ARRAY),
    TAG_ANY_NUMERIC(Constants.NBT.TAG_ANY_NUMERIC);

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
            case Constants.NBT.TAG_BYTE: return TAG_BYTE;
            case Constants.NBT.TAG_SHORT: return TAG_SHORT;
            case Constants.NBT.TAG_INT: return TAG_INT;
            case Constants.NBT.TAG_LONG: return TAG_LONG;
            case Constants.NBT.TAG_FLOAT: return TAG_FLOAT;
            case Constants.NBT.TAG_DOUBLE: return TAG_DOUBLE;
            case Constants.NBT.TAG_BYTE_ARRAY: return TAG_BYTE_ARRAY;
            case Constants.NBT.TAG_STRING: return TAG_STRING;
            case Constants.NBT.TAG_LIST: return TAG_LIST;
            case Constants.NBT.TAG_COMPOUND: return TAG_COMPOUND;
            case Constants.NBT.TAG_INT_ARRAY: return TAG_INT_ARRAY;
            case Constants.NBT.TAG_LONG_ARRAY: return TAG_LONG_ARRAY;
            case Constants.NBT.TAG_ANY_NUMERIC: return TAG_ANY_NUMERIC;
            default: return TAG_END;
        }
    }
}

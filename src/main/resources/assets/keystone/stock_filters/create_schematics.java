import keystone.api.KeystoneDirectories;
import keystone.api.WorldRegion;
import keystone.api.enums.RetrievalMode;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Direction;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CreateSchematics extends KeystoneFilter
{
    private static BlockType markerBlockType = blockType("minecraft:structure_block[mode=save]");

    @Variable BlockPalette replaceMarkerWith = palette("minecraft:air");
    @Variable BlockMask borderMask = blacklist("minecraft:air");
    @Variable BlockType structureVoid = blockType("minecraft:structure_void");
    @Variable String fileExtension = "kschem";

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        Block block = region.getBlock(x, y, z);
        if (block.blockType().equals(markerBlockType))
        {
            // Read name from structure block
            String identifier = block.tileEntity().getString("name").trim();
            if (identifier == null || identifier == "") return;

            // Create file path from structure name
            String[] pathTokens = identifier.split(":", 2);
            Path path = Paths.get(KeystoneDirectories.getSchematicsDirectory().toPath().resolve(pathTokens[0]).toString(), pathTokens[1].split("/"));

            // Save schematic
            region.setBlock(x, y, z, replaceMarkerWith.randomBlock());
            BoundingBox schematicBounds = getSchematicBounds(x, y, z, region);
            KeystoneSchematic schematic = schematic(schematicBounds, region.getWorldModifiers(), RetrievalMode.CURRENT, structureVoid);
            SchematicLoader.saveSchematic(schematic, path.toString() + "." + fileExtension);
            region.setBlock(x, y, z, block);
        }
    }

    private BoundingBox getSchematicBounds(int x, int y, int z, WorldRegion region)
    {
        int[] min = new int[] { x, y, z };
        int[] max = new int[] { x, y, z };
        int iterations = 100000000;

        Deque frontier = new ArrayDeque();
        Set visited = new HashSet();

        frontier.push(new BlockPos(x, y, z));
        while (!frontier.isEmpty() && iterations > 0)
        {
            iterations--;

            BlockPos pos = (BlockPos)frontier.remove();
            visited.add(pos);

            if (region.bounds.contains(pos.x, pos.y, pos.z) && borderMask.valid(region.getBlockType(pos.x, pos.y, pos.z)))
            {
                min[0] = Math.min(min[0], pos.x);
                min[1] = Math.min(min[1], pos.y);
                min[2] = Math.min(min[2], pos.z);

                max[0] = Math.max(max[0], pos.x);
                max[1] = Math.max(max[1], pos.y);
                max[2] = Math.max(max[2], pos.z);

                for (Direction direction : Direction.values())
                {
                    BlockPos neighbor = new BlockPos(pos.x + direction.getVector().x, pos.y + direction.getVector().y, pos.z + direction.getVector().z);
                    if (!visited.contains(neighbor) && !frontier.contains(neighbor)) frontier.push(neighbor);
                }
            }
        }

        return new BoundingBox(min[0], min[1], min[2], max[0], max[1], max[2]);
    }
}

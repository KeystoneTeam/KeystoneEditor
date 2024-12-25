package keystone.core.modules.world.change_queue;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class DirtyChunkList
{
    private final List<Chunk> chunks;
    private final List<ChunkSection> chunkSections;
    private final List<ChunkSectionPos> sectionPositions;
    
    public DirtyChunkList()
    {
        this.chunks = new ArrayList<>();
        this.chunkSections = new ArrayList<>();
        this.sectionPositions = new ArrayList<>();
    }
    
    public List<Chunk> getChunks() { return chunks; }
    public List<ChunkSection> getChunkSections() { return chunkSections; }
    public List<ChunkSectionPos> getSectionPositions() { return sectionPositions; }
    
    public void dirtyChunkSection(WorldChunk chunk, ChunkSection section, int sectionY)
    {
        if (!chunks.contains(chunk)) chunks.add(chunk);
        if (!chunkSections.contains(section)) chunkSections.add(section);
        ChunkSectionPos sectionPos = ChunkSectionPos.from(chunk.getPos(), sectionY);
        if (!sectionPositions.contains(sectionPos)) sectionPositions.add(sectionPos);
    }
    
    public void forEachSection(BiConsumer<ChunkSection, ChunkSectionPos> consumer)
    {
        for (int i = 0; i < chunkSections.size(); i++) consumer.accept(chunkSections.get(i), sectionPositions.get(i));
    }
}

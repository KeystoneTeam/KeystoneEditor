package keystone.core.renderer.blocks.buffer;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class SuperByteBuffer extends TemplateBuffer
{

    public interface IVertexLighter
    {
        public int getPackedLight(float x, float y, float z);
    }

    // Vertex Position
    private MatrixStack transforms;

    // Vertex Texture Coords
    private SpriteShiftFunc spriteShiftFunc;

    // Vertex Lighting
    private boolean shouldLight;
    private int packedLightCoords;
    private int otherBlockLight;
    private Matrix4f lightTransform;

    // Vertex Coloring
    private boolean shouldColor;
    private int r, g, b, a;

    // TEMPORARY
    private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();

    public SuperByteBuffer(BufferBuilder buf)
    {
        super(buf);
        transforms = new MatrixStack();
    }

    private static final Long2DoubleMap skyLightCache = new Long2DoubleOpenHashMap();
    private static final Long2DoubleMap blockLightCache = new Long2DoubleOpenHashMap();
    Vector4f pos = new Vector4f();
    Vec3f normal = new Vec3f();
    Vector4f lightPos = new Vector4f();

    private float diffuseLight(float x, float y, float z)
    {
        return Math.min(x * x * 0.6f + y * y * ((3f + y) / 4f) + z * z * 0.8f, 1f);
    }

    public void renderInto(MatrixStack input, VertexConsumer builder)
    {
        ByteBuffer buffer = template;
        if (((Buffer) buffer).limit() == 0)
            return;
        ((Buffer) buffer).rewind();

        Matrix3f normalMat = transforms.peek()
                .getNormalMatrix()
                .copy();
        // normalMat.multiply(transforms.peek().getNormal());

        Matrix4f modelMat = input.peek()
                .getPositionMatrix()
                .copy();

        Matrix4f localTransforms = transforms.peek()
                .getPositionMatrix();
        modelMat.multiply(localTransforms);

        if (shouldLight && lightTransform != null)
        {
            skyLightCache.clear();
            blockLightCache.clear();
        }

        float f = .5f;
        int vertexCount = vertexCount(buffer);
        for (int i = 0; i < vertexCount; i++)
        {
            float x = getX(buffer, i);
            float y = getY(buffer, i);
            float z = getZ(buffer, i);
            byte r = getR(buffer, i);
            byte g = getG(buffer, i);
            byte b = getB(buffer, i);
            byte a = getA(buffer, i);
            float normalX = getNX(buffer, i) / 127f;
            float normalY = getNY(buffer, i) / 127f;
            float normalZ = getNZ(buffer, i) / 127f;

            float staticDiffuse = diffuseLight(normalX, normalY, normalZ);
            normal.set(normalX, normalY, normalZ);
            normal.transform(normalMat);
            float nx = normal.getX();
            float ny = normal.getY();
            float nz = normal.getZ();
            float instanceDiffuse = diffuseLight(nx, ny, nz);

            pos.set(x, y, z, 1F);
            pos.transform(modelMat);
            builder.vertex(pos.getX(), pos.getY(), pos.getZ());

            // builder.color((byte) Math.max(0, nx * 255), (byte) Math.max(0, ny * 255), (byte) Math.max(0, nz * 255), a);
            if (shouldColor)
            {
                // float lum = (r < 0 ? 255 + r : r) / 256f;
                int colorR = Math.min(255, (int) (((float) this.r) * instanceDiffuse));
                int colorG = Math.min(255, (int) (((float) this.g) * instanceDiffuse));
                int colorB = Math.min(255, (int) (((float) this.b) * instanceDiffuse));
                builder.color(colorR, colorG, colorB, this.a);
            } else
            {
                float diffuseMult = instanceDiffuse / staticDiffuse;
                int colorR = Math.min(255, (int) (((float) Byte.toUnsignedInt(r)) * diffuseMult));
                int colorG = Math.min(255, (int) (((float) Byte.toUnsignedInt(g)) * diffuseMult));
                int colorB = Math.min(255, (int) (((float) Byte.toUnsignedInt(b)) * diffuseMult));
                builder.color(colorR, colorG, colorB, a);
            }

            float u = getU(buffer, i);
            float v = getV(buffer, i);

            if (spriteShiftFunc != null)
            {
                spriteShiftFunc.shift(builder, u, v);
            } else
                builder.texture(u, v);

            if (shouldLight)
            {
                int light = packedLightCoords;
                if (lightTransform != null)
                {
                    lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
                    lightPos.transform(localTransforms);
                    lightPos.transform(lightTransform);

                    light = getLight(MinecraftClient.getInstance().world, lightPos);
                    if (otherBlockLight >= 0)
                    {
                        light = getMaxBlockLight(light, otherBlockLight);
                    }
                }
                builder.light(light);
            } else
                builder.light(getLight(buffer, i));

            builder.normal(nx, ny, nz).next();
        }

        transforms = new MatrixStack();

        spriteShiftFunc = null;
        shouldColor = false;
        shouldLight = false;
        otherBlockLight = -1;
    }

    public SuperByteBuffer translate(Vec3d vec)
    {
        return translate(vec.x, vec.y, vec.z);
    }

    public SuperByteBuffer translate(double x, double y, double z)
    {
        return translate((float) x, (float) y, (float) z);
    }

    public SuperByteBuffer translate(float x, float y, float z)
    {
        transforms.translate(x, y, z);
        return this;
    }

    public SuperByteBuffer transform(MatrixStack stack)
    {
        transforms.peek()
                .getPositionMatrix()
                .multiply(stack.peek()
                        .getPositionMatrix());
        transforms.peek()
                .getNormalMatrix()
                .multiply(stack.peek()
                        .getNormalMatrix());
        return this;
    }

    public SuperByteBuffer rotate(Direction axis, float radians)
    {
        if (radians == 0) return this;
        transforms.multiply(axis.getUnitVector().getRadialQuaternion(radians));
        return this;
    }

    public SuperByteBuffer rotate(Quaternion q)
    {
        transforms.multiply(q);
        return this;
    }

    public SuperByteBuffer rotateCentered(Direction axis, float radians)
    {
        return translate(.5f, .5f, .5f).rotate(axis, radians)
                .translate(-.5f, -.5f, -.5f);
    }

    public SuperByteBuffer rotateCentered(Quaternion q)
    {
        return translate(.5f, .5f, .5f).rotate(q)
                .translate(-.5f, -.5f, -.5f);
    }

    public SuperByteBuffer light(int packedLightCoords)
    {
        shouldLight = true;
        lightTransform = null;
        this.packedLightCoords = packedLightCoords;
        return this;
    }

    public SuperByteBuffer light(Matrix4f lightTransform)
    {
        shouldLight = true;
        this.lightTransform = lightTransform;
        return this;
    }

    public SuperByteBuffer light(Matrix4f lightTransform, int otherBlockLight)
    {
        shouldLight = true;
        this.lightTransform = lightTransform;
        this.otherBlockLight = otherBlockLight;
        return this;
    }

    public SuperByteBuffer color(int color)
    {
        shouldColor = true;
        r = ((color >> 16) & 0xFF);
        g = ((color >> 8) & 0xFF);
        b = (color & 0xFF);
        a = 255;
        return this;
    }

    //private static int getLight(World world, Vector4f lightPos)
    //{
    //    BlockPos.Mutable pos = new BlockPos.Mutable();
    //    double sky = 0, block = 0;
    //    pos.set(lightPos.getX() + 0, lightPos.getY() + 0, lightPos.getZ() + 0);
    //    sky += skyLightCache.computeIfAbsent(pos.asLong(), $ -> world.getLightLevel(LightType.SKY, pos));
    //    block += blockLightCache.computeIfAbsent(pos.asLong(), $ -> world.getLightLevel(LightType.BLOCK, pos));
    //    return ((int) sky) << 20 | ((int) block) << 4;
    //}

    public boolean isEmpty()
    {
        return ((Buffer) template).limit() == 0;
    }

    @FunctionalInterface
    public interface SpriteShiftFunc
    {
        void shift(VertexConsumer builder, float u, float v);
    }

    private static int getMaxBlockLight(int packedLight, int otherBlockLight)
    {
        int unpackedLight = LightmapTextureManager.getBlockLightCoordinates(packedLight);
        if (unpackedLight < otherBlockLight) packedLight = (packedLight & 0xFFFF0000) | (otherBlockLight << 4);
        return unpackedLight;
    }

    public static int transformColor(byte component, float scale) {
        return MathHelper.clamp((int) (Byte.toUnsignedInt(component) * scale), 0, 255);
    }

    public static int transformColor(int component, float scale) {
        return MathHelper.clamp((int) (component * scale), 0, 255);
    }

    public static int maxLight(int packedLight1, int packedLight2) {
        int blockLight1 = LightmapTextureManager.getBlockLightCoordinates(packedLight1);
        int skyLight1 = LightmapTextureManager.getSkyLightCoordinates(packedLight1);
        int blockLight2 = LightmapTextureManager.getBlockLightCoordinates(packedLight2);
        int skyLight2 = LightmapTextureManager.getSkyLightCoordinates(packedLight2);
        return LightmapTextureManager.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
    }

    private static int getLight(World world, Vector4f lightPos) {
        BlockPos pos = new BlockPos(lightPos.getX(), lightPos.getY(), lightPos.getZ());
        return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> WorldRenderer.getLightmapCoordinates(world, pos));
    }
}
package keystone.core.renderer.blocks.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;

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

    public SuperByteBuffer(BufferBuilder buf)
    {
        super(buf);
        transforms = new MatrixStack();
    }

    public static float getUnInterpolatedU(TextureAtlasSprite sprite, float u)
    {
        float f = sprite.getU1() - sprite.getU0();
        return (u - sprite.getU0()) / f * 16.0F;
    }

    public static float getUnInterpolatedV(TextureAtlasSprite sprite, float v)
    {
        float f = sprite.getV1() - sprite.getV0();
        return (v - sprite.getV0()) / f * 16.0F;
    }

    private static final Long2DoubleMap skyLightCache = new Long2DoubleOpenHashMap();
    private static final Long2DoubleMap blockLightCache = new Long2DoubleOpenHashMap();
    Vector4f pos = new Vector4f();
    Vector3f normal = new Vector3f();
    Vector4f lightPos = new Vector4f();

    public void renderInto(MatrixStack input, IVertexBuilder builder)
    {
        ByteBuffer buffer = template;
        if (((Buffer) buffer).limit() == 0)
            return;
        ((Buffer) buffer).rewind();

        Matrix3f normalMat = transforms.last()
                .normal()
                .copy();
        // normalMat.multiply(transforms.peek().getNormal());

        Matrix4f modelMat = input.last()
                .pose()
                .copy();

        Matrix4f localTransforms = transforms.last()
                .pose();
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

            float staticDiffuse = LightUtil.diffuseLight(normalX, normalY, normalZ);
            normal.set(normalX, normalY, normalZ);
            normal.transform(normalMat);
            float nx = normal.x();
            float ny = normal.y();
            float nz = normal.z();
            float instanceDiffuse = LightUtil.diffuseLight(nx, ny, nz);

            pos.set(x, y, z, 1F);
            pos.transform(modelMat);
            builder.vertex(pos.x(), pos.y(), pos.z());

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
                builder.uv(u, v);

            if (shouldLight)
            {
                int light = packedLightCoords;
                if (lightTransform != null)
                {
                    lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
                    lightPos.transform(localTransforms);
                    lightPos.transform(lightTransform);

                    light = getLight(Minecraft.getInstance().level, lightPos);
                    if (otherBlockLight >= 0)
                    {
                        light = getMaxBlockLight(light, otherBlockLight);
                    }
                }
                builder.uv2(light);
            } else
                builder.uv2(getLight(buffer, i));

            builder.normal(nx, ny, nz)
                    .endVertex();
        }

        transforms = new MatrixStack();

        spriteShiftFunc = null;
        shouldColor = false;
        shouldLight = false;
        otherBlockLight = -1;
    }

    public SuperByteBuffer translate(Vector3d vec)
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
        transforms.last()
                .pose()
                .multiply(stack.last()
                        .pose());
        transforms.last()
                .normal()
                .mul(stack.last()
                        .normal());
        return this;
    }

    public SuperByteBuffer rotate(Direction axis, float radians)
    {
        if (radians == 0)
            return this;
        transforms.mulPose(axis.step()
                .rotation(radians));
        return this;
    }

    public SuperByteBuffer rotate(Quaternion q)
    {
        transforms.mulPose(q);
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

    private static int getLight(World world, Vector4f lightPos)
    {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        double sky = 0, block = 0;
        pos.set(lightPos.x() + 0, lightPos.y() + 0, lightPos.z() + 0);
        sky += skyLightCache.computeIfAbsent(pos.asLong(), $ -> world.getBrightness(LightType.SKY, pos));
        block += blockLightCache.computeIfAbsent(pos.asLong(), $ -> world.getBrightness(LightType.BLOCK, pos));
        return ((int) sky) << 20 | ((int) block) << 4;
    }

    public boolean isEmpty()
    {
        return ((Buffer) template).limit() == 0;
    }

    @FunctionalInterface
    public interface SpriteShiftFunc
    {
        void shift(IVertexBuilder builder, float u, float v);
    }

    private static int getMaxBlockLight(int packedLight, int otherBlockLight)
    {
        int unpackedLight = LightTexture.block(packedLight);
        if (unpackedLight < otherBlockLight) packedLight = (packedLight & 0xFFFF0000) | (otherBlockLight << 4);
        return unpackedLight;
    }
}
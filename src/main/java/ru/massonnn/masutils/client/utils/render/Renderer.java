package ru.massonnn.masutils.client.utils.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.ShapeIndexBuffer;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.BuiltBuffer.DrawParameters;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.BufferAllocator;
import ru.massonnn.masutils.Masutils;

public class Renderer {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final List<RenderPipeline> EXCLUDED_FROM_BATCHING = new ArrayList<>();
    private static final BufferAllocator GENERAL_ALLOCATOR = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);
    private static final float DEFAULT_LINE_WIDTH = 0f;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector4f COLOR_MODULATOR_TRANSLUCENT = new Vector4f(1f, 1f, 1f, 0.5f);
    private static final Int2ObjectMap<BufferAllocator> ALLOCATORS = new Int2ObjectArrayMap<>(5);
    private static final Int2ObjectMap<BatchedDraw> BATCHED_DRAWS = new Int2ObjectArrayMap<>(5);
    private static final Map<VertexFormat, MappableRingBuffer> VERTEX_BUFFERS = new Object2ObjectOpenHashMap<>();
    private static final List<PreparedDraw> PREPARED_DRAWS = new ArrayList<>();
    private static final List<Draw> DRAWS = new ArrayList<>();
    private static BatchedDraw lastUnbatchedDraw = null;

    public static BufferBuilder getBuffer(RenderPipeline pipeline) {
        return getBuffer(pipeline, DEFAULT_LINE_WIDTH);
    }

    public static BufferBuilder getBuffer(RenderPipeline pipeline, float lineWidth) {
        return getBuffer(pipeline, TextureSetup.empty(), lineWidth, false);
    }

    public static BufferBuilder getBuffer(RenderPipeline pipeline, TextureSetup textureSetup) {
        return getBuffer(pipeline, Objects.requireNonNull(textureSetup, "textureSetup must not be null"), false);
    }

    public static BufferBuilder getBuffer(RenderPipeline pipeline, TextureSetup textureSetup, boolean translucent) {
        return getBuffer(pipeline, Objects.requireNonNull(textureSetup, "textureSetup must not be null"), DEFAULT_LINE_WIDTH, translucent);
    }

    private static BufferBuilder getBuffer(RenderPipeline pipeline, TextureSetup textureSetup, float lineWidth, boolean translucent) {
        if (!EXCLUDED_FROM_BATCHING.contains(pipeline)) {
            return setupBatched(pipeline, textureSetup, lineWidth, translucent);
        } else {
            return setupUnbatched(pipeline, textureSetup, lineWidth, translucent);
        }
    }

    private static BufferBuilder setupBatched(RenderPipeline pipeline, TextureSetup textureSetup, float lineWidth, boolean translucent) {
        int hash = hash(pipeline, textureSetup, lineWidth, translucent);
        BatchedDraw draw = BATCHED_DRAWS.get(hash);

        if (draw == null) {
            BufferAllocator allocator = ALLOCATORS.computeIfAbsent(hash, _hash -> new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE));
            BufferBuilder bufferBuilder = new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
            BATCHED_DRAWS.put(hash, new BatchedDraw(bufferBuilder, pipeline, textureSetup, lineWidth, translucent));

            return bufferBuilder;
        } else {
            return draw.bufferBuilder();
        }
    }

    private static BufferBuilder setupUnbatched(RenderPipeline pipeline, TextureSetup textureSetup, float lineWidth, boolean translucent) {
        if (lastUnbatchedDraw != null) {
            prepareBatchedDraw(lastUnbatchedDraw);
        }

        BufferBuilder bufferBuilder = new BufferBuilder(GENERAL_ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        lastUnbatchedDraw = new BatchedDraw(bufferBuilder, pipeline, textureSetup, lineWidth, translucent);

        return bufferBuilder;
    }

    private static int hash(RenderPipeline pipeline, TextureSetup textureSetup, float lineWidth, boolean translucent) {
        int hash = 1;
        hash = 31 * hash + pipeline.hashCode();
        hash = 31 * hash + textureSetup.hashCode();
        hash = 31 * hash + Float.hashCode(lineWidth);
        hash = 31 * hash + Boolean.hashCode(translucent);

        return hash;
    }

    protected static void excludePipelineFromBatching(RenderPipeline pipeline) {
        EXCLUDED_FROM_BATCHING.add(pipeline);
    }

    private static void endBatches() {
        for (Int2ObjectMap.Entry<BatchedDraw> entry : Int2ObjectMaps.fastIterable(BATCHED_DRAWS)) {
            prepareBatchedDraw(entry.getValue());
        }

        if (lastUnbatchedDraw != null) {
            prepareBatchedDraw(lastUnbatchedDraw);
            lastUnbatchedDraw = null;
        }
    }

    private static void prepareBatchedDraw(BatchedDraw draw) {
        PREPARED_DRAWS.add(new PreparedDraw(draw.bufferBuilder().end(), draw.pipeline(), draw.textureSetup(), draw.lineWidth(), draw.translucent()));
    }

    protected static void executeDraws() {
        endBatches();

        setupDraws();

        for (Draw draw : DRAWS) {
            draw(draw);
        }

        for (MappableRingBuffer buffer : VERTEX_BUFFERS.values()) {
            buffer.rotate();
        }

        BATCHED_DRAWS.clear();
        PREPARED_DRAWS.clear();
        DRAWS.clear();
    }

    private static void setupDraws() {
        setupVertexBuffers();
        Object2IntMap<VertexFormat> vertexBufferPositions = new Object2IntOpenHashMap<>();

        for (PreparedDraw prepared : PREPARED_DRAWS) {
            BuiltBuffer builtBuffer = prepared.builtBuffer();
            DrawParameters drawParameters = builtBuffer.getDrawParameters();
            VertexFormat format = drawParameters.format();

            MappableRingBuffer vertices = VERTEX_BUFFERS.get(format);
            ByteBuffer vertexData = builtBuffer.getBuffer();
            int vertexBufferPosition = vertexBufferPositions.getInt(format);
            int remainingVertexBytes = vertexData.remaining();

            copyDataInto(vertices, vertexData, vertexBufferPosition, remainingVertexBytes);
            vertexBufferPositions.put(format, vertexBufferPosition + remainingVertexBytes);

            DRAWS.add(new Draw(
                    builtBuffer,
                    vertices.getBlocking(),
                    vertexBufferPosition / format.getVertexSize(),
                    drawParameters.indexCount(),
                    prepared.pipeline(),
                    prepared.textureSetup(),
                    prepared.lineWidth(),
                    prepared.translucent()
            ));
        }
    }

    private static void copyDataInto(MappableRingBuffer target, ByteBuffer source, int position, int remainingBytes) {
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(target.getBlocking().slice(position, remainingBytes), false, true)) {
            MemoryUtil.memCopy(source, mappedView.data());
        }
    }

    private static void setupVertexBuffers() {
        Object2IntMap<VertexFormat> vertexBufferSizes = collectVertexBufferSizes();

        for (Object2IntMap.Entry<VertexFormat> entry : Object2IntMaps.fastIterable(vertexBufferSizes)) {
            VertexFormat format = entry.getKey();
            int vertexBufferSize = entry.getIntValue();
            MappableRingBuffer vertexBuffer = VERTEX_BUFFERS.get(format);

            VERTEX_BUFFERS.put(format, initOrResizeBuffer(vertexBuffer, "Skyblocker vertex buffer for: " + format, vertexBufferSize, GpuBuffer.USAGE_VERTEX));
        }
    }

    private static MappableRingBuffer initOrResizeBuffer(MappableRingBuffer buffer, String name, int neededSize, int usageType) {
        if (buffer == null || buffer.size() < neededSize) {
            if (buffer != null) {
                buffer.close();
            }

            return new MappableRingBuffer(() -> name, GpuBuffer.USAGE_MAP_WRITE | usageType, neededSize);
        }

        return buffer;
    }

    private static Object2IntMap<VertexFormat> collectVertexBufferSizes() {
        Object2IntMap<VertexFormat> vertexSizes = new Object2IntOpenHashMap<>();

        for (PreparedDraw prepared : PREPARED_DRAWS) {
            DrawParameters drawParameters = prepared.builtBuffer().getDrawParameters();
            VertexFormat format = drawParameters.format();

            vertexSizes.put(format, vertexSizes.getOrDefault(format, 0) + drawParameters.vertexCount() * format.getVertexSize());
        }

        return vertexSizes;
    }

    private static void draw(Draw draw) {
        GpuBuffer indices;
        IndexType indexType;

        if (draw.pipeline().getVertexFormatMode() == DrawMode.QUADS) {
            draw.builtBuffer().sortQuads(GENERAL_ALLOCATOR, RenderSystem.getProjectionType().getVertexSorter());
            indices = draw.pipeline().getVertexFormat().uploadImmediateIndexBuffer(draw.builtBuffer().getSortedBuffer());
            indexType = draw.builtBuffer().getDrawParameters().indexType();
        } else {
            ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(draw.pipeline().getVertexFormatMode());
            indices = shapeIndexBuffer.getIndexBuffer(draw.indexCount());
            indexType = shapeIndexBuffer.getIndexType();
        }

        draw(draw, indices, indexType);
    }

    private static void draw(Draw draw, GpuBuffer indices, IndexType indexType) {

        GpuBufferSlice dynamicTransforms = setupDynamicTransforms(draw.lineWidth, draw.translucent);

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "masutils world rendering",
                        getMainColorTexture(),
                        OptionalInt.empty(),
                        null,
                        OptionalDouble.empty()
                )) {

            renderPass.setPipeline(draw.pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            if (draw.textureSetup.texure0() != null) {
                renderPass.bindSampler("Sampler0", draw.textureSetup.texure0());
            }
            if (draw.textureSetup.texure2() != null) {
                renderPass.bindSampler("Sampler2", draw.textureSetup.texure2());
            }

            renderPass.setVertexBuffer(0, draw.vertices);
            renderPass.setIndexBuffer(indices, indexType);

            renderPass.drawIndexed(draw.baseVertex, 0, draw.indexCount, 1);
        }

        BuiltBuffer built = draw.builtBuffer;
        draw.builtBuffer().close();
    }

    private static GpuBufferSlice setupDynamicTransforms(float lineWidth, boolean translucent) {
        return RenderSystem.getDynamicUniforms()
                .write(RenderSystem.getModelViewMatrix(), translucent ? COLOR_MODULATOR_TRANSLUCENT : COLOR_MODULATOR, new Vector3f(), RenderSystem.getTextureMatrix(), lineWidth);
    }

    private static GpuTextureView getMainColorTexture() {
        return CLIENT.getFramebuffer().getColorAttachmentView();
    }

    private static GpuTextureView getMainDepthTexture() {
        return CLIENT.getFramebuffer().getDepthAttachmentView();
    }

    private static void applyViewOffsetZLayering() {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        RenderSystem.getProjectionType().apply(modelViewStack, 1f);
    }

    private static void unapplyViewOffsetZLayering() {
        RenderSystem.getModelViewStack().popMatrix();
    }

    public static void close() {
        GENERAL_ALLOCATOR.close();

        for (BufferAllocator allocator : ALLOCATORS.values()) {
            allocator.close();
        }

        for (MappableRingBuffer vertexBuffer : VERTEX_BUFFERS.values()) {
            vertexBuffer.close();
        }
    }

    private record Draw(BuiltBuffer builtBuffer, GpuBuffer vertices, int baseVertex, int indexCount,
                        RenderPipeline pipeline, TextureSetup textureSetup, float lineWidth, boolean translucent) {
    }

    private record PreparedDraw(BuiltBuffer builtBuffer, RenderPipeline pipeline, TextureSetup textureSetup,
                                float lineWidth, boolean translucent) {
    }

    private record BatchedDraw(BufferBuilder bufferBuilder, RenderPipeline pipeline, TextureSetup textureSetup,
                               float lineWidth, boolean translucent) {
    }
}
package ru.massonnn.masutils.client.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.utils.render.FrustumUtils;
import ru.massonnn.masutils.client.utils.render.MasutilsRenderLayers;
import ru.massonnn.masutils.client.utils.render.OcclusionCulling;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;

public class RenderHelper {
    private static final Identifier TRANSLUCENT_DRAW = Identifier.of(Masutils.NAMESPACE, "translucent_draw");
    private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final BufferAllocator ALLOCATOR = new BufferAllocator(256 * 128);

    public static void init() {
        // Hook disabled - waypoint rendering is handled directly via
        // VertexConsumerProvider in renderFilledInternal
    }

    private static Class<?> resolveFabricClass(String className) throws ClassNotFoundException {
        ClassLoader[] loaders = new ClassLoader[] {
                RenderHelper.class.getClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassLoader.getSystemClassLoader()
        };

        for (ClassLoader loader : loaders) {
            if (loader == null)
                continue;
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
            }
        }

        try {
            Class<?> fabricEventClass = Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents");
            ClassLoader fabricLoader = fabricEventClass.getClassLoader();
            if (fabricLoader != null) {
                try {
                    return Class.forName(className, false, fabricLoader);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                }
            }
        } catch (Exception e) {
        }

        throw new ClassNotFoundException("Fabric class not found: " + className);
    }

    public static void renderFilledWithBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents,
            float alpha, boolean throughWalls) {
        renderFilled(context, pos, colorComponents, alpha, throughWalls);
        renderBeaconBeam(context, pos, colorComponents);
    }

    public static void renderFilled(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha,
            boolean throughWalls) {
        renderFilled(context, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                colorComponents, alpha, throughWalls);
    }

    public static void renderFilled(WorldRenderContext context, Vec3d pos, Vec3d dimensions, float[] colorComponents,
            float alpha, boolean throughWalls) {
        renderFilled(context, pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z,
                colorComponents, alpha, throughWalls);
    }

    public static void renderFilled(WorldRenderContext context, Box box, float[] colorComponents, float alpha,
            boolean throughWalls) {
        renderFilled(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colorComponents, alpha,
                throughWalls);
    }

    public static void renderFilled(WorldRenderContext context, double minX, double minY, double minZ, double maxX,
            double maxY, double maxZ, float[] colorComponents, float alpha, boolean throughWalls) {
        if (throughWalls) {
            if (FrustumUtils.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
                renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha, true);
            }
        } else {
            if (OcclusionCulling.getRegularCuller().isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
                renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha, false);
            }
        }
    }

    private static void renderFilledInternal(WorldRenderContext context, double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ, float[] colorComponents, float alpha, boolean throughWalls) {
        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        // Set depth function BEFORE rendering
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        // Use immediate consumer for instant rendering
        VertexConsumerProvider.Immediate immediateConsumers = VertexConsumerProvider.immediate(ALLOCATOR);
        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer buffer = immediateConsumers.getBuffer(MasutilsRenderLayers.FILLED);

        System.out.println("[MasUtils] Rendering box: throughWalls=" + throughWalls + ", color=[" + colorComponents[0]
                + "," + colorComponents[1] + "," + colorComponents[2] + "], alpha=" + alpha);

        // Draw box edges as lines
        drawBoxEdges(buffer, entry, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha);

        // Draw immediately
        immediateConsumers.draw();

        // Restore depth function
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        matrices.pop();
    }

    private static void drawBoxEdges(VertexConsumer buffer, MatrixStack.Entry entry, double minX, double minY,
            double minZ,
            double maxX, double maxY, double maxZ, float[] color, float alpha) {
        // Draw all 12 edges of the box
        // Bottom face edges
        drawLine(buffer, entry, minX, minY, minZ, maxX, minY, minZ, color, alpha);
        drawLine(buffer, entry, maxX, minY, minZ, maxX, minY, maxZ, color, alpha);
        drawLine(buffer, entry, maxX, minY, maxZ, minX, minY, maxZ, color, alpha);
        drawLine(buffer, entry, minX, minY, maxZ, minX, minY, minZ, color, alpha);

        // Top face edges
        drawLine(buffer, entry, minX, maxY, minZ, maxX, maxY, minZ, color, alpha);
        drawLine(buffer, entry, maxX, maxY, minZ, maxX, maxY, maxZ, color, alpha);
        drawLine(buffer, entry, maxX, maxY, maxZ, minX, maxY, maxZ, color, alpha);
        drawLine(buffer, entry, minX, maxY, maxZ, minX, maxY, minZ, color, alpha);

        // Vertical edges
        drawLine(buffer, entry, minX, minY, minZ, minX, maxY, minZ, color, alpha);
        drawLine(buffer, entry, maxX, minY, minZ, maxX, maxY, minZ, color, alpha);
        drawLine(buffer, entry, maxX, minY, maxZ, maxX, maxY, maxZ, color, alpha);
        drawLine(buffer, entry, minX, minY, maxZ, minX, maxY, maxZ, color, alpha);
    }

    private static void drawLine(VertexConsumer buffer, MatrixStack.Entry entry, double x1, double y1, double z1,
            double x2, double y2, double z2, float[] color, float alpha) {
        // Calculate direction vector for normal
        float dx = (float) (x2 - x1);
        float dy = (float) (y2 - y1);
        float dz = (float) (z2 - z1);
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        System.out.println(
                "[MasUtils] Drawing line: (" + x1 + "," + y1 + "," + z1 + ") -> (" + x2 + "," + y2 + "," + z2 + ")");

        buffer.vertex(entry, (float) x1, (float) y1, (float) z1).color(color[0], color[1], color[2], alpha)
                .normal(entry, dx, dy, dz);
        buffer.vertex(entry, (float) x2, (float) y2, (float) z2).color(color[0], color[1], color[2], alpha)
                .normal(entry, dx, dy, dz);
    }

    public static void renderBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents) {
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        matrices.push();
        matrices.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
        // Beacon beam rendering logic could be added here if needed,
        // but current implementation only did a translate/push/pop which does nothing.
        matrices.pop();
    }

    public static void renderOutline(WorldRenderContext context, BlockPos pos, float[] colorComponents, float lineWidth,
            boolean throughWalls) {
        renderOutline(context, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                colorComponents, 1f, lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, Vec3d pos, Vec3d dimensions, float[] colorComponents,
            float lineWidth, boolean throughWalls) {
        renderOutline(context, pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z,
                colorComponents, 1f, lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, Box box, float[] colorComponents, float lineWidth,
            boolean throughWalls) {
        renderOutline(context, box, colorComponents, 1f, lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, Box box, float[] colorComponents, float alpha,
            float lineWidth, boolean throughWalls) {
        renderOutline(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colorComponents, alpha,
                lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, double minX, double minY, double minZ, double maxX,
            double maxY, double maxZ, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
        if (FrustumUtils.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();
            Tessellator tessellator = Tessellator.getInstance();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(lineWidth);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

            matrices.push();
            matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ());

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL);
            VertexRendering.drawBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ, colorComponents[0],
                    colorComponents[1], colorComponents[2], alpha);
            buffer.end();

            matrices.pop();
            GL11.glLineWidth(1f);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }
    }

    public static void renderLinesFromPoints(WorldRenderContext context, Vec3d[] points, float[] colorComponents,
            float alpha, float lineWidth, boolean throughWalls) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Tessellator tessellator = Tessellator.getInstance();
        MatrixStack.Entry entry = matrices.peek();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR_NORMAL);

        for (int i = 0; i < points.length; i++) {
            Vec3d nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
            Vector3f normalVec = nextPoint.toVector3f()
                    .sub((float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).normalize();
            if (i + 1 == points.length) {
                normalVec.negate();
            }

            buffer
                    .vertex(entry, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
                    .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                    .normal(entry, normalVec);
        }

        buffer.end();

        matrices.pop();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void renderLineFromCursor(WorldRenderContext context, Vec3d point, float[] colorComponents,
            float alpha, float lineWidth) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Tessellator tessellator = Tessellator.getInstance();
        MatrixStack.Entry entry = matrices.peek();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_ALWAYS);

        float pitch = context.camera().getPitch();
        float yaw = context.camera().getYaw();
        Vec3d cameraPoint = camera.add(Vec3d.fromPolar(pitch, yaw));

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL);

        Vector3f normal = point.toVector3f().sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
                .normalize();
        buffer
                .vertex(entry, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(entry, normal);

        buffer
                .vertex(entry, (float) point.getX(), (float) point.getY(), (float) point.getZ())
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(entry, normal);

        buffer.end();

        matrices.pop();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void renderQuad(WorldRenderContext context, Vec3d[] points, float[] colorComponents, float alpha,
            boolean throughWalls) {
        Matrix4f positionMatrix = new Matrix4f();
        Vec3d camera = context.camera().getPos();

        positionMatrix.translate((float) -camera.x, (float) -camera.y, (float) -camera.z);

        Tessellator tessellator = Tessellator.getInstance();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 4; i++) {
            buffer.vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
                    .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);
        }
        buffer.end();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void renderText(WorldRenderContext context, Text text, Vec3d pos, boolean throughWalls) {
        renderText(context, text, pos, 1, throughWalls);
    }

    public static void renderText(WorldRenderContext context, Text text, Vec3d pos, float scale, boolean throughWalls) {
        renderText(context, text, pos, scale, 0, throughWalls);
    }

    public static void renderText(WorldRenderContext context, Text text, Vec3d pos, float scale, float yOffset,
            boolean throughWalls) {
        renderText(context, text.asOrderedText(), pos, scale, yOffset, throughWalls);
    }

    public static void renderText(WorldRenderContext context, OrderedText text, Vec3d pos, float scale, float yOffset,
            boolean throughWalls) {
        Matrix4f positionMatrix = new Matrix4f();
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        TextRenderer textRenderer = CLIENT.textRenderer;

        scale *= 0.025f;

        positionMatrix
                .translate((float) (pos.getX() - cameraPos.getX()), (float) (pos.getY() - cameraPos.getY()),
                        (float) (pos.getZ() - cameraPos.getZ()))
                .rotate(camera.getRotation())
                .scale(scale, -scale, scale);

        float xOffset = -textRenderer.getWidth(text) / 2f;

        VertexConsumerProvider.Immediate consumers = VertexConsumerProvider.immediate(ALLOCATOR);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        textRenderer.draw(text, xOffset, yOffset, 0xFFFFFFFF, false, positionMatrix, consumers,
                throughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE);
        consumers.draw();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    private static void drawTranslucents(WorldRenderContext context) {
        // This method is no longer used. Rendering is handled directly in
        // renderFilledInternal
        // via the VertexConsumerProvider without needing a separate hook.
    }

    public static void runOnRenderThread(Runnable runnable) {
        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else {
            CLIENT.execute(runnable);
        }
    }

    public static Box getBlockBoundingBox(ClientWorld world, BlockPos pos) {
        return getBlockBoundingBox(world, world.getBlockState(pos), pos);
    }

    public static Box getBlockBoundingBox(ClientWorld world, BlockState state, BlockPos pos) {
        return state.getOutlineShape(world, pos).asCuboid().getBoundingBox().offset(pos);
    }

    private static void playNotificationSound() {
        if (CLIENT.player != null) {
            CLIENT.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 0.1f);
        }
    }

    public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width,
            int height, int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;
        context.fill(x, y, x + width, y + height, argb);
    }

    public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width,
            int height, Color color) {
        renderNineSliceColored(context, texture, x, y, width, height,
                ColorHelper.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
    }

}

package ru.massonnn.masutils.client.utils.render.primitive;

import org.joml.Matrix4f;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import ru.massonnn.masutils.client.utils.render.MasutilsRenderPipeline;
import ru.massonnn.masutils.client.utils.render.MatrixHelper;
import ru.massonnn.masutils.client.utils.render.Renderer;
import ru.massonnn.masutils.client.utils.render.state.FilledBoxRenderState;

public final class FilledBoxRenderer implements PrimitiveRenderer<FilledBoxRenderState> {
    static final FilledBoxRenderer INSTANCE = new FilledBoxRenderer();

    private FilledBoxRenderer() {}

    @Override
    public void submitPrimitives(FilledBoxRenderState state, CameraRenderState cameraState) {
        BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? MasutilsRenderPipeline.FILLED_THROUGH_WALLS : RenderPipelines.DEBUG_FILLED_BOX, 3f);
        Matrix4f positionMatrix = new Matrix4f()
                .translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);
        MatrixStack matrices = MatrixHelper.toStack(positionMatrix);

        float minX = (float) state.minX;
        float minY = (float) state.minY;
        float minZ = (float) state.minZ;
        float maxX = (float) state.maxX;
        float maxY = (float) state.maxY;
        float maxZ = (float) state.maxZ;
        float r = state.colourComponents[0];
        float g = state.colourComponents[1];
        float b = state.colourComponents[2];
        float a = state.alpha;
        Matrix4f m = matrices.peek().getPositionMatrix();

        quad(buffer, m, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        quad(buffer, m, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, a);
        quad(buffer, m, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        quad(buffer, m, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a);
        quad(buffer, m, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a);
        quad(buffer, m, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void quad(
            BufferBuilder buffer,
            Matrix4f m,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float x4,
            float y4,
            float z4,
            float r,
            float g,
            float b,
            float a
    ) {
        buffer.vertex(m, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(m, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(m, x3, y3, z3).color(r, g, b, a);
        buffer.vertex(m, x4, y4, z4).color(r, g, b, a);
    }
}

package ru.massonnn.masutils.client.utils.render.primitive;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.utils.render.MasutilsRenderPipeline;
import ru.massonnn.masutils.client.utils.render.Renderer;
import ru.massonnn.masutils.client.utils.render.state.OutlinedBoxRenderState;

public final class OutlinedBoxRenderer implements PrimitiveRenderer<OutlinedBoxRenderState> {
    static final OutlinedBoxRenderer INSTANCE = new OutlinedBoxRenderer();

    private OutlinedBoxRenderer() {}

    @Override
    public void submitPrimitives(OutlinedBoxRenderState state, CameraRenderState cameraState) {
        BufferBuilder buffer = Renderer.getBuffer(
                state.throughWalls ? MasutilsRenderPipeline.LINES_THROUGH_WALLS : MasutilsRenderPipeline.LINES,
                state.lineWidth
        );

        float x1 = (float) (state.minX - cameraState.pos.x);
        float y1 = (float) (state.minY - cameraState.pos.y);
        float z1 = (float) (state.minZ - cameraState.pos.z);
        float x2 = (float) (state.maxX - cameraState.pos.x);
        float y2 = (float) (state.maxY - cameraState.pos.y);
        float z2 = (float) (state.maxZ - cameraState.pos.z);

        float r = state.colourComponents[0];
        float g = state.colourComponents[1];
        float b = state.colourComponents[2];
        float a = state.alpha / 255f;

        Quaternionf rotationQuat = cameraState.orientation;
        Vector3f forward = new Vector3f(0, 0, -1);
        forward.rotate(rotationQuat);

        float nx = forward.x();
        float ny = forward.y();
        float nz = forward.z();

        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.001f) {
            nx /= len;
            ny /= len;
            nz /= len;
        } else {
            nx = 0f; ny = 0f; nz = 1f;
        }

        ny *= 0.1f;
        len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.001f) {
            nx /= len;
            ny /= len;
            nz /= len;
        }

        line(buffer, x1, y1, z1, x2, y1, z1, r, g, b, a, nx, ny, nz);
        line(buffer, x2, y1, z1, x2, y1, z2, r, g, b, a, nx, ny, nz);
        line(buffer, x2, y1, z2, x1, y1, z2, r, g, b, a, nx, ny, nz);
        line(buffer, x1, y1, z2, x1, y1, z1, r, g, b, a, nx, ny, nz);

        line(buffer, x1, y2, z1, x2, y2, z1, r, g, b, a, nx, ny, nz);
        line(buffer, x2, y2, z1, x2, y2, z2, r, g, b, a, nx, ny, nz);
        line(buffer, x2, y2, z2, x1, y2, z2, r, g, b, a, nx, ny, nz);
        line(buffer, x1, y2, z2, x1, y2, z1, r, g, b, a, nx, ny, nz);

        line(buffer, x1, y1, z1, x1, y2, z1, r, g, b, a, nx, ny, nz);
        line(buffer, x2, y1, z1, x2, y2, z1, r, g, b, a, nx, ny, nz);
        line(buffer, x2, y1, z2, x2, y2, z2, r, g, b, a, nx, ny, nz);
        line(buffer, x1, y1, z2, x1, y2, z2, r, g, b, a, nx, ny, nz);
    }

    private void line(BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2,
                      float r, float g, float b, float a, float nx, float ny, float nz) {
        builder.vertex(x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz);
        builder.vertex(x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz);
    }
}
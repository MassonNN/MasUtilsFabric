package ru.massonnn.masutils.client.utils.render.primitive;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.massonnn.masutils.client.utils.render.MasutilsRenderPipeline;
import ru.massonnn.masutils.client.utils.render.Renderer;
import ru.massonnn.masutils.client.utils.render.state.CursorLineRenderState;
import ru.massonnn.masutils.client.utils.render.state.OutlinedBoxRenderState;

public final class CursorLineRenderer implements PrimitiveRenderer<CursorLineRenderState> {
    static final CursorLineRenderer INSTANCE = new CursorLineRenderer();

    private CursorLineRenderer() {}

    @Override
    public void submitPrimitives(CursorLineRenderState state, CameraRenderState cameraState) {
        BufferBuilder buffer = Renderer.getBuffer(
                MasutilsRenderPipeline.LINES,
                state.lineWidth
        );

        // 1. Вычисляем относительные координаты (Мир -> Вид)
        // Координаты цели минус координаты камеры
        float targetX = (float) (state.x - cameraState.pos.x);
        float targetY = (float) (state.y - cameraState.pos.y);
        float targetZ = (float) (state.z - cameraState.pos.z);

        // 2. Вычисляем точку "у прицела"
        // Чтобы линия не обрезалась, начнем её чуть впереди камеры (например, на 0.1 блока)
        // Используем вектор направления взгляда из ориентации камеры
        Vector3f lookVec = new Vector3f(0, 0, -0.1f); // Направление "вперед" в OpenGL
        lookVec.rotate(cameraState.orientation);

        float startX = lookVec.x;
        float startY = lookVec.y;
        float startZ = lookVec.z;

        float r = state.colourComponents[0];
        float g = state.colourComponents[1];
        float b = state.colourComponents[2];
        float a = state.alpha / 255f;

        // 3. Рисуем линию
        line(buffer, startX, startY, startZ, targetX, targetY, targetZ, r, g, b, a, 0f, 1f, 0f);
    }

    private void line(BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2,
                      float r, float g, float b, float a, float nx, float ny, float nz) {
        builder.vertex(x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz);
        builder.vertex(x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz);
    }
}
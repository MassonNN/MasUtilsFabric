package ru.massonnn.masutils.client.utils.render.primitive;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix4f;
import ru.massonnn.masutils.client.utils.render.Renderer;
import ru.massonnn.masutils.client.utils.render.state.TextRenderState;

public final class TextPrimitiveRenderer implements PrimitiveRenderer<TextRenderState> {
    static final TextPrimitiveRenderer INSTANCE = new TextPrimitiveRenderer();

    private static final RenderPipeline SEE_THROUGH = RenderPipelines.RENDERTYPE_TEXT_SEETHROUGH;
    private static final RenderPipeline NORMAL = RenderPipelines.RENDERTYPE_TEXT;

    private static final RenderPipeline BACKGROUND = RenderPipelines.DEBUG_QUADS;

    private TextPrimitiveRenderer() {}

    @Override
    public void submitPrimitives(TextRenderState state, CameraRenderState cameraState) {
        RenderPipeline textPipeline = state.throughWalls ? SEE_THROUGH : NORMAL;

        // Матрица для текста (оригинальная)
        Matrix4f positionMatrix = new Matrix4f()
                .translate((float) (state.pos.x - cameraState.pos.x),
                        (float) (state.pos.y - cameraState.pos.y),
                        (float) (state.pos.z - cameraState.pos.z))
                .rotate(cameraState.orientation)
                .scale(state.scale, -state.scale, state.scale);

        // 1. Рисуем фон (чёрный полупрозрачный прямоугольник)
        drawBackground(positionMatrix, state);

        // 2. Рисуем сам текст поверх фона
        state.glyphs.draw(new TextRenderer.GlyphDrawer() {
            @Override
            public void drawGlyph(TextDrawable glyph) {
                TextureSetup textureSetup = TextureSetup.withoutGlTexture(glyph.textureView());
                BufferBuilder textBuffer = Renderer.getBuffer(textPipeline, textureSetup);
                glyph.render(positionMatrix, textBuffer, LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE, false);
            }

            @Override
            public void drawRectangle(TextDrawable bakedGlyph) {
                drawGlyph(bakedGlyph);
            }
        });
    }

    private void drawBackground(Matrix4f matrix, TextRenderState state) {
        // 1. Получаем размеры. Если нет getWidth(), используем кол-во символов как аппроксимацию,
        // но лучше вытянуть реальное значение из state.glyphs.
        float width = state.glyphs.getScreenRect().width();
        float height = state.glyphs.getScreenRect().height();

        // 2. Корректировка положения (экспериментальные значения для центрирования)
        // Текст часто рисуется "вверх" от базовой линии, поэтому фон нужно поднять
        float yOffsetCorrection = - height * 0.05f; // Сдвиг вниз, если фон "задрался" выше текста

        // 3. Отступы (Padding)
        float paddingX = 0.4f;
        float paddingY = 0.4f;

        // Рассчитываем границы
        float left   = -paddingX;
        float right  = width + paddingX;
        float top    = -paddingY;
        float bottom = height + paddingY;

        // Центрирование по горизонтали (если текст рисуется из центра)
        float centerX = width / 2f;
        left -= centerX;
        right -= centerX;

        // Цвет и глубина
        float r = 0.0f, g = 0.0f, b = 0.0f, a = 0.6f;
        float z = -0.01f; // Фон ДОЛЖЕН быть дальше текста (отрицательный Z в локальных координатах)

        BufferBuilder bgBuffer = Renderer.getBuffer(BACKGROUND);

        bgBuffer.vertex(matrix, left,   top,    z).color(r, g, b, a);
        bgBuffer.vertex(matrix, right,  top,    z).color(r, g, b, a);
        bgBuffer.vertex(matrix, right,  bottom, z).color(r, g, b, a);
        bgBuffer.vertex(matrix, left,   bottom, z).color(r, g, b, a);
    }
}
package com.niko.littlepiggy.fx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/*
 * Delad shader som blandar en sprites färg mot rent vitt,
 * styrt av uniformen u_flashAmount (0 = ingen flash, 1 = helt vit).
 *
 * Vertex-shadern är identisk med libGDX default SpriteBatch-shader,
 * bara fragment-shadern gör något extra.
 */
public final class HitFlashShader {

    private static final String VERTEX =
            "attribute vec4 a_position;\n"
                    + "attribute vec4 a_color;\n"
                    + "attribute vec2 a_texCoord0;\n"
                    + "uniform mat4 u_projTrans;\n"
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                    + "void main() {\n"
                    + "    v_color = a_color;\n"
                    + "    v_color.a = v_color.a * (255.0/254.0);\n"
                    + "    v_texCoords = a_texCoord0;\n"
                    + "    gl_Position = u_projTrans * a_position;\n"
                    + "}\n";

    private static final String FRAGMENT =
            "#ifdef GL_ES\n"
                    + "#define LOWP lowp\n"
                    + "precision mediump float;\n"
                    + "#else\n"
                    + "#define LOWP\n"
                    + "#endif\n"
                    + "varying LOWP vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                    + "uniform sampler2D u_texture;\n"
                    + "uniform float u_flashAmount;\n"
                    + "void main() {\n"
                    + "    vec4 texColor = texture2D(u_texture, v_texCoords);\n"
                    + "    vec3 flashed = mix(texColor.rgb, vec3(1.0), u_flashAmount);\n"
                    + "    gl_FragColor = vec4(flashed, texColor.a) * v_color;\n"
                    + "}\n";

    private static ShaderProgram shader;

    private HitFlashShader() {
    }

    public static ShaderProgram get() {

        if (shader == null) {

            shader = new ShaderProgram(VERTEX, FRAGMENT);

            if (!shader.isCompiled()) {
                Gdx.app.error(
                        "HitFlashShader",
                        "Kompileringsfel: " + shader.getLog());
            }
        }

        return shader;
    }
}

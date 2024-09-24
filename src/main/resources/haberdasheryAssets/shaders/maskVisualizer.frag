uniform sampler2D u_texture;
uniform sampler2D u_mask;
uniform bool u_viewMask;

varying vec4 v_color;
varying vec2 v_texCoords;

vec3 checkerboard(in float u, in float v)
{
    float checkSize = 16;
    float fmodResult = mod(floor(checkSize * u) + floor(checkSize * v), 2.0);
    float fin = min(max(sign(fmodResult), 0.4), 0.6);
    return vec3(fin, fin, fin);
}

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    float maskAlpha = texture2D(u_mask, v_texCoords).a;
    if (u_viewMask) {
        gl_FragColor = vec4(maskAlpha, maskAlpha, maskAlpha, 1) * v_color;
    } else {
        vec3 checker = checkerboard(v_texCoords.x, v_texCoords.y);
        gl_FragColor = texColor * maskAlpha * v_color;
        gl_FragColor = vec4(
            gl_FragColor.rgb + checker.rgb * (1.0 - gl_FragColor.a),
            1
        );
    }
}

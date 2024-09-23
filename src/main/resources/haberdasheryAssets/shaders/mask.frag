uniform sampler2D u_texture;
uniform sampler2D u_mask;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    float maskAlpha = texture2D(u_mask, v_texCoords).a;
    texColor *= maskAlpha;
    gl_FragColor = texColor * v_color;
}

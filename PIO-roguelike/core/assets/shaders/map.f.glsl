varying vec2 tex_coord;

uniform sampler2D u_texture;

void main() {
    gl_FragColor = texture2D(u_texture, tex_coord);
}

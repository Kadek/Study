attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projectionViewMatrix;

varying vec2 tex_coord;

void main() {
    gl_Position = u_projectionViewMatrix * a_position;
    tex_coord = a_texCoord0;
}

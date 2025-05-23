uniform mat4 uMVPMatrix;
attribute vec4 v_Position;
attribute vec2 a_TextureCoordinates;
varying vec2 v_TextureCoordinates;
void main()
{
    v_TextureCoordinates = a_TextureCoordinates;
    gl_Position = uMVPMatrix * v_Position;
}

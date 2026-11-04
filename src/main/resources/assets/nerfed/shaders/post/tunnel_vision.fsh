#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec4 c = texture(InSampler, texCoord);
    float d = distance(texCoord, vec2(0.5));    // 0 at the centre, about 0.7 at the corners
    float dark = smoothstep(0.15, 0.6, d);      // lower numbers give a tighter tunnel
    fragColor = vec4(c.rgb * (1.0 - dark), 1.0);
}
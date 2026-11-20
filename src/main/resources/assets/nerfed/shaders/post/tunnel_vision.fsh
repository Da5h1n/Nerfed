#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform TunnelConfig {
    float Inner;     // distance from centre where darkening starts
    float Outer;     // distance where it is fully dark (corners are about 0.7)
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec4 c = texture(InSampler, texCoord);
    float d = distance(texCoord, vec2(0.5));
    float dark = smoothstep(Inner, Outer, d);
    fragColor = vec4(c.rgb * (1.0 - dark), 1.0);
}

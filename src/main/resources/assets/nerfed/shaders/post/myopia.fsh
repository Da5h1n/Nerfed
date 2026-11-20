#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform MyopiaConfig {
    float Radius;    // blur spacing in pixels: 0.6 mild, 1.2 moderate, 2.0 severe
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
    vec3 sum = vec3(0.0);
    float total = 0.0;

    // 7x7 gaussian-weighted blur
    for (int x = -3; x <= 3; x++) {
        for (int y = -3; y <= 3; y++) {
            float w = exp(-float(x * x + y * y) / 8.0);
            sum += texture(InSampler, texCoord + vec2(x, y) * texel * Radius).rgb * w;
            total += w;
        }
    }
    fragColor = vec4(sum / total, 1.0);
}

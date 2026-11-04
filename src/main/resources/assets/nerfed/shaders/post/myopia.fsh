#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
    vec3 sum = vec3(0.0);
    float total = 0.0;

    // 7x7 gaussian-weighted blur; raise the 2.0 for a stronger blur
    for (int x = -3; x <= 3; x++) {
        for (int y = -3; y <= 3; y++) {
            float w = exp(-float(x * x + y * y) / 8.0);
            sum += texture(InSampler, texCoord + vec2(x, y) * texel * 2.0).rgb * w;
            total += w;
        }
    }
    fragColor = vec4(sum / total, 1.0);
}
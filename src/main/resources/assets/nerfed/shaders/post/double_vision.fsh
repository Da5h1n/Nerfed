#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform DoubleVisionConfig {
    float OffsetX;   // ghost image offset, as a fraction of the screen
    float OffsetY;
    float Ghost;     // how visible the second image is (0.5 = as strong as the real one)
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec3 a = texture(InSampler, texCoord).rgb;
    vec3 b = texture(InSampler, texCoord + vec2(OffsetX, OffsetY)).rgb;
    fragColor = vec4(mix(a, b, Ghost), 1.0);
}

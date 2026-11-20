#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform NightBlindnessConfig {
    float Severity;  // 0.0 = no effect, 1.0 = full
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec3 c = texture(InSampler, texCoord).rgb;
    float lum = dot(c, vec3(0.2126, 0.7152, 0.0722));

    // Dim areas fade toward black while well-lit areas are left alone.
    // Torches, glowstone and daylight survive; dark caves and night do not.
    float lo = Severity * 0.12;
    float hi = Severity * 0.55 + 0.001;
    float keep = smoothstep(lo, hi, lum);

    fragColor = vec4(c * keep, 1.0);
}

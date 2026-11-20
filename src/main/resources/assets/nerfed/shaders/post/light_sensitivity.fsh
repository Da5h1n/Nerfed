#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform LightSensitivityConfig {
    float Severity;  // 0.0 = no effect, 1.0 = full
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

vec3 brightPart(vec2 uv) {
    vec3 c = texture(InSampler, uv).rgb;
    float lum = dot(c, vec3(0.2126, 0.7152, 0.0722));
    return c * smoothstep(0.55, 0.95, lum);
}

void main() {
    vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
    vec3 c = texture(InSampler, texCoord).rgb;
    float lum = dot(c, vec3(0.2126, 0.7152, 0.0722));

    // Glare: bright areas bleed into their surroundings (two rings of 8 taps)
    vec3 glow = vec3(0.0);
    for (int i = 0; i < 16; i++) {
        float a = float(i) * 0.3926991;                 // 2 * pi / 16
        float r = (i % 2 == 0) ? 8.0 : 20.0;
        glow += brightPart(texCoord + vec2(cos(a), sin(a)) * r * texel);
    }
    glow /= 16.0;

    vec3 result = c + glow * (2.0 * Severity);

    // Bright surfaces wash out toward white
    float bright = smoothstep(0.55, 1.0, lum);
    result = mix(result, vec3(1.0), bright * 0.55 * Severity);

    fragColor = vec4(clamp(result, 0.0, 1.0), 1.0);
}

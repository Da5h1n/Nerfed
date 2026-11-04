#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform ColourblindConfig {
    int Mode;
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

// Machado et al. colour vision deficiency matrices (full severity).
// GLSL fills matrices by column, so "c * mat3(...)" applies the rows as written.
vec3 simulate(vec3 c) {
    if (Mode == 1) {        // protanopia
                            return c * mat3( 0.152286,  1.052583, -0.204868,
                            0.114503,  0.786281,  0.099216,
                            -0.003882, -0.048116,  1.051998);
    } else if (Mode == 2) { // deuteranopia
                            return c * mat3( 0.367322,  0.860646, -0.227968,
                            0.280085,  0.672501,  0.047413,
                            -0.011820,  0.042940,  0.968881);
    } else if (Mode == 3) { // tritanopia
                            return c * mat3( 1.255528, -0.076749, -0.178779,
                            -0.078411,  0.930809,  0.147602,
                            0.004733,  0.691367,  0.303900);
    } else if (Mode == 4) { // monochromacy
                            return vec3(dot(c, vec3(0.2126, 0.7152, 0.0722)));
    }
    return c;
}

void main() {
    vec3 srgb = texture(InSampler, texCoord).rgb;
    vec3 linear = pow(srgb, vec3(2.2));              // simulate in linear light
    vec3 result = clamp(simulate(linear), 0.0, 1.0);
    fragColor = vec4(pow(result, vec3(1.0 / 2.2)), 1.0);
}
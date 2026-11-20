#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform BlindnessConfig {
    mat4 InvProjection;
    mat4 InvViewRotation;
    vec4 ParamsA;       // x: base visibility, y: bright light survival, z: echo gain, w: blur px
    vec4 ParamsB;       // x: 1.0 if depth is 0..1, y: pulse speed (blocks/s), zw: unused
    vec4 Pulse[3];      // x: seconds since start, y: reach (blocks), z: gain (strength * fade), w: < 0 = unused
    vec4 Emitters[12];  // pulse p uses [p*4 .. p*4+3]: xyz camera-relative position, w: path length (< 0 = unused)
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

const float TAIL_SECONDS = 1.1;   // how long a surface stays lit after its echo arrives
const float FRONT_WIDTH  = 1.4;   // thickness of the bright leading edge (blocks of path)
const float SPLASH_NEAR  = 3.0;   // bounce emitters light surfaces fully within this distance...
const float SPLASH_FAR   = 12.0;  // ...and not at all beyond this
const vec3  ECHO_COLOUR  = vec3(0.55, 0.85, 1.0);

vec4 surfaceAt(vec2 uv) {
    float d = textureLod(DepthSampler, uv, 0.0).r;
    if (d <= 0.00001) return vec4(0.0);
    float z = ParamsB.x > 0.5 ? d : d * 2.0 - 1.0;
    vec4 view = InvProjection * vec4(uv * 2.0 - 1.0, z, 1.0);
    view /= view.w;
    return vec4((InvViewRotation * vec4(view.xyz, 0.0)).xyz, 1.0);
}

float jump(vec2 uv, float dist) {
    vec4 n = surfaceAt(uv);
    float nd = n.w > 0.5 ? length(n.xyz) : dist + 3.0;
    return abs(nd - dist) / max(dist, 0.5);
}

float edgeAt(vec2 uv, float dist) {
    vec2 t = 1.0 / vec2(textureSize(DepthSampler, 0));
    float e = jump(uv + vec2( t.x, 0.0), dist);
    e = max(e, jump(uv + vec2(-t.x, 0.0), dist));
    e = max(e, jump(uv + vec2(0.0,  t.y), dist));
    e = max(e, jump(uv + vec2(0.0, -t.y), dist));
    return smoothstep(0.02, 0.12, e);
}

void main() {
    vec3 scene = texture(InSampler, texCoord).rgb;
    if (ParamsA.w > 0.01) {
        vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
        vec3 sum = scene;
        for (int i = 0; i < 8; i++) {
            float a = float(i) * 0.7853982;
            sum += texture(InSampler, texCoord + vec2(cos(a), sin(a)) * ParamsA.w * texel).rgb;
        }
        scene = sum / 9.0;
    }

    float lum = dot(scene, vec3(0.2126, 0.7152, 0.0722));
    vec3 base = max(scene * ParamsA.x, scene * smoothstep(0.6, 1.0, lum) * ParamsA.y);

    vec4 s = surfaceAt(texCoord);
    vec3 dx = dFdx(s.xyz);   // derivatives stay outside any branch
    vec3 dy = dFdy(s.xyz);

    float echoSum = 0.0;

    if (s.w > 0.5 && ParamsA.z > 0.0) {
        vec3 P = s.xyz;
        float toListener = length(P);   // camera-relative, so the listener is the origin

        for (int p = 0; p < 3; p++) {
            vec4 pulse = Pulse[p];
            if (pulse.w < 0.0 || pulse.z <= 0.0) continue;

            float travelled = ParamsB.y * pulse.x;   // how far this pulse has gone so far
            float reach = pulse.y;
            float pulseSum = 0.0;

            // Faint outgoing wavefront so you can see the pulse leave the player
            if (travelled < reach) {
                float o = (toListener - travelled) / FRONT_WIDTH;
                pulseSum += exp(-o * o) * 0.3;
            }

            for (int j = 0; j < 4; j++) {
                vec4 e = Emitters[p * 4 + j];
                if (e.w < 0.0) continue;

                float toSurface = length(P - e.xyz);
                float path  = e.w + toSurface + toListener;
                float since = travelled - path;   // blocks past the front; < 0 = echo not back yet
                if (since < 0.0) continue;

                float range = 1.0 - smoothstep(reach * 1.4, reach * 2.2, path);
                if (range <= 0.0) continue;

                float splash = (j == 0) ? 1.0 : 1.0 - smoothstep(SPLASH_NEAR, SPLASH_FAR, toSurface);
                float atten  = 1.0 / (1.0 + path * 0.035);

                float x     = since / FRONT_WIDTH;
                float front = exp(-x * x);
                float tail  = smoothstep(0.0, 1.5, since) * exp(-since / (ParamsB.y * TAIL_SECONDS));

                pulseSum += (front + tail * 0.6) * splash * atten * range;
            }

            echoSum += pulseSum * pulse.z;
        }
    }

    float echo = 1.0 - exp(-echoSum * ParamsA.z * 1.6);   // soft clamp so overlaps don't blow out

    vec3 result = base;
    if (s.w > 0.5 && echo > 0.001) {
        vec3 n = normalize(cross(dx, dy) + vec3(1.0e-6));
        float shade = 0.45 + 0.55 * abs(dot(n, normalize(vec3(0.35, 0.85, 0.4))));
        float edge = edgeAt(texCoord, length(s.xyz));
        result += ECHO_COLOUR * echo * (shade * 0.7 + edge * 0.7);
    }

    fragColor = vec4(clamp(result, 0.0, 1.0), 1.0);
}
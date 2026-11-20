package uk.da5h1n.nerfed.client;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import uk.da5h1n.nerfed.Nerfed;

/**
 * Loaded only by Simple Voice Chat (via the "voicechat" entrypoint), so the mod stays optional.
 */
public class NerfedVoicechatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return Nerfed.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        Nerfed.LOGGER.info("[Deafness] Simple Voice Chat detected, voices will be affected by deafness");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        // Proximity voices, positional voices and group/static voices
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::onSound);
        registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, this::onSound);
        registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, this::onSound);
    }

    private void onSound(ClientReceiveSoundEvent event) {
        short[] audio = event.getRawAudio();
        if (audio == null || audio.length == 0) return;

        DeafnessEffect.processVoice(event.getId(), audio);
        event.setRawAudio(audio);
    }
}
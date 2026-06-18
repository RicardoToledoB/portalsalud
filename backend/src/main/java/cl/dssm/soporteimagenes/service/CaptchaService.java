package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.CaptchaChallengeDto;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {
    private static final long TTL_SECONDS = 600;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> challenges = new ConcurrentHashMap<>();

    public CaptchaChallengeDto createChallenge() {
        purgeExpired();
        int a = random.nextInt(8) + 2;
        int b = random.nextInt(8) + 2;
        String id = UUID.randomUUID().toString();
        challenges.put(id, new CaptchaEntry(String.valueOf(a + b), Instant.now().plusSeconds(TTL_SECONDS)));
        return new CaptchaChallengeDto(id, "¿Cuánto es " + a + " + " + b + "?");
    }

    public boolean validate(String captchaId, String answer) {
        if (captchaId == null || captchaId.isBlank() || answer == null || answer.isBlank()) return false;
        CaptchaEntry entry = challenges.remove(captchaId);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) return false;
        return entry.answer().equals(answer.trim());
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        challenges.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    private record CaptchaEntry(String answer, Instant expiresAt) {}
}

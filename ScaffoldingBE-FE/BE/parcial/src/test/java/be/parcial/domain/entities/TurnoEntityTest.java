package be.parcial.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TurnoEntityTest {

    @Test
    @DisplayName("should set timestamps on pre-persist")
    void onCreate_setsTimestamps() {
        TurnoEntity entity = new TurnoEntity();
        entity.onCreate();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should update updatedAt on pre-update")
    void onUpdate_updatesTimestamp() {
        TurnoEntity entity = new TurnoEntity();
        entity.onCreate();
        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();

        entity.onUpdate();

        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}

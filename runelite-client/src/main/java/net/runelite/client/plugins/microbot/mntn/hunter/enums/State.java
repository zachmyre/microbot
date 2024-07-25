package net.runelite.client.plugins.microbot.mntn.hunter.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum State {
    CHECK_TRAP,
    SET_TRAP,
    PICKUP_TRAP,
    WALK_AWAY,
}

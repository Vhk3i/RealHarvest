package com.realharvest.client.health;

import com.realharvest.health.BodyPart;
import com.realharvest.health.InjuryType;

import java.util.EnumMap;
import java.util.Map;

public class ClientBodyHealth {
    private static final Map<BodyPart, InjuryType> INJURIES = new EnumMap<>(BodyPart.class);

    static {
        reset();
    }

    public static void reset() {
        for (BodyPart part : BodyPart.values()) {
            INJURIES.put(part, InjuryType.NONE);
        }
    }

    public static void set(BodyPart part, InjuryType type) {
        INJURIES.put(part, type);
    }

    public static InjuryType get(BodyPart part) {
        return INJURIES.getOrDefault(part, InjuryType.NONE);
    }

    public static boolean hasLegInjury() {
        return get(BodyPart.LEFT_LEG) != InjuryType.NONE || get(BodyPart.RIGHT_LEG) != InjuryType.NONE;
    }

    public static boolean hasArmInjury() {
        return get(BodyPart.LEFT_ARM) != InjuryType.NONE || get(BodyPart.RIGHT_ARM) != InjuryType.NONE;
    }
}

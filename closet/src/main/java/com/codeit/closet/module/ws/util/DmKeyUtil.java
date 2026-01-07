package com.codeit.closet.module.ws.util;

import java.util.UUID;

public final class DmKeyUtil {

    private DmKeyUtil() {}

    public static String of(UUID userA, UUID userB) {
        String a = userA.toString();
        String b = userB.toString();

        return (a.compareTo(b) <= 0) ? (a + "_" + b) : (b + "_" + a);
    }
}
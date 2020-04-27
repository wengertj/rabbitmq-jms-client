/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.util;

import java.util.UUID;

/**
 * Utility class which generates unique string identifiers.
 */
public final class Util {
    /**
     * Generates a UUID string identifier.
     * @param prefix - prefix of uniquely generated string; may be <code>null</code>, in which case “<code>null</code>” is the prefix used.
     * @return a UUID string with given prefix
     */
    public static final String generateUUID(String prefix) {
        return new StringBuilder().append(prefix).append(UUID.randomUUID()).toString();
    }
}

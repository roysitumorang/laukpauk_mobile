package id.laukpauk.core;

import id.laukpauk.BuildConfig;

/** Port of lib/libraries/Environment.dart. Value comes from ENVIRONMENT in laukpauk.properties. */
public final class Environment {
    private Environment() {}

    /** "development", "testing" or "production". */
    public static final String environment = BuildConfig.ENVIRONMENT;

    public static boolean isProduction() {
        return "production".equals(environment);
    }
}

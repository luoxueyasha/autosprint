package com.roseyasa.autosprint;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue AUTO_JUMP = BUILDER
            .comment("Whether to turn on or off auto-jump when auto-running.")
            .define("autoJump", true);

    public static final ModConfigSpec.BooleanValue AUTO_SPRINT = BUILDER
            .comment("Whether to sprint or walk when auto-running on default.")
            .define("autoSprint", true);

    static final ModConfigSpec SPEC = BUILDER.build();

}

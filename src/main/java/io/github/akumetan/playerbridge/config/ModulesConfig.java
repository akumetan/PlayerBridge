package io.github.akumetan.playerbridge.config;

public record ModulesConfig(
        boolean inventory,
        boolean enderChest,
        boolean healthAndFood,
        boolean experience,
        boolean fireTicks,
        boolean gameMode,
        boolean activeEffects
) {
}
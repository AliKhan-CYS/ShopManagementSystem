package com.shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

/** Paths for mutable user data, kept outside the installed application directory. */
public final class AppPaths {
    private static final String APP_DIRECTORY = "ShopManagementSystem";

    private AppPaths() { }

    public static Path dataDirectory() throws IOException {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = localAppData == null || localAppData.isBlank()
                ? Path.of(System.getProperty("user.home"), ".local", "share")
                : Path.of(localAppData);
        Path directory = base.resolve(APP_DIRECTORY);
        Files.createDirectories(directory);
        try {
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------"));
        } catch (UnsupportedOperationException ignored) {
            // Windows user profile ACLs provide per-user access by default.
        }
        return directory;
    }

    public static Path databaseFile() throws IOException {
        return dataDirectory().resolve("shop.db");
    }

    public static Path profilePhotoDirectory() throws IOException {
        Path directory = dataDirectory().resolve("profile_photos");
        Files.createDirectories(directory);
        return directory;
    }
}

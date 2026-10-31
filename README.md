# Shop Management System

Windows desktop app for shop inventory, sales, customer records, and staff accounts.

## Run the packaged app

Open `app/ShopDesk/ShopDesk.exe`. The app includes its Java runtime and dependencies; source files are not included. The first launch asks you to create the first administrator account. Choose a unique password with at least 12 characters.

The installer and files intended for a GitHub release are in `github-release/`. Install with `ShopManagementSystem-1.0.0-setup.exe`.

## Build

Requirements: Windows x64, JDK 17 or newer with `jpackage`, Maven 3.9+, and WiX Toolset 3.14.1 or newer on `PATH` to create the installer.

Run from PowerShell:

```powershell
./scripts/build-release.ps1
```

The script compiles and packages the app, creates a self-contained app image under `app/`, creates the Windows setup executable under `github-release/`, and writes a SHA-256 checksum. It does not copy the development database or profile photos into either package.

## Local data and security

The SQLite database and profile photos are stored per Windows user under `%LOCALAPPDATA%\ShopManagementSystem`. A new installation starts with an empty database and creates the first admin during setup.

Passwords are stored with salted PBKDF2-HMAC-SHA256 hashes. Account and product inputs are validated before database writes. Admin recovery files hold one-time tokens that expire after 30 days. Profile photos are type, size, and dimension checked. The local database is not encrypted; protect access to your Windows account and its profile directory.

## Third-party licenses

The app bundles SQLite JDBC, JFreeChart, and FlatLaf. License references are in `THIRD_PARTY_NOTICES.txt`, included with the application image and release files.

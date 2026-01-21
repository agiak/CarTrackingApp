# App Icon Switching Guide

This project includes two different app icons that you can switch between:

## Available Icons

### 1. Primary Icon (Currently Active)
- **Name**: `ic_launcher`
- **Background Color**: `#1878A8` (Blue)
- **Resources**: 
  - `@mipmap/ic_launcher`
  - `@mipmap/ic_launcher_round`

### 2. Alternative Icon
- **Name**: `ic_launcher_alt`
- **Background Color**: `#E8F8F8` (Light Cyan)
- **Resources**: 
  - `@mipmap/ic_launcher_alt`
  - `@mipmap/ic_launcher_alt_round`

## How to Switch Icons

To change which icon is used as your app's launcher icon, follow these steps:

### Method 1: Edit AndroidManifest.xml (Permanent Change)

1. Open `app/src/main/AndroidManifest.xml`
2. Find the `<application>` tag
3. Change the `android:icon` and `android:roundIcon` attributes:

**For Primary Icon (Default):**
```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    ...>
```

**For Alternative Icon:**
```xml
<application
    android:icon="@mipmap/ic_launcher_alt"
    android:roundIcon="@mipmap/ic_launcher_alt_round"
    ...>
```

4. Clean and rebuild the project:
   - In Android Studio: `Build > Clean Project`
   - Then: `Build > Rebuild Project`

5. Uninstall the old app from your device/emulator
6. Install the new build

### Method 2: Using Build Variants (Advanced)

You can create different build variants or product flavors to automatically use different icons:

1. In `app/build.gradle.kts`, add:
```kotlin
android {
    flavorDimensions += "version"
    productFlavors {
        create("primary") {
            dimension = "version"
            // Uses default ic_launcher
        }
        create("alternative") {
            dimension = "version"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_alt"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_alt_round"
        }
    }
}
```

2. Update `AndroidManifest.xml`:
```xml
<application
    android:icon="${appIcon}"
    android:roundIcon="${appIconRound}"
    ...>
```

3. Select the desired flavor in Android Studio:
   - `Build > Select Build Variant`
   - Choose `primaryDebug` or `alternativeDebug`

## Icon Files Location

All icon files are located in:
```
app/src/main/res/
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml
│   ├── ic_launcher_round.xml
│   ├── ic_launcher_alt.xml
│   └── ic_launcher_alt_round.xml
├── mipmap-hdpi/
├── mipmap-mdpi/
├── mipmap-xhdpi/
├── mipmap-xxhdpi/
└── mipmap-xxxhdpi/
    ├── ic_launcher*.png
    └── ic_launcher_alt*.png
```

## Colors

The icon background colors are defined in `app/src/main/res/values/colors.xml`:

```xml
<color name="ic_launcher_background">#1878A8</color>
<color name="ic_launcher_alt_background">#E8F8F8</color>
```

## Notes

- Both icons support adaptive icons for Android 8.0+ (API 26+)
- PNG fallbacks are provided for older Android versions
- Icon changes require app reinstallation to take effect
- The icon displayed in Android Studio's design tools may not update immediately - test on a real device or emulator

## Testing

To verify the icon change:
1. Build and install the app
2. Check your device's app drawer
3. The new icon should appear after installation

---

**Last Updated**: January 21, 2026

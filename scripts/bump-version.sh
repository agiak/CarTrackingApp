#!/usr/bin/env bash
#
# Bumps the centralized version numbers in version.properties.
#
# The development and production tracks are fully independent — each has its own
# versionName and versionCode:
#   * development build -> developmentVersionName patch +1, developmentVersionCode +1
#   * production build  -> productionVersionName  patch +1, productionVersionCode  +1
#
# Usage:
#   scripts/bump-version.sh <development|production> [path/to/version.properties]
#
# Outputs (stdout and, when running in GitHub Actions, $GITHUB_OUTPUT):
#   versionName=<new version name for the chosen track>
#   versionCode=<new version code for the chosen track>
#   buildType=<development|production>
#
set -euo pipefail

BUILD_TYPE="${1:?Usage: bump-version.sh <development|production> [props-file]}"
PROPS_FILE="${2:-version.properties}"

if [[ "$BUILD_TYPE" != "development" && "$BUILD_TYPE" != "production" ]]; then
  echo "ERROR: invalid build type '$BUILD_TYPE' (expected 'development' or 'production')" >&2
  exit 1
fi

if [[ ! -f "$PROPS_FILE" ]]; then
  echo "ERROR: '$PROPS_FILE' not found" >&2
  exit 1
fi

# Read a property value (trims surrounding whitespace).
get_prop() {
  local key="$1"
  local value
  value="$(grep -E "^${key}=" "$PROPS_FILE" | head -n1 | cut -d'=' -f2-)"
  echo "${value//[[:space:]]/}"
}

developmentVersionName="$(get_prop developmentVersionName)"
developmentVersionCode="$(get_prop developmentVersionCode)"
productionVersionName="$(get_prop productionVersionName)"
productionVersionCode="$(get_prop productionVersionCode)"

# Backwards compatibility: fall back to a shared legacy "versionName" if the
# per-track names are not present yet.
legacyVersionName="$(get_prop versionName)"
developmentVersionName="${developmentVersionName:-$legacyVersionName}"
productionVersionName="${productionVersionName:-$legacyVersionName}"

if [[ -z "$developmentVersionName" || -z "$developmentVersionCode" || \
      -z "$productionVersionName" || -z "$productionVersionCode" ]]; then
  echo "ERROR: version.properties is missing one of: developmentVersionName, developmentVersionCode, productionVersionName, productionVersionCode" >&2
  exit 1
fi

# Increment the patch component of a MAJOR.MINOR.PATCH version name.
bump_patch() {
  local name="$1"
  local major minor patch
  IFS='.' read -r major minor patch <<< "$name"
  if [[ -z "${major:-}" || -z "${minor:-}" || -z "${patch:-}" ]]; then
    echo "ERROR: versionName '$name' is not in MAJOR.MINOR.PATCH form" >&2
    exit 1
  fi
  patch=$((patch + 1))
  echo "${major}.${minor}.${patch}"
}

# --- Bump only the selected track ---
if [[ "$BUILD_TYPE" == "development" ]]; then
  developmentVersionName="$(bump_patch "$developmentVersionName")"
  developmentVersionCode=$((developmentVersionCode + 1))
  activeVersionName="$developmentVersionName"
  activeVersionCode="$developmentVersionCode"
else
  productionVersionName="$(bump_patch "$productionVersionName")"
  productionVersionCode=$((productionVersionCode + 1))
  activeVersionName="$productionVersionName"
  activeVersionCode="$productionVersionCode"
fi

# --- Persist (rewrite the file, keeping the documentation header) ---
cat > "$PROPS_FILE" <<EOF
# Centralized version management for Caribou.
#
# developmentVersionName / developmentVersionCode -> bumped ONLY on development builds.
# productionVersionName  / productionVersionCode  -> bumped ONLY on production builds.
#
# The development and production tracks are fully independent: each has its own
# versionName and versionCode counter. This file is updated by
# scripts/bump-version.sh — used by the manual GitHub Actions workflow and by the
# local Gradle tasks (assembleDevelopmentBump / bundleProductionBump). Edit with care.
developmentVersionName=${developmentVersionName}
developmentVersionCode=${developmentVersionCode}
productionVersionName=${productionVersionName}
productionVersionCode=${productionVersionCode}
EOF

echo "versionName=${activeVersionName}"
echo "versionCode=${activeVersionCode}"
echo "buildType=${BUILD_TYPE}"

# Expose values to later GitHub Actions steps.
if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "versionName=${activeVersionName}"
    echo "versionCode=${activeVersionCode}"
    echo "buildType=${BUILD_TYPE}"
  } >> "$GITHUB_OUTPUT"
fi

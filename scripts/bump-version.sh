#!/usr/bin/env bash
#
# Bumps the centralized version numbers in version.properties.
#
# Rules:
#   * versionName            -> patch component incremented on EVERY build.
#   * development build      -> developmentVersionCode incremented by 1.
#   * production build       -> productionVersionCode  incremented by 1.
#   * The two version codes are independent counters.
#
# Usage:
#   scripts/bump-version.sh <development|production> [path/to/version.properties]
#
# Outputs (stdout and, when running in GitHub Actions, $GITHUB_OUTPUT):
#   versionName=<new version name>
#   versionCode=<active version code for the chosen build type>
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

versionName="$(get_prop versionName)"
developmentVersionCode="$(get_prop developmentVersionCode)"
productionVersionCode="$(get_prop productionVersionCode)"

if [[ -z "$versionName" || -z "$developmentVersionCode" || -z "$productionVersionCode" ]]; then
  echo "ERROR: version.properties is missing one of: versionName, developmentVersionCode, productionVersionCode" >&2
  exit 1
fi

# --- Increment versionName patch (semantic MAJOR.MINOR.PATCH) ---
IFS='.' read -r major minor patch <<< "$versionName"
if [[ -z "${major:-}" || -z "${minor:-}" || -z "${patch:-}" ]]; then
  echo "ERROR: versionName '$versionName' is not in MAJOR.MINOR.PATCH form" >&2
  exit 1
fi
patch=$((patch + 1))
newVersionName="${major}.${minor}.${patch}"

# --- Increment the relevant, independent version code ---
if [[ "$BUILD_TYPE" == "development" ]]; then
  developmentVersionCode=$((developmentVersionCode + 1))
  activeVersionCode="$developmentVersionCode"
else
  productionVersionCode=$((productionVersionCode + 1))
  activeVersionCode="$productionVersionCode"
fi

# --- Persist (rewrite the file, keeping the documentation header) ---
cat > "$PROPS_FILE" <<EOF
# Centralized version management for Caribou.
#
# versionName            -> incremented (patch) on EVERY build (development or production).
# developmentVersionCode -> incremented ONLY on development builds.
# productionVersionCode  -> incremented ONLY on production builds.
#
# The two version codes are fully independent counters.
# This file is updated automatically by .github/workflows/build-apk.yml
# (via scripts/bump-version.sh) and committed back to the repository so that
# the next build always continues from the correct version. Edit with care.
versionName=${newVersionName}
developmentVersionCode=${developmentVersionCode}
productionVersionCode=${productionVersionCode}
EOF

echo "versionName=${newVersionName}"
echo "versionCode=${activeVersionCode}"
echo "buildType=${BUILD_TYPE}"

# Expose values to later GitHub Actions steps.
if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "versionName=${newVersionName}"
    echo "versionCode=${activeVersionCode}"
    echo "buildType=${BUILD_TYPE}"
  } >> "$GITHUB_OUTPUT"
fi

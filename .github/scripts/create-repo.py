import json
import os
import hashlib
import sys
import subprocess
import re
from pathlib import Path

PACKAGE_NAME_REGEX = re.compile(r"package: name='([^']+)'")
VERSION_CODE_REGEX = re.compile(r"versionCode='([^']+)'")
VERSION_NAME_REGEX = re.compile(r"versionName='([^']+)'")
NSFW_REGEX = re.compile(r"'tachiyomi.extension.nsfw' value='([^']+)'")
APPLICATION_LABEL_REGEX = re.compile(r"^application-label:'([^']+)'", re.MULTILINE)

SOURCE_ID = 4512562552406354242


def calculate_sha256(file_path):
    sha256_hash = hashlib.sha256()
    with open(file_path, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()


def find_aapt():
    """Locate aapt in the Android SDK."""
    android_home = os.environ.get("ANDROID_HOME") or os.environ.get(
        "ANDROID_SDK_ROOT"
    )
    if android_home:
        for build_tools in sorted(
            (Path(android_home) / "build-tools").iterdir(), reverse=True
        ):
            aapt_path = build_tools / "aapt"
            if aapt_path.exists():
                return str(aapt_path)

    candidates = ["aapt", "aapt.exe"]
    for c in candidates:
        try:
            subprocess.run([c, "--version"], capture_output=True, check=False)
            return c
        except FileNotFoundError:
            pass
    return None


def generate_repo(apk_dir="apk"):
    repo_data = []

    if not os.path.exists(apk_dir):
        print(f"Error: APK directory not found: {apk_dir}")
        sys.exit(1)

    apk_files = [f for f in os.listdir(apk_dir) if f.endswith(".apk")]
    if not apk_files:
        print(f"Error: No APK files found in {apk_dir}")
        sys.exit(1)

    print(f"Found {len(apk_files)} APK(s) in {apk_dir}")

    aapt_path = find_aapt()

    for filename in sorted(apk_files):
        apk_path = os.path.join(apk_dir, filename)
        sha = calculate_sha256(apk_path)

        if aapt_path:
            try:
                badging = subprocess.check_output(
                    [aapt_path, "dump", "--include-meta-data", "badging", apk_path]
                ).decode()

                package_name = PACKAGE_NAME_REGEX.search(badging).group(1)
                version_code = int(VERSION_CODE_REGEX.search(badging).group(1))
                version_name = VERSION_NAME_REGEX.search(badging).group(1)
                app_name = APPLICATION_LABEL_REGEX.search(badging).group(1)
                nsfw = int(NSFW_REGEX.search(badging).group(1))
            except (subprocess.CalledProcessError, AttributeError, IndexError):
                print("Warning: aapt parsing failed, falling back to defaults")
                aapt_path = None

        if not aapt_path:
            print("Warning: aapt not available, using hardcoded values")
            package_name = "eu.kanade.tachiyomi.extension.all.kimcartoon"
            version_code = 1
            version_name = "1.0.0"
            app_name = "KimCartoon"
            nsfw = 0

        lang = "all"
        if "aniyomi-" in filename:
            lang_match = re.search(r"aniyomi-([^.]+)", filename)
            if lang_match:
                lang = lang_match.group(1)

        entry = {
            "name": app_name,
            "pkg": package_name,
            "apk": filename,
            "lang": lang,
            "code": version_code,
            "version": version_name,
            "nsfw": nsfw,
            "sources": [
                {
                    "name": "KimCartoon",
                    "lang": "all",
                    "id": str(SOURCE_ID),
                    "baseUrl": "https://kimcartoon.si",
                }
            ],
        }
        repo_data.append(entry)
        print(f"  Added: {app_name} ({package_name} v{version_name})")

    with open("index.min.json", "w") as f:
        json.dump(repo_data, f, separators=(",", ":"))

    print(f"Generated index.min.json with {len(repo_data)} extension(s)")


if __name__ == "__main__":
    apk_dir = sys.argv[1] if len(sys.argv) > 1 else "apk"
    generate_repo(apk_dir)

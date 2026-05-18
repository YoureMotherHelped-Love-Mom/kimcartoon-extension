import json
import os
import hashlib
import sys


def calculate_sha256(file_path):
    sha256_hash = hashlib.sha256()
    with open(file_path, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()


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

    for filename in sorted(apk_files):
        apk_path = os.path.join(apk_dir, filename)
        sha = calculate_sha256(apk_path)

        entry = {
            "name": "KimCartoon",
            "pkg": "eu.kanade.tachiyomi.extension.all.kimcartoon",
            "apk": filename,
            "lang": "all",
            "versionCode": 1,
            "versionName": "1.0.0",
            "sha256": sha,
        }
        repo_data.append(entry)
        print(f"  Added: {filename} ({sha[:16]}...)")

    with open("index.min.json", "w") as f:
        json.dump(repo_data, f, separators=(",", ":"))

    print(f"Generated index.min.json with {len(repo_data)} extension(s)")


if __name__ == "__main__":
    apk_dir = sys.argv[1] if len(sys.argv) > 1 else "apk"
    generate_repo(apk_dir)
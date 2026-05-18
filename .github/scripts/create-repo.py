import json
import os
import hashlib

def calculate_sha256(file_path):
    sha256_hash = hashlib.sha256()
    with open(file_path, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()

def generate_repo():
    repo_data = []
    apk_dir = "apk"
    
    if not os.path.exists(apk_dir):
        print("Error: No APK directory found at 'repo-output/apk'")
        return

    print(f"Scanning {apk_dir} for APK files...")
    
    for filename in os.listdir(apk_dir):
        if filename.endswith(".apk"):
            apk_path = os.path.join(apk_dir, filename)
            sha = calculate_sha256(apk_path)
            
            # Metadata for the extension
            # Note: This is simplified. Ideally, extract this from the APK using aapt2.
            entry = {
                "name": "KimCartoon",
                "pkg": "keiyoushi.extension.kimcartoon",
                "apk": filename,
                "versionCode": 1,
                "versionName": "1.0.0",
                "sha256": sha
            }
            repo_data.append(entry)
            print(f"Added: {filename} ({sha[:8]})")

    with open("index.min.json", "w") as f:
        json.dump(repo_data, f, separators=(',', ':'))
        
    print("Successfully generated index.min.json")

if __name__ == "__main__":
    generate_repo()
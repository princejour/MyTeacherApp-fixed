import os

manifest_path = "app/src/main/AndroidManifest.xml"
with open(manifest_path, "r") as f:
    content = f.read()

content = content.replace(
    'android:allowBackup="true"',
    'android:allowBackup="true"\n        android:icon="@mipmap/ic_launcher"\n        android:roundIcon="@mipmap/ic_launcher_round"'
)

with open(manifest_path, "w") as f:
    f.write(content)


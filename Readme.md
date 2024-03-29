Android 14 Root Certificate Installation Bypass
=============

Language/[日本語](Readme-ja.md)

## Overview

Android 14 and later make it difficult to install a trusted Root certificate on the system.

- https://httptoolkit.com/blog/android-14-breaks-system-certificate-installation/

Describe the steps to bypass.

## Bypass point

API-34 has been reading certificates from "/apex/com.android.conscrypt/cacerts" in the process of reading system certificates.
However, when the system property is set to "system.certs.enabled", the code is to retrieve certificates from "/system/etc/security/cacerts/".

- https://android-review.googlesource.com/c/platform/prebuilts/fullsdk/sources/+/2704396/1/android-34/android/security/net/config/SystemCertificateSource.java

````java
private static File getDirectory() {
    if ((System.getProperty("system.certs.enabled") != null)
            && (System.getProperty("system.certs.enabled")).equals("true")) {
        return new File(System.getenv("ANDROID_ROOT") + "/etc/security/cacerts");
    }
    File updatable_dir = new File("/apex/com.android.conscrypt/cacerts");
    if (updatable_dir.exists()
            && !(updatable_dir.list().length == 0)) {
        return updatable_dir;
    }
    return new File(System.getenv("ANDROID_ROOT") + "/etc/security/cacerts");
}
````

Use this specification for bypass processing.

## concrete procedure

Script by [Frida](https://frida.re/) to rewrite the system property "system.certs.enabled".

````js
setImmediate(function () {
  console.log("[*] Starting script");
  Java.perform(function () {
    var systemClass = Java.use("java.lang.System");
    systemClass.setProperty("system.certs.enabled","true");
  })
})
````

In this case, the Frida script must be specified at startup.
It is also inconvenient for use with multiple applications.

Android XposedModule has created an always available application.

The created XposedModule app is placed in the "OverrideSysPropModule/app/release" folder.

## procedure (Emulator)

### Install Magisk 

For emulators, install Magisk according to the following procedure.

1. Do a git clone from https://github.com/newbit1/rootAVD

````
git clone https://github.com/newbit1/rootAVD.git
````

2. Start the emulator.
3. Execute the following command from PowerShell on the administration screen to confirm the ADV to be installed.

````sh
.\rootAVD.bat ListAllAVDs

...

Command Examples:
rootAVD.bat
rootAVD.bat ListAllAVDs
rootAVD.bat InstallApps

rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img
rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img FAKEBOOTIMG
rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img DEBUG PATCHFSTAB GetUSBHPmodZ
rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img restore
rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img InstallKernelModules
rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img InstallPrebuiltKernelModules
rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img InstallPrebuiltKernelModules GetUSBHPmodZ PATCHFSTAB DEBUG
````

4. run ADV corresponding to API

````sh
.\rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img FAKEBOOTIMG
````

5. A 60-second wait is performed, which is terminated by pressing the Enter key.

```sh
[!] Temporarily installing Magisk
[*] Detecting current user
[-] Current user 0
[-] Starting Magisk
[*] Install/Patch /sdcard/Download/fakeboot.img and hit Enter when done(max. 60s)
```

6. Download Magisk

- https://github.com/topjohnwu/Magisk/releases

7. Install the latest Magisk

````sh
adb install Magisk.v2x.x.apk
````

8. Select "/sdcard/Download/fakeboot.img" from Select and Patch a File in Magisk Install to create the patch.

9. re-run ADV corresponding to API

````sh
.\rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img FAKEBOOTIMG
````

10. Enable "Zygisk" and "Enforce DenyList".

11. Reboot the system.

### Install Magisk Module

1. Install Magisk Module.

- https://github.com/NVISOsecurity/MagiskTrustUserCerts/releases
- https://github.com/LSPosed/LSPosed/releases (Install zygisk version)

Install as needed

- https://github.com/LSPosed/LSPosed.github.io/releases

### Install Xposed Module

1. Install Xposed Module in the "OverrideSysPropModule" folder.

````sh
cd OverrideSysPropModule\app\release
adb install OverrideSysprop.apk
````

2. Install a Root certificate such as Burp for user certificates.

3. Enable the Module for the application to which you want to apply it.

![OverrideSysProp](images/OverrideSysProp.png)

TIP: After enabling Xposed Module, it may not be recognized properly unless the Android device is rebooted.

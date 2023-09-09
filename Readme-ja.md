# Android 14 の Root証明書インストールバイパス

## 概要

Android 14 では信頼されたRoot証明書へのインストールが困難になっている｡

- https://httptoolkit.com/blog/android-14-breaks-system-certificate-installation/

本制限を回避するための方法を示す。

## 回避概要

証明書を読み込む処理にて、「/apex/com.android.conscrypt/cacerts」から読み込むようになっているがシステムプロパティが「system.certs.enabled」となっている場合は、以前の「/system/etc/security/cacerts/」より証明書を取得するコードになっている。

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

回避するために本挙動を利用します。

## 具体的な方法

「system.certs.enabled」のシステムプロパティを書き換える方法として、Android の XposedModule を作成することで行う。


「OverrideSysPropModule」フォルダに作成した XposedModule をおいている。

## 手順 (Emulator)

エミュレータの場合、以下の手順でMagiskをインストールする。

+ https://github.com/newbit1/rootAVD より git cloneを行う

```
git clone https://github.com/newbit1/rootAVD.git
```
+ エミュレータを起動
+ 管理画面の PowerShell から以下のコマンドを実行

```
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
```

### 最新の Magisk をインストール

+ 最新のMagsiskを以下よりダウンロード

- https://github.com/topjohnwu/Magisk/releases

+ 「rootAVD\Apps」のフォルダにコピーする。
+ 最新の Magisk をインストール

以下のコマンドを実行

```
.\rootAVD.bat system-images\android-34\google_apis_playstore\x86_64\ramdisk.img
```

### Magisk Module をインストール

以下のMagisk Module をインストールする。

- https://github.com/NVISOsecurity/MagiskTrustUserCerts/releases
- https://github.com/LSPosed/LSPosed/releases (zygisk 版をインストール)

必要に応じて以下をインストール

- https://github.com/LSPosed/LSPosed.github.io/releases

### XposedModule Module をインストール

「OverrideSysPropModule」フォルダ内の XposedModule をインストール。

````
cd OverrideSysPropModule\app\release
adb install app-release.apk
````

### Moduleを適用したいアプリに対して有効にする。

![OverrideSysProp](images/OverrideSysProp.png)

TIP:  端末を再起動しないとうまく認識しない場合がある。
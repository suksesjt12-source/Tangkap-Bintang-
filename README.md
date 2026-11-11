# Tangkap Bintang ⭐

Game Android sederhana: ketuk bintang yang jatuh sebanyak mungkin selama 30 detik. Setiap bintang bernilai 10 poin. Rekor terbaik tersimpan di perangkat.

## Membuat APK otomatis dengan GitHub Actions

Proyek ini sudah memiliki workflow di `.github/workflows/build-apk.yml`. Setelah source code diunggah ke repository GitHub:

1. Buka tab **Actions** di repository.
2. Pilih workflow **Build APK Android**.
3. Tunggu sampai proses selesai dengan status hijau **Success**.
4. Buka hasil run tersebut, cari bagian **Artifacts**, lalu unduh `TangkapBintang-debug-apk`.
5. Ekstrak ZIP artifact. File aplikasinya bernama `app-debug.apk`.

Kamu juga bisa menjalankan workflow secara manual dari **Actions → Build APK Android → Run workflow**.

## Upload dari HP Android

1. Unduh lalu ekstrak `TangkapBintang-GitHub.zip` di HP.
2. Buat repository baru di https://github.com/new. Nama contoh: `TangkapBintang`.
3. Pastikan repository dibuat dengan README kosong **tidak dicentang** agar tidak bentrok dengan file proyek.
4. Buka repository lewat Chrome. Jika tombol upload tidak terlihat, aktifkan **Situs desktop** dari menu Chrome.
5. Pilih **Add file → Upload files**. Unggah isi folder proyek yang sudah diekstrak, termasuk folder tersembunyi `.github` beserta file workflow di dalamnya.
6. Tekan **Commit changes**.
7. Buka tab **Actions** dan ikuti langkah build APK di atas.

> Penting: unggah isi folder proyek, bukan file ZIP-nya saja. Workflow harus berada tepat di `.github/workflows/build-apk.yml` pada akar repository.

## Info teknis

- Bahasa: Kotlin
- Minimum Android: Android 6.0 (API 23)
- Target SDK: API 34
- Build: Gradle 8.7 + Android Gradle Plugin 8.6.1
- Paket aplikasi: `com.example.tangkapbintang`

APK yang dibuat workflow adalah **debug APK** untuk instalasi dan pengujian, bukan rilis Play Store.

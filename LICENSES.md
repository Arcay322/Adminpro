Adminpro - Licenses and Third-Party Notices

This document summarizes the project's decisions about third-party libraries and licensing relevant to building and publishing the app.

Key decisions
- PDF generation: iText (AGPL) was previously present but removed to avoid AGPL viral obligations. The project now uses Android's built-in android.graphics.pdf.PdfDocument for PDF generation, which does not introduce a license conflict.

Notable dependencies (non-exhaustive)
- AndroidX, Jetpack Compose, Room, WorkManager, Coil, MPAndroidChart: these dependencies are commonly used in Android apps. Check each library's license in their respective Maven artifacts if you need to include full license text.

Recommendations before publishing to Google Play
- Review all dependencies and include their license texts if required by each library's terms.
- Prepare a privacy policy URL if the app collects or shares user data; add it to Play Console.
- Ensure you use a release keystore and sign your APK/AAB. Keep the keystore secure.
- Remove unused permissions (already removed WRITE_EXTERNAL_STORAGE) and only request runtime permissions when needed.

If you want, I can:
- Generate a `third_party_licenses/` folder with each dependency license text (automated via Gradle task) and wire it into an About screen.
- Add a short `README.md` describing the license choices and Play Store checklist items.

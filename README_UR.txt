MIRAL AI - Android Starter (Version 0.1)

یہ ایک ابتدائی Android Studio project ہے، مکمل AI assistant یا تیار APK نہیں۔

ابھی شامل:
- Urdu voice input کی کوشش (ur-PK)
- Text input
- Text-to-speech جواب
- سلام، وقت، اور Settings کھولنے جیسے محدود commands
- Microphone permission صرف voice button دبانے پر

چلانے کا طریقہ:
1. Android Studio میں یہ project folder کھولیں۔
2. Gradle sync مکمل ہونے دیں۔
3. JDK 17 استعمال کریں۔
4. اپنے Android phone پر USB debugging کے ذریعے Run کریں یا APK build کریں۔
5. پہلی بار microphone permission دیں۔

اہم:
- Urdu speech recognition اور Urdu TTS کی دستیابی فون کے speech service اور installed voice پر منحصر ہے۔
- ابھی OpenAI/Gemini API شامل نہیں۔ اگلے مرحلے میں secure backend کے ذریعے AI شامل کیا جائے گا۔
- API key کبھی Android app کے اندر hard-code نہ کریں۔
- WhatsApp، calls، notifications، accessibility اور always-on listening ابھی شامل نہیں؛ ہر sensitive action کے لیے واضح اجازت/confirmation درکار ہوگی۔

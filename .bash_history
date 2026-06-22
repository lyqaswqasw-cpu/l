# 1️⃣ تنظيف المجلدات تماماً لبدء مشروع ali2 الجديد بنظافة
rm -rf ~/project_ali2 ~/ali2_tmp
mkdir -p ~/project_ali2 ~/ali2_tmp
# 2️⃣ تحديد مسار ملف الـ zip في جهازك بشكل ذكي
ZIP_PATH=""
for path in "/storage/emulated/0/Download" "/sdcard/Download"; do     if [ -f "$path/ali2.zip" ]; then ZIP_PATH="$path/ali2.zip";     elif [ -f "$path/ALI2.zip" ]; then ZIP_PATH="$path/ALI2.zip";     elif [ -f "$path/Ali2.zip" ]; then ZIP_PATH="$path/Ali2.zip";     fi; done
if [ -z "$ZIP_PATH" ]; then     exit 1; fi
# 3️⃣ فك الضغط في المجلد المؤقت
unzip -q -o "$ZIP_PATH" -d ~/ali2_tmp
# 4️⃣ البحث عن مجلد المشروع الحقيقي ونقله لجذر العمل
cd ~/ali2_tmp
REAL_ROOT=$(find . -name "settings.gradle" -o -name "settings.gradle.kts" -o -name "build.gradle" | grep -v "/app/" | head -n 1 | xargs dirname 2>/dev/null)
if [ -z "$REAL_ROOT" ] || [ "$REAL_ROOT" = "." ]; then     REAL_ROOT=$(find . -name "*gradle*" | head -n 1 | xargs dirname 2>/dev/null); fi
cd "$REAL_ROOT"
cp -r * ~/project_ali2/ 2>/dev/null
cp -r .* ~/project_ali2/ 2>/dev/null
# 5️⃣ إصلاح ذكي لملف الكتالوج لضمان حل مشكلة Unresolved reference 'libs'
cd ~/ali2_tmp
TOML_FIND=$(find . -name "libs.versions.toml" | head -n 1)
mkdir -p ~/project_ali2/gradle
if [ -n "$TOML_FIND" ]; then     cp "$TOML_FIND" ~/project_ali2/gradle/libs.versions.toml;     echo "✅ تم العثور على ملف libs.versions.toml وتثبيته في مساره الصحيح."; fi
# 6️⃣ الانتقال للمجلد الرئيسي وحقن ملفات البناء والـ Wrapper المفقودة
cd ~/project_ali2
rm -rf ~/ali2_tmp
# تحميل ملف البناء السحابي لجيتهاب الخاص بك
mkdir -p .github/workflows
curl -sL https://raw.githubusercontent.com/ld97jsp-sudo/16/main/.github/workflows/build.yml -o .github/workflows/build.yml
# تحميل الـ Gradle Wrapper الرسمي لحل مشكلة gradlew نهائياً
echo "⏳ جاري جلب وملء ملفات الـ Gradle المفقودة للتطبيق الجديد..."
curl -sL https://raw.githubusercontent.com/android/architecture-samples/main/gradlew -o gradlew
chmod +x gradlew
mkdir -p gradle/wrapper
curl -sL https://raw.githubusercontent.com/android/architecture-samples/main/gradle/wrapper/gradle-wrapper.properties -o gradle/wrapper/gradle-wrapper.properties
curl -sL https://raw.githubusercontent.com/android/architecture-samples/main/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar
# توليد مفتاح التوقيع الـ Keystore
keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null
# 7️⃣ تهيئة الـ Git والرفع الإجباري المباشر لمستودع ali2
git init
git config --global user.email "ali@example.com"
git config --global user.name "ld97jsp-sudo"
git branch -M main
git remote add origin https://github.com/ld97jsp-sudo/ali2.git
git add .
git add -f debug.keystore gradle/libs.versions.toml gradlew gradle/wrapper/* 2>/dev/null
git commit -m "Initial commit for ali2 app with complete gradle fixes"
git push -u origin main --force
# 1️⃣ تنظيف المجلدات تماماً لبدء مشروع ali3 الجديد بنظافة كاملة
rm -rf ~/project_ali3 ~/ali3_tmp
mkdir -p ~/project_ali3 ~/ali3_tmp
# 2️⃣ تحديد مسار ملف الـ zip في جهازك بشكل ذكي (بمختلف الحروف)
ZIP_PATH=""
for path in "/storage/emulated/0/Download" "/sdcard/Download"; do     if [ -f "$path/ali3.zip" ]; then ZIP_PATH="$path/ali3.zip";     elif [ -f "$path/ALI3.zip" ]; then ZIP_PATH="$path/ALI3.zip";     elif [ -f "$path/Ali3.zip" ]; then ZIP_PATH="$path/Ali3.zip";     fi; done
if [ -z "$ZIP_PATH" ]; then     echo -e "\e[33m💡 تأكد من نقل الملف إلى وحدة التخزين الداخلية داخل مجلد Download وتسميته ali3.zip\e[0m\n";     exit 1; fi
# 1️⃣ تنظيف المجلدات تماماً لبدء مشروع ali3 الجديد بنظافة كاملة
rm -rf ~/project_ali3 ~/ali3_tmp
mkdir -p ~/project_ali3 ~/ali3_tmp
# 2️⃣ تحديد مسار ملف الـ zip في جهازك بشكل ذكي (بمختلف الحروف)
ZIP_PATH=""
for path in "/storage/emulated/0/Download" "/sdcard/Download"; do     if [ -f "$path/ali3.zip" ]; then ZIP_PATH="$path/ali3.zip";     elif [ -f "$path/ALI3.zip" ]; then ZIP_PATH="$path/ALI3.zip";     elif [ -f "$path/Ali3.zip" ]; then ZIP_PATH="$path/Ali3.zip";     fi; done
if [ -z "$ZIP_PATH" ]; then     echo -e "\e[33m💡 تأكد من نقل الملف إلى وحدة التخزين الداخلية داخل مجلد Download وتسميته ali3.zip\e[0m\n";     exit 1; fi

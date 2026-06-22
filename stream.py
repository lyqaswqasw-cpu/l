import subprocess
import sys

# روابط البث الخاصة بك
INPUT_URL = "http://live.lynxiptv.xyz:80/live/171348492752/5S6HGsea3j/255244.m3u8"
OUTPUT_URL = "rtmps://dc4-1.rtmp.t.me/s/3968168673:lMm_xYiaOQEvk0Ye-nKt4A"

# مسار الخط الافتراضي في نظام أندرويد
FONT_PATH = "/system/fonts/Roboto-Regular.ttf"

# الفلاتر المشتركة:
# 1. scale=1280:720 -> لتمديد الفيديو وإجبارة على ملء الشاشة بالكامل وإلغاء الأطراف السوداء
# 2. drawtext -> لطباعة العلامة المائية في أسفل اليمين بعد التوسيع
COMBINED_FILTERS = f"scale=1280:720,drawtext=text='Loop Live':x=w-tw-20:y=h-th-20:fontsize=28:fontcolor=white:box=1:boxcolor=black@0.4:fontfile={FONT_PATH}"

# أمر FFmpeg المعدل لملء الشاشة
ffmpeg_cmd = [
    "ffmpeg",
    "-re",
    "-i", INPUT_URL,
    "-vf", COMBINED_FILTERS,    # تطبيق فلاتر الأبعاد والعلامة المائية معاً
    "-c:v", "libx264",          # ترميز الفيديو المطلوب لتعديل الأبعاد
    "-preset", "ultrafast",     # أسرع نمط لتقليل الضغط على الهاتف ومنع التقطيع
    "-tune", "zerolatency",     # لضمان عدم حدوث تأخير في البث
    "-c:a", "copy",             # نسخ الصوت كما هو لتوفير الطاقة
    "-f", "flv",
    OUTPUT_URL
]

print("=" * 50)
print("  🔥 جاري بدء البث المباشر [ملء الشاشة بالكامل]...  ")
print("  [ الشعار مفعّل: Loop Live - الأطراف السوداء تم إلغاؤها ]  ")
print("=" * 50)

try:
    # تشغيل الأمر ومتابعة المخرجات فورياً
    process = subprocess.Popen(ffmpeg_cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    
    # عرض حالة البث في تيرمكس
    for line in process.stdout:
        print(line, end="")
        
except KeyboardInterrupt:
    print("\n\n[-] تم إيقاف البث بواسطة المستخدم.")
except FileNotFoundError:
    print("\n[!] خطأ: أداة FFmpeg غير مثبتة في تيرمكس!")
except Exception as e:
    print(f"\n[!] حدث خطأ غير متوقع: {e}")


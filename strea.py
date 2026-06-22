import subprocess
import time
import sys

# روابط البث
INPUT_URL = "http://live.lynxiptv.xyz:80/live/171348492752/5S6HGsea3j/255244.m3u8"
OUTPUT_URL = "rtmps://dc4-1.rtmp.t.me/s/3968168673:lMm_xYiaOQEvk0Ye-nKt4A"

# إعدادات متطورة لـ FFmpeg لضمان استقرار البث ومنع الانقطاع
ffmpeg_cmd = [
    "ffmpeg",
    "-hide_banner",
    "-loglevel", "warning", # تقليل النصوص الزائدة لعرض الأخطاء المهمة فقط
    "-headers", "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\r\n", # لتجنب حظر السيرفر
    "-reconnect", "1",
    "-reconnect_at_eof", "1",
    "-reconnect_streamed", "1",
    "-reconnect_delay_max", "5", # محاولة إعادة الاتصال خلال 5 ثوانٍ لو انقطع السيرفر
    "-re",
    "-i", INPUT_URL,
    "-c:v", "copy", # نسخ الفيديو بدون استهلاك المعالج
    "-c:a", "copy", # نسخ الصوت
    "-f", "flv",
    "-flvflags", "no_duration_filesize",
    OUTPUT_URL
]

def start_streaming():
    attempt = 1
    while True:
        print("\n" + "="*50)
        print(f"  [+] محاولة تشغيل البث رقم ({attempt}) ...")
        print("="*50)
        
        try:
            # تشغيل البث ومراقبة العملية
            process = subprocess.Popen(ffmpeg_cmd)
            process.wait() # الانتظار لحين انتهاء العملية أو حدوث خطأ
            
            # إذا انتهت العملية بدون خطأ أو بسبب انقطاع الرابط
            print("\n[!] تحذير: انقطع الاتصال برابط البث أو حدثت مشكلة في الشبكة.")
            
        except KeyboardInterrupt:
            print("\n\n[-] تم إيقاف السكريبت بالكامل بواسطة المستخدم.")
            sys.exit(0)
        except Exception as e:
            print(f"\n[!] حدث خطأ غير متوقع: {e}")
        
        # الانتظار 3 ثوانٍ قبل إعادة المحاولة تلقائياً لتجنب الضغط
        print("[*] جاري إعادة الاتصال تلقائياً خلال 3 ثوانٍ... لا تفعل شيء.")
        time.sleep(3)
        attempt += 1

if __name__ == "__main__":
    print("="*50)
    print("  🔥 نظام البث الاحترافي والمستمر لـ تليجرام يعمل الآن 🔥")
    print("  [ ميزة إعادة الاتصال التلقائي وتخطي الحظر مفعّلة ]")
    print("="*50)
    start_streaming()


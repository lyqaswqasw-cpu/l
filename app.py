from flask import Flask, render_template_string

app = Flask(__name__)

# 🔴 تم إضافة رابط البث الخاص بك هنا بنجاح
M3U8_URL = "http://live.lynxiptv.xyz:80/live/171348492752/5S6HGsea3j/255244.m3u8"

# 🔴 اكتب اسم قناتك هنا ليظهر كعلامة مائية (يمكنك تعديله في أي وقت)
CHANNEL_NAME = "MY LIVE" 

# تصميم الصفحة والمشغل الاحترافي (HTML5 + Plyr.js + HLS.js)
HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>البث المباشر الخاص بك</title>
    
    <!-- ستايل المشغل الشهير Plyr CSS -->
    <link rel="stylesheet" href="https://cdn.plyr.io/3.7.8/plyr.css" />
    
    <style>
        body {
            margin: 0;
            padding: 0;
            background-color: #0d0e12;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            overflow: hidden;
        }

        /* حاوية المشغل */
        .player-container {
            position: relative;
            width: 90%;
            max-width: 850px;
            border-radius: 16px;
            overflow: hidden;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.7);
            border: 1px solid rgba(255, 255, 255, 0.08);
        }

        video {
            width: 100%;
            display: block;
        }

        /* تصميم العلامة المائية الزجاجية النيون */
        .watermark {
            position: absolute;
            top: 15px;
            right: 15px;
            background: rgba(255, 255, 255, 0.07);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            color: #00ffb3; /* لون نيوني أخضر مميز */
            padding: 6px 14px;
            border-radius: 30px;
            font-size: 13px;
            font-weight: bold;
            letter-spacing: 1px;
            text-shadow: 0 0 8px rgba(0, 255, 179, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.15);
            pointer-events: none; /* تمنع التداخل مع النقر على الشاشة */
            z-index: 10;
        }
    </style>
</head>
<body>

<div class="player-container">
    <!-- العلامة المائية -->
    <div class="watermark">{{ channel_name }}</div>
    
    <!-- مشغل الفيديو -->
    <video id="video-player" controls crossorigin playsinline></video>
</div>

<!-- مكتبة HLS.js لتشغيل روابط m3u8 على جميع المتصفحات -->
<script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
<!-- مكتبة Plyr JS للمظهر الجميل وازرار التحكم -->
<script src="https://cdn.plyr.io/3.7.8/plyr.polyfilled.js"></script>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const video = document.getElementById('video-player');
        const streamUrl = "{{ m3u8_url }}";

        // تخصيص أزرار التحكم في المشغل
        const player = new Plyr(video, {
            controls: ['play-large', 'play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'],
            tooltips: { controls: true, seek: true }
        });

        // تشغيل البث المباشر
        if (Hls.isSupported()) {
            const hls = new Hls();
            hls.loadSource(streamUrl);
            hls.attachMedia(video);
            window.hls = hls;
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            // دعم متصفحات الأجهزة الذكية والأيفون تلقائياً
            video.src = streamUrl;
        }
    });
</script>

</body>
</html>
"""

@app.route('/')
def home():
    return render_template_string(HTML_TEMPLATE, m3u8_url=M3U8_URL, channel_name=CHANNEL_NAME)

if __name__ == '__main__':
    # تشغيل السيرفر المحلي
    app.run(host='0.0.0.0', port=5000, debug=True)


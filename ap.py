import requests
from flask import Flask, render_template_string, Response, stream_with_context

app = Flask(__name__)

# إنشاء جلسة اتصال ثابتة لزيادة سرعة جلب البث بشكل خارق وثبات الاتصال
session = requests.Session()

# معرّف التيلجرام الخاص بك للعلامة المائية
TELEGRAM_USER = "@jdj_q" 

# رابط البث الداخلي عبر البروكسي الذكي
STREAM_PROXY_URL = "/proxy/live/171348492752/5S6HGsea3j/255244.m3u8"

HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Premium Immersive Player</title>
    
    <style>
        * {
            box-sizing: border-box;
            user-select: none;
        }
        body {
            margin: 0;
            padding: 0;
            background-color: #060709;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            overflow: hidden;
        }

        /* حاوية المشغل الفخمة مع الإضاءة الخلفية المحيطية */
        .premium-player-wrapper {
            position: relative;
            width: 95%;
            max-width: 900px;
            aspect-ratio: 16 / 9;
            background: #000;
            border-radius: 24px;
            overflow: hidden;
            box-shadow: 0 0 50px rgba(0, 240, 255, 0.15), 0 30px 60px rgba(0, 0, 0, 0.8);
            border: 1px solid rgba(255, 255, 255, 0.05);
        }

        video {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }

        /* واجهة التحكم المخصصة وغير المطروقة */
        .custom-controls-overlay {
            position: absolute;
            inset: 0;
            background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0) 40%, rgba(0,0,0,0) 100%);
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
            padding: 20px 25px;
            opacity: 0;
            transition: opacity 0.4s cubic-bezier(0.25, 1, 0.5, 1);
            z-index: 5;
        }

        .premium-player-wrapper:hover .custom-controls-overlay {
            opacity: 1;
        }

        /* شريط الأدوات السفلي الزجاجي النيوني */
        .controls-bar {
            background: rgba(15, 18, 26, 0.5);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.08);
            border-radius: 16px;
            padding: 12px 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        }

        .control-group {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        /* تصميم أزرار التحكم */
        .btn-control {
            background: none;
            border: none;
            color: #e2e8f0;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s ease;
            padding: 6px;
            border-radius: 50%;
        }
        .btn-control:hover {
            color: #00f0ff;
            background: rgba(255, 255, 255, 0.05);
            transform: scale(1.08);
        }
        .btn-control svg {
            width: 22px;
            height: 22px;
            fill: currentColor;
        }

        /* كبسولة العلامة المائية للتيلجرام الاحترافية (في الأسفل) */
        .telegram-watermark {
            position: absolute;
            bottom: 85px;
            left: 25px;
            display: flex;
            align-items: center;
            gap: 8px;
            background: rgba(0, 136, 204, 0.12);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(0, 136, 204, 0.35);
            padding: 6px 14px;
            border-radius: 30px;
            color: #ffffff;
            font-size: 13px;
            font-weight: 700;
            letter-spacing: 0.5px;
            box-shadow: 0 4px 15px rgba(0, 136, 204, 0.2);
            transition: opacity 0.4s ease;
            pointer-events: none;
            z-index: 10;
        }
        
        /* جعل العلامة المائية تختفي مع عناصر التحكم بشكل جزئي وتظل خفيفة لحماية المحتوى */
        .premium-player-wrapper:not(:hover) .telegram-watermark {
            opacity: 0.5; /* تظل ظاهرة بنسبة 50% لحماية الحقوق حتى عند اختفاء الأزرار */
        }

        /* خط الصوت الذكي */
        .volume-container {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .volume-slider {
            -webkit-appearance: none;
            width: 70px;
            height: 4px;
            border-radius: 2px;
            background: rgba(255,255,255,0.2);
            outline: none;
            transition: width 0.3s ease;
        }
        .volume-slider::-webkit-slider-thumb {
            -webkit-appearance: none;
            width: 12px;
            height: 12px;
            border-radius: 50%;
            background: #00f0ff;
            cursor: pointer;
            box-shadow: 0 0 8px #00f0ff;
        }

        /* مؤشر حالة البث المباشر المضيء */
        .live-badge {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 11px;
            font-weight: bold;
            color: #ff3b30;
            background: rgba(255, 59, 48, 0.15);
            padding: 4px 10px;
            border-radius: 8px;
            border: 1px solid rgba(255, 59, 48, 0.3);
            letter-spacing: 1px;
        }
        .live-dot {
            width: 6px;
            height: 6px;
            background-color: #ff3b30;
            border-radius: 50%;
            animation: pulse 1.5s infinite;
        }

        @keyframes pulse {
            0% { transform: scale(0.9); opacity: 0.6; }
            50% { transform: scale(1.2); opacity: 1; box-shadow: 0 0 8px #ff3b30; }
            100% { transform: scale(0.9); opacity: 0.6; }
        }
    </style>
</head>
<body>

<div class="premium-player-wrapper" id="player-container">
    
    <!-- ✈️ العلامة المائية للتيلجرام بالأسفل -->
    <div class="telegram-watermark">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="#0088cc">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69.01-.03.01-.14-.07-.2-.08-.06-.19-.04-.27-.02-.11.02-1.93 1.23-5.46 3.62-.51.35-.98.53-1.39.51-.46-.01-1.35-.26-2.01-.48-.81-.27-1.46-.42-1.4-.88.03-.24.37-.49 1.03-.74 4.04-1.76 6.74-2.92 8.09-3.48 3.85-1.6 4.64-1.88 5.17-1.89.11 0 .37.03.54.17.14.12.18.28.2.45-.02.07-.02.13-.03.19z"/>
        </svg>
        <span>{{ telegram_user }}</span>
    </div>

    <!-- مشغل الفيديو الخام السريع -->
    <video id="main-video" playsinline crossorigin="anonymous" autoplay></video>

    <!-- واجهة التحكم الزجاجية المبتكرة -->
    <div class="custom-controls-overlay">
        <div class="controls-bar">
            <div class="control-group">
                <button class="btn-control" id="play-btn" title="تشغيل/إيقاف">
                    <svg viewBox="0 0 24 24" id="play-icon"><path d="M8 5v14l11-7z"/></svg>
                </button>
                
                <div class="volume-container">
                    <button class="btn-control" id="mute-btn">
                        <svg viewBox="0 0 24 24" id="volume-icon"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/></svg>
                    </button>
                    <input type="range" class="volume-slider" id="volume-range" min="0" max="1" step="0.05" value="1">
                </div>
            </div>

            <div class="control-group">
                <div class="live-badge">
                    <div class="live-dot"></div>
                    <span>مباشر</span>
                </div>
                <button class="btn-control" id="fullscreen-btn" title="ملء الشاشة">
                    <svg viewBox="0 0 24 24"><path d="M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z"/></svg>
                </button>
            </div>
        </div>
    </div>
</div>

<!-- محرك البث فائق السرعة HLS.js -->
<script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>

<script>
    const video = document.getElementById('main-video');
    const playBtn = document.getElementById('play-btn');
    const playIcon = document.getElementById('play-icon');
    const muteBtn = document.getElementById('mute-btn');
    const volumeIcon = document.getElementById('volume-icon');
    const volumeRange = document.getElementById('volume-range');
    const fullscreenBtn = document.getElementById('fullscreen-btn');
    const container = document.getElementById('player-container');
    const streamUrl = "{{ m3u8_url }}";

    // 🚀 تهيئة محرك البث بأقصى إعدادات السرعة وتقليل زمن التأخير (Low Latency)
    if (Hls.isSupported()) {
        const hls = new Hls({
            enableWorker: true,
            lowLatencyMode: true,
            maxBufferSize: 0,
            maxBufferLength: 8,
            liveSyncDurationCount: 2,
            liveMaxLatencyDurationCount: 4
        });
        hls.loadSource(streamUrl);
        hls.attachMedia(video);
    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
        video.src = streamUrl;
    }

    // نظام التحكم المخصص
    playBtn.addEventListener('click', () => {
        if (video.paused) {
            video.play();
            playIcon.innerHTML = '<path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>';
        } else {
            video.pause();
            playIcon.innerHTML = '<path d="M8 5v14l11-7z"/>';
        }
    });

    muteBtn.addEventListener('click', () => {
        video.muted = !video.muted;
        updateVolumeUI();
    });

    volumeRange.addEventListener('input', (e) => {
        video.volume = e.target.value;
        video.muted = (video.volume === 0);
        updateVolumeUI();
    });

    function updateVolumeUI() {
        if (video.muted || video.volume === 0) {
            volumeIcon.innerHTML = '<path d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.21.05-.42.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z"/>';
        } else {
            volumeIcon.innerHTML = '<path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>';
        }
    }

    fullscreenBtn.addEventListener('click', () => {
        if (!document.fullscreenElement) {
            container.requestFullscreen().catch(err => alert(err.message));
        } else {
            document.exitFullscreen();
        }
    });

    // تشغيل تلقائي صامت للتوافق التام مع سياسات المتصفحات العالمية بدون حجب
    window.addEventListener('DOMContentLoaded', () => {
        video.muted = true;
        volumeRange.value = 0;
        updateVolumeUI();
        video.play().catch(() => {});
    });
</script>
</body>
</html>
"""

@app.route('/')
def home():
    return render_template_string(HTML_TEMPLATE, m3u8_url=STREAM_PROXY_URL, telegram_user=TELEGRAM_USER)

# 🛠️ البروكسي العملاق لكسر الحجب واستخدام ميزة الـ Session لضمان أقصى سرعة
@app.route('/proxy/live/<path:subpath>')
def proxy_stream(subpath):
    target_url = f"http://live.lynxiptv.xyz:80/live/{subpath}"
    
    headers = {
        "User-Agent": "VLC/3.0.18 LibVLC/3.0.18",
        "Accept": "*/*",
        "Connection": "keep-alive"
    }
    
    try:
        # استخدام الـ session هنا هو السر وراء السرعة الفائقة وتخطي قيود البث لعدد كبير من المشاهدين
        req = session.get(target_url, headers=headers, stream=True, timeout=8)
        
        @stream_with_context
        def generate():
            for chunk in req.iter_content(chunk_size=65536): # تكبير حجم الـ Chunk لتدفق أسرع
                yield chunk
        
        response = Response(generate(), content_type=req.headers.get('content-type'))
        response.headers.add('Access-Control-Allow-Origin', '*')
        return response
    except Exception as e:
        return str(e), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)


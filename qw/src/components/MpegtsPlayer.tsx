import React, { useEffect, useRef, useState } from "react";
import mpegts from "mpegts.js";
import { 
  Play, 
  Pause, 
  Volume2, 
  VolumeX, 
  Maximize, 
  Minimize, 
  RefreshCw, 
  Tv, 
  Maximize2, 
  Settings, 
  AlertTriangle 
} from "lucide-react";

interface MpegtsPlayerProps {
  streamUrl: string;
  channelName: string;
  channelIcon?: string;
}

type AspectRatioMode = "original" | "fill" | "16-9" | "4-3" | "cover";

export default function MpegtsPlayer({ streamUrl, channelName, channelIcon }: MpegtsPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const playerRef = useRef<mpegts.Player | null>(null);

  // States
  const [isPlaying, setIsPlaying] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [volume, setVolume] = useState(0.8);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isPlayingError, setIsPlayingError] = useState(false);
  const [aspectMode, setAspectMode] = useState<AspectRatioMode>("original");
  const [showAspectMenu, setShowAspectMenu] = useState(false);
  const [connectionAttempts, setConnectionAttempts] = useState(0);
  const [showControls, setShowControls] = useState(true);
  const [flashIcon, setFlashIcon] = useState<"play" | "pause" | null>(null);
  
  const controlsTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const flashTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const lastClickRef = useRef<number>(0);

  const triggerShowControls = () => {
    setShowControls(true);
    if (controlsTimeoutRef.current) {
      clearTimeout(controlsTimeoutRef.current);
    }
    if (isPlaying) {
      controlsTimeoutRef.current = setTimeout(() => {
        setIsPlaying((currentPlay) => {
          if (currentPlay) {
            setShowControls(false);
          }
          return currentPlay;
        });
      }, 4000);
    }
  };

  const triggerFlashIcon = (action: "play" | "pause") => {
    setFlashIcon(action);
    if (flashTimeoutRef.current) {
      clearTimeout(flashTimeoutRef.current);
    }
    flashTimeoutRef.current = setTimeout(() => {
      setFlashIcon(null);
    }, 500);
  };

  useEffect(() => {
    triggerShowControls();
    return () => {
      if (controlsTimeoutRef.current) {
        clearTimeout(controlsTimeoutRef.current);
      }
    };
  }, [isPlaying]);

  const handleContainerMouseMove = () => {
    triggerShowControls();
  };

  const handleContainerClick = () => {
    // Show controls when clicking container or background safely
    triggerShowControls();
  };

  const handleVideoClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    const now = Date.now();
    const delay = now - lastClickRef.current;
    
    if (delay < 300) {
      // Double tap -> Play / Pause trigger
      handlePlayPause();
      triggerShowControls();
    } else {
      // Single tap -> Toggle controls display simply
      setShowControls((prev) => !prev);
      if (!showControls) {
        triggerShowControls();
      }
    }
    lastClickRef.current = now;
  };

  // Re-initialize player when streamUrl changes
  useEffect(() => {
    setIsLoading(true);
    setIsPlayingError(false);
    setIsPlaying(false);
    
    // Destroy previous player instance if any
    cleanUpPlayer();

    const videoElement = videoRef.current;
    if (!videoElement || !streamUrl) return;

    // Check if mpegts mse is supported
    if (!mpegts.getFeatureList().mseLivePlayback) {
      console.warn("MPEG-TS Live playback via Media Source Extensions is not supported in this browser.");
      setIsPlayingError(true);
      setIsLoading(false);
      return;
    }

    try {
      const player = mpegts.createPlayer(
        {
          type: "mse",
          isLive: true,
          url: streamUrl,
          cors: true,
        },
        {
          enableStashBuffer: false,
          stashInitialSize: 128,
          lazyLoad: false,
        }
      );

      playerRef.current = player;
      player.attachMediaElement(videoElement);
      player.load();

      // Handle events
      player.on(mpegts.Events.ERROR, (type, detail) => {
        console.error("mpegts player error:", type, detail);
        setIsPlayingError(true);
        setIsLoading(false);
      });

      player.on(mpegts.Events.LOADING_COMPLETE, () => {
        setIsLoading(false);
      });

      // Auto-play action
      const playPromise = player.play();
      if (playPromise && typeof playPromise.then === "function") {
        playPromise
          .then(() => {
            setIsPlaying(true);
            setIsLoading(false);
          })
          .catch((err) => {
            console.warn("Playback prevented or error:", err);
            // Some browsers require interaction, so let browser handle pause
            setIsPlaying(false);
            setIsLoading(false);
          });
      } else {
        setIsPlaying(true);
        setIsLoading(false);
      }

      // Sync initial volume
      videoElement.volume = volume;
      videoElement.muted = isMuted;

    } catch (e) {
      console.error("Exception playing stream via mpegts.js", e);
      setIsPlayingError(true);
      setIsLoading(false);
    }

    return () => {
      cleanUpPlayer();
    };
  }, [streamUrl, connectionAttempts]);

  // Player cleanup function
  const cleanUpPlayer = () => {
    if (playerRef.current) {
      try {
        playerRef.current.pause();
        playerRef.current.unload();
        playerRef.current.detachMediaElement();
        playerRef.current.destroy();
      } catch (err) {
        console.warn("Error cleaning up player", err);
      }
      playerRef.current = null;
    }
  };

  // Toggle Play/Pause
  const handlePlayPause = () => {
    const video = videoRef.current;
    const player = playerRef.current;
    if (!video || !player) return;

    if (isPlaying) {
      player.pause();
      setIsPlaying(false);
      triggerFlashIcon("pause");
    } else {
      const p = player.play();
      if (p && typeof p.then === "function") {
        p.then(() => {
          setIsPlaying(true);
          triggerFlashIcon("play");
        }).catch(() => setIsPlaying(false));
      } else {
        setIsPlaying(true);
        triggerFlashIcon("play");
      }
    }
  };

  // Toggle Mute
  const handleMuteToggle = () => {
    const video = videoRef.current;
    if (!video) return;
    const nextMuted = !isMuted;
    video.muted = nextMuted;
    setIsMuted(nextMuted);
  };

  // Volume slider change
  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    const video = videoRef.current;
    if (video) {
      video.volume = val;
      video.muted = val === 0;
      setIsMuted(val === 0);
    }
  };

  // Reload action
  const handleReload = () => {
    setConnectionAttempts(prev => prev + 1);
  };

  // Fullscreen handlers
  const toggleFullscreen = () => {
    const container = containerRef.current;
    if (!container) return;

    if (!document.fullscreenElement) {
      container.requestFullscreen()
        .then(() => {
          setIsFullscreen(true);
          // Try to lock screen to landscape on compatible devices (mobiles/tablets)
          const orientation = window.screen && window.screen.orientation;
          if (orientation && typeof (orientation as any).lock === "function") {
            (orientation as any).lock("landscape").catch((err: any) => {
              console.warn("Landscape lock not available/supported:", err);
            });
          }
        })
        .catch(err => console.error("Error enabling fullscreen", err));
    } else {
      document.exitFullscreen()
        .then(() => {
          setIsFullscreen(false);
          // Unlock rotation lock
          const orientation = window.screen && window.screen.orientation;
          if (orientation && typeof (orientation as any).unlock === "function") {
            (orientation as any).unlock();
          }
        });
    }
  };

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };

    document.addEventListener("fullscreenchange", handleFullscreenChange);
    return () => {
      document.removeEventListener("fullscreenchange", handleFullscreenChange);
    };
  }, []);

  // Map aspect ratio mode to tailwind styles on the video tag
  const getVideoStyles = (): React.CSSProperties => {
    switch (aspectMode) {
      case "fill":
        return { objectFit: "fill", width: "100%", height: "100%" };
      case "16-9":
        return { objectFit: "fill", width: "100%", aspectRatio: "16/9" };
      case "4-3":
        return { objectFit: "fill", width: "100%", aspectRatio: "4/3", maxHeight: "100%" };
      case "cover":
        return { objectFit: "cover", width: "100%", height: "100%" };
      case "original":
      default:
        return { objectFit: "contain", width: "100%", height: "100%" };
    }
  };

  // Sizing mode title helper
  const getAspectTitleString = (mode: AspectRatioMode) => {
    switch (mode) {
      case "original": return "الحجم الأصلي";
      case "fill": return "تعبئة الشاشة (تمغيط)";
      case "16-9": return "مقاس أبعاد (16:9)";
      case "4-3": return "مقاس أبعاد (4:3)";
      case "cover": return "تغطية كاملة (اقتصاص)";
    }
  };

  return (
    <div 
      id="video-player-root"
      ref={containerRef} 
      onMouseMove={handleContainerMouseMove}
      onClick={handleContainerClick}
      className={`relative w-full bg-black select-none group flex flex-col justify-center items-center overflow-hidden transition-all duration-300 ${
        isFullscreen 
          ? "w-screen h-screen rounded-none border-0" 
          : "aspect-video rounded-xl shadow-2xl border border-gray-800/40"
      }`}
    >
      {/* Aspect Ratio Sizing Overlay indicator */}
      <div className={`absolute top-4 right-4 z-40 bg-black/75 backdrop-blur-md text-emerald-400 text-xs px-3 py-1.5 rounded-full border border-emerald-500/30 flex items-center gap-1.5 font-medium pointer-events-none transition-opacity duration-300 ${showControls ? "opacity-100" : "opacity-0"}`}>
        <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
        <span>{getAspectTitleString(aspectMode)}</span>
      </div>

      {/* Center Flashing Action Play/Pause Feedbacks */}
      {flashIcon && (
        <div className="absolute inset-0 flex items-center justify-center bg-black/20 z-40 pointer-events-none animate-ping duration-150">
          <div className="bg-black/80 p-5 rounded-full border border-emerald-500/30 text-emerald-400 shadow-2xl flex items-center justify-center">
            {flashIcon === "play" ? (
              <Play className="fill-current text-emerald-400 ml-1" size={32} />
            ) : (
              <Pause className="text-emerald-400" size={32} />
            )}
          </div>
        </div>
      )}

      {/* Video element */}
      <video
        ref={videoRef}
        style={getVideoStyles()}
        className={`w-full h-full transition-all duration-300 pointer-events-auto cursor-pointer ${
          isFullscreen ? "max-h-full" : "max-h-[82vh]"
        }`}
        onClick={handleVideoClick}
        playsInline
      />

      {/* Live Badge (Static/Decorative overlay) */}
      <span className="absolute top-4 left-4 z-40 bg-red-600/90 text-white text-[11px] sm:text-xs px-2.5 py-1 rounded font-bold uppercase tracking-wider flex items-center gap-1.5">
        <span className="w-1.5 h-1.5 sm:w-2 sm:h-2 rounded-full bg-white animate-ping"></span>
        بث مباشر LIVE
      </span>

      {/* State Overlay: Loader */}
      {isLoading && !isPlayingError && (
        <div className="absolute inset-0 bg-neutral-950/85 backdrop-blur-sm flex flex-col items-center justify-center gap-4 z-30 pointer-events-none">
          <div className="relative w-14 h-14 sm:w-16 sm:h-16">
            <div className="absolute inset-0 rounded-full border-4 border-emerald-500/20"></div>
            <div className="absolute inset-0 rounded-full border-4 border-emerald-500 border-t-transparent animate-spin"></div>
          </div>
          <div className="text-center px-4">
            <p className="text-gray-200 font-semibold text-base sm:text-lg animate-pulse">جاري تشغيل القناة...</p>
            <p className="text-gray-500 text-[11px] sm:text-xs mt-1 font-sans">يرجى الانتظار لتوصيل البث عبر البروكسي المختار</p>
          </div>
        </div>
      )}

      {/* State Overlay: Error or Connection failures */}
      {isPlayingError && (
        <div className="absolute inset-0 bg-neutral-950/90 backdrop-blur-md flex flex-col items-center justify-center gap-4 z-30 px-6 text-center">
          <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-full text-red-500 animate-bounce">
            <AlertTriangle size={32} />
          </div>
          <div>
            <h4 className="text-red-400 font-bold text-lg">فشل تشغيل قناة {channelName || "المحددة"}</h4>
            <p className="text-gray-400 text-xs mt-2 max-w-sm mx-auto leading-relaxed">
              قد يكون البث متوقفاً مؤقتاً من المصدر أو يوجد ضغط على شبكة الاتصال. يرجى محاولة إعادة الاتصال أو اختيار قناة أخرى.
            </p>
          </div>
          <button
            onClick={(e) => {
              e.stopPropagation();
              handleReload();
            }}
            className="flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white font-medium rounded-lg shadow-lg active:scale-95 transition-all text-sm mt-2 pointer-events-auto"
          >
            <RefreshCw size={16} />
            إعادة محاولة الاتصال
          </button>
        </div>
      )}

      {/* Top bar info (visible on hover) */}
      <div className={`absolute top-0 inset-x-0 bg-gradient-to-b from-black/95 to-transparent p-4 flex items-center justify-between transition-opacity duration-300 z-35 pt-12 pointer-events-none ${showControls ? "opacity-100" : "opacity-0 md:group-hover:opacity-100"}`}>
        <div className="flex items-center gap-2.5 sm:gap-3 pointer-events-auto">
          {channelIcon ? (
            <img 
              src={channelIcon} 
              alt="" 
              onError={(e) => {
                e.currentTarget.style.display = "none";
              }}
              className="w-8 h-8 sm:w-10 sm:h-10 object-contain rounded-md bg-zinc-900 border border-zinc-700/50 p-0.5" 
            />
          ) : (
            <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-md bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-500 shadow-md">
              <Tv size={18} />
            </div>
          )}
          <div>
            <h3 className="text-white font-bold tracking-tight text-xs sm:text-sm md:text-base leading-none mb-1">{channelName}</h3>
            <p className="text-zinc-400 text-[10px] sm:text-xs flex items-center gap-1.5 select-none">
              <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
              <span>بث مستقر فائق الدقة (FHD)</span>
            </p>
          </div>
        </div>
      </div>

      {/* Bottom Control Bar wrapper */}
      <div className={`absolute bottom-0 inset-x-0 bg-gradient-to-t from-black/95 via-black/75 to-transparent p-4 pt-12 transition-opacity duration-300 z-35 flex flex-col gap-2.5 sm:gap-3 ${showControls ? "opacity-100" : "opacity-0 md:group-hover:opacity-100"}`}>
        
        {/* Seekbar-less / Live indicator status line */}
        <div className="w-full flex justify-between items-center text-[10px] sm:text-xs text-zinc-400 px-1 select-none">
          <div className="flex items-center gap-1.5 text-emerald-400">
            <span className="w-1.5 h-1.5 sm:w-2 sm:h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>بث حي مستمر وآمن</span>
          </div>
          <span className="text-zinc-400/80 font-semibold text-[10px] sm:text-xs">بوابة المشغل الآمن</span>
        </div>

        {/* Controls action buttons */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2 sm:gap-4">
            
            {/* Play / Pause */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                handlePlayPause();
              }}
              style={{ color: "#fff" }}
              className="p-2 sm:p-2.5 rounded-lg bg-zinc-800/85 hover:bg-zinc-700 text-white transition-all active:scale-90 cursor-pointer"
              title={isPlaying ? "إيقاف مؤقت" : "تشغيل"}
            >
              {isPlaying ? <Pause size={18} /> : <Play size={18} className="fill-current" />}
            </button>

            {/* Reload Stream */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleReload();
              }}
              className="p-2 sm:p-2.5 rounded-lg bg-zinc-800/85 hover:bg-zinc-700 text-white transition-all active:scale-95 cursor-pointer"
              title="إعادة تحميل البث"
            >
              <RefreshCw size={18} />
            </button>

            {/* Volume controls */}
            <div className="flex items-center gap-1.5 sm:gap-2 bg-zinc-800/80 rounded-lg px-2 sm:px-3 py-1.5" onClick={(e) => e.stopPropagation()}>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleMuteToggle();
                }}
                className="text-zinc-300 hover:text-white transition-colors cursor-pointer"
              >
                {isMuted || volume === 0 ? <VolumeX size={16} /> : <Volume2 size={16} />}
              </button>
              <input
                type="range"
                min="0"
                max="1"
                step="0.05"
                value={isMuted ? 0 : volume}
                onChange={handleVolumeChange}
                className="w-14 sm:w-20 md:w-28 h-1 accent-emerald-500 bg-zinc-650 rounded-lg appearance-none cursor-pointer"
              />
            </div>
          </div>

          {/* Right side controls: Sizing, Aspect ratios, fullscreen */}
          <div className="flex items-center gap-2 sm:gap-3 relative">
            
            {/* Sizing Toggles Button menu */}
            <div className="relative" onClick={(e) => e.stopPropagation()}>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowAspectMenu(!showAspectMenu);
                }}
                className={`flex items-center gap-1 sm:gap-1.5 px-2.5 sm:px-3 py-1.5 sm:py-2 rounded-lg text-[10px] sm:text-xs md:text-sm font-semibold transition-all shadow-md cursor-pointer ${
                  showAspectMenu 
                    ? "bg-emerald-600 text-white border border-emerald-500" 
                    : "bg-zinc-800/85 hover:bg-zinc-700 text-zinc-100 border border-zinc-700/30"
                }`}
                title="تعديل مقاس وحجوم الشاشة"
              >
                <Settings size={14} className="sm:size-[16px]" />
                <span className="hidden sm:inline">أبعاد الحجوم</span>
              </button>

              {/* Aspect selection pop-up menu */}
              {showAspectMenu && (
                <div className="absolute bottom-12 left-0 min-w-[180px] sm:min-w-[200px] bg-neutral-900 border border-neutral-805 rounded-xl shadow-2xl p-1.5 sm:p-2 z-50 animate-in fade-in slide-in-from-bottom-2 duration-200">
                  <p className="text-zinc-500 text-[9px] sm:text-[10px] uppercase font-bold tracking-wider px-2 py-1 select-none text-right">أبعاد العرض المتاحة:</p>
                  
                  <button
                    onClick={() => {
                      setAspectMode("original");
                      setShowAspectMenu(false);
                    }}
                    className={`w-full text-right px-2.5 py-1.5 sm:py-2 rounded-lg text-[11px] sm:text-xs font-semibold flex items-center justify-between cursor-pointer ${
                      aspectMode === "original" ? "bg-emerald-500/15 text-emerald-400" : "hover:bg-zinc-800 text-zinc-300"
                    }`}
                  >
                    <span>الحجم الأصلي (تناسب)</span>
                    <Tv size={12} />
                  </button>

                  <button
                    onClick={() => {
                      setAspectMode("fill");
                      setShowAspectMenu(false);
                    }}
                    className={`w-full text-right px-2.5 py-1.5 sm:py-2 rounded-lg text-[11px] sm:text-xs font-semibold flex items-center justify-between cursor-pointer ${
                      aspectMode === "fill" ? "bg-emerald-500/15 text-emerald-400" : "hover:bg-zinc-800 text-zinc-300"
                    }`}
                  >
                    <span>تعبئة الشاشة (ملء ممتد)</span>
                    <Maximize size={12} />
                  </button>

                  <button
                    onClick={() => {
                      setAspectMode("16-9");
                      setShowAspectMenu(false);
                    }}
                    className={`w-full text-right px-2.5 py-1.5 sm:py-2 rounded-lg text-[11px] sm:text-xs font-semibold flex items-center justify-between cursor-pointer ${
                      aspectMode === "16-9" ? "bg-emerald-500/15 text-emerald-400" : "hover:bg-zinc-800 text-zinc-300"
                    }`}
                  >
                    <span>عرض سينمائي (16:9)</span>
                    <span className="font-mono text-[9px] sm:text-[10px] bg-zinc-800 text-zinc-400 px-1.5 py-0.5 rounded">16:9</span>
                  </button>

                  <button
                    onClick={() => {
                      setAspectMode("4-3");
                      setShowAspectMenu(false);
                    }}
                    className={`w-full text-right px-2.5 py-1.5 sm:py-2 rounded-lg text-[11px] sm:text-xs font-semibold flex items-center justify-between cursor-pointer ${
                      aspectMode === "4-3" ? "bg-emerald-500/15 text-emerald-400" : "hover:bg-zinc-800 text-zinc-300"
                    }`}
                  >
                    <span>تلفاز كلاسيكي (4:3)</span>
                    <span className="font-mono text-[9px] sm:text-[10px] bg-zinc-800 text-zinc-400 px-1.5 py-0.5 rounded">4:3</span>
                  </button>

                  <button
                    onClick={() => {
                      setAspectMode("cover");
                      setShowAspectMenu(false);
                    }}
                    className={`w-full text-right px-2.5 py-1.5 sm:py-2 rounded-lg text-[11px] sm:text-xs font-semibold flex items-center justify-between cursor-pointer ${
                      aspectMode === "cover" ? "bg-emerald-500/15 text-emerald-400" : "hover:bg-zinc-800 text-zinc-300"
                    }`}
                  >
                    <span>زوم ذكي اقتصاص (Cover)</span>
                    <Maximize2 size={12} />
                  </button>
                  
                </div>
              )}
            </div>

            {/* Standard Fullscreen toggle */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                toggleFullscreen();
              }}
              className="p-2 sm:p-2.5 rounded-lg bg-zinc-800/85 hover:bg-zinc-700 text-white transition-all active:scale-95 cursor-pointer"
              title="ملء الشاشة بالكامل"
            >
              {isFullscreen ? <Minimize size={18} /> : <Maximize size={18} />}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useState, useMemo } from "react";
import { 
  Tv, 
  Search, 
  Heart, 
  Info,
  Layers,
  Send,
  ShieldCheck,
  Calendar,
  Radio,
  Grid,
  RefreshCcw,
  CheckCircle,
  Maximize2,
  Minimize2
} from "lucide-react";
import MpegtsPlayer from "./components/MpegtsPlayer";
import { LiveCategory, LiveStream, AccountData } from "./types";

export default function App() {
  // Application states
  const [categories, setCategories] = useState<LiveCategory[]>([]);
  const [activeCategoryId, setActiveCategoryId] = useState<string>("all");
  const [channels, setChannels] = useState<LiveStream[]>([]);
  const [activeChannel, setActiveChannel] = useState<LiveStream | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [favorites, setFavorites] = useState<number[]>([]);
  
  // Account status info
  const [accountInfo, setAccountInfo] = useState<AccountData | null>(null);
  
  // Interface/Loading States
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);
  const [isLoadingChannels, setIsLoadingChannels] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [favoritesTab, setFavoritesTab] = useState(false);
  const [isTheaterMode, setIsTheaterMode] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Load favorites from local storage on mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem("loop_live_favorites");
      if (stored) {
        setFavorites(JSON.parse(stored));
      }
    } catch (e) {
      console.warn("Could not read local storage favorites", e);
    }
  }, []);

  // Fetch initial data on mount
  useEffect(() => {
    const initApp = async () => {
      setIsLoadingCategories(true);
      try {
        // Fetch Categories
        const catRes = await fetch("/api/categories");
        if (catRes.ok) {
          const catData = await catRes.json();
          // Sort categories by name
          if (Array.isArray(catData)) {
            const sorted = [...catData].sort((a, b) => 
              a.category_name.localeCompare(b.category_name, "ar")
            );
            setCategories(sorted);
          }
        } else {
          console.warn("Failed to retrieve categories directly");
        }

        // Fetch Account Status Info
        const infoRes = await fetch("/api/info");
        if (infoRes.ok) {
          const infoData = await infoRes.json();
          setAccountInfo(infoData);
        }
      } catch (err) {
        console.error("Initialization error:", err);
        setErrorMessage("حدث خطأ عند الاتصال بالسيرفر. يرجى مراجعة الاتصال.");
      } finally {
        setIsLoadingCategories(false);
      }
    };

    initApp();
  }, []);

  // Fetch channels when category changes
  useEffect(() => {
    const fetchChannels = async () => {
      setIsLoadingChannels(true);
      setErrorMessage(null);
      try {
        const queryParam = activeCategoryId === "all" ? "all" : activeCategoryId;
        const chanRes = await fetch(`/api/streams?category_id=${queryParam}`);
        if (!chanRes.ok) {
          throw new Error("فشل في تحميل قائمة البث المباشر");
        }
        
        const chanData = await chanRes.json();
        if (Array.isArray(chanData)) {
          setChannels(chanData);
          
          // Auto-play the first channel automatically upon entering if no channel is active
          if (chanData.length > 0 && !activeChannel) {
            setActiveChannel(chanData[0]);
          }
        } else {
          setChannels([]);
        }
      } catch (err: any) {
        console.error("Channels fetch error:", err);
        setErrorMessage("تعذر جلب قنوات البث لهذه الفئة. القنوات قد تكون غير متاحة.");
      } finally {
        setIsLoadingChannels(false);
      }
    };

    fetchChannels();
  }, [activeCategoryId]);

  // Toggle favorite channel status
  const toggleFavorite = (streamId: number, e: React.MouseEvent) => {
    e.stopPropagation(); // Prevent choosing the channel to play
    let updated: number[];
    if (favorites.includes(streamId)) {
      updated = favorites.filter(id => id !== streamId);
    } else {
      updated = [...favorites, streamId];
    }
    setFavorites(updated);
    try {
      localStorage.setItem("loop_live_favorites", JSON.stringify(updated));
    } catch (err) {
      console.error("Failed to save favorites", err);
    }
  };

  // Filter channels based on Search Bar and Favorites Tab
  const filteredChannels = useMemo(() => {
    let result = channels;

    // Filter by favorites if tab is checked
    if (favoritesTab) {
      result = result.filter(chan => favorites.includes(chan.stream_id));
    }

    // Filter by text query
    if (searchQuery.trim() !== "") {
      const normalizedQuery = searchQuery.toLowerCase();
      result = result.filter(chan => 
        chan.name.toLowerCase().includes(normalizedQuery) ||
        chan.stream_id.toString().includes(normalizedQuery)
      );
    }

    return result;
  }, [channels, favorites, favoritesTab, searchQuery]);

  // Format expiration date nicely
  const formattedExpiry = useMemo(() => {
    if (!accountInfo?.user_info?.exp_date) return "غير متوفر";
    const timestamp = parseInt(accountInfo.user_info.exp_date);
    if (isNaN(timestamp)) return accountInfo.user_info.exp_date;
    
    // If timestamp is long or is a date
    if (timestamp === 0 || timestamp > 2500000000) return "لا ينتهي (دائم)";
    const date = new Date(timestamp * 1000);
    return date.toLocaleDateString("ar-EG", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }, [accountInfo]);

  // Calculate current active channel stream url securely via backend proxy over HTTPS
  const currentStreamUrl = useMemo(() => {
    if (!activeChannel) return "";
    return `${window.location.origin}/api/stream-ts/${activeChannel.stream_id}`;
  }, [activeChannel]);

  // Handle reload category manually
  const handleReloadData = async () => {
    setIsRefreshing(true);
    try {
      const queryParam = activeCategoryId === "all" ? "all" : activeCategoryId;
      const chanRes = await fetch(`/api/streams?category_id=${queryParam}`);
      if (chanRes.ok) {
        const chanData = await chanRes.json();
        if (Array.isArray(chanData)) {
          setChannels(chanData);
        }
      }
      
      const infoRes = await fetch("/api/info");
      if (infoRes.ok) {
        const infoData = await infoRes.json();
        setAccountInfo(infoData);
      }
    } catch (e) {
      console.warn("Failed to refresh manually", e);
    } finally {
      setIsRefreshing(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#07090e] text-slate-100 flex flex-col font-sans antialiased overflow-x-hidden pb-12">
      
      {/* 1. TOP HEADER BANNER: TELEGRAM JOIN & PROMO */}
      <div className="w-full bg-gradient-to-r from-emerald-600 via-teal-700 to-indigo-800 text-white relative py-2 sm:py-3 px-4 shadow-md select-none">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2.5">
          <div className="flex items-center gap-2.5 text-center sm:text-right">
            <span className="flex h-2.5 w-2.5 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-white"></span>
            </span>
            <p className="text-xs sm:text-sm font-semibold tracking-wide">
              تابع قناة التليجرام الرسمية لـ <span className="text-emerald-300">Loop Live</span> للحصول على تحديثات البث والدعم المباشر!
            </p>
          </div>
          <a
            href="https://t.me/jdj_q"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 px-4 py-1.5 bg-white text-indigo-900 hover:bg-neutral-100 text-xs sm:text-sm font-extrabold rounded-full shadow-lg hover:shadow-xl active:scale-95 transition-all text-center"
          >
            <Send size={14} className="fill-current rotate-[210deg]" />
            قناة التليجرام المباشرة @jdj_q
          </a>
        </div>
      </div>

      {/* Main Container Layout */}
      <main className="max-w-7xl w-full mx-auto p-4 md:p-6 lg:p-8 flex flex-col gap-6 flex-1">
        
        {/* 2. SECONDARY HEADER: APP BRAND & STATUS */}
        <div className="flex flex-col md:flex-row items-center md:justify-between gap-6 bg-slate-900/40 border border-slate-800/40 p-5 rounded-2xl backdrop-blur-md">
          <div className="flex items-center gap-4 text-center md:text-right">
            <div className="p-3 bg-gradient-to-tr from-emerald-500 to-teal-400 rounded-xl text-black shadow-xl shadow-emerald-500/10">
              <Tv size={36} />
            </div>
            <div>
              <div className="flex items-center gap-2.5 justify-center md:justify-start">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">Loop Live</h1>
                <span className="text-[10px] bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-full font-semibold">تطبيق ويب IPTV</span>
              </div>
              <p className="text-slate-400 text-xs sm:text-sm mt-1 max-w-lg">
                بث مباشر عالي الجودة مشغل عبر شبكة بروكسي ذكية لمشاهدة فورية بدون قيود CORS
              </p>
            </div>
          </div>

          {/* Account Widget Info */}
          {accountInfo?.user_info && (
            <div className="grid grid-cols-2 md:flex items-center gap-4 text-xs bg-slate-950/60 p-3.5 rounded-xl border border-slate-800/60 divide-x divide-slate-800/80 divide-x-reverse min-w-[280px]">
              <div className="px-3.5">
                <div className="text-slate-500 mb-1.5 flex items-center gap-1">
                  <ShieldCheck size={13} className="text-emerald-400" />
                  <span>حالة الحساب</span>
                </div>
                <span className="font-bold text-emerald-400 text-right block">{accountInfo.user_info.status === "Active" ? "نشط ومفعل" : "غير معروف"}</span>
              </div>
              <div className="px-3.5">
                <div className="text-slate-500 mb-1.5 flex items-center gap-1">
                  <Calendar size={13} className="text-sky-400" />
                  <span>تاريخ الانتهاء</span>
                </div>
                <span className="font-mono font-bold text-gray-200 block truncate">{formattedExpiry}</span>
              </div>
              <div className="hidden sm:block px-3.5">
                <div className="text-slate-500 mb-1.5 flex items-center gap-1">
                  <Radio size={13} className="text-orange-400 animate-pulse" />
                  <span>الاتصالات النشطة</span>
                </div>
                <span className="font-mono font-bold text-gray-200 block text-right">
                  {accountInfo.user_info.active_cons} / {accountInfo.user_info.max_connections || "1"}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* 3. MAIN WORKSPACE GRID: PLAYER ON THE LEFT, LISTS ON THE RIGHT */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
          
          {/* A. LEFT COLUMN: VIDEO PLAYER & CHANNEL DETAILS */}
          <div className={`${isTheaterMode ? "lg:col-span-3 transition-all duration-300" : "lg:col-span-2"} flex flex-col gap-5`}>
            
            {/* HORIZONTAL PLAYER ELEMENT */}
            {activeChannel ? (
              <MpegtsPlayer 
                streamUrl={currentStreamUrl}
                channelName={activeChannel.name}
                channelIcon={activeChannel.stream_icon}
              />
            ) : (
              <div className="aspect-video bg-slate-950 border border-slate-800 rounded-2xl flex flex-col items-center justify-center text-center p-6 gap-3">
                <div className="relative">
                  <div className="p-4 bg-emerald-500/5 text-emerald-500/60 rounded-full border border-emerald-500/10">
                    <Tv size={48} className="animate-pulse" />
                  </div>
                </div>
                <div>
                  <h4 className="text-gray-300 font-bold text-base">بانتظار اختيار قناة البث المباشر</h4>
                  <p className="text-xs text-slate-500 mt-1 max-w-sm">
                    اختر فئة من القائمة على اليمين ثم اضغط على أي قناة لبدء بثها تلقائياً وبشكل أفقي.
                  </p>
                </div>
              </div>
            )}

            {/* Active Channel Details card */}
            {activeChannel && (
              <div className="bg-slate-900/30 border border-slate-800/40 p-4 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  {activeChannel.stream_icon ? (
                    <img 
                      src={activeChannel.stream_icon} 
                      alt="" 
                      onError={(e) => {
                        e.currentTarget.style.display = "none";
                      }}
                      className="w-12 h-12 object-contain bg-slate-950 border border-slate-800/80 p-0.5 rounded-lg" 
                    />
                  ) : (
                    <div className="w-12 h-12 bg-emerald-500/15 text-emerald-500 flex items-center justify-center rounded-lg border border-emerald-500/20">
                      <Tv size={22} />
                    </div>
                  )}
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-sans text-emerald-500 text-xs text-right font-extrabold px-1.5 py-0.5 bg-emerald-500/10 rounded">قناة رقم {activeChannel.num}</span>
                      <h2 className="text-white font-bold text-[15px] sm:text-lg">{activeChannel.name}</h2>
                    </div>
                    <div className="flex items-center gap-3 text-xs text-slate-400 mt-1">
                      <span className="flex items-center gap-1.5 text-zinc-400 font-semibold">
                        <Layers size={13} className="text-emerald-500" />
                        بث مباشر فائق الدقة ومستقر
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
                  {/* Theater Mode Button */}
                  <button
                    onClick={() => setIsTheaterMode(!isTheaterMode)}
                    className={`flex-1 sm:flex-none flex items-center justify-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg border transition-all active:scale-95 cursor-pointer ${
                      isTheaterMode
                        ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/40"
                        : "bg-slate-800/30 hover:bg-slate-800/80 text-slate-300 border-slate-700/30"
                    }`}
                    title={isTheaterMode ? "العرض الافتراضي" : "توسيع نمط المسرح"}
                  >
                    {isTheaterMode ? <Minimize2 size={14} /> : <Maximize2 size={14} />}
                    <span>{isTheaterMode ? "تضييق المشغل" : "نمط المسرح"}</span>
                  </button>

                  <button
                    onClick={(e) => toggleFavorite(activeChannel.stream_id, e)}
                    className={`flex-1 sm:flex-none flex items-center justify-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg border transition-all active:scale-95 cursor-pointer ${
                      favorites.includes(activeChannel.stream_id)
                        ? "bg-rose-500/10 text-rose-400 border-rose-500/30"
                        : "bg-slate-800/30 hover:bg-slate-800/80 text-slate-300 border-slate-700/30"
                    }`}
                  >
                    <Heart size={14} className={favorites.includes(activeChannel.stream_id) ? "fill-rose-500" : ""} />
                    <span>{favorites.includes(activeChannel.stream_id) ? "مفضّلة" : "المفضّلة"}</span>
                  </button>
                </div>
              </div>
            )}

            {/* Quick Informational Tips */}
            <div className="bg-emerald-500/5 border border-emerald-500/10 p-3.5 rounded-xl flex items-center gap-3 text-xs text-emerald-400/90 leading-relaxed font-light">
              <Info size={16} className="shrink-0 animate-pulse text-emerald-400" />
              <p>
                <strong>تنويه:</strong> يتوفر في المشغل خيار <strong>أبعاد الحجوم</strong>، يمكنك الضغط عليه لتغيير قياس الشاشة حسب اختيارك (ملء شاشة، 16:9، تعبئة الشاشة بشكل ممتد) لتفادي تشوهات شريط الفيديو.
              </p>
            </div>
          </div>

          {/* B. RIGHT COLUMN: GROUPS AND DIRECT CHANNELS LIST */}
          <div className={`${isTheaterMode ? "lg:col-span-3 transition-all duration-300" : "lg:col-span-1"} bg-slate-900/20 border border-slate-800/40 rounded-2xl p-4 flex flex-col gap-4 relative overflow-hidden backdrop-blur-md`}>
            
            {/* Search and filter controls */}
            <div className="flex flex-col gap-3">
              <div className="relative">
                <Search className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
                <input
                  type="text"
                  placeholder="ابحث باسم القناة المفضلة..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full text-right bg-slate-950 border border-slate-800 text-slate-200 placeholder-slate-500 pr-10 pl-3 py-2 rounded-xl text-sm focus:outline-none focus:ring-1 focus:ring-emerald-500"
                />
              </div>

              {/* Favorites vs Channels tab toggle */}
              <div className="grid grid-cols-2 gap-2 bg-slate-950 p-1 rounded-xl border border-slate-800/30">
                <button
                  onClick={() => setFavoritesTab(false)}
                  className={`py-1.5 rounded-lg text-xs font-bold transition-all ${
                    !favoritesTab 
                      ? "bg-slate-800 text-emerald-400 shadow-sm" 
                      : "text-slate-400 hover:text-white"
                  }`}
                >
                  <span className="flex items-center justify-center gap-1.5">
                    <Grid size={12} />
                    قنوات البث المباشر ({channels.length})
                  </span>
                </button>
                <button
                  onClick={() => setFavoritesTab(true)}
                  className={`py-1.5 rounded-lg text-xs font-bold transition-all ${
                    favoritesTab 
                      ? "bg-slate-800 text-rose-400 shadow-sm" 
                      : "text-slate-400 hover:text-white"
                  }`}
                >
                  <span className="flex items-center justify-center gap-1.5">
                    <Heart size={12} className={favorites.length > 0 ? "fill-rose-500 text-rose-500" : ""} />
                    المفضلة ({favorites.length})
                  </span>
                </button>
              </div>
            </div>

            {/* Categories selector - ONLY display category lists if we are not on Favorites focus tab */}
            {!favoritesTab && (
              <div className="flex flex-col gap-2">
                <label className="text-zinc-500 text-xs font-bold mr-1">اختر باقة أو فئة القنوات:</label>
                {isLoadingCategories ? (
                  <div className="h-9 bg-slate-950/40 border border-slate-800/40 rounded-xl animate-pulse"></div>
                ) : (
                  <select
                    value={activeCategoryId}
                    onChange={(e) => {
                      setActiveCategoryId(e.target.value);
                      setSearchQuery(""); // Clear search on category shift
                    }}
                    className="w-full text-right bg-slate-950 border border-slate-800 text-slate-200 px-3 py-2 rounded-xl text-xs sm:text-sm font-semibold focus:outline-none focus:ring-1 focus:ring-emerald-500 cursor-pointer"
                  >
                    <option value="all">كل الفئات والقنوات البث المباشر</option>
                    {categories.map((cat) => (
                      <option key={cat.category_id} value={cat.category_id}>
                        {cat.category_name}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            )}

            {/* Refresh, Loading status controls */}
            <div className="flex items-center justify-between text-xs text-slate-500">
              <span>الفئة الحالية: {!favoritesTab ? categories.find(c => c.category_id === activeCategoryId)?.category_name || "تلقائي" : "المفضّلة المختارة"}</span>
              <button
                onClick={handleReloadData}
                disabled={isRefreshing}
                className="flex items-center gap-1 hover:text-emerald-400 transition-colors disabled:opacity-40 cursor-pointer"
                title="تحديث البيانات"
              >
                <RefreshCcw size={12} className={isRefreshing ? "animate-spin" : ""} />
                تحديث
              </button>
            </div>

            {/* Channels Scroll list wrapper */}
            <div className={`overflow-y-auto pr-1 rounded-lg relative transition-all duration-300 ${
              isTheaterMode 
                ? "grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3.5 max-h-[550px]" 
                : "flex flex-col gap-2 h-[460px]"
            }`}>
              
              {isLoadingChannels ? (
                <div className={`absolute inset-0 bg-slate-950/40 p-1 ${
                  isTheaterMode 
                    ? "grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3.5 h-full" 
                    : "flex flex-col gap-2.5"
                }`}>
                  {[...Array(isTheaterMode ? 12 : 6)].map((_, i) => (
                    <div key={i} className="h-14 bg-slate-900/50 border border-slate-800/30 rounded-xl animate-pulse flex items-center justify-between px-3">
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 rounded bg-slate-800"></div>
                        <div className="w-24 h-4 rounded bg-slate-800"></div>
                      </div>
                      <div className="w-4 h-4 rounded-full bg-slate-800"></div>
                    </div>
                  ))}
                </div>
              ) : filteredChannels.length === 0 ? (
                <div className="flex flex-col items-center justify-center p-12 text-slate-500 text-center gap-2">
                  <Tv size={32} className="stroke-slate-700" />
                  <p className="text-xs">لم يتم العثور على أي قنوات بث مباشرة</p>
                  {favoritesTab && <p className="text-[10px] text-zinc-600">يمكنك الضغط على علامة القلب لتضيف قنواتك المفضلة هنا.</p>}
                </div>
              ) : (
                filteredChannels.map((chan) => {
                  const isActive = activeChannel?.stream_id === chan.stream_id;
                  const isFav = favorites.includes(chan.stream_id);
                  return (
                    <div
                      key={chan.stream_id}
                      onClick={() => setActiveChannel(chan)}
                      className={`group p-2.5 rounded-xl border flex items-center justify-between gap-3 cursor-pointer transition-all active:scale-[0.99] ${
                        isActive 
                          ? "bg-emerald-500/10 border-emerald-500/40 shadow-md shadow-emerald-500/5" 
                          : "bg-slate-950/70 hover:bg-slate-900/60 border-slate-800/60 hover:border-slate-700/60"
                      }`}
                    >
                      <div className="flex items-center gap-2.5 overflow-hidden">
                        
                        {/* Channel icon or TV placeholder code */}
                        {chan.stream_icon ? (
                          <img
                            src={chan.stream_icon}
                            alt=""
                            loading="lazy"
                            onError={(e) => {
                              e.currentTarget.style.display = "none";
                            }}
                            className="w-10 h-10 object-contain rounded-md bg-zinc-950 border border-zinc-900 p-0.5"
                          />
                        ) : null}
                        
                        {/* If image missing/fails, we render a default style badge block */}
                        <div className="w-10 h-10 shrink-0 bg-slate-900 flex-col items-center justify-center rounded-md border border-slate-800 group-hover:border-slate-700 text-[10px] text-slate-500 font-bold tracking-tighter uppercase hidden [img&]:flex select-none">
                          <Tv size={14} className="stroke-slate-600 mb-0.5" />
                          <span>TS</span>
                        </div>

                        {/* Text information */}
                        <div className="truncate text-right">
                          <p className={`font-bold text-xs truncate ${isActive ? "text-emerald-400" : "text-gray-200 group-hover:text-white"}`}>
                            {chan.name}
                          </p>
                          <p className="text-[10px] text-zinc-400 font-semibold mt-0.5">قناة رقم {chan.num || chan.stream_id}</p>
                        </div>
                      </div>

                      {/* Right heart toggle button */}
                      <button
                        onClick={(e) => toggleFavorite(chan.stream_id, e)}
                        className={`p-1.5 rounded-md hover:bg-slate-800 text-slate-400 hover:text-rose-400 transition-colors cursor-pointer shrink-0`}
                        title="إضافة للمفضلة"
                      >
                        <Heart size={14} className={isFav ? "fill-rose-500 text-rose-500" : ""} />
                      </button>
                    </div>
                  );
                })
              )}
            </div>

            {/* Bottom active status ticker */}
            <div className="bg-slate-950/80 p-2.5 rounded-xl border border-slate-900 text-[10px] text-slate-500 flex items-center justify-between select-none">
              <span className="flex items-center gap-1 text-emerald-400/90 font-medium">
                <CheckCircle size={10} className="text-emerald-500" />
                خادم بث ذكي آمن ومستقر
              </span>
              <span>الحالة: متصل بنجاح</span>
            </div>

          </div>

        </div>

      </main>

      {/* Footer copyright */}
      <footer className="mt-auto py-6 border-t border-slate-900/60 max-w-7xl w-full mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500 select-none">
        <p className="text-center sm:text-right">جميع الحقوق محفوظة لـ <strong className="text-zinc-400 font-bold">Loop Live IPTV</strong> © {new Date().getFullYear()}</p>
        <div className="flex items-center gap-4">
          <a href="https://t.me/jdj_q" target="_blank" rel="noopener noreferrer" className="hover:text-emerald-400 transition-colors">الدعم الفني عبر تليجرام</a>
          <span>•</span>
          <span className="font-mono">Standard Proxied MSE Playback Engine</span>
        </div>
      </footer>

    </div>
  );
}

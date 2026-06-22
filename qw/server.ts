import express from "express";
import path from "path";
import http from "http";
import https from "https";
import { Readable } from "stream";
import { createServer as createViteServer } from "vite";

async function startServer() {
  const app = express();
  const PORT = 3000;

  // Middleware for parsing JSON
  app.use(express.json());

  // XTreme Codes Credentials
  const HOST = "http://toytcl.xyz:8080";
  const USERNAME = "357643467990765";
  const PASSWORD = "Ofgo3yz8CH";

  // Memory Caching
  let categoriesCache: any = null;
  let categoriesCacheTime = 0;
  const CACHE_DURATION = 3 * 60 * 1000; // 3 minutes

  let streamsCache: Map<string, { data: any; time: number }> = new Map();

  // Helper with Timeout
  async function fetchWithTimeout(url: string, timeout = 12000) {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), timeout);
    try {
      const response = await fetch(url, { signal: controller.signal });
      clearTimeout(id);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (err) {
      clearTimeout(id);
      throw err;
    }
  }

  // 1. Get Categories Proxy Route
  app.get("/api/categories", async (req, res) => {
    const now = Date.now();
    if (categoriesCache && now - categoriesCacheTime < CACHE_DURATION) {
      return res.json(categoriesCache);
    }

    try {
      const apiUrl = `${HOST}/player_api.php?username=${USERNAME}&password=${PASSWORD}&action=get_live_categories`;
      const data = await fetchWithTimeout(apiUrl);
      categoriesCache = data;
      categoriesCacheTime = now;
      res.json(data);
    } catch (error: any) {
      console.error("Error fetching categories:", error?.message);
      if (categoriesCache) {
        return res.json(categoriesCache);
      }
      // Return a basic fallback structure or error
      res.status(500).json({ error: "Failed to fetch categories" });
    }
  });

  // 2. Get Streams Proxy Route
  app.get("/api/streams", async (req, res) => {
    const categoryId = (req.query.category_id as string) || "all";
    const now = Date.now();
    
    const cached = streamsCache.get(categoryId);
    if (cached && now - cached.time < CACHE_DURATION) {
      return res.json(cached.data);
    }

    try {
      let apiUrl = `${HOST}/player_api.php?username=${USERNAME}&password=${PASSWORD}&action=get_live_streams`;
      if (categoryId !== "all") {
        apiUrl += `&category_id=${categoryId}`;
      }
      const data = await fetchWithTimeout(apiUrl);
      streamsCache.set(categoryId, { data, time: now });
      res.json(data);
    } catch (error: any) {
      console.error(`Error fetching streams for category ${categoryId}:`, error?.message);
      if (cached) {
        return res.json(cached.data);
      }
      res.status(500).json({ error: "Failed to fetch streams" });
    }
  });

  // 3. Account Status / Server Info Proxy Route
  app.get("/api/info", async (req, res) => {
    try {
      const apiUrl = `${HOST}/player_api.php?username=${USERNAME}&password=${PASSWORD}`;
      const data = await fetchWithTimeout(apiUrl);
      res.json(data);
    } catch (error: any) {
      console.error("Error fetching account info:", error?.message);
      res.status(500).json({ error: "Failed to fetch account info" });
    }
  });

  // 4. Secure TS Stream Proxy bypass for HTTPS mixed-content blocks
  app.get("/api/stream-ts/:streamId", (req, res) => {
    const streamId = req.params.streamId;
    if (!streamId) {
      return res.status(400).send("Stream ID is required");
    }

    // Set CORS and standard fast-seeking stream video headers
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "*");
    res.setHeader("Content-Type", "video/mp2t");
    res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    res.setHeader("Pragma", "no-cache");
    res.setHeader("Expires", "0");

    if (req.method === "OPTIONS") {
      return res.sendStatus(200);
    }

    // List of cascading proxy and direct URLs to attempt in order
    const urls = [
      // 1. Direct proxy with unencoded double slash (the VLC raw format tested by user)
      `http://194.60.93.157/proxy?url=http://toytcl.xyz:8080/live//357643467990765/Ofgo3yz8CH/${streamId}.ts`,
      // 2. Direct proxy with unencoded single slash
      `http://194.60.93.157/proxy?url=http://toytcl.xyz:8080/live/357643467990765/Ofgo3yz8CH/${streamId}.ts`,
      // 3. Direct proxy with URI encoded double slash
      `http://194.60.93.157/proxy?url=${encodeURIComponent(`${HOST}/live//${USERNAME}/${PASSWORD}/${streamId}.ts`)}`,
      // 4. Direct proxy with URI encoded single slash
      `http://194.60.93.157/proxy?url=${encodeURIComponent(`${HOST}/live/${USERNAME}/${PASSWORD}/${streamId}.ts`)}`,
      // 5. Direct streaming bypass with double slash
      `http://toytcl.xyz:8080/live//357643467990765/Ofgo3yz8CH/${streamId}.ts`,
      // 6. Direct streaming bypass with single slash
      `http://toytcl.xyz:8080/live/357643467990765/Ofgo3yz8CH/${streamId}.ts`
    ];

    console.log(`[Stream Proxy] Starting cascade fetching for Stream ID: ${streamId}`);

    let activeClientRequest: http.ClientRequest | null = null;
    let activeIncomingResponse: http.IncomingMessage | null = null;
    let hasResponded = false;
    let isCleanedUp = false;

    const cleanup = () => {
      if (isCleanedUp) return;
      isCleanedUp = true;
      console.log(`[Stream Proxy] Tearing down upstream connections for Stream ID: ${streamId}`);
      try {
        if (activeIncomingResponse) {
          activeIncomingResponse.unpipe(res);
          activeIncomingResponse.destroy();
        }
        if (activeClientRequest) {
          activeClientRequest.destroy();
        }
      } catch (err) {
        console.error("[Stream Proxy] Error during cleanup:", err);
      }
    };

    // Listen for client abort (e.g. closing browser page or changing channel)
    req.on("close", () => {
      cleanup();
    });
    req.on("end", () => {
      cleanup();
    });

    const tryUrl = (index: number) => {
      if (index >= urls.length) {
        console.error(`[Stream Proxy] All upstream alternatives returned an error or timed out for Stream ID ${streamId}`);
        if (!hasResponded && !res.headersSent) {
          hasResponded = true;
          res.status(502).json({ error: "فشلت جميع محاولات الاتصال ومصادر البث البديلة." });
        }
        return;
      }

      const urlStr = urls[index];
      console.log(`[Stream Proxy] Trying path ${index + 1}/${urls.length}: ${urlStr}`);

      const makeRequest = (targetUrlStr: string, redirectsAllowed = 5) => {
        if (redirectsAllowed <= 0) {
          console.error(`[Stream Proxy] Too many redirects on option ${index + 1}`);
          tryUrl(index + 1);
          return;
        }

        try {
          const parsedUrl = new URL(targetUrlStr);
          const protocolClient = parsedUrl.protocol === "https:" ? https : http;

          const requestOptions: http.RequestOptions = {
            method: "GET",
            headers: {
              "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
              "Accept": "*/*",
              "Connection": "keep-alive"
            },
            timeout: 8000 // 8 seconds per connection attempt to keep switching snappy
          };

          const clientReq = protocolClient.get(targetUrlStr, requestOptions, (incomingRes) => {
            activeIncomingResponse = incomingRes;
            const status = incomingRes.statusCode || 200;

            // Handle Redirects (301, 302, 303, 307, 308)
            if (status >= 300 && status < 400 && incomingRes.headers.location) {
              let redirectUrl = incomingRes.headers.location;
              if (!redirectUrl.startsWith("http")) {
                redirectUrl = new URL(redirectUrl, parsedUrl.origin).toString();
              }
              console.log(`[Stream Proxy] Path ${index + 1} redirect to: ${redirectUrl}`);
              incomingRes.resume(); // free up socket
              makeRequest(redirectUrl, redirectsAllowed - 1);
              return;
            }

            // If source matches ok response
            if (status >= 200 && status < 300) {
              console.log(`[Stream Proxy] Success on Path ${index + 1} with status: ${status}! Piping video stream to client...`);
              if (!res.headersSent) {
                res.status(status);
              }
              hasResponded = true;

              // Pipe binary data directly to Express response
              incomingRes.pipe(res);
            } else {
              console.warn(`[Stream Proxy] Path ${index + 1} failed with status: ${status}`);
              incomingRes.resume();
              tryUrl(index + 1);
            }
          });

          activeClientRequest = clientReq;

          clientReq.on("timeout", () => {
            console.warn(`[Stream Proxy] Path ${index + 1} timed out`);
            clientReq.destroy();
            tryUrl(index + 1);
          });

          clientReq.on("error", (err) => {
            console.error(`[Stream Proxy] Path ${index + 1} request error: ${err.message}`);
            clientReq.destroy();
            tryUrl(index + 1);
          });

        } catch (err: any) {
          console.error(`[Stream Proxy] Exception building request for Path ${index + 1}: ${err.message}`);
          tryUrl(index + 1);
        }
      };

      makeRequest(urlStr);
    };

    tryUrl(0);
  });

  // Vite development or production routing
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();

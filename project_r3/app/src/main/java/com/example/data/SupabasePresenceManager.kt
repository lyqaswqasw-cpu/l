package com.example.data

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class SupabasePresenceManager {

    companion object {
        private const val TAG = "SupabasePresence"
        private const val WS_URL = "wss://wfxpyrcxmjdyeiakocsw.supabase.co/realtime/v1/websocket?apikey=sb_publishable_4PGoe5Bfvw1qWWD-pYVWQ_5vn7zPws&vsn=1.0.0"
        private const val TOPIC = "realtime:global_presence"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val myUserId = "user_${UUID.randomUUID().toString().take(6)}"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Tracks live online user set - mapped from key -> session count
    private val _onlineUsersMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    
    private val _onlineCount = MutableStateFlow(1) // Starts with 1 (the current user)
    val onlineCount: StateFlow<Int> = _onlineCount

    fun start() {
        if (webSocket != null) return
        connect()
    }

    private fun connect() {
        Log.d(TAG, "Connecting to Supabase Realtime WebSocket...")
        val request = Request.Builder().url(WS_URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d(TAG, "WebSocket Opened. Joining room $TOPIC...")
                joinTopic(webSocket)
                startHeartbeat(webSocket)
                
                reconnectJob?.cancel()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                handleMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WebSocket Closing: $code / $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.e(TAG, "WebSocket Closed: $code / $reason")
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e(TAG, "WebSocket Failure: ${t.message}", t)
                scheduleReconnect()
            }
        })
    }

    private fun joinTopic(ws: WebSocket) {
        try {
            val joinPayload = JSONObject().apply {
                put("topic", TOPIC)
                put("event", "phx_join")
                put("payload", JSONObject().apply {
                    put("config", JSONObject().apply {
                        put("broadcast", JSONObject().apply {
                            put("ack", false)
                            put("self", false)
                        })
                        put("presence", JSONObject().apply {
                            put("key", myUserId)
                        })
                    })
                })
                put("ref", "join_ref_1")
            }
            ws.send(joinPayload.toString())
            Log.d(TAG, "Join payload sent!")
        } catch (e: Exception) {
            Log.e(TAG, "Error compiling join payload: ${e.message}")
        }
    }

    private fun startHeartbeat(ws: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            var refIndex = 1
            while (isActive && isConnected) {
                delay(25000) // 25 seconds heartbeat
                try {
                    val heartbeat = JSONObject().apply {
                        put("topic", "phoenix")
                        put("event", "heartbeat")
                        put("payload", JSONObject())
                        put("ref", "hb_${refIndex++}")
                    }
                    ws.send(heartbeat.toString())
                    Log.d(TAG, "Heartbeat pulse sent.")
                } catch (e: Exception) {
                    Log.e(TAG, "Heartbeat fail: ${e.message}")
                }
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val obj = JSONObject(text)
            val event = obj.optString("event")
            val topic = obj.optString("topic")

            if (topic == TOPIC) {
                when (event) {
                    "presence_state" -> {
                        val payload = obj.optJSONObject("payload") ?: return
                        val currentMap = mutableMapOf<String, Int>()
                        val iterator = payload.keys()
                        while (iterator.hasNext()) {
                            val key = iterator.next()
                            val userObj = payload.optJSONObject(key)
                            val metas = userObj?.optJSONArray("metas")
                            currentMap[key] = metas?.length() ?: 1
                        }
                        _onlineUsersMap.value = currentMap
                        updateCount()
                    }
                    "presence_diff" -> {
                        val payload = obj.optJSONObject("payload") ?: return
                        val joins = payload.optJSONObject("joins")
                        val leaves = payload.optJSONObject("leaves")
                        
                        val updatedMap = _onlineUsersMap.value.toMutableMap()
                        
                        if (joins != null) {
                            val iterator = joins.keys()
                            while (iterator.hasNext()) {
                                val key = iterator.next()
                                val userObj = joins.optJSONObject(key)
                                val metas = userObj?.optJSONArray("metas")
                                updatedMap[key] = metas?.length() ?: 1
                            }
                        }
                        
                        if (leaves != null) {
                            val iterator = leaves.keys()
                            while (iterator.hasNext()) {
                                val key = iterator.next()
                                updatedMap.remove(key)
                            }
                        }
                        
                        _onlineUsersMap.value = updatedMap
                        updateCount()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing websocket message: ${e.message}")
        }
    }

    private fun updateCount() {
        // Safe count calculation
        val currentPresences = _onlineUsersMap.value
        val uniqueConnectedUsers = currentPresences.size
        
        // Ensure count is at least 1 (the current user themselves)
        val finalCount = if (uniqueConnectedUsers > 0) uniqueConnectedUsers else 1
        _onlineCount.value = finalCount
        Log.d(TAG, "Updated presence count: $finalCount users online [Presences Map Size: ${currentPresences.size}]")
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(5000)
            Log.d(TAG, "Retrying contact to Supabase RT...")
            connect()
        }
    }

    fun stop() {
        heartbeatJob?.cancel()
        reconnectJob?.cancel()
        try {
            webSocket?.close(1000, "App closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection: ${e.message}")
        }
        webSocket = null
        isConnected = false
    }
}



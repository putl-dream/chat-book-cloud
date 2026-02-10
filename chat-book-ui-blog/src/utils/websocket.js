
import { ElMessage } from 'element-plus';

export default class SocketService {
    /**
     * @param {string} url - WebSocket URL
     * @param {string} token - Authentication token (subprotocol)
     * @param {object} options - Configuration options
     */
    constructor(url, token, options = {}) {
        this.url = url;
        this.token = token;
        this.socket = null;

        // 1. Multiple listeners support: Map<string, Set<Function>>
        this.handlers = new Map();

        // Global event listeners
        this.globalHandlers = {
            open: new Set(),
            close: new Set(),
            error: new Set()
        };

        // Configuration with defaults
        this.options = Object.assign({
            reconnectInterval: 3000,     // 3s between reconnection attempts
            maxReconnectAttempts: 10,    // Maximum number of reconnection attempts
            heartbeatInterval: 30000,    // 30s heartbeat interval
            heartbeatMessage: { type: 'PING' } // Heartbeat payload
        }, options);

        // State management
        this.reconnectAttempts = 0;
        this.isExplicitlyClosed = false; // Flag to check if closed by user
        this.messageQueue = [];          // 3. Message queue for buffering
        this.heartbeatTimer = null;      // Timer for heartbeat
        this.reconnectTimer = null;      // Timer for reconnection
    }

    /**
     * Establish WebSocket connection
     */
    connect() {
        if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
            console.log('WebSocket is already connecting or connected');
            return;
        }

        console.log(`Connecting to WebSocket: ${this.url}`);
        const protocols = this.token ? [this.token] : [];

        try {
            this.socket = new WebSocket(this.url, protocols);
            this.bindEvents();
        } catch (e) {
            console.error('WebSocket connection creation failed:', e);
            this.handleReconnect();
        }
    }

    /**
     * Bind WebSocket internal events
     */
    bindEvents() {
        this.socket.onopen = (event) => {
            console.log(`WebSocket connected to ${this.url}`);
            this.reconnectAttempts = 0; // Reset reconnection attempts
            this.startHeartbeat();      // 4. Start heartbeat
            this.flushQueue();          // 3. Flush buffered messages
            this.emitGlobal('open', event);
        };

        this.socket.onmessage = (event) => {
            // Reset heartbeat or activity timer if needed (optional)
            try {
                // Ignore empty or keep-alive responses if necessary
                if (event.data === 'PONG') return;

                const message = JSON.parse(event.data);

                // Dispatch to registered handlers
                this.emit(message.type, message.data);
            } catch (error) {
                console.error('Error parsing WebSocket message:', error);
            }
        };

        this.socket.onclose = (event) => {
            console.log('WebSocket disconnected', event);
            this.stopHeartbeat();
            this.emitGlobal('close', event);

            // 2. Auto-reconnection logic
            if (!this.isExplicitlyClosed) {
                this.handleReconnect();
            }
        };

        this.socket.onerror = (error) => {
            console.error('WebSocket error:', error);
            this.emitGlobal('error', error);
        };
    }

    /**
     * Handle reconnection with backoff or simple interval
     */
    handleReconnect() {
        if (this.reconnectAttempts < this.options.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.options.maxReconnectAttempts}) in ${this.options.reconnectInterval}ms...`);

            if (this.reconnectTimer) clearTimeout(this.reconnectTimer);

            this.reconnectTimer = setTimeout(() => {
                this.connect();
            }, this.options.reconnectInterval);
        } else {
            console.error('WebSocket reconnection failed: Max attempts reached.');
            // Optional: Notify user
            ElMessage.error('网络连接已断开，请刷新页面重试');
        }
    }

    /**
     * Register a handler for a specific message type
     * Supports multiple listeners per type
     */
    on(type, callback) {
        if (!this.handlers.has(type)) {
            this.handlers.set(type, new Set());
        }
        this.handlers.get(type).add(callback);
    }

    /**
     * Remove a handler or all handlers for a type
     */
    off(type, callback) {
        if (this.handlers.has(type)) {
            if (callback) {
                this.handlers.get(type).delete(callback);
            } else {
                this.handlers.delete(type);
            }
        }
    }

    /**
     * Emit data to all registered listeners for a type
     */
    emit(type, data) {
        if (this.handlers.has(type)) {
            this.handlers.get(type).forEach(callback => {
                try {
                    callback(data);
                } catch (e) {
                    console.error(`Error in WebSocket handler for type ${type}:`, e);
                }
            });
        }
    }

    // Global event registration helpers
    onOpen(callback) { this.globalHandlers.open.add(callback); }
    onClose(callback) { this.globalHandlers.close.add(callback); }
    onError(callback) { this.globalHandlers.error.add(callback); }

    // Global event emitter
    emitGlobal(event, data) {
        this.globalHandlers[event].forEach(cb => {
            try { cb(data); } catch (e) { console.error(e); }
        });
    }

    /**
     * Send message
     * If not connected, queues the message
     */
    send(type, data) {
        // Flatten the message structure: { type, ...data }
        // If data is not an object (e.g. null), it will be ignored by spread syntax
        const messagePayload = data && typeof data === 'object' ? { type, ...data } : { type, data };
        // Special handling if data was null/undefined to avoid {type, data: undefined} if we want just {type}
        // But the previous line logic:
        // if data is null: { type, data: null } -> wait, spread null is empty.
        // let's be precise.

        let finalMessage;
        if (data && typeof data === 'object') {
            finalMessage = { type, ...data };
        } else if (data === undefined || data === null) {
            finalMessage = { type };
        } else {
            // primitive type, wrap in 'data' field? or just send as is?
            // Backend expects flat structure usually.
            // But if data is a string, we can't flatten it.
            // Let's assume for now we keep 'data' field for primitives, but flattened for objects.
            // However, to be consistent with backend @JsonUnwrapped, it expects fields.
            // If we send { type, data: "some string" }, backend might not map it unless there is a 'data' field.
            // But we are removing 'data' field nesting in backend via @JsonUnwrapped.
            // So primitives might fail unless mapped to a specific field.
            // Given current usage in Text.vue and Chat.vue, data is always Object or null.
            finalMessage = { type, data };
        }

        // Re-evaluating:
        // Text.vue: always object.
        // Chat.vue: object or null.
        // So simply { type, ...data } works for object.
        // For null, { type, ...null } is { type }. This is perfect for SystemMessage.

        const message = JSON.stringify({ type, ...data });

        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(message);
            return true;
        } else {
            console.warn('WebSocket is not open. Message queued.');
            this.messageQueue.push(message);

            // If connection is dead but not detected, try to reconnect? 
            // Usually onclose handles this, but if in 'zombie' state, 
            // sending might fail or buffer. 
            // If completely closed/null, trigger connect if not explicitly closed?
            if (!this.socket || this.socket.readyState === WebSocket.CLOSED) {
                if (!this.isExplicitlyClosed) this.connect();
            }
            return false;
        }
    }

    /**
     * Flush queued messages
     */
    flushQueue() {
        while (this.messageQueue.length > 0) {
            if (this.socket && this.socket.readyState === WebSocket.OPEN) {
                const message = this.messageQueue.shift();
                this.socket.send(message);
            } else {
                break;
            }
        }
    }

    /**
     * Start heartbeat mechanism to keep connection alive and detect zombies
     */
    startHeartbeat() {
        this.stopHeartbeat();
        this.heartbeatTimer = setInterval(() => {
            if (this.socket && this.socket.readyState === WebSocket.OPEN) {
                // Send heartbeat message
                // Using a try-catch in case send fails synchronously
                try {
                    this.socket.send(JSON.stringify(this.options.heartbeatMessage));
                } catch (e) {
                    console.error('Heartbeat send failed:', e);
                    // If send fails, it might mean the socket is broken
                    // We can trigger reconnect if needed, or let onerror/onclose handle it
                }
            }
        }, this.options.heartbeatInterval);
    }

    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }

    /**
     * Close the connection manually
     */
    close() {
        this.isExplicitlyClosed = true;
        this.stopHeartbeat();
        if (this.reconnectTimer) clearTimeout(this.reconnectTimer);

        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
    }

    /**
     * Check if connected
     */
    isConnected() {
        return this.socket && this.socket.readyState === WebSocket.OPEN;
    }
}

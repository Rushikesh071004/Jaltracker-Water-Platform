import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { useAuth } from './AuthContext';
import {
  Bell,
  CheckCircle2,
  AlertCircle,
  IndianRupee,
  ShieldCheck,
  X,
  Sparkles,
} from 'lucide-react';

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [activeToast, setActiveToast] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const stompClientRef = useRef(null);

  useEffect(() => {
    // Determine WebSocket URL (using ws:// or wss:// based on current protocol)
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // If backend runs on port 8080 during local dev
    const host = window.location.hostname;
    const wsUrl = `${protocol}//${host}:8080/ws`;

    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        setIsConnected(true);
        console.log('[JALTRACKER WS] Connected to live STOMP broker at', wsUrl);

        // 1. Subscribe to global notification channel
        client.subscribe('/topic/notifications', (message) => {
          try {
            const data = JSON.parse(message.body);
            handleIncomingNotification(data);
          } catch (err) {
            console.error('[JALTRACKER WS] Error parsing message:', err);
          }
        });

        // 2. Subscribe to user-specific private channel if logged in
        if (user?.id) {
          client.subscribe(`/topic/users/${user.id}`, (message) => {
            try {
              const data = JSON.parse(message.body);
              handleIncomingNotification(data);
            } catch (err) {
              console.error('[JALTRACKER WS] Error parsing user message:', err);
            }
          });
        }
      },
      onDisconnect: () => {
        setIsConnected(false);
        console.log('[JALTRACKER WS] Disconnected from STOMP broker.');
      },
      onStompError: (frame) => {
        console.warn('[JALTRACKER WS] STOMP Protocol Error:', frame.headers['message']);
      },
    });

    try {
      client.activate();
      stompClientRef.current = client;
    } catch (err) {
      console.warn('[JALTRACKER WS] Failed to activate STOMP client:', err);
    }

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [user?.id]);

  const handleIncomingNotification = (notif) => {
    const item = {
      id: notif.id || Date.now().toString(),
      type: notif.type || 'INFO',
      title: notif.title || 'Live System Update',
      message: notif.message || '',
      timestamp: notif.timestamp || new Date().toISOString(),
      payload: notif.payload || {},
      read: false,
    };

    setNotifications((prev) => [item, ...prev]);
    setActiveToast(item);

    // Auto-dismiss active toast after 6 seconds
    setTimeout(() => {
      setActiveToast((current) => (current?.id === item.id ? null : current));
    }, 6000);
  };

  const markAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const dismissToast = () => {
    setActiveToast(null);
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <WebSocketContext.Provider
      value={{
        isConnected,
        notifications,
        unreadCount,
        markAllRead,
        dismissToast,
      }}
    >
      {children}

      {/* Real-time Push Notification Floating Toast Banner */}
      {activeToast && (
        <div className="fixed top-5 right-5 z-50 max-w-md w-full animate-in fade-in slide-in-from-top-4 duration-300">
          <div className="bg-slate-900 text-white rounded-2xl shadow-2xl border border-slate-700/80 p-4 flex items-start gap-3.5 backdrop-blur-md bg-slate-900/95">
            <div className="p-2.5 rounded-xl bg-brand-600/20 text-brand-400 border border-brand-500/30 shrink-0 mt-0.5">
              {activeToast.type === 'PAYMENT_SUCCESS' ? (
                <IndianRupee className="w-5 h-5 text-emerald-400" />
              ) : activeToast.type === 'USER_APPROVAL' ? (
                <ShieldCheck className="w-5 h-5 text-sky-400" />
              ) : activeToast.type === 'BILL_FINALIZED' ? (
                <Sparkles className="w-5 h-5 text-amber-400" />
              ) : (
                <Bell className="w-5 h-5 text-brand-400" />
              )}
            </div>

            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between gap-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-brand-400">
                  {activeToast.type.replace('_', ' ')}
                </span>
                <span className="text-[10px] text-slate-400">Just now</span>
              </div>
              <h4 className="text-sm font-bold text-white truncate mt-0.5">{activeToast.title}</h4>
              <p className="text-xs text-slate-300 mt-1 leading-relaxed">{activeToast.message}</p>
            </div>

            <button
              onClick={dismissToast}
              className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition shrink-0"
              aria-label="Dismiss Notification"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};

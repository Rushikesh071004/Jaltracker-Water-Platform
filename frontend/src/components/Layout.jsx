import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import {
  Droplets,
  LayoutDashboard,
  Truck,
  Sliders,
  FileSpreadsheet,
  CalendarDays,
  BarChart3,
  Receipt,
  Users,
  LogOut,
  Menu,
  X,
  Bell,
  Sparkles,
  ShieldCheck,
  UserCheck,
  Compass,
  Radio,
  Check,
} from 'lucide-react';

export const Layout = () => {
  const { user, isAdmin, isSuperAdmin, logout } = useAuth();
  const { isConnected, notifications, unreadCount, markAllRead } = useWebSocket();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [notifMenuOpen, setNotifMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const adminNav = [
    { name: 'Dashboard', to: '/admin/dashboard', icon: LayoutDashboard },
    { name: 'Account Verifications', to: '/admin/verifications', icon: ShieldCheck },
    { name: 'Bulk Water Purchases', to: '/admin/bulk-purchases', icon: Truck },
    { name: 'Tariff Rate Config', to: '/admin/tariffs', icon: Sliders },
    { name: 'Meter Readings & Logs', to: '/admin/meter-readings', icon: FileSpreadsheet },
    { name: 'Billing Cycles', to: '/admin/billing-cycles', icon: CalendarDays },
    { name: 'Usage Comparison', to: '/admin/usage-comparison', icon: BarChart3 },
    { name: 'Households & Meters', to: '/admin/households', icon: Users },
  ];

  const residentNav = [
    { name: 'Jal Dashboard', to: '/resident/dashboard', icon: LayoutDashboard },
    { name: 'Comparative Usage', to: '/resident/comparison', icon: Compass },
    { name: 'Invoices & Razorpay', to: '/resident/invoices', icon: Receipt },
  ];

  const navItems = isAdmin ? adminNav : residentNav;

  return (
    <div className="min-h-screen flex bg-slate-50 text-slate-800">
      {/* Sidebar for Desktop */}
      <aside className="hidden lg:flex lg:flex-col lg:w-64 bg-slate-900 text-slate-200 border-r border-slate-800 shrink-0">
        {/* Brand Header */}
        <div className="h-16 flex items-center px-6 gap-3 border-b border-slate-800 bg-slate-950/60">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-sky-400 flex items-center justify-center text-white shadow-lg shadow-brand-500/20">
            <Droplets className="w-6 h-6" />
          </div>
          <div>
            <span className="font-bold text-lg tracking-tight text-white block">JALTRACKER</span>
            <span className="text-[10px] text-brand-400 font-semibold tracking-wide uppercase">
              {isSuperAdmin ? 'Master Superadmin' : isAdmin ? 'Society Admin' : 'Resident Portal'}
            </span>
          </div>
        </div>

        {/* Current Apartment Banner */}
        <div className="px-5 py-4 bg-slate-800/40 border-b border-slate-800">
          <p className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">Registered Society</p>
          <p className="text-sm font-medium text-white truncate mt-0.5">
            {user?.apartmentName || 'Shanti Niketan Heights'}
          </p>
          {!isAdmin && user?.flatNumber && (
            <span className="inline-flex items-center mt-1 px-2 py-0.5 rounded text-xs font-medium bg-brand-500/20 text-brand-300 border border-brand-500/30">
              Unit {user.flatNumber}
            </span>
          )}
        </div>

        {/* Navigation Links */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 ${
                    isActive
                      ? 'bg-brand-600 text-white shadow-md shadow-brand-600/30'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/70'
                  }`
                }
              >
                <Icon className="w-5 h-5 shrink-0" />
                <span>{item.name}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* User Card & Logout */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/40">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-9 h-9 rounded-full bg-slate-700 flex items-center justify-center text-white text-sm font-bold">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{user?.fullName}</p>
              <p className="text-xs text-slate-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 text-xs font-semibold rounded-lg bg-slate-800 hover:bg-rose-950/40 text-slate-300 hover:text-rose-400 border border-slate-700/60 transition"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Navbar */}
        <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 sm:px-6 z-10 shrink-0">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-2 rounded-lg text-slate-600 hover:bg-slate-100"
              aria-label="Toggle Navigation"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
            <div className="flex items-center gap-2">
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></div>
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider hidden sm:inline">
                Live Water Metering Network
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3 sm:gap-4">
            <div className="hidden md:flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 border border-slate-200 text-xs text-slate-600">
              {isSuperAdmin ? (
                <>
                  <ShieldCheck className="w-4 h-4 text-purple-600" />
                  <span className="font-semibold text-purple-900">Master Superadmin</span>
                </>
              ) : isAdmin ? (
                <>
                  <ShieldCheck className="w-4 h-4 text-brand-600" />
                  <span className="font-semibold text-slate-800">Society Administrator</span>
                </>
              ) : (
                <>
                  <UserCheck className="w-4 h-4 text-emerald-600" />
                  <span className="font-semibold text-slate-800">Resident ({user?.flatNumber || 'Flat'})</span>
                </>
              )}
            </div>

            {/* Live STOMP WebSocket Bell & Dropdown */}
            <div className="relative">
              <button
                onClick={() => setNotifMenuOpen(!notifMenuOpen)}
                className="relative p-2 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition"
                title={isConnected ? 'Real-Time WebSocket Connected' : 'WebSocket Reconnecting...'}
                aria-label="View notifications"
              >
                <Bell className="w-5 h-5" />
                {isConnected && (
                  <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-500 ring-2 ring-white" />
                )}
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 px-1.5 py-0.5 text-[10px] font-bold bg-rose-500 text-white rounded-full min-w-[18px] text-center shadow-sm">
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              {/* Notification Popover Dropdown */}
              {notifMenuOpen && (
                <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-2xl shadow-xl border border-slate-200 z-50 overflow-hidden animate-in fade-in slide-in-from-top-2 duration-150">
                  <div className="p-3.5 border-b border-slate-100 flex items-center justify-between bg-slate-50">
                    <div className="flex items-center gap-2">
                      <Radio className={`w-3.5 h-3.5 ${isConnected ? 'text-emerald-500 animate-pulse' : 'text-amber-500'}`} />
                      <span className="text-xs font-bold text-slate-800">Live Notifications</span>
                    </div>
                    {unreadCount > 0 && (
                      <button
                        onClick={markAllRead}
                        className="text-[11px] text-brand-600 hover:text-brand-700 font-semibold flex items-center gap-1"
                      >
                        <Check className="w-3 h-3" />
                        <span>Mark all read</span>
                      </button>
                    )}
                  </div>

                  <div className="max-h-80 overflow-y-auto divide-y divide-slate-100">
                    {notifications.length === 0 ? (
                      <div className="p-6 text-center text-slate-400">
                        <Bell className="w-8 h-8 mx-auto text-slate-300 mb-2 opacity-50" />
                        <p className="text-xs">No notifications yet</p>
                        <p className="text-[10px] text-slate-400 mt-0.5">Real-time alerts will appear here automatically</p>
                      </div>
                    ) : (
                      notifications.slice(0, 10).map((n) => (
                        <div
                          key={n.id}
                          className={`p-3 text-xs transition ${
                            n.read ? 'bg-white' : 'bg-sky-50/50 font-medium'
                          } hover:bg-slate-50`}
                        >
                          <div className="flex items-start justify-between gap-2">
                            <span className="text-[10px] font-bold text-brand-600 uppercase">
                              {n.type?.replace('_', ' ')}
                            </span>
                            <span className="text-[10px] text-slate-400">
                              {new Date(n.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          <p className="font-semibold text-slate-800 mt-0.5">{n.title}</p>
                          <p className="text-slate-600 text-[11px] mt-0.5 line-clamp-2">{n.message}</p>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>

            <div className="flex items-center gap-2">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-semibold text-slate-800 leading-tight">{user?.fullName}</p>
                <p className="text-xs text-slate-500">{user?.role?.replace('ROLE_', '')}</p>
              </div>
              <div className="w-9 h-9 rounded-full bg-gradient-to-tr from-brand-600 to-sky-500 text-white flex items-center justify-center font-bold text-sm shadow-sm">
                {user?.fullName?.charAt(0) || 'U'}
              </div>
            </div>
          </div>
        </header>

        {/* Mobile Navigation Drawer */}
        {mobileMenuOpen && (
          <div className="lg:hidden bg-slate-900 text-slate-200 border-b border-slate-800 px-4 py-3 space-y-1">
            <div className="pb-2 mb-2 border-b border-slate-800 text-xs text-slate-400">
              Complex: <span className="text-white font-medium">{user?.apartmentName}</span>
            </div>
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={() => setMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium ${
                      isActive ? 'bg-brand-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
                    }`
                  }
                >
                  <Icon className="w-5 h-5 shrink-0" />
                  <span>{item.name}</span>
                </NavLink>
              );
            })}
            <div className="pt-2 mt-2 border-t border-slate-800">
              <button
                onClick={handleLogout}
                className="w-full flex items-center gap-2 px-3 py-2 text-sm text-rose-400 hover:bg-rose-950/30 rounded-lg"
              >
                <LogOut className="w-4 h-4" />
                <span>Sign Out</span>
              </button>
            </div>
          </div>
        )}

        {/* Page Content Body */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

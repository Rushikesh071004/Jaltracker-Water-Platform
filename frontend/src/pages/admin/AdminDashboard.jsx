import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import {
  Droplets,
  Truck,
  Sliders,
  FileSpreadsheet,
  CalendarDays,
  BarChart3,
  AlertTriangle,
  CheckCircle,
  RefreshCw,
  ArrowRight,
  ShieldAlert,
  Flame,
  Activity,
  Users,
  UserCheck,
} from 'lucide-react';
import { SkeletonCard } from '../../components/skeleton/SkeletonCard';
import { SkeletonTable } from '../../components/skeleton/SkeletonTable';
import { SkeletonChart } from '../../components/skeleton/SkeletonChart';

export const AdminDashboard = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [alerts, setAlerts] = useState([]);
  const [activeCycle, setActiveCycle] = useState(null);
  const [comparisonData, setComparisonData] = useState(null);
  const [scanning, setScanning] = useState(false);
  const [statusMsg, setStatusMsg] = useState('');

  const apartmentId = user?.apartmentId || 1;

  const fetchData = async () => {
    setLoading(true);
    try {
      const [cyclesRes, alertsRes, compRes] = await Promise.all([
        api.get(`/admin/billing/cycles?apartmentId=${apartmentId}`),
        api.get(`/alerts?apartmentId=${apartmentId}`),
        api.get(`/admin/analytics/usage-comparison?apartmentId=${apartmentId}`),
      ]);

      const cycles = Array.isArray(cyclesRes.data) ? cyclesRes.data : [];
      const openCycle = cycles.find((c) => c.status === 'OPEN') || cycles[0] || null;
      setActiveCycle(openCycle);

      const alertList = Array.isArray(alertsRes.data) ? alertsRes.data : [];
      setAlerts(alertList.filter((a) => a.status !== 'RESOLVED'));
      setComparisonData(compRes.data || null);
    } catch (err) {
      console.error('Failed to load admin overview:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [apartmentId]);

  const handleTriggerScan = async () => {
    setScanning(true);
    setStatusMsg('');
    try {
      const res = await api.post('/alerts/scan');
      setStatusMsg(res.data.message || 'Anomaly scan completed.');
      fetchData();
    } catch (err) {
      setStatusMsg('Failed to run anomaly scan.');
    } finally {
      setScanning(false);
    }
  };

  const handleAcknowledgeAlert = async (alertId) => {
    try {
      await api.patch(`/alerts/${alertId}/status`, { status: 'RESOLVED' });
      setAlerts((prev) => prev.filter((a) => a.id !== alertId));
    } catch (err) {
      console.error('Failed to update alert:', err);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-32 bg-slate-200/70 rounded-2xl animate-pulse" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <SkeletonCard count={4} />
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <SkeletonChart title="Water Usage Analytics" />
          </div>
          <div>
            <SkeletonTable rows={4} cols={3} />
          </div>
        </div>
      </div>
    );
  }

  const criticalLeaks = alerts.filter((a) => a.alertType === 'LEAK_DETECTED_2SIGMA');
  const totalWaterPurchasedCost = activeCycle?.totalBulkWaterCost || 0;
  const totalConsumedKl = comparisonData?.totalApartmentUsageKl || 0;

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 bg-gradient-to-r from-slate-900 to-slate-800 text-white p-6 rounded-2xl shadow-lg border border-slate-700/60">
        <div>
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-brand-500/20 text-brand-300 border border-brand-500/30 mb-2">
            Operations & Billing Hub
          </span>
          <h1 className="text-2xl font-bold tracking-tight">Apartment Administration Console</h1>
          <p className="text-sm text-slate-300 mt-1">
            Managing <span className="text-white font-medium">{user?.apartmentName || 'Shanti Niketan Heights'}</span> &bull; 7 Flats (Metered & Flat Area Units)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleTriggerScan}
            disabled={scanning}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-600/30 transition disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${scanning ? 'animate-spin' : ''}`} />
            <span>{scanning ? 'Analyzing Outliers...' : 'Run 2σ Leak Scan'}</span>
          </button>
        </div>
      </div>

      {statusMsg && (
        <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between">
          <span>{statusMsg}</span>
          <button onClick={() => setStatusMsg('')} className="text-xs font-semibold text-emerald-700 hover:underline">
            Dismiss
          </button>
        </div>
      )}

      {/* Critical Leak Alerts Banner if any */}
      {criticalLeaks.length > 0 && (
        <div className="p-4 rounded-2xl bg-rose-50 border-2 border-rose-300 shadow-sm flex items-start gap-4">
          <div className="w-10 h-10 rounded-xl bg-rose-600 text-white flex items-center justify-center shrink-0">
            <Flame className="w-6 h-6 animate-bounce" />
          </div>
          <div className="flex-1">
            <h3 className="text-base font-bold text-rose-900">
              Immediate Attention Required: {criticalLeaks.length} Potential Leak(s) Detected!
            </h3>
            <p className="text-xs text-rose-700 mt-0.5">
              The automated statistical engine identified water consumption exceeding 2 standard deviations (&gt;2&sigma;) above baseline.
            </p>
            <div className="mt-3 space-y-2">
              {criticalLeaks.map((l) => (
                <div key={l.id} className="flex flex-col sm:flex-row sm:items-center justify-between bg-white p-3 rounded-xl border border-rose-200 text-xs gap-2">
                  <div>
                    <span className="font-bold text-slate-900">Unit {l.household?.flatNumber || 'A-101'}:</span>{' '}
                    <span className="text-slate-600">{l.message}</span>
                  </div>
                  <button
                    onClick={() => handleAcknowledgeAlert(l.id)}
                    className="px-3 py-1 bg-rose-100 hover:bg-rose-200 text-rose-800 font-semibold rounded-lg shrink-0 transition"
                  >
                    Mark Resolved
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Active Cycle</span>
            <span className="w-8 h-8 rounded-lg bg-sky-50 text-brand-600 flex items-center justify-center">
              <CalendarDays className="w-4 h-4" />
            </span>
          </div>
          <p className="text-xl font-bold text-slate-900 mt-2 truncate">
            {activeCycle?.cycleName || 'No Open Cycle'}
          </p>
          <span className="inline-block mt-1 px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-100 text-emerald-800">
            Status: {activeCycle?.status || 'N/A'}
          </span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Bulk Water Cost</span>
            <span className="w-8 h-8 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
              <Truck className="w-4 h-4" />
            </span>
          </div>
          <p className="text-2xl font-bold text-slate-900 mt-2">
            ₹{Number(totalWaterPurchasedCost).toFixed(2)}
          </p>
          <p className="text-xs text-slate-500 mt-1">Tanker & municipal deliveries</p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Metered Consumption</span>
            <span className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
              <Droplets className="w-4 h-4" />
            </span>
          </div>
          <p className="text-2xl font-bold text-slate-900 mt-2">
            {Number(totalConsumedKl).toFixed(2)} <span className="text-sm font-normal text-slate-500">kL</span>
          </p>
          <p className="text-xs text-slate-500 mt-1">
            Avg: {comparisonData?.apartmentAverageUsageKl ? Number(comparisonData.apartmentAverageUsageKl).toFixed(1) : 0} kL / unit
          </p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Pending Alerts</span>
            <span className="w-8 h-8 rounded-lg bg-rose-50 text-rose-600 flex items-center justify-center">
              <AlertTriangle className="w-4 h-4" />
            </span>
          </div>
          <p className="text-2xl font-bold text-slate-900 mt-2">{alerts.length}</p>
          <p className="text-xs text-slate-500 mt-1">
            {criticalLeaks.length} critical leak &bull; {alerts.length - criticalLeaks.length} threshold overuse
          </p>
        </div>
      </div>

      {/* Admin Operations Navigation Cards */}
      <div>
        <h2 className="text-base font-bold text-slate-900 mb-3">Core Operational Engines</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Link
            to="/admin/bulk-purchases"
            className="group p-5 bg-white rounded-2xl border border-slate-200 hover:border-brand-500 shadow-sm hover:shadow-md transition flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center mb-3 group-hover:scale-110 transition">
                <Truck className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 group-hover:text-brand-600 transition">Bulk Water Purchases</h3>
              <p className="text-xs text-slate-500 mt-1">
                Record tanker deliveries and municipal bulk supply volume and unit costs to apportion shared pool.
              </p>
            </div>
            <div className="mt-4 flex items-center text-xs font-semibold text-brand-600 gap-1">
              <span>Manage Purchases</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
            </div>
          </Link>

          <Link
            to="/admin/tariffs"
            className="group p-5 bg-white rounded-2xl border border-slate-200 hover:border-brand-500 shadow-sm hover:shadow-md transition flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center mb-3 group-hover:scale-110 transition">
                <Sliders className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 group-hover:text-brand-600 transition">Tariff Rate Configuration</h3>
              <p className="text-xs text-slate-500 mt-1">
                Configure progressive rate tiers (e.g. base rate for first 10 kL, conservation rates for excess).
              </p>
            </div>
            <div className="mt-4 flex items-center text-xs font-semibold text-brand-600 gap-1">
              <span>Edit Rate Tiers</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
            </div>
          </Link>

          <Link
            to="/admin/meter-readings"
            className="group p-5 bg-white rounded-2xl border border-slate-200 hover:border-brand-500 shadow-sm hover:shadow-md transition flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center mb-3 group-hover:scale-110 transition">
                <FileSpreadsheet className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 group-hover:text-brand-600 transition">Meter Reading Ingestion</h3>
              <p className="text-xs text-slate-500 mt-1">
                Manual daily household entry and bulk CSV upload with duplicate detection and error diagnostics.
              </p>
            </div>
            <div className="mt-4 flex items-center text-xs font-semibold text-brand-600 gap-1">
              <span>Upload Readings</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
            </div>
          </Link>

          <Link
            to="/admin/billing-cycles"
            className="group p-5 bg-white rounded-2xl border border-slate-200 hover:border-brand-500 shadow-sm hover:shadow-md transition flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-600 flex items-center justify-center mb-3 group-hover:scale-110 transition">
                <CalendarDays className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 group-hover:text-brand-600 transition">Billing Cycle Controls</h3>
              <p className="text-xs text-slate-500 mt-1">
                Open, preview calculations, finalize, and archive billing cycles. Generates invoices & dispatches email alerts.
              </p>
            </div>
            <div className="mt-4 flex items-center text-xs font-semibold text-brand-600 gap-1">
              <span>Control Cycles</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
            </div>
          </Link>

          <Link
            to="/admin/usage-comparison"
            className="group p-5 bg-white rounded-2xl border border-slate-200 hover:border-brand-500 shadow-sm hover:shadow-md transition flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-indigo-500/10 text-indigo-600 flex items-center justify-center mb-3 group-hover:scale-110 transition">
                <BarChart3 className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 group-hover:text-brand-600 transition">Usage Comparison Charts</h3>
              <p className="text-xs text-slate-500 mt-1">
                Interactive bar charts comparing consumption across all households, wings, and identifying outlier units.
              </p>
            </div>
            <div className="mt-4 flex items-center text-xs font-semibold text-brand-600 gap-1">
              <span>View Analytics</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
            </div>
          </Link>

          <Link
            to="/admin/households"
            className="group p-5 bg-white rounded-2xl border border-slate-200 hover:border-brand-500 shadow-sm hover:shadow-md transition flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-rose-500/10 text-rose-600 flex items-center justify-center mb-3 group-hover:scale-110 transition">
                <Users className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 group-hover:text-brand-600 transition">Households & Meters</h3>
              <p className="text-xs text-slate-500 mt-1">
                Register new flats, assign residents, configure water meter serial numbers, and set usage limits.
              </p>
            </div>
            <div className="mt-4 flex items-center text-xs font-semibold text-brand-600 gap-1">
              <span>Manage Directory</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
            </div>
          </Link>

          <Link
            to="/admin/verifications"
            className="group p-5 bg-white rounded-2xl border border-slate-200 hover:border-brand-500 shadow-sm hover:shadow-md transition flex flex-col justify-between"
          >
            <div>
              <div className="w-10 h-10 rounded-xl bg-teal-500/10 text-teal-600 flex items-center justify-center mb-3 group-hover:scale-110 transition">
                <UserCheck className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-slate-900 group-hover:text-brand-600 transition">Account Verifications</h3>
              <p className="text-xs text-slate-500 mt-1">
                Review pending Resident & Admin onboarding requests, approve authentic registrations, or reject unauthorized signups.
              </p>
            </div>
            <div className="mt-4 flex items-center text-xs font-semibold text-brand-600 gap-1">
              <span>Review Requests</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
};

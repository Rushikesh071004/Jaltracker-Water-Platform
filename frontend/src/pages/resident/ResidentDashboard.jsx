import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import { RazorpayModal } from '../../components/RazorpayModal';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  ReferenceLine,
} from 'recharts';
import {
  Droplets,
  Calendar,
  Flame,
  Lightbulb,
  Receipt,
  Download,
  AlertTriangle,
  ArrowRight,
  Sparkles,
  TrendingUp,
  ShieldCheck,
  CheckCircle2,
  CreditCard,
  Clock,
  IndianRupee,
} from 'lucide-react';
import { SkeletonCard } from '../../components/skeleton/SkeletonCard';
import { SkeletonChart } from '../../components/skeleton/SkeletonChart';
import { SkeletonTable } from '../../components/skeleton/SkeletonTable';

export const ResidentDashboard = () => {
  const { user } = useAuth();
  const householdId = user?.householdId || 1;

  const [loading, setLoading] = useState(true);
  const [comparison, setComparison] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [selectedInvoiceForPayment, setSelectedInvoiceForPayment] = useState(null);
  const [showRazorpayModal, setShowRazorpayModal] = useState(false);
  const [paymentBannerFeedback, setPaymentBannerFeedback] = useState(null);

  const fetchData = async () => {
    try {
      const [compRes, invRes, alertsRes] = await Promise.all([
        api.get(`/resident/analytics/comparison?householdId=${householdId}`),
        api.get(`/invoices?householdId=${householdId}`),
        api.get(`/alerts?householdId=${householdId}`),
      ]);
      setComparison(compRes.data);
      setInvoices(invRes.data);
      setAlerts(alertsRes.data.filter((a) => a.status !== 'RESOLVED'));
    } catch (err) {
      console.error('Failed to load resident dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [householdId]);

  const waterSavingTips = [
    {
      title: 'Fix Dripping Faucets & Flappers',
      savings: 'Up to 75 Liters / Day',
      category: 'Bathroom',
      description: 'A slow toilet leak or faucet drip often wastes more than 2,000 liters monthly. Drop food dye in your cistern to check for silent leaks.',
    },
    {
      title: 'Install High-Efficiency Aerators',
      savings: 'Save 30% - 50% Sink Water',
      category: 'Kitchen & Bath',
      description: 'Low-flow aerators mix air with water to maintain high pressure while cutting volume from 8 L/min down to 3.5 L/min.',
    },
    {
      title: 'Full Appliance Loads Only',
      savings: 'Save ~40 Liters / Cycle',
      category: 'Laundry & Dishwashing',
      description: 'Running washing machines and dishwashers only when completely loaded maximizes water efficiency per kilogram of clothes.',
    },
    {
      title: '5-Minute Showers',
      savings: 'Save ~35 Liters / Shower',
      category: 'Daily Habit',
      description: 'Reducing shower duration by just 2 minutes saves over 1,000 liters per occupant each month.',
    },
  ];

  const downloadPdf = async (invoiceId, invoiceNumber) => {
    try {
      const res = await api.get(`/invoices/${invoiceId}/download`, { responseType: 'blob' });
      const blob = new Blob([res.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `invoice_${invoiceNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      alert('Failed to download invoice PDF');
    }
  };

  const handleOpenPayModal = (inv) => {
    setSelectedInvoiceForPayment(inv);
    setShowRazorpayModal(true);
  };

  const handlePaymentSuccess = (verifyData) => {
    setPaymentBannerFeedback({
      message: `Payment of ₹${selectedInvoiceForPayment?.totalAmount} successfully recorded! Transaction: ${verifyData.paymentId || 'Verified'}.`,
    });
    fetchData();
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
            <SkeletonChart title="Daily Household Water Consumption" />
          </div>
          <div>
            <SkeletonTable rows={4} cols={3} />
          </div>
        </div>
      </div>
    );
  }

  const latestInvoice = invoices[0];
  const chartData = comparison?.dailyTrend || [];
  const activeAlert = alerts[0];

  // Helper to determine status styling
  const isPaid = latestInvoice?.paymentStatus === 'PAID';
  const isOverdue = latestInvoice?.paymentStatus === 'OVERDUE';
  const isUnpaid = latestInvoice && (latestInvoice.paymentStatus === 'PENDING' || !isPaid);

  return (
    <div className="space-y-6">
      {/* Top Welcome Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-sky-950 to-slate-900 text-white p-6 rounded-2xl shadow-lg border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-brand-500/20 text-brand-300 border border-brand-500/30">
              Unit {comparison?.flatNumber || user?.flatNumber || 'A-101'}
            </span>
            <span className="text-xs text-slate-400 font-medium">{user?.apartmentName || 'Shanti Niketan Heights'}</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight">Welcome back, {user?.fullName || 'Rushikesh Waghmare'}!</h1>
          <p className="text-sm text-slate-300 mt-1">
            Track daily water consumption, view monthly bills, and settle payments securely with Razorpay.
          </p>
        </div>

        {/* Eco Status Pill */}
        <div className="bg-slate-800/80 p-3.5 rounded-xl border border-slate-700/80 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[10px] uppercase font-semibold text-slate-400 block tracking-wider">Conservation Rating</span>
            <span className="text-sm font-bold text-white">{comparison?.conservationStatus || 'OPTIMAL'}</span>
          </div>
        </div>
      </div>

      {/* Payment Success Alert Notification */}
      {paymentBannerFeedback && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-300 text-emerald-900 shadow-sm flex items-center justify-between">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="w-6 h-6 text-emerald-600 shrink-0" />
            <div>
              <h4 className="font-bold text-sm">Payment Confirmed via Razorpay</h4>
              <p className="text-xs text-emerald-800 mt-0.5">{paymentBannerFeedback.message}</p>
            </div>
          </div>
          <button
            onClick={() => setPaymentBannerFeedback(null)}
            className="text-emerald-700 hover:text-emerald-900 text-xs font-bold px-2 py-1"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Requirement 4: Prominent Payment Status Card */}
      {latestInvoice && (
        <div
          className={`p-5 rounded-2xl border shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4 transition ${
            isPaid
              ? 'bg-emerald-50/70 border-emerald-200 text-emerald-950'
              : isOverdue
              ? 'bg-rose-50 border-rose-300 text-rose-950 ring-1 ring-rose-400/30'
              : 'bg-amber-50/80 border-amber-300 text-amber-950 ring-1 ring-amber-400/30'
          }`}
        >
          <div className="flex items-start md:items-center gap-4">
            <div
              className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 ${
                isPaid
                  ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-600/30'
                  : isOverdue
                  ? 'bg-rose-600 text-white shadow-lg shadow-rose-600/30 animate-pulse'
                  : 'bg-amber-500 text-white shadow-lg shadow-amber-500/30'
              }`}
            >
              {isPaid ? (
                <CheckCircle2 className="w-6 h-6" />
              ) : isOverdue ? (
                <AlertTriangle className="w-6 h-6" />
              ) : (
                <Clock className="w-6 h-6" />
              )}
            </div>

            <div>
              <div className="flex items-center gap-2">
                <span
                  className={`px-2.5 py-0.5 rounded-full text-[11px] font-black uppercase tracking-wider ${
                    isPaid
                      ? 'bg-emerald-200 text-emerald-900'
                      : isOverdue
                      ? 'bg-rose-200 text-rose-900'
                      : 'bg-amber-200 text-amber-900'
                  }`}
                >
                  Status: {isPaid ? 'PAID' : isOverdue ? 'OVERDUE' : 'UNPAID'}
                </span>
                <span className="text-xs font-mono text-slate-500 font-semibold">
                  Statement #{latestInvoice.invoiceNumber}
                </span>
              </div>

              <div className="flex items-baseline gap-2 mt-1">
                <span className="text-2xl font-black font-mono tracking-tight text-slate-900">
                  ₹{Number(latestInvoice.totalAmount).toFixed(2)}
                </span>
                <span className="text-xs text-slate-600 font-medium">
                  {isPaid
                    ? `Settled on ${new Date(latestInvoice.paymentDate || latestInvoice.createdAt).toLocaleDateString('en-IN')}`
                    : `Due by ${latestInvoice.billingCycle?.dueDate || 'End of Month'}`}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            {/* If UNPAID or OVERDUE: Display prominent "Pay Now" Razorpay button */}
            {!isPaid && (
              <button
                type="button"
                onClick={() => handleOpenPayModal(latestInvoice)}
                className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-600 to-sky-700 hover:from-sky-500 hover:to-sky-600 text-white font-bold text-xs shadow-lg shadow-sky-600/25 flex items-center gap-2 transition hover:scale-[1.02]"
              >
                <CreditCard className="w-4 h-4" />
                <span>Pay Now via Razorpay</span>
              </button>
            )}

            <button
              onClick={() => downloadPdf(latestInvoice.id, latestInvoice.invoiceNumber)}
              className="px-4 py-2.5 rounded-xl bg-white hover:bg-slate-50 border border-slate-200 text-slate-700 font-semibold text-xs shadow-sm flex items-center gap-1.5 transition"
            >
              <Download className="w-4 h-4 text-slate-500" />
              <span>Download PDF</span>
            </button>
          </div>
        </div>
      )}

      {/* Critical Leak / Anomaly Banner */}
      {activeAlert && (
        <div className="p-4 rounded-2xl bg-rose-50 border border-rose-300 shadow-sm flex items-start gap-4">
          <div className="w-10 h-10 rounded-xl bg-rose-600 text-white flex items-center justify-center shrink-0">
            <Flame className="w-5 h-5 animate-pulse" />
          </div>
          <div className="flex-1">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-rose-900">{activeAlert.title}</h3>
              <span className="text-xs font-semibold text-rose-700 bg-rose-100 px-2 py-0.5 rounded">
                Logged on {activeAlert.readingDate}
              </span>
            </div>
            <p className="text-xs text-rose-800 mt-1">{activeAlert.message}</p>
            <div className="mt-2 text-xs font-semibold text-rose-900 flex items-center gap-2">
              <span>Recommendation: Check toilet flapper valves, inspect bathroom lines, and report to maintenance.</span>
            </div>
          </div>
        </div>
      )}

      {/* Grid: Current Cycle Summary + Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Cycle Summary Card */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                Current 30-Day Cycle
              </span>
              <Droplets className="w-5 h-5 text-brand-600" />
            </div>

            <div className="mt-4">
              <span className="text-3xl font-extrabold text-slate-900 font-mono">
                {Number(comparison?.householdUsageKl || 0).toFixed(2)}
              </span>
              <span className="text-sm font-medium text-slate-500 ml-1.5">kL consumed</span>
            </div>

            {/* Benchmark vs Apartment Average */}
            <div className="mt-4 p-3 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-1.5">
              <div className="flex justify-between">
                <span className="text-slate-500">Society Average:</span>
                <span className="font-mono font-bold text-slate-800">
                  {Number(comparison?.apartmentAverageKl || 0).toFixed(2)} kL
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Similar Sized Units:</span>
                <span className="font-mono font-bold text-slate-800">
                  {Number(comparison?.similarSizeAverageKl || 0).toFixed(2)} kL
                </span>
              </div>
            </div>

            {/* Quick Invoice Summary */}
            {latestInvoice && (
              <div className="mt-4 pt-4 border-t border-slate-100">
                <span className="text-[11px] uppercase tracking-wider font-semibold text-slate-400 block mb-1">
                  Latest Statement ({latestInvoice.billingCycle?.cycleName || 'Current'})
                </span>
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-xl font-bold font-mono text-slate-900">
                      ₹{Number(latestInvoice.totalAmount).toFixed(2)}
                    </span>
                    <span className="block text-[11px] text-slate-500">
                      Due: {latestInvoice.billingCycle?.dueDate || 'End of Month'}
                    </span>
                  </div>
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      isPaid
                        ? 'bg-emerald-100 text-emerald-800'
                        : isOverdue
                        ? 'bg-rose-100 text-rose-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {isPaid ? 'PAID' : isOverdue ? 'OVERDUE' : 'UNPAID'}
                  </span>
                </div>
              </div>
            )}
          </div>

          <div className="mt-6 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
            <Link to="/resident/comparison" className="font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1">
              <span>Detailed Comparison</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
            <Link to="/resident/invoices" className="font-semibold text-slate-700 hover:text-slate-900 flex items-center gap-1">
              <Receipt className="w-3.5 h-3.5" />
              <span>All Bills</span>
            </Link>
          </div>
        </div>

        {/* Daily Consumption Trend Line Chart */}
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h2 className="font-bold text-slate-900 text-base">Daily Consumption Trend (Last 30 Days)</h2>
              <p className="text-xs text-slate-500">Identifies daily volume fluctuations and statistical outlier spikes.</p>
            </div>
            <div className="flex items-center gap-3 text-xs">
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-brand-600"></span>
                <span className="text-slate-600">Daily kL</span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 bg-rose-500"></span>
                <span className="text-slate-600">Threshold (1.5 kL)</span>
              </div>
            </div>
          </div>

          <div className="h-64 sm:h-72 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 10 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis
                  dataKey="date"
                  stroke="#94a3b8"
                  fontSize={10}
                  tickFormatter={(val) => (val ? val.slice(5) : '')} // Show MM-DD
                  tickLine={false}
                />
                <YAxis stroke="#94a3b8" fontSize={10} tickLine={false} />
                <Tooltip
                  formatter={(val) => [`${Number(val).toFixed(3)} kL`, 'Consumption']}
                  labelFormatter={(lbl) => `Date: ${lbl}`}
                  contentStyle={{ backgroundColor: '#0f172a', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <ReferenceLine y={1.5} stroke="#f43f5e" strokeDasharray="4 4" />
                <Line
                  type="monotone"
                  dataKey="consumptionKl"
                  stroke="#0284c7"
                  strokeWidth={2.5}
                  dot={{ r: 3, fill: '#0284c7' }}
                  activeDot={{ r: 6, fill: '#0ea5e9' }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Water-Saving Tips Feed */}
      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <Lightbulb className="w-5 h-5 text-amber-500" />
          <h2 className="text-base font-bold text-slate-900">Conservation Tips & Reduction Strategies</h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {waterSavingTips.map((tip, idx) => (
            <div key={idx} className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition flex flex-col justify-between">
              <div>
                <span className="inline-block px-2 py-0.5 rounded text-[10px] font-bold bg-amber-50 text-amber-800 border border-amber-200/60 mb-2">
                  {tip.category}
                </span>
                <h3 className="font-bold text-slate-900 text-sm">{tip.title}</h3>
                <p className="text-xs text-slate-500 mt-1.5 leading-relaxed">{tip.description}</p>
              </div>
              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center gap-1.5 text-xs font-bold text-emerald-600">
                <CheckCircle2 className="w-4 h-4 shrink-0" />
                <span>{tip.savings}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Razorpay Demo Checkout Modal */}
      <RazorpayModal
        isOpen={showRazorpayModal}
        onClose={() => setShowRazorpayModal(false)}
        invoice={selectedInvoiceForPayment}
        onPaymentSuccess={handlePaymentSuccess}
      />
    </div>
  );
};
export default ResidentDashboard;

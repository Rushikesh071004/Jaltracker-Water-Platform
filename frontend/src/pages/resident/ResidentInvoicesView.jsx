import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import { RazorpayModal } from '../../components/RazorpayModal';
import {
  Receipt,
  Download,
  CreditCard,
  CheckCircle2,
  Clock,
  AlertCircle,
  Building,
  QrCode,
  IndianRupee,
  X,
  ShieldCheck,
} from 'lucide-react';

export const ResidentInvoicesView = () => {
  const { user } = useAuth();
  const householdId = user?.householdId || 1;

  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [showRazorpayModal, setShowRazorpayModal] = useState(false);
  const [feedback, setFeedback] = useState({ type: '', message: '' });

  const loadInvoices = async () => {
    try {
      const res = await api.get(`/invoices?householdId=${householdId}`);
      setInvoices(res.data);
    } catch (err) {
      console.error('Failed to load invoices:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInvoices();
  }, [householdId]);

  const handleDownloadPdf = async (invoiceId, invoiceNumber) => {
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
      alert('Failed to download invoice PDF.');
    }
  };

  const handlePayClick = (inv) => {
    setSelectedInvoice(inv);
    setShowRazorpayModal(true);
  };

  const handlePaymentSuccess = (verifyData) => {
    setFeedback({
      type: 'success',
      message: `Payment for Invoice #${selectedInvoice?.invoiceNumber} verified and recorded successfully via Razorpay (Txn ID: ${verifyData.paymentId || 'Success'})!`,
    });
    loadInvoices();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Billing Statements & Invoices</h1>
          <p className="text-sm text-slate-500 mt-1">
            Review detailed consumption breakdowns, download official PDF bills, and settle payments in INR via Razorpay.
          </p>
        </div>
      </div>

      {feedback.message && (
        <div
          className={`p-4 rounded-xl text-sm flex items-center justify-between border ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
              : 'bg-rose-50 text-rose-800 border-rose-200'
          }`}
        >
          <div className="flex items-center gap-3">
            {feedback.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
            ) : (
              <AlertCircle className="w-5 h-5 text-rose-600 shrink-0" />
            )}
            <span>{feedback.message}</span>
          </div>
          <button
            onClick={() => setFeedback({ type: '', message: '' })}
            className="text-xs font-bold text-slate-500 hover:text-slate-800"
          >
            ✕
          </button>
        </div>
      )}

      {/* Invoices List Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Receipt className="w-5 h-5 text-sky-600" />
            <h2 className="font-bold text-slate-900 text-base">Payment History & Outstanding Dues</h2>
          </div>
          <span className="text-xs text-slate-400 font-medium">{invoices.length} Statements Found</span>
        </div>

        {invoices.length === 0 ? (
          <div className="py-12 text-center text-slate-400">
            <Receipt className="w-12 h-12 text-slate-300 mx-auto mb-2" />
            <p className="text-sm font-medium text-slate-600">No invoices generated for this unit yet.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-200 text-slate-400 uppercase font-semibold text-[10px] bg-slate-50/50">
                  <th className="py-3 px-4">Invoice #</th>
                  <th className="py-3 px-4">Billing Cycle</th>
                  <th className="py-3 px-4 text-right">Metered Usage</th>
                  <th className="py-3 px-4 text-right">Metered Charge</th>
                  <th className="py-3 px-4 text-right">Shared Charge</th>
                  <th className="py-3 px-4 text-right">Total Payable</th>
                  <th className="py-3 px-4">Payment Due</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {invoices.map((inv) => {
                  const isPaid = inv.paymentStatus === 'PAID';
                  const isOverdue = inv.paymentStatus === 'OVERDUE';
                  const statusLabel = isPaid ? 'PAID' : isOverdue ? 'OVERDUE' : 'UNPAID';

                  return (
                    <tr key={inv.id} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-4 font-mono font-medium text-slate-900">{inv.invoiceNumber}</td>
                      <td className="py-3 px-4 font-medium text-slate-800">
                        {inv.billingCycle?.cycleName || 'Monthly Cycle'}
                      </td>
                      <td className="py-3 px-4 text-right font-mono font-semibold text-slate-800">
                        {Number(inv.meteredConsumptionKl).toFixed(3)} kL
                      </td>
                      <td className="py-3 px-4 text-right font-mono text-slate-600">
                        ₹{Number(inv.meteredCharge).toFixed(2)}
                      </td>
                      <td className="py-3 px-4 text-right font-mono text-slate-600">
                        ₹{Number(inv.sharedCommonCharge).toFixed(2)}
                      </td>
                      <td className="py-3 px-4 text-right font-mono font-bold text-sky-700 text-sm">
                        ₹{Number(inv.totalAmount).toFixed(2)}
                      </td>
                      <td className="py-3 px-4 font-mono text-slate-600">
                        {inv.billingCycle?.dueDate || '30th of month'}
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-block px-2.5 py-0.5 rounded text-[10px] font-bold ${
                            isPaid
                              ? 'bg-emerald-100 text-emerald-800'
                              : isOverdue
                              ? 'bg-rose-100 text-rose-800'
                              : 'bg-amber-100 text-amber-800'
                          }`}
                        >
                          {statusLabel}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right space-x-2">
                        <button
                          onClick={() => handleDownloadPdf(inv.id, inv.invoiceNumber)}
                          className="inline-flex items-center gap-1 px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-lg text-xs transition"
                          title="Download PDF Bill"
                        >
                          <Download className="w-3.5 h-3.5" />
                          <span>PDF</span>
                        </button>

                        {!isPaid && (
                          <button
                            onClick={() => handlePayClick(inv)}
                            className="inline-flex items-center gap-1 px-3 py-1 bg-sky-600 hover:bg-sky-500 text-white font-semibold rounded-lg text-xs shadow-sm transition"
                          >
                            <CreditCard className="w-3.5 h-3.5" />
                            <span>Pay Now</span>
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Razorpay Demo Checkout Modal */}
      <RazorpayModal
        isOpen={showRazorpayModal}
        onClose={() => setShowRazorpayModal(false)}
        invoice={selectedInvoice}
        onPaymentSuccess={handlePaymentSuccess}
      />
    </div>
  );
};
export default ResidentInvoicesView;

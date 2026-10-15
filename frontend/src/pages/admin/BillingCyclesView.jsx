import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import {
  CalendarDays,
  Plus,
  Play,
  CheckCircle,
  Archive,
  Receipt,
  Download,
  AlertCircle,
  IndianRupee,
  Droplets,
  Layers,
  X,
  UserCheck,
} from 'lucide-react';

export const BillingCyclesView = () => {
  const { user } = useAuth();
  const apartmentId = user?.apartmentId || 1;

  const [cycles, setCycles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCycle, setSelectedCycle] = useState(null);

  // Preview & Invoices State
  const [calculationResult, setCalculationResult] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [calculating, setCalculating] = useState(false);
  const [actionFeedback, setActionFeedback] = useState({ type: '', message: '' });

  // New Cycle Modal State
  const [showNewModal, setShowNewModal] = useState(false);
  const [newCycleName, setNewCycleName] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [creatingCycle, setCreatingCycle] = useState(false);

  // Manual Bill Issuance State
  const [verifiedResidents, setVerifiedResidents] = useState([]);
  const [showManualBillModal, setShowManualBillModal] = useState(false);
  const [manualResidentId, setManualResidentId] = useState('');
  const [manualKl, setManualKl] = useState('15.00');
  const [manualMeteredCharge, setManualMeteredCharge] = useState('250.00');
  const [manualSharedCharge, setManualSharedCharge] = useState('100.00');
  const [manualNotes, setManualNotes] = useState('Monthly manual water bill assessment in INR (₹)');
  const [issuingManualBill, setIssuingManualBill] = useState(false);

  const loadVerifiedResidents = async () => {
    try {
      const res = await api.get('/users');
      const residents = (Array.isArray(res.data) ? res.data : []).filter(
        (u) => u.accountStatus === 'VERIFIED' && u.role === 'ROLE_RESIDENT'
      );
      setVerifiedResidents(residents);
      if (residents.length > 0 && !manualResidentId) {
        setManualResidentId(residents[0].id);
      }
    } catch (err) {
      console.error('Failed to load residents:', err);
    }
  };

  const loadCycles = async () => {
    try {
      const res = await api.get(`/admin/billing/cycles?apartmentId=${apartmentId}`);
      setCycles(res.data);
      if (res.data.length > 0 && !selectedCycle) {
        selectCycle(res.data[0]);
      }
    } catch (err) {
      console.error('Failed to load cycles:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCycles();
    loadVerifiedResidents();
  }, [apartmentId]);

  const selectCycle = async (cycle) => {
    setSelectedCycle(cycle);
    setCalculationResult(null);
    setInvoices([]);
    setActionFeedback({ type: '', message: '' });

    if (cycle.status === 'FINALIZED' || cycle.status === 'ARCHIVED') {
      try {
        const invRes = await api.get(`/admin/invoices?cycleId=${cycle.id}`);
        setInvoices(invRes.data);
      } catch (err) {
        console.error('Failed to load invoices for cycle:', err);
      }
    }
  };

  const handlePreviewCalculation = async () => {
    if (!selectedCycle) return;
    setCalculating(true);
    setActionFeedback({ type: '', message: '' });
    try {
      const res = await api.get(`/admin/billing/cycles/${selectedCycle.id}/calculate`);
      setCalculationResult(res.data);
    } catch (err) {
      setActionFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Failed to calculate cycle charges.',
      });
    } finally {
      setCalculating(false);
    }
  };

  const handleFinalizeCycle = async () => {
    if (!selectedCycle) return;
    if (!window.confirm(`Are you sure you want to finalize '${selectedCycle.cycleName}'? This will lock meter calculations, generate PDF invoices, and dispatch bill notifications to residents.`)) {
      return;
    }

    setCalculating(true);
    try {
      await api.post(`/admin/billing/cycles/${selectedCycle.id}/finalize`);
      setActionFeedback({
        type: 'success',
        message: `Cycle '${selectedCycle.cycleName}' finalized successfully! All invoices generated and emailed.`,
      });
      loadCycles();
    } catch (err) {
      setActionFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Failed to finalize cycle.',
      });
    } finally {
      setCalculating(false);
    }
  };

  const handleArchiveCycle = async () => {
    if (!selectedCycle) return;
    try {
      await api.post(`/admin/billing/cycles/${selectedCycle.id}/archive`);
      setActionFeedback({
        type: 'success',
        message: `Cycle '${selectedCycle.cycleName}' moved to archives.`,
      });
      loadCycles();
    } catch (err) {
      setActionFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Failed to archive cycle.',
      });
    }
  };

  const handleCreateCycle = async (e) => {
    e.preventDefault();
    setCreatingCycle(true);
    try {
      const res = await api.post('/admin/billing/cycles', {
        apartmentId,
        cycleName: newCycleName,
        startDate,
        endDate,
        dueDate,
      });
      setShowNewModal(false);
      setNewCycleName('');
      loadCycles();
      selectCycle(res.data);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create billing cycle');
    } finally {
      setCreatingCycle(false);
    }
  };

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
      alert('Failed to download PDF invoice.');
    }
  };

  const handleIssueManualBill = async (e) => {
    e.preventDefault();
    if (!manualResidentId) {
      alert('Please select a resident to bill.');
      return;
    }

    setIssuingManualBill(true);
    try {
      const totalAmount = (parseFloat(manualMeteredCharge || 0) + parseFloat(manualSharedCharge || 0)).toFixed(2);
      const res = await api.post('/admin/invoices/issue', {
        residentUserId: manualResidentId,
        billingCycleId: selectedCycle?.id || undefined,
        meteredConsumptionKl: parseFloat(manualKl || 0),
        meteredCharge: parseFloat(manualMeteredCharge || 0),
        sharedCommonCharge: parseFloat(manualSharedCharge || 0),
        totalAmount: parseFloat(totalAmount),
        notes: manualNotes,
      });

      setShowManualBillModal(false);
      setActionFeedback({
        type: 'success',
        message: `Water bill #${res.data.invoiceNumber} of ₹${totalAmount} successfully assigned and issued!`,
      });

      if (selectedCycle) {
        selectCycle(selectedCycle);
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to issue manual water bill');
    } finally {
      setIssuingManualBill(false);
    }
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
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Billing Cycle Controls</h1>
          <p className="text-sm text-slate-500 mt-1">
            Open new periods, preview consumption & shared cost distributions, finalize billing, and archive records.
          </p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          <button
            onClick={() => {
              if (verifiedResidents.length > 0 && !manualResidentId) {
                setManualResidentId(verifiedResidents[0].id);
              }
              setShowManualBillModal(true);
            }}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold shadow-md shadow-emerald-600/20 transition"
          >
            <IndianRupee className="w-4 h-4" />
            <span>Issue Water Bill</span>
          </button>

          <button
            onClick={() => setShowNewModal(true)}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-500/20 transition"
          >
            <Plus className="w-4 h-4" />
            <span>Open New Billing Cycle</span>
          </button>
        </div>
      </div>

      {actionFeedback.message && (
        <div
          className={`p-4 rounded-xl text-sm flex items-center gap-3 border ${
            actionFeedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
              : 'bg-rose-50 text-rose-800 border-rose-200'
          }`}
        >
          {actionFeedback.type === 'success' ? <CheckCircle className="w-5 h-5 text-emerald-600 shrink-0" /> : <AlertCircle className="w-5 h-5 text-rose-600 shrink-0" />}
          <span>{actionFeedback.message}</span>
        </div>
      )}

      {/* Main Grid: Cycle List on Left, Active Actions on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Cycle List */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-3">
          <h2 className="font-bold text-slate-900 text-sm uppercase tracking-wider text-slate-500 mb-2">
            Billing Periods
          </h2>
          <div className="space-y-2">
            {cycles.map((c) => {
              const isSelected = selectedCycle?.id === c.id;
              return (
                <div
                  key={c.id}
                  onClick={() => selectCycle(c)}
                  className={`p-3.5 rounded-xl border cursor-pointer transition ${
                    isSelected
                      ? 'bg-brand-50/80 border-brand-500 shadow-sm ring-1 ring-brand-500'
                      : 'bg-slate-50/60 hover:bg-slate-100 border-slate-200'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-900">{c.cycleName}</span>
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        c.status === 'OPEN'
                          ? 'bg-emerald-100 text-emerald-800'
                          : c.status === 'FINALIZED'
                          ? 'bg-blue-100 text-blue-800'
                          : 'bg-slate-200 text-slate-700'
                      }`}
                    >
                      {c.status}
                    </span>
                  </div>
                  <div className="mt-2 text-xs text-slate-500 flex justify-between font-mono">
                    <span>{c.startDate} &rarr; {c.endDate}</span>
                    <span>Due: {c.dueDate}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Selected Cycle Management Panel */}
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-6">
          {selectedCycle ? (
            <>
              {/* Cycle Status Header */}
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-slate-100 gap-3">
                <div>
                  <div className="flex items-center gap-2">
                    <h2 className="text-xl font-bold text-slate-900">{selectedCycle.cycleName}</h2>
                    <span
                      className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                        selectedCycle.status === 'OPEN'
                          ? 'bg-emerald-100 text-emerald-800'
                          : selectedCycle.status === 'FINALIZED'
                          ? 'bg-blue-100 text-blue-800'
                          : 'bg-slate-200 text-slate-700'
                      }`}
                    >
                      {selectedCycle.status}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500 mt-0.5">
                    Period: {selectedCycle.startDate} to {selectedCycle.endDate} &bull; Payment Due: {selectedCycle.dueDate}
                  </p>
                </div>

                {/* Lifecycle Action Buttons */}
                <div className="flex items-center gap-2">
                  {selectedCycle.status === 'OPEN' && (
                    <>
                      <button
                        onClick={handlePreviewCalculation}
                        disabled={calculating}
                        className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold transition"
                      >
                        <Play className="w-3.5 h-3.5" />
                        <span>Preview Rates</span>
                      </button>

                      <button
                        onClick={handleFinalizeCycle}
                        disabled={calculating}
                        className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-500/20 transition disabled:opacity-50"
                      >
                        <CheckCircle className="w-3.5 h-3.5" />
                        <span>{calculating ? 'Processing...' : 'Finalize & Issue Invoices'}</span>
                      </button>
                    </>
                  )}

                  {selectedCycle.status === 'FINALIZED' && (
                    <button
                      onClick={handleArchiveCycle}
                      className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold transition"
                    >
                      <Archive className="w-3.5 h-3.5" />
                      <span>Archive Period</span>
                    </button>
                  )}
                </div>
              </div>

              {/* Real-time Calculation Preview */}
              {calculationResult && (
                <div className="space-y-4 p-4 rounded-xl bg-slate-50 border border-slate-200">
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-bold text-slate-900">
                      Tiered Calculation & Distribution Preview
                    </h3>
                    <span className="text-xs font-mono font-bold text-brand-700">
                      Total Billing: ₹{calculationResult.totalCycleBilling}
                    </span>
                  </div>

                  <div className="grid grid-cols-3 gap-2 text-center text-xs">
                    <div className="p-2.5 bg-white rounded-lg border border-slate-200">
                      <span className="text-[10px] uppercase font-semibold text-slate-400">Metered Consumption</span>
                      <p className="font-bold text-slate-900 mt-0.5">{calculationResult.totalMeteredConsumptionKl} kL</p>
                    </div>
                    <div className="p-2.5 bg-white rounded-lg border border-slate-200">
                      <span className="text-[10px] uppercase font-semibold text-slate-400">Metered Charges</span>
                      <p className="font-bold text-slate-900 mt-0.5">₹{calculationResult.totalMeteredCharges}</p>
                    </div>
                    <div className="p-2.5 bg-white rounded-lg border border-slate-200">
                      <span className="text-[10px] uppercase font-semibold text-slate-400">Shared Bulk Pool</span>
                      <p className="font-bold text-slate-900 mt-0.5">₹{calculationResult.totalSharedCommonCharges}</p>
                    </div>
                  </div>

                  {/* Per-household distribution breakdown table */}
                  <div className="overflow-x-auto bg-white rounded-xl border border-slate-200">
                    <table className="w-full text-left text-xs">
                      <thead>
                        <tr className="border-b border-slate-200 text-slate-400 uppercase font-semibold text-[10px]">
                          <th className="py-2.5 px-3">Unit / Resident</th>
                          <th className="py-2.5 px-3">Type</th>
                          <th className="py-2.5 px-3 text-right">Usage</th>
                          <th className="py-2.5 px-3 text-right">Metered (₹)</th>
                          <th className="py-2.5 px-3 text-right">Shared (₹)</th>
                          <th className="py-2.5 px-3 text-right">Total Payable</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {calculationResult.householdBills.map((b) => (
                          <tr key={b.householdId} className="hover:bg-slate-50">
                            <td className="py-2.5 px-3">
                              <span className="font-bold text-slate-900">{b.flatNumber}</span>
                              <span className="block text-[11px] text-slate-500">{b.residentName || 'Unassigned'}</span>
                            </td>
                            <td className="py-2.5 px-3">
                              <span className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${b.isMetered ? 'bg-sky-100 text-sky-800' : 'bg-slate-100 text-slate-700'}`}>
                                {b.isMetered ? 'Metered' : 'Flat Area'}
                              </span>
                            </td>
                            <td className="py-2.5 px-3 text-right font-mono">{Number(b.meteredConsumptionKl).toFixed(3)} kL</td>
                            <td className="py-2.5 px-3 text-right font-mono">₹{Number(b.meteredCharge).toFixed(2)}</td>
                            <td className="py-2.5 px-3 text-right font-mono">₹{Number(b.sharedCommonCharge).toFixed(2)}</td>
                            <td className="py-2.5 px-3 text-right font-mono font-bold text-brand-600">₹{Number(b.totalAmount).toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Invoices List for Finalized Cycle */}
              {(selectedCycle.status === 'FINALIZED' || selectedCycle.status === 'ARCHIVED') && (
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <h3 className="font-bold text-slate-900 text-sm">Issued Invoices Ledger</h3>
                    <span className="text-xs text-slate-500">{invoices.length} Invoices Active</span>
                  </div>

                  <div className="overflow-x-auto bg-white rounded-xl border border-slate-200">
                    <table className="w-full text-left text-xs">
                      <thead>
                        <tr className="border-b border-slate-200 text-slate-400 uppercase font-semibold text-[10px]">
                          <th className="py-2.5 px-3">Invoice #</th>
                          <th className="py-2.5 px-3">Flat</th>
                          <th className="py-2.5 px-3 text-right">Usage</th>
                          <th className="py-2.5 px-3 text-right">Total Due</th>
                          <th className="py-2.5 px-3">Status</th>
                          <th className="py-2.5 px-3 text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {invoices.map((inv) => (
                          <tr key={inv.id} className="hover:bg-slate-50">
                            <td className="py-2.5 px-3 font-mono font-medium text-slate-800">{inv.invoiceNumber}</td>
                            <td className="py-2.5 px-3 font-bold text-slate-900">{inv.household?.flatNumber}</td>
                            <td className="py-2.5 px-3 text-right font-mono">{Number(inv.meteredConsumptionKl).toFixed(3)} kL</td>
                            <td className="py-2.5 px-3 text-right font-mono font-bold text-slate-900">₹{Number(inv.totalAmount).toFixed(2)}</td>
                            <td className="py-2.5 px-3">
                              <span
                                className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                                  inv.paymentStatus === 'PAID'
                                    ? 'bg-emerald-100 text-emerald-800'
                                    : inv.paymentStatus === 'OVERDUE'
                                    ? 'bg-rose-100 text-rose-800'
                                    : 'bg-amber-100 text-amber-800'
                                }`}
                              >
                                {inv.paymentStatus}
                              </span>
                            </td>
                            <td className="py-2.5 px-3 text-right">
                              <button
                                onClick={() => downloadPdf(inv.id, inv.invoiceNumber)}
                                className="inline-flex items-center gap-1 px-2.5 py-1 bg-brand-50 hover:bg-brand-100 text-brand-700 font-semibold rounded-lg text-xs transition"
                                title="Download PDF Invoice"
                              >
                                <Download className="w-3.5 h-3.5" />
                                <span>PDF</span>
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          ) : (
            <div className="py-12 text-center text-slate-400">
              <CalendarDays className="w-12 h-12 text-slate-300 mx-auto mb-2" />
              <p className="text-sm">Select a billing period to manage its lifecycle.</p>
            </div>
          )}
        </div>
      </div>

      {/* New Cycle Modal */}
      {showNewModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <h3 className="text-lg font-bold text-slate-900 mb-1">Open New Billing Cycle</h3>
            <p className="text-xs text-slate-500 mb-4">Define billing period duration and invoice payment deadline.</p>

            <form onSubmit={handleCreateCycle} className="space-y-4 text-sm">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Cycle Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Cycle 2026-11 (November)"
                  value={newCycleName}
                  onChange={(e) => setNewCycleName(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Start Date *</label>
                  <input
                    type="date"
                    required
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">End Date *</label>
                  <input
                    type="date"
                    required
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Payment Due Date *</label>
                <input
                  type="date"
                  required
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
                />
              </div>

              <div className="pt-3 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowNewModal(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creatingCycle}
                  className="px-5 py-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-500/20"
                >
                  {creatingCycle ? 'Opening...' : 'Open Period'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Manual Bill Issuance Modal */}
      {showManualBillModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center">
                  <Receipt className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-900">Issue Water Bill in INR (₹)</h3>
                  <p className="text-xs text-slate-500">Assign manual water charges to approved resident</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowManualBillModal(false)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleIssueManualBill} className="mt-4 space-y-3.5 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  Select Approved Resident / Flat *
                </label>
                <select
                  required
                  value={manualResidentId}
                  onChange={(e) => setManualResidentId(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 text-xs"
                >
                  {verifiedResidents.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.fullName} &bull; {r.flatNumber || 'Flat'} (@{r.username})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  Metered Consumption Volume (kL) *
                </label>
                <input
                  type="number"
                  step="0.001"
                  min="0"
                  required
                  value={manualKl}
                  onChange={(e) => setManualKl(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">
                    Metered Charge (₹ INR) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={manualMeteredCharge}
                    onChange={(e) => setManualMeteredCharge(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                  />
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">
                    Shared Common Cost (₹ INR) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={manualSharedCharge}
                    onChange={(e) => setManualSharedCharge(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                  />
                </div>
              </div>

              <div className="p-3 rounded-xl bg-brand-50 border border-brand-200 flex items-center justify-between">
                <span className="font-semibold text-brand-900 text-xs">Total Bill Amount (INR):</span>
                <span className="font-extrabold text-base text-brand-700 font-mono">
                  ₹{(parseFloat(manualMeteredCharge || 0) + parseFloat(manualSharedCharge || 0)).toFixed(2)}
                </span>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  Invoice Notes / Description
                </label>
                <input
                  type="text"
                  value={manualNotes}
                  onChange={(e) => setManualNotes(e.target.value)}
                  placeholder="e.g. Monthly water consumption charge"
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 text-xs"
                />
              </div>

              <div className="pt-2 flex gap-2">
                <button
                  type="button"
                  onClick={() => setShowManualBillModal(false)}
                  className="flex-1 py-2.5 px-3 rounded-xl text-slate-600 hover:bg-slate-100 text-xs font-semibold transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={issuingManualBill}
                  className="flex-1 py-2.5 px-3 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-bold shadow-md shadow-brand-500/20 transition disabled:opacity-50"
                >
                  {issuingManualBill ? 'Issuing Bill...' : 'Issue Water Bill (₹)'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import { Truck, Plus, CheckCircle2, AlertCircle, Calendar, IndianRupee, FileText } from 'lucide-react';

export const BulkPurchaseView = () => {
  const { user } = useAuth();
  const apartmentId = user?.apartmentId || 1;

  const [cycles, setCycles] = useState([]);
  const [selectedCycleId, setSelectedCycleId] = useState('');
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState({ type: '', message: '' });

  // Form state
  const [purchaseDate, setPurchaseDate] = useState(new Date().toISOString().split('T')[0]);
  const [supplierName, setSupplierName] = useState('');
  const [purchaseType, setPurchaseType] = useState('TANKER');
  const [volumeKl, setVolumeKl] = useState('');
  const [unitCostPerKl, setUnitCostPerKl] = useState('');
  const [invoiceReference, setInvoiceReference] = useState('');
  const [notes, setNotes] = useState('');

  const loadData = async () => {
    try {
      const cyclesRes = await api.get(`/admin/billing/cycles?apartmentId=${apartmentId}`);
      setCycles(cyclesRes.data);

      if (cyclesRes.data.length > 0) {
        const defaultCycle = cyclesRes.data.find((c) => c.status === 'OPEN') || cyclesRes.data[0];
        setSelectedCycleId(defaultCycle.id);
        loadPurchases(defaultCycle.id);
      } else {
        setLoading(false);
      }
    } catch (err) {
      console.error('Failed to load cycles:', err);
      setLoading(false);
    }
  };

  const loadPurchases = async (cycleId) => {
    try {
      const res = await api.get(`/admin/billing/cycles/${cycleId}/purchases`);
      setPurchases(res.data);
    } catch (err) {
      console.error('Failed to load purchases:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [apartmentId]);

  const handleCycleChange = (e) => {
    const cid = e.target.value;
    setSelectedCycleId(cid);
    loadPurchases(cid);
  };

  const calculatedTotal =
    volumeKl && unitCostPerKl ? (parseFloat(volumeKl) * parseFloat(unitCostPerKl)).toFixed(2) : '0.00';

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedCycleId) {
      setFeedback({ type: 'error', message: 'Please select an active billing cycle.' });
      return;
    }

    setSubmitting(true);
    setFeedback({ type: '', message: '' });

    try {
      await api.post('/admin/billing/bulk-purchases', {
        billingCycleId: parseInt(selectedCycleId, 10),
        apartmentId,
        purchaseDate,
        supplierName,
        purchaseType,
        volumeKl: parseFloat(volumeKl),
        unitCostPerKl: parseFloat(unitCostPerKl),
        invoiceReference,
        notes,
      });

      setFeedback({
        type: 'success',
        message: `Successfully recorded ${volumeKl} kL bulk water delivery (₹${calculatedTotal}).`,
      });

      // Reset form
      setSupplierName('');
      setVolumeKl('');
      setUnitCostPerKl('');
      setInvoiceReference('');
      setNotes('');

      // Refresh list
      loadPurchases(selectedCycleId);
    } catch (err) {
      setFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Failed to record bulk water purchase.',
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Bulk Water Purchases</h1>
          <p className="text-sm text-slate-500 mt-1">
            Record supplemental tanker deliveries and municipal bulk quotas for shared cost distribution.
          </p>
        </div>

        {/* Cycle Filter */}
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Billing Cycle:</label>
          <select
            value={selectedCycleId}
            onChange={handleCycleChange}
            className="px-3 py-2 bg-white border border-slate-300 rounded-xl text-sm font-medium text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            {cycles.map((c) => (
              <option key={c.id} value={c.id}>
                {c.cycleName} ({c.status})
              </option>
            ))}
          </select>
        </div>
      </div>

      {feedback.message && (
        <div
          className={`p-4 rounded-xl text-sm flex items-center gap-3 border ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
              : 'bg-rose-50 text-rose-800 border-rose-200'
          }`}
        >
          {feedback.type === 'success' ? <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" /> : <AlertCircle className="w-5 h-5 text-rose-600 shrink-0" />}
          <span>{feedback.message}</span>
        </div>
      )}

      {/* Grid: Form on Left, Table on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Bulk Purchase Entry Form */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div className="flex items-center gap-2.5 mb-5 pb-3 border-b border-slate-100">
            <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
              <Truck className="w-4 h-4" />
            </div>
            <h2 className="font-bold text-slate-900 text-base">New Water Delivery</h2>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4 text-sm">
            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Purchase Date *</label>
              <input
                type="date"
                required
                value={purchaseDate}
                onChange={(e) => setPurchaseDate(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Supply Source *</label>
              <select
                value={purchaseType}
                onChange={(e) => setPurchaseType(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
              >
                <option value="TANKER">Private Water Tanker (Delivery)</option>
                <option value="MUNICIPAL">Municipal Water Authority (Bulk)</option>
                <option value="EMERGENCY_SUPPLY">Emergency Water Reserve</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Supplier Name *</label>
              <input
                type="text"
                required
                placeholder="e.g. Cauvery Water Suppliers Pvt Ltd"
                value={supplierName}
                onChange={(e) => setSupplierName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Volume (kL) *</label>
                <input
                  type="number"
                  step="0.001"
                  min="0.1"
                  required
                  placeholder="e.g. 40.0"
                  value={volumeKl}
                  onChange={(e) => setVolumeKl(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Cost / kL (₹) *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.1"
                  required
                  placeholder="e.g. 65.00"
                  value={unitCostPerKl}
                  onChange={(e) => setUnitCostPerKl(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono"
                />
              </div>
            </div>

            {/* Live Calculation Callout */}
            <div className="p-3 bg-brand-50/70 rounded-xl border border-brand-100 flex items-center justify-between">
              <span className="text-xs font-semibold text-brand-800">Calculated Batch Cost:</span>
              <span className="text-base font-bold text-brand-700">₹{calculatedTotal}</span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Challan / Invoice #</label>
              <input
                type="text"
                placeholder="e.g. TNK-2026-10-04"
                value={invoiceReference}
                onChange={(e) => setInvoiceReference(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Notes / Destination</label>
              <textarea
                rows="2"
                placeholder="e.g. Lobby & garden sump refill"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-semibold shadow-md shadow-brand-500/20 transition disabled:opacity-50"
            >
              <Plus className="w-4 h-4" />
              <span>{submitting ? 'Recording...' : 'Record Bulk Purchase'}</span>
            </button>
          </form>
        </div>

        {/* Deliveries Table */}
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
            <div>
              <h2 className="font-bold text-slate-900 text-base">Recorded Deliveries for Selected Cycle</h2>
              <p className="text-xs text-slate-500">
                Total Pool Cost: <span className="font-bold text-slate-800">₹{purchases.reduce((acc, p) => acc + Number(p.totalCost), 0).toFixed(2)}</span>
              </p>
            </div>
          </div>

          {purchases.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center py-12 text-center text-slate-400">
              <Truck className="w-12 h-12 text-slate-300 mb-2 stroke-[1.5]" />
              <p className="text-sm font-medium text-slate-600">No bulk deliveries recorded for this cycle yet.</p>
              <p className="text-xs text-slate-400 max-w-sm mt-1">
                Enter tanker or municipal purchases on the left to add them to the shared common cost pool.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-slate-200 text-slate-400 uppercase tracking-wider font-semibold">
                    <th className="py-3 px-3">Date</th>
                    <th className="py-3 px-3">Type</th>
                    <th className="py-3 px-3">Supplier</th>
                    <th className="py-3 px-3 text-right">Volume (kL)</th>
                    <th className="py-3 px-3 text-right">Unit Rate</th>
                    <th className="py-3 px-3 text-right">Total Cost</th>
                    <th className="py-3 px-3">Ref / Notes</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {purchases.map((p) => (
                    <tr key={p.id} className="hover:bg-slate-50/80 transition">
                      <td className="py-3 px-3 font-medium text-slate-800">{p.purchaseDate}</td>
                      <td className="py-3 px-3">
                        <span
                          className={`inline-block px-2 py-0.5 rounded text-[10px] font-semibold ${
                            p.purchaseType === 'TANKER'
                              ? 'bg-amber-100 text-amber-800'
                              : 'bg-blue-100 text-blue-800'
                          }`}
                        >
                          {p.purchaseType}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-slate-700 font-medium">{p.supplierName}</td>
                      <td className="py-3 px-3 text-right font-mono font-semibold text-slate-900">
                        {Number(p.volumeKl).toFixed(3)}
                      </td>
                      <td className="py-3 px-3 text-right font-mono text-slate-600">₹{Number(p.unitCostPerKl).toFixed(2)}</td>
                      <td className="py-3 px-3 text-right font-mono font-bold text-emerald-700">
                        ₹{Number(p.totalCost).toFixed(2)}
                      </td>
                      <td className="py-3 px-3 text-slate-500 max-w-xs truncate">
                        {p.invoiceReference ? <span className="font-mono text-slate-600">{p.invoiceReference}</span> : '-'}
                        {p.notes && <span className="block text-[11px] text-slate-400">{p.notes}</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import { Sliders, Plus, Trash2, CheckCircle2, AlertCircle, Calculator, Info } from 'lucide-react';

export const TariffConfigView = () => {
  const { user } = useAuth();
  const apartmentId = user?.apartmentId || 1;

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState({ type: '', message: '' });

  const [planName, setPlanName] = useState('Standard Residential Tiered Tariff 2026');
  const [description, setDescription] = useState('Progressive tiered tariff scheme to promote water conservation.');
  const [effectiveFrom, setEffectiveFrom] = useState(new Date().toISOString().split('T')[0]);

  const [tiers, setTiers] = useState([
    { tierName: 'Tier 1 - Essential Life Line', minVolumeKl: 0, maxVolumeKl: 10, ratePerKl: 20.00, tierOrder: 1 },
    { tierName: 'Tier 2 - Normal Residential', minVolumeKl: 10, maxVolumeKl: 20, ratePerKl: 35.00, tierOrder: 2 },
    { tierName: 'Tier 3 - High Consumption', minVolumeKl: 20, maxVolumeKl: 35, ratePerKl: 55.00, tierOrder: 3 },
    { tierName: 'Tier 4 - Excessive Surcharge', minVolumeKl: 35, maxVolumeKl: '', ratePerKl: 80.00, tierOrder: 4 },
  ]);

  // Interactive Live Calculator state
  const [simulatedUsage, setSimulatedUsage] = useState(25);

  const loadTariff = async () => {
    try {
      const res = await api.get(`/admin/tariffs/active?apartmentId=${apartmentId}`);
      if (res.data) {
        setPlanName(res.data.name);
        setDescription(res.data.description || '');
        setEffectiveFrom(res.data.effectiveFrom);
        if (res.data.tiers && res.data.tiers.length > 0) {
          setTiers(
            res.data.tiers.map((t) => ({
              id: t.id,
              tierName: t.tierName,
              minVolumeKl: t.minVolumeKl,
              maxVolumeKl: t.maxVolumeKl !== null ? t.maxVolumeKl : '',
              ratePerKl: t.ratePerKl,
              tierOrder: t.tierOrder,
            }))
          );
        }
      }
    } catch (err) {
      console.warn('Could not load existing tariff, using defaults:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTariff();
  }, [apartmentId]);

  const handleAddTier = () => {
    const lastTier = tiers[tiers.length - 1];
    const newMin = lastTier && lastTier.maxVolumeKl ? parseFloat(lastTier.maxVolumeKl) : 40;
    const newTier = {
      tierName: `Tier ${tiers.length + 1} - Rate Band`,
      minVolumeKl: newMin,
      maxVolumeKl: '',
      ratePerKl: lastTier ? (parseFloat(lastTier.ratePerKl) * 1.5).toFixed(2) : 5.0,
      tierOrder: tiers.length + 1,
    };
    setTiers([...tiers, newTier]);
  };

  const handleRemoveTier = (index) => {
    if (tiers.length <= 1) return;
    const updated = tiers.filter((_, i) => i !== index).map((t, idx) => ({ ...t, tierOrder: idx + 1 }));
    setTiers(updated);
  };

  const handleTierChange = (index, field, value) => {
    const updated = [...tiers];
    updated[index][field] = value;
    setTiers(updated);
  };

  // Client-side simulation of the Tiered Tariff Engine for live feedback
  const computeSimulation = () => {
    const vol = parseFloat(simulatedUsage) || 0;
    let total = 0;
    const breakdowns = [];

    for (const t of tiers) {
      const min = parseFloat(t.minVolumeKl) || 0;
      const max = t.maxVolumeKl !== '' && t.maxVolumeKl !== null ? parseFloat(t.maxVolumeKl) : null;
      const rate = parseFloat(t.ratePerKl) || 0;

      if (vol <= min) {
        breakdowns.push({ tierName: t.tierName, volInTier: 0, rate, subtotal: 0 });
        continue;
      }

      const upper = max !== null && vol > max ? max : vol;
      const volInTier = Math.max(0, upper - min);
      const subtotal = volInTier * rate;
      total += subtotal;

      breakdowns.push({ tierName: t.tierName, volInTier, rate, subtotal });
    }

    return { total: total.toFixed(2), breakdowns };
  };

  const simulation = computeSimulation();

  const handleSave = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setFeedback({ type: '', message: '' });

    try {
      const payload = {
        apartmentId,
        name: planName,
        description,
        effectiveFrom,
        isActive: true,
        tiers: tiers.map((t, idx) => ({
          tierName: t.tierName,
          minVolumeKl: parseFloat(t.minVolumeKl) || 0,
          maxVolumeKl: t.maxVolumeKl !== '' && t.maxVolumeKl !== null ? parseFloat(t.maxVolumeKl) : null,
          ratePerKl: parseFloat(t.ratePerKl),
          tierOrder: idx + 1,
        })),
      };

      await api.post('/admin/tariffs', payload);
      setFeedback({ type: 'success', message: 'Tariff plan and rate tiers successfully published!' });
    } catch (err) {
      setFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Failed to save tariff plan configuration.',
      });
    } finally {
      setSubmitting(false);
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
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Tariff Rate Configuration</h1>
          <p className="text-sm text-slate-500 mt-1">
            Configure progressive volumetric rate tiers (e.g., base lifeline tier, higher rates for overuse).
          </p>
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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Tier Config Form */}
        <form onSubmit={handleSave} className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pb-4 border-b border-slate-100 text-sm">
            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Plan Name *</label>
              <input
                type="text"
                required
                value={planName}
                onChange={(e) => setPlanName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 text-sm"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Effective Date *</label>
              <input
                type="date"
                required
                value={effectiveFrom}
                onChange={(e) => setEffectiveFrom(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 text-sm"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Policy Description</label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 text-sm"
              />
            </div>
          </div>

          {/* Tiers List */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-bold uppercase tracking-wider text-slate-700">Configured Rate Tiers</h2>
              <button
                type="button"
                onClick={handleAddTier}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-brand-50 hover:bg-brand-100 text-brand-700 text-xs font-semibold border border-brand-200 transition"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Tier</span>
              </button>
            </div>

            {tiers.map((tier, idx) => (
              <div
                key={idx}
                className="p-4 rounded-xl bg-slate-50/70 border border-slate-200 grid grid-cols-1 sm:grid-cols-12 gap-3 items-center text-xs"
              >
                <div className="sm:col-span-4">
                  <label className="block text-[10px] uppercase font-semibold text-slate-400 mb-0.5">Tier Name</label>
                  <input
                    type="text"
                    required
                    value={tier.tierName}
                    onChange={(e) => handleTierChange(idx, 'tierName', e.target.value)}
                    className="w-full px-2.5 py-1.5 bg-white border border-slate-300 rounded-lg font-medium"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-[10px] uppercase font-semibold text-slate-400 mb-0.5">Min (kL)</label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    value={tier.minVolumeKl}
                    onChange={(e) => handleTierChange(idx, 'minVolumeKl', e.target.value)}
                    className="w-full px-2.5 py-1.5 bg-white border border-slate-300 rounded-lg font-mono"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-[10px] uppercase font-semibold text-slate-400 mb-0.5">Max (kL)</label>
                  <input
                    type="number"
                    step="0.1"
                    placeholder="Infinity"
                    value={tier.maxVolumeKl}
                    onChange={(e) => handleTierChange(idx, 'maxVolumeKl', e.target.value)}
                    className="w-full px-2.5 py-1.5 bg-white border border-slate-300 rounded-lg font-mono placeholder:text-slate-400"
                  />
                </div>

                <div className="sm:col-span-3">
                  <label className="block text-[10px] uppercase font-semibold text-slate-400 mb-0.5">Rate / kL (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={tier.ratePerKl}
                    onChange={(e) => handleTierChange(idx, 'ratePerKl', e.target.value)}
                    className="w-full px-2.5 py-1.5 bg-white border border-slate-300 rounded-lg font-mono font-bold text-slate-900"
                  />
                </div>

                <div className="sm:col-span-1 flex justify-end">
                  <button
                    type="button"
                    onClick={() => handleRemoveTier(idx)}
                    disabled={tiers.length <= 1}
                    className="p-1.5 text-slate-400 hover:text-rose-600 disabled:opacity-20 transition"
                    title="Remove Tier"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="pt-4 border-t border-slate-100 flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-semibold text-sm shadow-md shadow-brand-500/20 transition disabled:opacity-50"
            >
              <span>{submitting ? 'Publishing...' : 'Save & Publish Tariff Plan'}</span>
            </button>
          </div>
        </form>

        {/* Live Interactive Tariff Simulator Widget */}
        <div className="bg-gradient-to-b from-slate-900 to-slate-950 text-white p-6 rounded-2xl border border-slate-800 shadow-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-4 pb-3 border-b border-slate-800">
              <Calculator className="w-5 h-5 text-brand-400" />
              <h2 className="font-bold text-white text-base">Live Tariff Simulator</h2>
            </div>

            <p className="text-xs text-slate-400 mb-4">
              Test how your configured rate tiers calculate charges in real-time for any household consumption volume.
            </p>

            <div className="mb-5">
              <div className="flex justify-between text-xs font-semibold text-slate-300 mb-1.5">
                <span>Simulated Household Usage:</span>
                <span className="font-mono text-brand-400 text-sm font-bold">{simulatedUsage} kL</span>
              </div>
              <input
                type="range"
                min="1"
                max="60"
                step="1"
                value={simulatedUsage}
                onChange={(e) => setSimulatedUsage(e.target.value)}
                className="w-full accent-brand-500 h-2 bg-slate-700 rounded-lg cursor-pointer"
              />
              <div className="flex justify-between text-[10px] text-slate-500 mt-1 font-mono">
                <span>0 kL</span>
                <span>20 kL</span>
                <span>40 kL</span>
                <span>60 kL</span>
              </div>
            </div>

            {/* Breakdown List */}
            <div className="space-y-2 text-xs">
              <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">
                Tier Breakdown:
              </span>
              {simulation.breakdowns.map((b, i) => (
                <div key={i} className="flex items-center justify-between p-2 rounded-lg bg-slate-800/60 border border-slate-800">
                  <div>
                    <span className="text-slate-300 font-medium block">{b.tierName}</span>
                    <span className="text-[10px] text-slate-400 font-mono">
                      {b.volInTier.toFixed(1)} kL &times; ₹{b.rate.toFixed(2)}
                    </span>
                  </div>
                  <span className="font-mono font-bold text-white">₹{b.subtotal.toFixed(2)}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Total Charge Box */}
          <div className="mt-6 pt-4 border-t border-slate-800 bg-slate-850 p-4 rounded-xl border border-slate-700/60">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
              Simulated Metered Charge:
            </span>
            <div className="text-3xl font-extrabold text-emerald-400 font-mono mt-1">
              ₹{simulation.total}
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              Effective Avg: ₹{(parseFloat(simulation.total) / (parseFloat(simulatedUsage) || 1)).toFixed(2)} / kL
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import { Users, Plus, Gauge, CheckCircle2, AlertCircle, Home, UserPlus } from 'lucide-react';

export const HouseholdsView = () => {
  const { user } = useAuth();
  const apartmentId = user?.apartmentId || 1;

  const [households, setHouseholds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showMeterModal, setShowMeterModal] = useState(false);
  const [selectedHousehold, setSelectedHousehold] = useState(null);

  // Household Form State
  const [flatNumber, setFlatNumber] = useState('');
  const [blockOrWing, setBlockOrWing] = useState('Wing A');
  const [floorNumber, setFloorNumber] = useState(1);
  const [areaSqft, setAreaSqft] = useState(1200);
  const [occupants, setOccupants] = useState(3);
  const [isMetered, setIsMetered] = useState(true);
  const [thresholdKl, setThresholdKl] = useState(1.5);
  const [feedback, setFeedback] = useState({ type: '', message: '' });

  // Meter Config Form State
  const [meterSerial, setMeterSerial] = useState('');
  const [initialReading, setInitialReading] = useState(0);

  const loadHouseholds = async () => {
    try {
      const res = await api.get(`/admin/households?apartmentId=${apartmentId}`);
      setHouseholds(res.data);
    } catch (err) {
      console.error('Failed to load households:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHouseholds();
  }, [apartmentId]);

  const handleAddHousehold = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/households', {
        apartmentId,
        flatNumber,
        blockOrWing,
        floorNumber: parseInt(floorNumber, 10),
        areaSqft: parseFloat(areaSqft),
        numberOfOccupants: parseInt(occupants, 10),
        isMetered,
        dailyUsageThresholdKl: parseFloat(thresholdKl),
      });

      setFeedback({ type: 'success', message: `Flat ${flatNumber} successfully onboarded.` });
      setShowAddModal(false);
      setFlatNumber('');
      loadHouseholds();
    } catch (err) {
      setFeedback({ type: 'error', message: err.response?.data?.message || 'Failed to add household' });
    }
  };

  const handleConfigureMeter = async (e) => {
    e.preventDefault();
    if (!selectedHousehold) return;

    try {
      await api.post('/admin/meters', {
        meterSerialNumber: meterSerial,
        householdId: selectedHousehold.id,
        initialReading: parseFloat(initialReading),
        status: 'ACTIVE',
        meterType: 'DIGITAL',
      });

      setFeedback({ type: 'success', message: `Meter ${meterSerial} configured for Flat ${selectedHousehold.flatNumber}.` });
      setShowMeterModal(false);
      setMeterSerial('');
      loadHouseholds();
    } catch (err) {
      setFeedback({ type: 'error', message: err.response?.data?.message || 'Failed to configure meter' });
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
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Households & Water Meters</h1>
          <p className="text-sm text-slate-500 mt-1">
            Directory of registered flats, occupants, digital water meters, and consumption thresholds.
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-500/20 transition self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Register New Flat</span>
        </button>
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

      {/* Directory Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <h2 className="font-bold text-slate-900 text-base">Registered Units ({households.length})</h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-200 text-slate-400 uppercase font-semibold text-[10px] bg-slate-50/50">
                <th className="py-3 px-4">Flat #</th>
                <th className="py-3 px-4">Wing & Floor</th>
                <th className="py-3 px-4">Area & Occupancy</th>
                <th className="py-3 px-4">Metering Status</th>
                <th className="py-3 px-4">Water Meter</th>
                <th className="py-3 px-4">Daily Threshold</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {households.map((h) => (
                <tr key={h.id} className="hover:bg-slate-50 transition">
                  <td className="py-3 px-4 font-bold text-slate-900 text-sm">{h.flatNumber}</td>
                  <td className="py-3 px-4 text-slate-600">
                    {h.blockOrWing} &bull; Floor {h.floorNumber || 1}
                  </td>
                  <td className="py-3 px-4 text-slate-600">
                    <span className="font-semibold text-slate-800">{h.areaSqft} sqft</span>
                    <span className="block text-[11px] text-slate-400">{h.numberOfOccupants} occupants</span>
                  </td>
                  <td className="py-3 px-4">
                    <span
                      className={`inline-block px-2.5 py-0.5 rounded text-[10px] font-semibold ${
                        h.isMetered ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {h.isMetered ? 'Metered Unit' : 'Unmetered (Flat Area Share)'}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    {h.waterMeter ? (
                      <div>
                        <span className="font-mono font-medium text-slate-900 block">{h.waterMeter.meterSerialNumber}</span>
                        <span className="text-[10px] text-slate-400">
                          Last: {Number(h.waterMeter.lastReadingValue || 0).toFixed(2)} kL
                        </span>
                      </div>
                    ) : (
                      <span className="text-slate-400 italic">None assigned</span>
                    )}
                  </td>
                  <td className="py-3 px-4 font-mono font-semibold text-slate-700">
                    {Number(h.dailyUsageThresholdKl || 1.5).toFixed(2)} kL/day
                  </td>
                  <td className="py-3 px-4 text-right">
                    {!h.waterMeter && (
                      <button
                        onClick={() => {
                          setSelectedHousehold(h);
                          setShowMeterModal(true);
                        }}
                        className="inline-flex items-center gap-1 px-2.5 py-1 bg-brand-50 hover:bg-brand-100 text-brand-700 font-semibold rounded-lg text-xs transition"
                      >
                        <Gauge className="w-3.5 h-3.5" />
                        <span>Add Meter</span>
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add Household Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <h3 className="text-lg font-bold text-slate-900 mb-1">Onboard New Flat</h3>
            <p className="text-xs text-slate-500 mb-4">Register a household into the apartment building structure.</p>

            <form onSubmit={handleAddHousehold} className="space-y-4 text-sm">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Flat Number *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. D-401"
                    value={flatNumber}
                    onChange={(e) => setFlatNumber(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Wing / Block</label>
                  <input
                    type="text"
                    value={blockOrWing}
                    onChange={(e) => setBlockOrWing(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Floor</label>
                  <input
                    type="number"
                    value={floorNumber}
                    onChange={(e) => setFloorNumber(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Area (sqft)</label>
                  <input
                    type="number"
                    value={areaSqft}
                    onChange={(e) => setAreaSqft(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Occupants</label>
                  <input
                    type="number"
                    value={occupants}
                    onChange={(e) => setOccupants(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="meteredCheck"
                  checked={isMetered}
                  onChange={(e) => setIsMetered(e.target.checked)}
                  className="rounded text-brand-600 focus:ring-brand-500 w-4 h-4"
                />
                <label htmlFor="meteredCheck" className="text-xs font-medium text-slate-700">
                  Flat is Equipped with an Individual Water Meter
                </label>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">
                  Daily Usage Threshold (kL)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={thresholdKl}
                  onChange={(e) => setThresholdKl(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl font-mono"
                />
              </div>

              <div className="pt-3 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-500/20"
                >
                  Register Flat
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Meter Config Modal */}
      {showMeterModal && selectedHousehold && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-sm w-full p-6 shadow-2xl border border-slate-200">
            <h3 className="text-lg font-bold text-slate-900 mb-1">
              Configure Meter for Unit {selectedHousehold.flatNumber}
            </h3>
            <p className="text-xs text-slate-500 mb-4">Assign a water meter device serial number to this flat.</p>

            <form onSubmit={handleConfigureMeter} className="space-y-4 text-sm">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Meter Serial Number *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. WM-GH-D401"
                  value={meterSerial}
                  onChange={(e) => setMeterSerial(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Initial Reading (kL) *</label>
                <input
                  type="number"
                  step="0.001"
                  required
                  value={initialReading}
                  onChange={(e) => setInitialReading(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl font-mono"
                />
              </div>

              <div className="pt-3 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowMeterModal(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-500/20"
                >
                  Save Meter
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import {
  FileSpreadsheet,
  Upload,
  Plus,
  Download,
  CheckCircle2,
  AlertTriangle,
  AlertCircle,
  FileCheck,
  Calendar,
  Clock,
  Edit3,
  Save,
  X,
  History,
  RefreshCw,
} from 'lucide-react';

export const MeterReadingView = () => {
  const { user } = useAuth();
  const apartmentId = user?.apartmentId || 1;

  const [activeTab, setActiveTab] = useState('logs'); // 'logs', 'csv', 'manual'
  const [households, setHouseholds] = useState([]);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [logsLoading, setLogsLoading] = useState(false);

  // Manual Form State
  const [selectedHouseholdId, setSelectedHouseholdId] = useState('');
  const [readingDate, setReadingDate] = useState(new Date().toISOString().split('T')[0]);
  const [currentReadingKl, setCurrentReadingKl] = useState('');
  const [manualNotes, setManualNotes] = useState('');
  const [manualSubmitting, setManualSubmitting] = useState(false);
  const [manualFeedback, setManualFeedback] = useState({ type: '', message: '' });

  // CSV State
  const [csvFile, setCsvFile] = useState(null);
  const [csvUploading, setCsvUploading] = useState(false);
  const [csvResult, setCsvResult] = useState(null);

  // Edit Modal State (Requirement 3)
  const [editingLog, setEditingLog] = useState(null);
  const [editReadingKl, setEditReadingKl] = useState('');
  const [editConsumptionKl, setEditConsumptionKl] = useState('');
  const [editNotes, setEditNotes] = useState('');
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editFeedback, setEditFeedback] = useState({ type: '', message: '' });

  const fetchHouseholds = async () => {
    try {
      const res = await api.get(`/admin/households?apartmentId=${apartmentId}`);
      setHouseholds(res.data);
      if (res.data.length > 0) {
        setSelectedHouseholdId(res.data[0].id);
      }
    } catch (err) {
      console.error('Failed to load households:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchLogs = async () => {
    setLogsLoading(true);
    try {
      const res = await api.get(`/usage/apartment-logs?apartmentId=${apartmentId}`);
      setLogs(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to load meter reading logs:', err);
    } finally {
      setLogsLoading(false);
    }
  };

  useEffect(() => {
    fetchHouseholds();
    fetchLogs();
  }, [apartmentId]);

  // Download Sample CSV
  const handleDownloadTemplate = () => {
    const csvContent =
      'flat_number,reading_date,current_reading_kl,notes\n' +
      'A-101,2026-10-15,147.200,Regular morning inspection\n' +
      'A-102,2026-10-15,184.100,Regular morning inspection\n' +
      'A-103,2026-10-15,113.400,Regular morning inspection\n' +
      'B-201,2026-10-15,213.250,Regular morning inspection\n' +
      'B-202,2026-10-15,300.200,Regular morning inspection\n';

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'jaltracker_meter_readings_template.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleManualSubmit = async (e) => {
    e.preventDefault();
    setManualSubmitting(true);
    setManualFeedback({ type: '', message: '' });

    try {
      await api.post('/usage/log', {
        householdId: parseInt(selectedHouseholdId, 10),
        readingDate,
        currentReadingKl: parseFloat(currentReadingKl),
        notes: manualNotes,
      });

      setManualFeedback({
        type: 'success',
        message: `Successfully recorded reading ${currentReadingKl} kL for date ${readingDate}.`,
      });
      setCurrentReadingKl('');
      setManualNotes('');
      fetchLogs();
    } catch (err) {
      setManualFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Failed to record reading.',
      });
    } finally {
      setManualSubmitting(false);
    }
  };

  const handleCsvUpload = async (e) => {
    e.preventDefault();
    if (!csvFile) return;

    setCsvUploading(true);
    setCsvResult(null);

    const formData = new FormData();
    formData.append('apartmentId', apartmentId);
    formData.append('file', csvFile);

    try {
      const res = await api.post('/usage/upload-csv', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setCsvResult(res.data);
      setCsvFile(null);
      fetchLogs();
    } catch (err) {
      setCsvResult({
        errors: [err.response?.data?.message || 'CSV upload failed.'],
        totalRows: 0,
        successfulRows: 0,
        failedRows: 1,
        skippedDuplicates: 0,
      });
    } finally {
      setCsvUploading(false);
    }
  };

  // Open Edit Modal (Requirement 3)
  const openEditModal = (logItem) => {
    setEditingLog(logItem);
    setEditReadingKl(logItem.currentReadingKl || '');
    setEditConsumptionKl(logItem.dailyConsumptionKl || '');
    setEditNotes(logItem.notes || '');
    setEditFeedback({ type: '', message: '' });
  };

  // Submit Edit Meter Reading (Requirement 3: PUT /api/usage-logs/{id})
  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!editingLog) return;

    setEditSubmitting(true);
    setEditFeedback({ type: '', message: '' });

    try {
      const res = await api.put(`/usage-logs/${editingLog.id}`, {
        currentReadingKl: parseFloat(editReadingKl),
        dailyConsumptionKl: editConsumptionKl ? parseFloat(editConsumptionKl) : null,
        notes: editNotes,
      });

      setEditFeedback({
        type: 'success',
        message: 'Meter reading updated and consumption recalculated successfully!',
      });

      // Update state in logs list
      setLogs((prev) =>
        prev.map((l) => (l.id === editingLog.id ? { ...l, ...res.data } : l))
      );

      setTimeout(() => {
        setEditingLog(null);
      }, 1000);
    } catch (err) {
      setEditFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Failed to update meter reading.',
      });
    } finally {
      setEditSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Meter Reading Management</h1>
          <p className="text-sm text-slate-500 mt-1">
            Capture, inspect, and update daily water consumption readings across all society flats.
          </p>
        </div>

        {/* Tab Pill Buttons */}
        <div className="flex bg-slate-200/80 p-1 rounded-xl">
          <button
            onClick={() => setActiveTab('logs')}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'logs' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Reading Logs & Editor
          </button>
          <button
            onClick={() => setActiveTab('csv')}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'csv' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Bulk CSV Upload
          </button>
          <button
            onClick={() => setActiveTab('manual')}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'manual' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Manual Entry
          </button>
        </div>
      </div>

      {/* Tab 1: Meter Reading Logs & Editor Table (Requirement 3) */}
      {activeTab === 'logs' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-5 border-b border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-bold text-slate-900">Recent Meter Reading Logs</h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Click "Edit" on any reading to update the cumulative dial or daily consumption.
              </p>
            </div>
            <button
              onClick={fetchLogs}
              disabled={logsLoading}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-semibold transition"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${logsLoading ? 'animate-spin' : ''}`} />
              <span>Refresh Logs</span>
            </button>
          </div>

          {logsLoading ? (
            <div className="flex items-center justify-center py-16">
              <div className="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : logs.length === 0 ? (
            <div className="p-12 text-center text-slate-500 text-xs">
              No meter readings found. Upload a CSV or enter manual readings to populate logs.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-600">
                <thead className="bg-slate-50 text-slate-700 font-bold uppercase tracking-wider text-[11px] border-b border-slate-200">
                  <tr>
                    <th className="py-3.5 px-4">Reading Date</th>
                    <th className="py-3.5 px-4">Unit / Flat</th>
                    <th className="py-3.5 px-4">Meter Serial</th>
                    <th className="py-3.5 px-4 text-right">Cumulative (kL)</th>
                    <th className="py-3.5 px-4 text-right">Daily Usage (kL)</th>
                    <th className="py-3.5 px-4">Source</th>
                    <th className="py-3.5 px-4">Notes</th>
                    <th className="py-3.5 px-4 text-center">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {logs.map((logItem) => (
                    <tr key={logItem.id} className="hover:bg-slate-50/80 transition">
                      <td className="py-3 px-4 font-medium text-slate-900">{logItem.readingDate}</td>
                      <td className="py-3 px-4 font-bold text-slate-800">
                        {logItem.household?.flatNumber || 'Flat Unit'}
                      </td>
                      <td className="py-3 px-4 font-mono text-[11px] text-slate-500">
                        {logItem.meter?.meterSerialNumber || 'N/A'}
                      </td>
                      <td className="py-3 px-4 text-right font-mono font-semibold text-slate-900">
                        {Number(logItem.currentReadingKl).toFixed(3)}
                      </td>
                      <td className="py-3 px-4 text-right font-mono font-bold text-brand-600">
                        {Number(logItem.dailyConsumptionKl).toFixed(3)}
                      </td>
                      <td className="py-3 px-4">
                        <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-slate-100 text-slate-700">
                          {logItem.logSource}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-500 max-w-xs truncate">{logItem.notes || '—'}</td>
                      <td className="py-3 px-4 text-center">
                        <button
                          onClick={() => openEditModal(logItem)}
                          className="inline-flex items-center gap-1 px-2.5 py-1 bg-brand-50 hover:bg-brand-100 text-brand-700 rounded-lg text-xs font-semibold transition"
                        >
                          <Edit3 className="w-3.5 h-3.5" />
                          <span>Edit</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Bulk CSV Upload */}
      {activeTab === 'csv' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <h2 className="text-base font-bold text-slate-900">Upload CSV Meter Readings</h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Import multiple flat readings simultaneously with format validation and duplicate suppression.
              </p>
            </div>
            <button
              onClick={handleDownloadTemplate}
              className="flex items-center gap-2 px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold transition"
            >
              <Download className="w-4 h-4 text-slate-500" />
              <span>Download CSV Template</span>
            </button>
          </div>

          <form onSubmit={handleCsvUpload} className="space-y-5">
            <div className="border-2 border-dashed border-slate-300 rounded-2xl p-8 text-center hover:border-brand-500 transition bg-slate-50/50">
              <Upload className="w-10 h-10 text-brand-500 mx-auto mb-3" />
              <label className="block text-sm font-semibold text-slate-800 cursor-pointer">
                <span>Select Meter Reading CSV file</span>
                <input
                  type="file"
                  accept=".csv"
                  onChange={(e) => setCsvFile(e.target.files[0])}
                  className="hidden"
                />
              </label>
              <p className="text-xs text-slate-500 mt-1">Accepts standard .csv format with headers</p>
              {csvFile && (
                <div className="mt-4 inline-flex items-center gap-2 px-3 py-1.5 bg-brand-50 text-brand-700 border border-brand-200 rounded-lg text-xs font-medium">
                  <FileCheck className="w-4 h-4" />
                  <span>{csvFile.name} ({(csvFile.size / 1024).toFixed(1)} KB)</span>
                </div>
              )}
            </div>

            <button
              type="submit"
              disabled={!csvFile || csvUploading}
              className="flex items-center justify-center gap-2 px-6 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-semibold text-sm shadow-md shadow-brand-500/20 transition disabled:opacity-50"
            >
              <FileSpreadsheet className="w-4 h-4" />
              <span>{csvUploading ? 'Validating & Uploading...' : 'Upload & Process Readings'}</span>
            </button>
          </form>

          {/* CSV Result Output */}
          {csvResult && (
            <div className="p-5 rounded-2xl bg-slate-50 border border-slate-200 space-y-3">
              <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                <span>Upload Summary</span>
              </h3>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                <div className="p-3 bg-white rounded-xl border border-slate-200">
                  <span className="text-slate-500 block">Total Rows</span>
                  <span className="text-base font-bold text-slate-900">{csvResult.totalRows}</span>
                </div>
                <div className="p-3 bg-white rounded-xl border border-emerald-200">
                  <span className="text-emerald-600 block font-semibold">Successful</span>
                  <span className="text-base font-bold text-emerald-700">{csvResult.successfulRows}</span>
                </div>
                <div className="p-3 bg-white rounded-xl border border-amber-200">
                  <span className="text-amber-600 block font-semibold">Duplicates Skipped</span>
                  <span className="text-base font-bold text-amber-700">{csvResult.skippedDuplicates}</span>
                </div>
                <div className="p-3 bg-white rounded-xl border border-rose-200">
                  <span className="text-rose-600 block font-semibold">Failed Rows</span>
                  <span className="text-base font-bold text-rose-700">{csvResult.failedRows}</span>
                </div>
              </div>

              {csvResult.errors && csvResult.errors.length > 0 && (
                <div className="mt-3 p-3 bg-rose-50 border border-rose-200 rounded-xl space-y-1">
                  <span className="text-xs font-bold text-rose-900 block">Errors Encountered:</span>
                  <ul className="list-disc list-inside text-xs text-rose-700 space-y-0.5">
                    {csvResult.errors.map((err, i) => (
                      <li key={i}>{err}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Tab 3: Manual Entry */}
      {activeTab === 'manual' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-5">
          <div>
            <h2 className="text-base font-bold text-slate-900">Manual Daily Meter Log</h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Submit daily water meter readings for a specific flat. The system automatically computes daily consumption.
            </p>
          </div>

          {manualFeedback.message && (
            <div
              className={`p-3.5 rounded-xl text-xs font-medium flex items-center gap-2 ${
                manualFeedback.type === 'success'
                  ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                  : 'bg-rose-50 text-rose-800 border border-rose-200'
              }`}
            >
              {manualFeedback.type === 'success' ? (
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
              ) : (
                <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
              )}
              <span>{manualFeedback.message}</span>
            </div>
          )}

          <form onSubmit={handleManualSubmit} className="space-y-4 max-w-xl">
            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Select Flat Unit *</label>
              <select
                required
                value={selectedHouseholdId}
                onChange={(e) => setSelectedHouseholdId(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 text-xs"
              >
                {households.map((h) => (
                  <option key={h.id} value={h.id}>
                    Flat {h.flatNumber} ({h.blockOrWing || 'Wing A'}) - {h.isMetered ? 'Metered Unit' : 'Unmetered Unit'}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Reading Date *</label>
                <input
                  type="date"
                  required
                  value={readingDate}
                  onChange={(e) => setReadingDate(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 text-xs"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Cumulative Reading (kL) *</label>
                <input
                  type="number"
                  step="0.001"
                  min="0"
                  required
                  placeholder="e.g. 147.250"
                  value={currentReadingKl}
                  onChange={(e) => setCurrentReadingKl(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Notes / Remarks</label>
              <input
                type="text"
                placeholder="e.g. Verified dial meter indicator"
                value={manualNotes}
                onChange={(e) => setManualNotes(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 text-xs"
              />
            </div>

            <button
              type="submit"
              disabled={manualSubmitting}
              className="flex items-center justify-center gap-2 px-6 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-semibold text-xs shadow-md shadow-brand-500/20 transition disabled:opacity-50"
            >
              <span>{manualSubmitting ? 'Recording Log...' : 'Record Reading Log'}</span>
            </button>
          </form>
        </div>
      )}

      {/* Edit Reading Modal (Requirement 3: PUT /api/usage-logs/{id}) */}
      {editingLog && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-slate-200 space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-brand-50 text-brand-600 flex items-center justify-center">
                  <Edit3 className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="font-bold text-slate-900 text-sm">Edit Meter Reading Log #{editingLog.id}</h3>
                  <p className="text-xs text-slate-500">
                    Flat {editingLog.household?.flatNumber} &bull; Reading Date: {editingLog.readingDate}
                  </p>
                </div>
              </div>
              <button
                onClick={() => setEditingLog(null)}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-100"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {editFeedback.message && (
              <div
                className={`p-3 rounded-xl text-xs font-medium flex items-center gap-2 ${
                  editFeedback.type === 'success'
                    ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                    : 'bg-rose-50 text-rose-800 border border-rose-200'
                }`}
              >
                {editFeedback.type === 'success' ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                ) : (
                  <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
                )}
                <span>{editFeedback.message}</span>
              </div>
            )}

            <form onSubmit={handleEditSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Cumulative Reading Value (kL) *
                </label>
                <input
                  type="number"
                  step="0.001"
                  min="0"
                  required
                  value={editReadingKl}
                  onChange={(e) => setEditReadingKl(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Daily Consumption (kL) (Optional Override)
                </label>
                <input
                  type="number"
                  step="0.001"
                  min="0"
                  placeholder="Auto-calculated if blank"
                  value={editConsumptionKl}
                  onChange={(e) => setEditConsumptionKl(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                />
                <p className="text-[11px] text-slate-500 mt-1">
                  Leave empty to auto-calculate difference from previous logged reading.
                </p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Correction Reason / Notes
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Dial reading adjustment per technician verification"
                  value={editNotes}
                  onChange={(e) => setEditNotes(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:ring-2 focus:ring-brand-500 text-xs"
                />
              </div>

              <div className="pt-2 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setEditingLog(null)}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={editSubmitting}
                  className="flex items-center gap-1.5 px-5 py-2 bg-brand-600 hover:bg-brand-500 text-white rounded-xl text-xs font-semibold shadow-md shadow-brand-500/20 transition disabled:opacity-50"
                >
                  <Save className="w-3.5 h-3.5" />
                  <span>{editSubmitting ? 'Updating...' : 'Save & Recalculate'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import {
  UserCheck,
  ShieldCheck,
  UserX,
  AlertCircle,
  Building,
  Home,
  Mail,
  Phone,
  Clock,
  CheckCircle2,
  RefreshCw,
  Search,
  Filter,
  FileText,
  IndianRupee,
  Plus,
  Download,
  Users,
  Sparkles,
  Receipt,
  X,
} from 'lucide-react';
import { SkeletonCard } from '../../components/skeleton/SkeletonCard';
import { SkeletonTable } from '../../components/skeleton/SkeletonTable';

export const UserVerificationsView = () => {
  const { user, isSuperAdmin } = useAuth();
  const [activeTab, setActiveTab] = useState('pending'); // 'pending' | 'approved'
  const [pendingUsers, setPendingUsers] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  // Manual Bill Modal State
  const [showBillModal, setShowBillModal] = useState(false);
  const [selectedResidentForBill, setSelectedResidentForBill] = useState(null);
  const [meteredKl, setMeteredKl] = useState('15.00');
  const [meteredCharge, setMeteredCharge] = useState('250.00');
  const [sharedCharge, setSharedCharge] = useState('100.00');
  const [billNotes, setBillNotes] = useState('Monthly water bill assessment in INR (₹)');
  const [issuingBill, setIssuingBill] = useState(false);
  const [lastIssuedInvoice, setLastIssuedInvoice] = useState(null);

  const loadData = async () => {
    setLoading(true);
    setFeedback(null);
    try {
      const [pendingRes, allRes] = await Promise.all([
        api.get('/users/pending'),
        api.get('/users'),
      ]);
      setPendingUsers(Array.isArray(pendingRes.data) ? pendingRes.data : []);
      setAllUsers(Array.isArray(allRes.data) ? allRes.data : []);
    } catch (err) {
      console.error('Failed to load user management data:', err);
      setFeedback({ type: 'error', message: 'Failed to retrieve account records.' });
    } finally {
      setLoading(false);
    }
  };

  const loadDataSilently = async () => {
    try {
      const [pendingRes, allRes] = await Promise.all([
        api.get('/users/pending'),
        api.get('/users'),
      ]);
      setPendingUsers(Array.isArray(pendingRes.data) ? pendingRes.data : []);
      setAllUsers(Array.isArray(allRes.data) ? allRes.data : []);
    } catch (err) {
      console.error('Silent sync failed:', err);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleApprove = async (userId, userName) => {
    // 1. Snapshot previous state for rollback
    const prevPending = [...pendingUsers];
    const prevAll = [...allUsers];

    const targetUser = pendingUsers.find((u) => u.id === userId);
    if (!targetUser) return;

    // 2. Optimistic instant UI update
    const verifiedUser = { ...targetUser, accountStatus: 'VERIFIED', isOptimistic: true };
    setPendingUsers((prev) => prev.filter((u) => u.id !== userId));
    setAllUsers((prev) => [verifiedUser, ...prev.filter((u) => u.id !== userId)]);
    setSelectedResidentForBill(verifiedUser);
    setFeedback({
      type: 'success',
      message: `Account for "${userName}" verified! Resident can now log in immediately.`,
    });

    setActionLoading(userId);

    try {
      // 3. API request in background
      await api.patch(`/users/${userId}/status`, { status: 'VERIFIED' });
      loadDataSilently();
    } catch (err) {
      // 4. Rollback on failure
      console.error('Approval failed, rolling back:', err);
      setPendingUsers(prevPending);
      setAllUsers(prevAll);
      setFeedback({
        type: 'error',
        message: err.response?.data?.message || `Failed to verify "${userName}". Changes rolled back.`,
      });
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (userId, userName) => {
    if (!window.confirm(`Are you sure you want to reject the registration for "${userName}"?`)) {
      return;
    }

    // 1. Snapshot previous state
    const prevPending = [...pendingUsers];
    const prevAll = [...allUsers];

    const targetUser = pendingUsers.find((u) => u.id === userId);
    if (!targetUser) return;

    // 2. Optimistic instant UI update
    setPendingUsers((prev) => prev.filter((u) => u.id !== userId));
    setAllUsers((prev) => [
      { ...targetUser, accountStatus: 'REJECTED' },
      ...prev.filter((u) => u.id !== userId),
    ]);
    setFeedback({
      type: 'warning',
      message: `Registration for "${userName}" has been rejected.`,
    });

    setActionLoading(userId);

    try {
      await api.patch(`/users/${userId}/status`, { status: 'REJECTED' });
      loadDataSilently();
    } catch (err) {
      console.error('Rejection failed, rolling back:', err);
      setPendingUsers(prevPending);
      setAllUsers(prevAll);
      setFeedback({
        type: 'error',
        message: err.response?.data?.message || `Failed to reject "${userName}". Changes rolled back.`,
      });
    } finally {
      setActionLoading(null);
    }
  };

  const openBillModalForResident = (resident) => {
    setSelectedResidentForBill(resident);
    setMeteredKl('15.00');
    setMeteredCharge('250.00');
    setSharedCharge('100.00');
    setBillNotes(`Water utility bill for Flat ${resident.flatNumber || resident.householdId || ''} (${resident.fullName})`);
    setLastIssuedInvoice(null);
    setShowBillModal(true);
  };

  const handleIssueBill = async (e) => {
    e.preventDefault();
    if (!selectedResidentForBill) return;

    setIssuingBill(true);
    try {
      const totalAmount = (parseFloat(meteredCharge || 0) + parseFloat(sharedCharge || 0)).toFixed(2);
      const payload = {
        residentUserId: selectedResidentForBill.id,
        householdId: selectedResidentForBill.householdId || undefined,
        meteredConsumptionKl: parseFloat(meteredKl || 0),
        meteredCharge: parseFloat(meteredCharge || 0),
        sharedCommonCharge: parseFloat(sharedCharge || 0),
        totalAmount: parseFloat(totalAmount),
        notes: billNotes,
      };

      const res = await api.post('/admin/invoices/issue', payload);
      setLastIssuedInvoice(res.data);
      setFeedback({
        type: 'success',
        message: `Water bill ${res.data.invoiceNumber} of ₹${totalAmount} successfully issued to ${selectedResidentForBill.fullName} (Flat ${selectedResidentForBill.flatNumber || ''})!`,
      });
    } catch (err) {
      console.error('Failed to issue invoice:', err);
      alert(err.response?.data?.message || 'Failed to issue water bill');
    } finally {
      setIssuingBill(false);
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

  const verifiedResidents = allUsers.filter(
    (u) => u.accountStatus === 'VERIFIED' && u.role === 'ROLE_RESIDENT'
  );

  const filteredPending = pendingUsers.filter(
    (u) =>
      u.fullName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.username?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.flatNumber?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const filteredApproved = verifiedResidents.filter(
    (u) =>
      u.fullName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.username?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.flatNumber?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 bg-gradient-to-r from-slate-900 to-slate-800 text-white p-6 rounded-2xl shadow-lg border border-slate-700/60">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 mb-2">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Governance & Access Control</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight">Resident Approval & Billing Management</h1>
          <p className="text-sm text-slate-300 mt-1">
            Review pending registrations, approve flat residents, and manually issue INR (₹) water bills.
          </p>
        </div>

        <button
          onClick={loadData}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md shadow-brand-600/30 transition disabled:opacity-50 self-start md:self-auto"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Records</span>
        </button>
      </div>

      {feedback && (
        <div
          className={`p-4 rounded-xl text-xs font-medium flex items-center justify-between ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : feedback.type === 'warning'
              ? 'bg-amber-50 text-amber-800 border border-amber-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>{feedback.message}</span>
          </div>
          <div className="flex items-center gap-3">
            {selectedResidentForBill && feedback.type === 'success' && !showBillModal && (
              <button
                type="button"
                onClick={() => openBillModalForResident(selectedResidentForBill)}
                className="px-3 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold transition flex items-center gap-1.5"
              >
                <IndianRupee className="w-3.5 h-3.5" />
                <span>Issue Water Bill Now</span>
              </button>
            )}
            <button onClick={() => setFeedback(null)} className="font-bold hover:underline">
              Dismiss
            </button>
          </div>
        </div>
      )}

      {/* Tabs Navigation */}
      <div className="flex border-b border-slate-200">
        <button
          onClick={() => setActiveTab('pending')}
          className={`flex items-center gap-2 py-3 px-5 text-sm font-bold border-b-2 transition ${
            activeTab === 'pending'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Clock className="w-4 h-4 text-amber-500" />
          <span>Pending Registrations</span>
          <span className="ml-1.5 px-2 py-0.5 rounded-full text-xs font-bold bg-amber-100 text-amber-800">
            {pendingUsers.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('approved')}
          className={`flex items-center gap-2 py-3 px-5 text-sm font-bold border-b-2 transition ${
            activeTab === 'approved'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Users className="w-4 h-4 text-emerald-500" />
          <span>Approved Residents & Manual Billing</span>
          <span className="ml-1.5 px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
            {verifiedResidents.length}
          </span>
        </button>
      </div>

      {/* Search Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by resident name, username, flat number, or email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
        <div className="text-xs font-medium text-slate-500">
          Showing {activeTab === 'pending' ? filteredPending.length : filteredApproved.length} record(s)
        </div>
      </div>

      {/* TAB 1: PENDING REGISTRATIONS */}
      {activeTab === 'pending' && (
        <div>
          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              <SkeletonCard count={6} />
            </div>
          ) : filteredPending.length === 0 ? (
            <div className="bg-white p-12 rounded-2xl border border-slate-200 text-center space-y-3">
              <div className="w-12 h-12 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-slate-900 text-base">Pending Verification Queue Clear</h3>
              <p className="text-xs text-slate-500 max-w-sm mx-auto">
                No pending user registration requests found. All registered residents have been verified.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              {filteredPending.map((u) => {
                const isAdminRole = u.role === 'ROLE_ADMIN';
                const canApprove = isSuperAdmin || !isAdminRole;

                return (
                  <div
                    key={u.id}
                    className="bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition p-5 flex flex-col justify-between space-y-4"
                  >
                    <div className="space-y-3">
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <span
                            className={`inline-block px-2 py-0.5 rounded text-[11px] font-bold uppercase tracking-wider ${
                              isAdminRole
                                ? 'bg-purple-100 text-purple-800 border border-purple-200'
                                : 'bg-brand-50 text-brand-700 border border-brand-200'
                            }`}
                          >
                            {isAdminRole ? 'Society Admin' : 'Resident'}
                          </span>
                          <h3 className="text-base font-bold text-slate-900 mt-1">{u.fullName}</h3>
                          <div className="text-[11px] font-mono text-slate-500">@{u.username || 'unassigned'}</div>
                        </div>
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-200">
                          PENDING
                        </span>
                      </div>

                      <div className="space-y-1.5 text-xs text-slate-600 bg-slate-50 p-3 rounded-xl border border-slate-100">
                        <div className="flex items-center gap-2">
                          <Mail className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                          <span className="truncate">{u.email}</span>
                        </div>
                        {u.phone && (
                          <div className="flex items-center gap-2">
                            <Phone className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                            <span>{u.phone}</span>
                          </div>
                        )}
                        <div className="flex items-center gap-2 font-medium text-slate-900">
                          <Home className="w-3.5 h-3.5 text-brand-500 shrink-0" />
                          <span>Flat: {u.flatNumber || 'Pending Assignment'}</span>
                        </div>
                        <div className="flex items-center gap-2 text-[11px] text-slate-400">
                          <Building className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                          <span>{u.apartmentName || 'Shanti Niketan Heights'}</span>
                        </div>
                      </div>
                    </div>

                    <div className="pt-2 border-t border-slate-100 flex items-center gap-2">
                      {canApprove ? (
                        <>
                          <button
                            onClick={() => handleApprove(u.id, u.fullName)}
                            disabled={actionLoading === u.id}
                            className="flex-1 flex items-center justify-center gap-1.5 py-2 px-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold transition disabled:opacity-50 shadow-sm"
                          >
                            <UserCheck className="w-3.5 h-3.5" />
                            <span>{actionLoading === u.id ? 'Approving...' : 'Approve & Verify'}</span>
                          </button>
                          <button
                            onClick={() => handleReject(u.id, u.fullName)}
                            disabled={actionLoading === u.id}
                            className="py-2 px-3 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-xl text-xs font-semibold transition"
                            title="Reject Application"
                          >
                            <UserX className="w-3.5 h-3.5" />
                          </button>
                        </>
                      ) : (
                        <div className="w-full text-center py-2 px-3 bg-slate-100 rounded-xl text-[11px] text-slate-500">
                          Superadmin approval required
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* TAB 2: APPROVED RESIDENTS & MANUAL BILLING */}
      {activeTab === 'approved' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
            <div>
              <h2 className="font-bold text-slate-900 text-sm">Approved Residents Roster</h2>
              <p className="text-xs text-slate-500">
                Verified flat occupants eligible for metered water consumption and manual billing.
              </p>
            </div>
          </div>

          {loading ? (
            <div className="p-4">
              <SkeletonTable rows={5} cols={6} />
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                <tr className="border-b border-slate-200 bg-slate-50/75 text-slate-400 uppercase font-semibold text-[10px]">
                  <th className="py-3 px-4">Flat / Unit</th>
                  <th className="py-3 px-4">Resident Name</th>
                  <th className="py-3 px-4">Username & Email</th>
                  <th className="py-3 px-4">Meter Status</th>
                  <th className="py-3 px-4">Account Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredApproved.map((res) => (
                  <tr key={res.id} className="hover:bg-slate-50 transition">
                    <td className="py-3 px-4 font-bold text-slate-900">
                      <div className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded bg-sky-50 text-sky-800 border border-sky-200">
                        <Home className="w-3 h-3 text-brand-600" />
                        <span>{res.flatNumber || 'Unit A'}</span>
                      </div>
                    </td>
                    <td className="py-3 px-4 font-bold text-slate-900">
                      {res.fullName}
                    </td>
                    <td className="py-3 px-4 text-slate-600">
                      <div className="font-mono text-sky-700 font-medium">@{res.username}</div>
                      <div className="text-[11px] text-slate-400">{res.email}</div>
                    </td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                        {res.isMetered ? 'Digital Smart Meter' : 'Standard Meter'}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800">
                        VERIFIED
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => openBillModalForResident(res)}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-brand-600 hover:bg-brand-500 text-white rounded-lg text-xs font-semibold shadow-sm transition"
                      >
                        <IndianRupee className="w-3.5 h-3.5" />
                        <span>Issue Water Bill</span>
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

      {/* MANUAL BILL ISSUANCE MODAL */}
      {showBillModal && selectedResidentForBill && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-200">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center">
                  <Receipt className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-900">Issue Water Bill in INR (₹)</h3>
                  <p className="text-xs text-slate-500">Manual billing for approved resident</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowBillModal(false)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {lastIssuedInvoice ? (
              <div className="py-6 text-center space-y-4">
                <div className="w-12 h-12 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <div>
                  <h4 className="text-base font-bold text-slate-900">Water Bill Issued Successfully!</h4>
                  <p className="text-xs text-slate-500 mt-1">
                    Invoice #{lastIssuedInvoice.invoiceNumber} of{' '}
                    <strong className="text-slate-900 font-mono">₹{lastIssuedInvoice.totalAmount}</strong> has been assigned to{' '}
                    {selectedResidentForBill.fullName}.
                  </p>
                </div>

                <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs font-mono text-left space-y-1">
                  <div>Invoice Number: <strong>{lastIssuedInvoice.invoiceNumber}</strong></div>
                  <div>Metered Usage: <strong>{lastIssuedInvoice.meteredConsumptionKl} kL</strong></div>
                  <div>Payment Status: <strong className="text-amber-600 font-bold">{lastIssuedInvoice.paymentStatus}</strong></div>
                </div>

                <div className="flex gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => downloadPdf(lastIssuedInvoice.id, lastIssuedInvoice.invoiceNumber)}
                    className="flex-1 py-2 px-3 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition"
                  >
                    <Download className="w-3.5 h-3.5 text-brand-600" />
                    <span>Download PDF</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowBillModal(false)}
                    className="flex-1 py-2 px-3 bg-brand-600 hover:bg-brand-500 text-white rounded-xl text-xs font-semibold transition"
                  >
                    Done
                  </button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleIssueBill} className="mt-4 space-y-3.5 text-xs">
                {/* Target Resident Summary Card */}
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between">
                  <div>
                    <div className="text-[10px] uppercase font-bold text-slate-400">Assigned Resident</div>
                    <div className="font-bold text-slate-900 text-sm">{selectedResidentForBill.fullName}</div>
                    <div className="text-[11px] text-slate-500">@{selectedResidentForBill.username} &bull; {selectedResidentForBill.email}</div>
                  </div>
                  <div className="text-right">
                    <span className="px-2 py-0.5 rounded font-bold bg-sky-100 text-sky-800 text-[10px]">
                      {selectedResidentForBill.flatNumber || 'Flat A'}
                    </span>
                  </div>
                </div>

                {/* Metered Usage */}
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">
                    Metered Consumption Volume (kL) *
                  </label>
                  <input
                    type="number"
                    step="0.001"
                    min="0"
                    required
                    value={meteredKl}
                    onChange={(e) => setMeteredKl(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                  />
                </div>

                {/* Metered Charge & Shared Common Pool */}
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
                      value={meteredCharge}
                      onChange={(e) => setMeteredCharge(e.target.value)}
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
                      value={sharedCharge}
                      onChange={(e) => setSharedCharge(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 font-mono text-xs"
                    />
                  </div>
                </div>

                {/* Total Calculated Banner */}
                <div className="p-3 rounded-xl bg-brand-50 border border-brand-200 flex items-center justify-between">
                  <span className="font-semibold text-brand-900 text-xs">Total Bill Amount (INR):</span>
                  <span className="font-extrabold text-base text-brand-700 font-mono">
                    ₹{(parseFloat(meteredCharge || 0) + parseFloat(sharedCharge || 0)).toFixed(2)}
                  </span>
                </div>

                {/* Notes */}
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">
                    Invoice Notes / Description
                  </label>
                  <input
                    type="text"
                    value={billNotes}
                    onChange={(e) => setBillNotes(e.target.value)}
                    placeholder="e.g. Monthly water consumption charge"
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-xl text-slate-900 focus:bg-white focus:ring-2 focus:ring-brand-500 text-xs"
                  />
                </div>

                <div className="pt-2 flex gap-2">
                  <button
                    type="button"
                    onClick={() => setShowBillModal(false)}
                    className="flex-1 py-2.5 px-3 rounded-xl text-slate-600 hover:bg-slate-100 text-xs font-semibold transition"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={issuingBill}
                    className="flex-1 py-2.5 px-3 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-bold shadow-md shadow-brand-500/20 transition disabled:opacity-50"
                  >
                    {issuingBill ? 'Issuing Bill...' : 'Issue Water Bill (₹)'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default UserVerificationsView;

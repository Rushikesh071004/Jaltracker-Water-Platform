import React, { useState } from 'react';
import api from '../api/axiosClient';
import {
  ShieldCheck,
  CreditCard,
  QrCode,
  Building2,
  CheckCircle2,
  AlertCircle,
  X,
  Lock,
  Smartphone,
  ChevronRight,
  Sparkles,
  ArrowRight,
} from 'lucide-react';

export const RazorpayModal = ({ isOpen, onClose, invoice, onPaymentSuccess }) => {
  if (!isOpen || !invoice) return null;

  const [activeTab, setActiveTab] = useState('upi'); // 'upi', 'card', 'netbanking'
  const [selectedUpiApp, setSelectedUpiApp] = useState('gpay');
  const [selectedBank, setSelectedBank] = useState('HDFC');
  const [upiId, setUpiId] = useState('resident@okhdfcbank');
  const [cardData, setCardData] = useState({
    number: '4111 1111 1111 1111',
    expiry: '12/28',
    cvv: '123',
    name: 'Rushikesh Waghmare',
  });
  const [processing, setProcessing] = useState(false);
  const [successData, setSuccessData] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');

  const amountInr = Number(invoice.totalAmount || 0).toFixed(2);

  const upiApps = [
    { id: 'gpay', name: 'Google Pay', icon: '⚡' },
    { id: 'phonepe', name: 'PhonePe', icon: '🟣' },
    { id: 'paytm', name: 'Paytm UPI', icon: '🔵' },
    { id: 'bhim', name: 'BHIM UPI', icon: '🇮🇳' },
  ];

  const banks = [
    { code: 'HDFC', name: 'HDFC Bank' },
    { code: 'SBI', name: 'State Bank of India' },
    { code: 'ICICI', name: 'ICICI Bank' },
    { code: 'AXIS', name: 'Axis Bank' },
    { code: 'KOTAK', name: 'Kotak Mahindra' },
  ];

  const handleFillTestCard = () => {
    setCardData({
      number: '4111 1111 1111 1111',
      expiry: '09/29',
      cvv: '786',
      name: 'Rushikesh Waghmare',
    });
  };

  const handlePay = async () => {
    setProcessing(true);
    setErrorMsg('');

    try {
      // Step 1: Create Order in INR on backend
      let orderId = invoice.razorpayOrderId;
      if (!orderId) {
        const orderRes = await api.post('/payments/razorpay/create-order', {
          invoiceId: invoice.id,
        });
        orderId = orderRes.data.orderId;
      }

      // Simulate network / 3D secure delay
      await new Promise((res) => setTimeout(res, 900));

      // Step 2: Generate demo payment ID and signature
      const randomSuffix = Math.floor(100000 + Math.random() * 900000);
      const paymentId = `pay_jal_${randomSuffix}`;
      const signature = `sig_jal_demo_${Date.now()}`;

      // Step 3: Verify and update status in PostgreSQL
      const verifyRes = await api.post('/payments/razorpay/verify', {
        invoiceId: invoice.id,
        razorpayOrderId: orderId,
        razorpayPaymentId: paymentId,
        razorpaySignature: signature,
      });

      setSuccessData({
        paymentId,
        orderId,
        amount: amountInr,
        invoiceNumber: invoice.invoiceNumber,
        paidAt: new Date().toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }),
      });

      if (onPaymentSuccess) {
        onPaymentSuccess(verifyRes.data);
      }
    } catch (err) {
      console.error('Razorpay payment error:', err);
      const message = err.response?.data?.error || err.response?.data?.message || 'Payment verification failed. Please try again.';
      setErrorMsg(message);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/70 backdrop-blur-sm flex items-center justify-center p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-slate-200 flex flex-col max-h-[90vh]">
        {/* Razorpay Blue Header */}
        <div className="bg-[#0b2239] text-white p-5 flex items-center justify-between border-b border-sky-900/40">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-sky-500/20 border border-sky-400/30 flex items-center justify-center text-sky-400 font-black text-lg">
              ₹
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-extrabold text-base tracking-tight text-white">Razorpay</span>
                <span className="text-[10px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-sky-500/20 text-sky-300 border border-sky-400/30">
                  Demo Mode
                </span>
              </div>
              <p className="text-xs text-sky-200/80 font-medium flex items-center gap-1">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400 inline" />
                JALTRACKER Secure Gateway &bull; INR Checkout
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            disabled={processing}
            className="w-8 h-8 rounded-full bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white flex items-center justify-center transition disabled:opacity-50"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-5 flex-1">
          {successData ? (
            /* Success State */
            <div className="py-6 text-center space-y-4 animate-in zoom-in-95 duration-200">
              <div className="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto shadow-inner">
                <CheckCircle2 className="w-10 h-10 stroke-[2.5]" />
              </div>

              <div>
                <span className="text-xs font-bold uppercase tracking-widest text-emerald-600">Payment Successful</span>
                <h3 className="text-2xl font-black text-slate-900 mt-1">₹{successData.amount}</h3>
                <p className="text-xs text-slate-500 mt-0.5">
                  Invoice <span className="font-mono font-semibold text-slate-700">{successData.invoiceNumber}</span> settled in full
                </p>
              </div>

              <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 text-left text-xs space-y-2">
                <div className="flex justify-between">
                  <span className="text-slate-500">Payment ID:</span>
                  <span className="font-mono font-bold text-slate-800">{successData.paymentId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Order ID:</span>
                  <span className="font-mono text-slate-700 text-[11px]">{successData.orderId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Timestamp:</span>
                  <span className="font-medium text-slate-800">{successData.paidAt} IST</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Status:</span>
                  <span className="font-bold text-emerald-600">PAID (Recorded in DB)</span>
                </div>
              </div>

              <button
                onClick={onClose}
                className="w-full py-3 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-sm shadow-lg shadow-emerald-600/20 transition"
              >
                Close & Return to Dashboard
              </button>
            </div>
          ) : (
            /* Payment Input State */
            <>
              {/* Bill Details Summary Card */}
              <div className="bg-sky-50/60 rounded-2xl p-4 border border-sky-100 flex items-center justify-between">
                <div>
                  <span className="text-[11px] uppercase font-bold text-sky-800 tracking-wider block">
                    Total Amount Due
                  </span>
                  <div className="flex items-baseline gap-1 mt-0.5">
                    <span className="text-2xl font-black text-slate-900 font-mono">₹{amountInr}</span>
                    <span className="text-xs font-semibold text-slate-500">INR</span>
                  </div>
                </div>

                <div className="text-right text-xs">
                  <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-200 block mb-1">
                    {invoice.paymentStatus || 'UNPAID'}
                  </span>
                  <span className="font-mono font-medium text-slate-600 block">{invoice.invoiceNumber}</span>
                </div>
              </div>

              {errorMsg && (
                <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-800 flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
                  <span>{errorMsg}</span>
                </div>
              )}

              {/* Payment Method Selector */}
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                  Select Payment Option:
                </label>
                <div className="grid grid-cols-3 gap-2">
                  <button
                    type="button"
                    onClick={() => setActiveTab('upi')}
                    className={`py-2.5 px-3 rounded-xl border text-xs font-bold flex flex-col items-center gap-1 transition ${
                      activeTab === 'upi'
                        ? 'bg-sky-500/10 border-sky-600 text-sky-800 ring-1 ring-sky-500'
                        : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                    }`}
                  >
                    <Smartphone className="w-4 h-4 text-sky-600" />
                    <span>UPI / QR</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setActiveTab('card')}
                    className={`py-2.5 px-3 rounded-xl border text-xs font-bold flex flex-col items-center gap-1 transition ${
                      activeTab === 'card'
                        ? 'bg-sky-500/10 border-sky-600 text-sky-800 ring-1 ring-sky-500'
                        : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                    }`}
                  >
                    <CreditCard className="w-4 h-4 text-sky-600" />
                    <span>Card</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setActiveTab('netbanking')}
                    className={`py-2.5 px-3 rounded-xl border text-xs font-bold flex flex-col items-center gap-1 transition ${
                      activeTab === 'netbanking'
                        ? 'bg-sky-500/10 border-sky-600 text-sky-800 ring-1 ring-sky-500'
                        : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                    }`}
                  >
                    <Building2 className="w-4 h-4 text-sky-600" />
                    <span>NetBanking</span>
                  </button>
                </div>
              </div>

              {/* UPI Tab */}
              {activeTab === 'upi' && (
                <div className="space-y-3 p-4 rounded-2xl bg-slate-50 border border-slate-200/80">
                  <span className="text-[11px] font-bold uppercase text-slate-500 block">Popular Indian UPI Apps</span>
                  <div className="grid grid-cols-2 gap-2">
                    {upiApps.map((app) => (
                      <button
                        key={app.id}
                        type="button"
                        onClick={() => setSelectedUpiApp(app.id)}
                        className={`p-2.5 rounded-xl border text-left text-xs font-semibold flex items-center justify-between transition ${
                          selectedUpiApp === app.id
                            ? 'bg-white border-sky-500 text-sky-800 shadow-sm ring-1 ring-sky-400'
                            : 'bg-white/60 border-slate-200 text-slate-700 hover:bg-white'
                        }`}
                      >
                        <div className="flex items-center gap-2">
                          <span>{app.icon}</span>
                          <span>{app.name}</span>
                        </div>
                        {selectedUpiApp === app.id && <CheckCircle2 className="w-4 h-4 text-sky-600" />}
                      </button>
                    ))}
                  </div>

                  <div className="pt-2">
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">UPI ID / VPA</label>
                    <input
                      type="text"
                      value={upiId}
                      onChange={(e) => setUpiId(e.target.value)}
                      placeholder="username@okhdfcbank"
                      className="w-full px-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none bg-white font-mono"
                    />
                  </div>
                </div>
              )}

              {/* Card Tab */}
              {activeTab === 'card' && (
                <div className="space-y-3 p-4 rounded-2xl bg-slate-50 border border-slate-200/80">
                  <div className="flex items-center justify-between">
                    <span className="text-[11px] font-bold uppercase text-slate-500">Test Card Details</span>
                    <button
                      type="button"
                      onClick={handleFillTestCard}
                      className="text-[11px] font-bold text-sky-600 hover:text-sky-700 flex items-center gap-1"
                    >
                      <Sparkles className="w-3.5 h-3.5" />
                      <span>Fill Demo Card</span>
                    </button>
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Card Number</label>
                    <input
                      type="text"
                      value={cardData.number}
                      onChange={(e) => setCardData({ ...cardData, number: e.target.value })}
                      className="w-full px-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none bg-white font-mono"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-[11px] font-semibold text-slate-600 mb-1">Expiry (MM/YY)</label>
                      <input
                        type="text"
                        value={cardData.expiry}
                        onChange={(e) => setCardData({ ...cardData, expiry: e.target.value })}
                        className="w-full px-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none bg-white font-mono"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-semibold text-slate-600 mb-1">CVV</label>
                      <input
                        type="password"
                        maxLength={4}
                        value={cardData.cvv}
                        onChange={(e) => setCardData({ ...cardData, cvv: e.target.value })}
                        className="w-full px-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none bg-white font-mono"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Cardholder Name</label>
                    <input
                      type="text"
                      value={cardData.name}
                      onChange={(e) => setCardData({ ...cardData, name: e.target.value })}
                      className="w-full px-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-sky-500 focus:outline-none bg-white"
                    />
                  </div>
                </div>
              )}

              {/* NetBanking Tab */}
              {activeTab === 'netbanking' && (
                <div className="space-y-3 p-4 rounded-2xl bg-slate-50 border border-slate-200/80">
                  <span className="text-[11px] font-bold uppercase text-slate-500 block">Select Your Bank</span>
                  <div className="grid grid-cols-1 gap-2">
                    {banks.map((b) => (
                      <button
                        key={b.code}
                        type="button"
                        onClick={() => setSelectedBank(b.code)}
                        className={`p-2.5 rounded-xl border text-left text-xs font-semibold flex items-center justify-between transition ${
                          selectedBank === b.code
                            ? 'bg-white border-sky-500 text-sky-800 shadow-sm ring-1 ring-sky-400'
                            : 'bg-white/60 border-slate-200 text-slate-700 hover:bg-white'
                        }`}
                      >
                        <div className="flex items-center gap-2">
                          <Building2 className="w-4 h-4 text-sky-600" />
                          <span>{b.name}</span>
                        </div>
                        {selectedBank === b.code && <CheckCircle2 className="w-4 h-4 text-sky-600" />}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* Pay Button */}
              <div className="pt-2">
                <button
                  type="button"
                  onClick={handlePay}
                  disabled={processing}
                  className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-sky-600 to-sky-700 hover:from-sky-500 hover:to-sky-600 text-white font-bold text-sm shadow-xl shadow-sky-600/25 flex items-center justify-center gap-2 transition disabled:opacity-50"
                >
                  {processing ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      <span>Verifying with Razorpay...</span>
                    </>
                  ) : (
                    <>
                      <Lock className="w-4 h-4" />
                      <span>Pay ₹{amountInr} via Razorpay</span>
                      <ArrowRight className="w-4 h-4" />
                    </>
                  )}
                </button>
                <p className="text-[11px] text-center text-slate-400 mt-2 flex items-center justify-center gap-1">
                  <Lock className="w-3 h-3 text-emerald-500" />
                  <span>256-bit SSL encrypted &bull; Razorpay Sandbox / Demo</span>
                </p>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

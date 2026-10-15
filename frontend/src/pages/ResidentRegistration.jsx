import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosClient';
import {
  Droplets,
  Building,
  User,
  Mail,
  Lock,
  Home,
  Phone,
  Eye,
  EyeOff,
  UserPlus,
  CheckCircle2,
  AlertCircle,
  ArrowRight,
  ShieldCheck,
  RefreshCw,
  Clock,
  Sparkles,
} from 'lucide-react';

export const ResidentRegistration = () => {
  const [fullName, setFullName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [flatNumber, setFlatNumber] = useState('');
  const [phone, setPhone] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [successData, setSuccessData] = useState(null);
  const [registeredCount, setRegisteredCount] = useState(0);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!fullName.trim() || !username.trim() || !email.trim() || !password || !flatNumber.trim()) {
      setError('Please fill in all required fields (Full Name, Username, Email, Password, and Flat Number).');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload = {
        fullName: fullName.trim(),
        username: username.trim().toLowerCase(),
        email: email.trim().toLowerCase(),
        password,
        flatNumber: flatNumber.trim().toUpperCase(),
        phone: phone.trim() || undefined,
        role: 'ROLE_RESIDENT',
      };

      const res = await api.post('/auth/register', payload);

      setSuccessData({
        fullName: res.data.fullName || fullName,
        username: res.data.username || username,
        email: res.data.email || email,
        flatNumber: res.data.flatNumber || flatNumber.toUpperCase(),
        status: res.data.accountStatus || 'PENDING',
      });
      setRegisteredCount((prev) => prev + 1);
    } catch (err) {
      console.error('Registration failed:', err);
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Registration failed. Please verify that this username or email is not already registered.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRegisterAnother = () => {
    // Reset form for sequential registration
    setFullName('');
    setUsername('');
    setEmail('');
    setPassword('');
    setFlatNumber('');
    setPhone('');
    setError('');
    setSuccessData(null);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-850 to-slate-950 flex flex-col justify-center py-10 sm:px-6 lg:px-8 text-slate-100">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-tr from-brand-600 to-sky-400 text-white shadow-xl shadow-brand-500/20 mb-3">
          <Droplets className="w-9 h-9" />
        </div>
        <h2 className="text-3xl font-extrabold tracking-tight text-white">JALTRACKER</h2>
        <p className="mt-1 text-sm text-slate-400">
          Resident Onboarding & Water Meter Registration
        </p>
        <div className="mt-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-800 border border-slate-700 text-xs text-brand-300">
          <Building className="w-3.5 h-3.5" />
          <span>Shanti Niketan Heights &bull; INR Currency System (₹)</span>
        </div>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-lg px-4">
        <div className="bg-slate-800/80 backdrop-blur-md border border-slate-700/80 py-8 px-6 shadow-2xl rounded-3xl sm:px-10">
          {/* Header Title */}
          <div className="mb-6 text-center">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-500/10 border border-brand-500/20 text-brand-300 text-xs font-semibold mb-2">
              <UserPlus className="w-3.5 h-3.5" />
              <span>Resident Self-Registration</span>
            </div>
            <h3 className="text-xl font-bold text-white tracking-tight">Create Resident Account</h3>
            <p className="text-xs text-slate-400 mt-1">
              Submit your details to request access. All accounts require Super Admin verification.
            </p>
            {registeredCount > 0 && (
              <div className="mt-2 text-xs font-semibold text-emerald-400">
                &bull; {registeredCount} resident account(s) registered in this session
              </div>
            )}
          </div>

          {error && (
            <div className="mb-5 p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-start gap-3 text-rose-300 text-xs">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-rose-400" />
              <span>{error}</span>
            </div>
          )}

          {/* Success / Pending Approval State */}
          {successData ? (
            <div className="space-y-5 animate-in fade-in duration-300">
              <div className="p-5 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 space-y-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
                    <CheckCircle2 className="w-6 h-6" />
                  </div>
                  <div>
                    <h4 className="font-bold text-sm text-white">Registration Submitted Successfully!</h4>
                    <p className="text-xs text-emerald-300/90 mt-0.5">
                      Account Status: <span className="font-bold underline text-amber-300">PENDING APPROVAL</span>
                    </p>
                  </div>
                </div>

                <div className="bg-slate-900/80 p-3.5 rounded-xl border border-slate-700/60 text-xs text-slate-300 space-y-1.5 font-mono">
                  <div className="flex justify-between">
                    <span className="text-slate-400 font-sans">Full Name:</span>
                    <span className="text-white font-bold">{successData.fullName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400 font-sans">Username:</span>
                    <span className="text-sky-300 font-bold">{successData.username}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400 font-sans">Email:</span>
                    <span className="text-slate-200">{successData.email}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400 font-sans">Flat Number:</span>
                    <span className="text-emerald-300 font-bold">{successData.flatNumber}</span>
                  </div>
                </div>

                <div className="flex items-start gap-2 pt-2 text-[11px] text-slate-300 border-t border-emerald-500/20">
                  <Clock className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                  <span>
                    Your account is currently in <strong>PENDING</strong> status. Once approved by the Super Admin,
                    you will be able to log in with username <strong className="text-white">{successData.username}</strong> and view your bills.
                  </span>
                </div>
              </div>

              {/* Action Buttons: Sequential Registration or Go to Login */}
              <div className="flex flex-col sm:flex-row gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleRegisterAnother}
                  className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-slate-700 hover:bg-slate-600 text-white font-bold text-xs transition border border-slate-600"
                >
                  <RefreshCw className="w-4 h-4 text-sky-400" />
                  <span>Register Another Resident</span>
                </button>

                <button
                  type="button"
                  onClick={() => navigate('/login')}
                  className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl shadow-lg shadow-brand-500/25 bg-gradient-to-r from-brand-600 to-sky-500 hover:from-brand-500 hover:to-sky-400 text-white font-bold text-xs transition"
                >
                  <span>Go to Sign In</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          ) : (
            /* Registration Form */
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Full Name */}
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                  Full Name *
                </label>
                <div className="relative rounded-xl">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="e.g. Rushikesh Waghmare"
                    className="block w-full pl-10 pr-3 py-2.5 bg-slate-900/90 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500 text-xs transition"
                  />
                </div>
              </div>

              {/* Username & Flat Number Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                    Username *
                  </label>
                  <div className="relative rounded-xl">
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                      <Sparkles className="w-4 h-4" />
                    </div>
                    <input
                      type="text"
                      required
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      placeholder="e.g. rushikesh.w"
                      className="block w-full pl-10 pr-3 py-2.5 bg-slate-900/90 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500 text-xs transition"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                    Flat / Apartment No. *
                  </label>
                  <div className="relative rounded-xl">
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                      <Home className="w-4 h-4" />
                    </div>
                    <input
                      type="text"
                      required
                      value={flatNumber}
                      onChange={(e) => setFlatNumber(e.target.value)}
                      placeholder="e.g. Flat B-303"
                      className="block w-full pl-10 pr-3 py-2.5 bg-slate-900/90 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500 text-xs transition uppercase"
                    />
                  </div>
                </div>
              </div>

              {/* Email Address */}
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                  Email Address *
                </label>
                <div className="relative rounded-xl">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="rushikesh.waghmare@jaltracker.com"
                    className="block w-full pl-10 pr-3 py-2.5 bg-slate-900/90 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500 text-xs transition"
                  />
                </div>
              </div>

              {/* Password & Phone Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                    Password *
                  </label>
                  <div className="relative rounded-xl">
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                      <Lock className="w-4 h-4" />
                    </div>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      required
                      minLength={6}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Min. 6 characters"
                      className="block w-full pl-10 pr-10 py-2.5 bg-slate-900/90 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500 text-xs transition"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-200"
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                    Phone (Optional)
                  </label>
                  <div className="relative rounded-xl">
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                      <Phone className="w-4 h-4" />
                    </div>
                    <input
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="+91 98765 43210"
                      className="block w-full pl-10 pr-3 py-2.5 bg-slate-900/90 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500 text-xs transition"
                    />
                  </div>
                </div>
              </div>

              {/* Status Note Banner */}
              <div className="p-3 rounded-xl bg-slate-900/90 border border-slate-700/80 flex items-start gap-2.5 text-slate-400 text-[11px] leading-relaxed">
                <ShieldCheck className="w-4 h-4 text-brand-400 shrink-0 mt-0.5" />
                <div>
                  <span className="text-slate-200 font-semibold">Automatic Security Policy: </span>
                  New registrations are created with <span className="text-amber-400 font-bold">ROLE_RESIDENT</span> and
                  status <span className="text-amber-400 font-bold">PENDING</span>. You will receive access once approved by the Super Admin.
                </div>
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl shadow-lg shadow-brand-500/25 bg-gradient-to-r from-brand-600 via-sky-600 to-sky-500 hover:from-brand-500 hover:to-sky-400 text-white font-bold text-xs transition disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span>Submitting Registration...</span>
                ) : (
                  <>
                    <UserPlus className="w-4 h-4" />
                    <span>Register as a Resident</span>
                  </>
                )}
              </button>
            </form>
          )}

          {/* Footer Navigation */}
          <div className="mt-6 pt-5 border-t border-slate-700/60 text-center">
            <p className="text-xs text-slate-400">
              Already have an approved account?{' '}
              <Link to="/login" className="font-bold text-brand-400 hover:text-brand-300 underline transition">
                Sign In here
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResidentRegistration;

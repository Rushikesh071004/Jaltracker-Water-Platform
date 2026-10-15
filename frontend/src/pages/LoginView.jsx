import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Droplets,
  ShieldAlert,
  CheckCircle2,
  ArrowRight,
  Lock,
  User,
  Mail,
  Shield,
  KeyRound,
  Sparkles,
  RefreshCw,
  Send,
  Building,
  Eye,
  EyeOff,
  UserCheck,
  UserPlus,
  Smartphone,
  MessageSquare,
  Check,
  RotateCcw,
} from 'lucide-react';

export const LoginView = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showOtpAlternative, setShowOtpAlternative] = useState(false);

  // Requirement 4: 2FA OTP Verification State (WhatsApp + Email)
  const [isOtpStep, setIsOtpStep] = useState(false);
  const [twoFactorOtp, setTwoFactorOtp] = useState('');
  const [otpSentDetails, setOtpSentDetails] = useState(null);

  // OTP flow state (optional alternative)
  const [otpEmail, setOtpEmail] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [demoOtpCode, setDemoOtpCode] = useState('');

  const [infoMessage, setInfoMessage] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login, loginWithOtp, sendOtp, sendLoginOtp, validateLoginOtp } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Two-Factor Authentication toggle (enabled by default)
  const [useTwoFactor, setUseTwoFactor] = useState(true);

  // Direct login without OTP (awaits JWT from backend and redirects)
  const handleDirectPasswordLogin = async (e) => {
    e?.preventDefault();
    if (!usernameOrEmail.trim() || !password) {
      setError('Please enter both your username (or email) and password.');
      return;
    }
    setError('');
    setInfoMessage('');
    setIsSubmitting(true);
    try {
      const loggedInUser = await login(usernameOrEmail.trim(), password);
      redirectAfterAuth(loggedInUser);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Invalid credentials or account issue.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Direct 1-click login for demo accounts
  const handleDirectLoginAccount = async (u, p) => {
    setError('');
    setInfoMessage('');
    setIsSubmitting(true);
    try {
      const loggedInUser = await login(u, p);
      redirectAfterAuth(loggedInUser);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Invalid credentials or account issue.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Primary Login Step 1: Submit Credentials
  const handleStandardLogin = async (e) => {
    e?.preventDefault();
    if (!usernameOrEmail.trim() || !password) {
      setError('Please enter both your username (or email) and password.');
      return;
    }
    setError('');
    setInfoMessage('');
    setIsSubmitting(true);

    try {
      if (!useTwoFactor) {
        // Direct password login: awaits JWT token and redirects
        const loggedInUser = await login(usernameOrEmail.trim(), password);
        redirectAfterAuth(loggedInUser);
        return;
      }

      // Step 4: 2FA OTP flow: dispatch codes to WhatsApp & Email
      const res = await sendLoginOtp(usernameOrEmail.trim(), password);
      setIsOtpStep(true);
      setOtpSentDetails(res);
      setDemoOtpCode(res.demoOtp || '');
      setInfoMessage(`Security OTP dispatched to your WhatsApp (${res.maskedPhone || 'registered phone'}) and Email (${res.maskedEmail || 'registered email'}).`);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Invalid username or password. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Primary Login Step 2: Validate 2FA OTP code and finalize JWT session
  const handleValidateTwoFactorOtp = async (e) => {
    e?.preventDefault();
    if (!twoFactorOtp.trim() || twoFactorOtp.length < 6) {
      setError('Please enter the complete 6-digit OTP code received on WhatsApp or Email.');
      return;
    }
    setError('');
    setIsSubmitting(true);

    try {
      const loggedInUser = await validateLoginOtp(usernameOrEmail.trim(), twoFactorOtp.trim());
      redirectAfterAuth(loggedInUser);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Invalid or expired verification code.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResendTwoFactorOtp = async () => {
    setError('');
    setIsSubmitting(true);
    try {
      const res = await sendLoginOtp(usernameOrEmail.trim(), password);
      setDemoOtpCode(res.demoOtp || '');
      setInfoMessage('A new verification code has been dispatched to WhatsApp and Email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend code.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Optional Alternative: Send OTP to email
  const handleSendOtp = async (e) => {
    e?.preventDefault();
    if (!otpEmail.trim()) {
      setError('Please provide your registered email address to receive OTP.');
      return;
    }
    setError('');
    setInfoMessage('');
    setIsSubmitting(true);

    try {
      const res = await sendOtp(otpEmail.trim());
      setOtpSent(true);
      setDemoOtpCode(res.demoOtp || '');
      setInfoMessage(`A 6-digit login verification code was sent to ${res.email || otpEmail}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to dispatch verification code. Please check email address.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Optional Alternative: Verify OTP and login
  const handleVerifyOtp = async (e) => {
    e?.preventDefault();
    if (!otpCode.trim()) {
      setError('Please enter the 6-digit verification code.');
      return;
    }
    setError('');
    setIsSubmitting(true);

    try {
      const loggedInUser = await loginWithOtp(otpEmail.trim(), otpCode.trim());
      redirectAfterAuth(loggedInUser);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid or expired OTP code.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const redirectAfterAuth = (user) => {
    if (user.role === 'ROLE_SUPERADMIN' || user.role === 'ROLE_ADMIN') {
      navigate('/admin/dashboard');
    } else {
      navigate('/resident/dashboard');
    }
  };

  const fillQuickCredentials = (userValue, passValue) => {
    setUsernameOrEmail(userValue);
    setPassword(passValue);
    setError('');
    setInfoMessage('');
    setIsOtpStep(false);
    setTwoFactorOtp('');
    setShowOtpAlternative(false);
  };

  const demoAccounts = [
    {
      roleTitle: 'Resident • Flat A-101',
      fullName: 'Rushikesh Waghmare',
      username: 'rushikesh',
      email: 'rushikesh@jaltracker.com',
      password: 'Resident@123',
      badgeColor: 'bg-emerald-950/40 border-emerald-800/40 text-emerald-300',
      pillColor: 'bg-emerald-400',
      tag: 'Metered Flat',
    },
    {
      roleTitle: 'Resident • Flat A-102',
      fullName: 'Rushikesh Waghmare',
      username: 'resident2',
      email: 'resident2@jaltracker.com',
      password: 'Resident@123',
      badgeColor: 'bg-teal-950/40 border-teal-800/40 text-teal-300',
      pillColor: 'bg-teal-400',
      tag: 'Family Unit',
    },
    {
      roleTitle: 'Resident • Flat A-103',
      fullName: 'Rushikesh Waghmare',
      username: 'resident3',
      email: 'resident3@jaltracker.com',
      password: 'Resident@123',
      badgeColor: 'bg-cyan-950/40 border-cyan-800/40 text-cyan-300',
      pillColor: 'bg-cyan-400',
      tag: 'Conservation',
    },
    {
      roleTitle: 'Society Admin',
      fullName: 'Rushikesh Waghmare (Society Admin)',
      username: 'admin',
      email: 'admin@jaltracker.com',
      password: 'Admin@123',
      badgeColor: 'bg-sky-950/40 border-sky-800/40 text-sky-300',
      pillColor: 'bg-sky-400',
      tag: 'Society Ops',
    },
    {
      roleTitle: 'Superadmin',
      fullName: 'Rushikesh Waghmare (Superadmin)',
      username: 'superadmin',
      email: 'superadmin@jaltracker.com',
      password: 'Superadmin@123',
      badgeColor: 'bg-purple-950/40 border-purple-800/40 text-purple-300',
      pillColor: 'bg-purple-400',
      tag: 'Master Access',
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-850 to-slate-950 flex flex-col justify-center py-10 sm:px-6 lg:px-8 text-slate-100">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-tr from-brand-600 to-sky-400 text-white shadow-xl shadow-brand-500/20 mb-3">
          <Droplets className="w-9 h-9" />
        </div>
        <h2 className="text-3xl font-extrabold tracking-tight text-white">JALTRACKER</h2>
        <p className="mt-1 text-sm text-slate-400">
          Smart Water Consumption, Conservation & Automated Billing Platform
        </p>
        <div className="mt-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-800 border border-slate-700 text-xs text-brand-300">
          <Building className="w-3.5 h-3.5" />
          <span>Shanti Niketan Heights &bull; INR Currency System (₹)</span>
        </div>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-md px-4">
        <div className="bg-slate-800/80 backdrop-blur-md border border-slate-700/80 py-8 px-6 shadow-2xl rounded-3xl sm:px-10">
          {/* Header Title */}
          <div className="mb-6 text-center">
            <h3 className="text-lg font-bold text-white tracking-tight">Account Sign In</h3>
            <p className="text-xs text-slate-400 mt-0.5">
              Enter your standard username or email with password to proceed
            </p>
          </div>

          {error && (
            <div className="mb-5 p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-start gap-3 text-rose-300 text-xs">
              <ShieldAlert className="w-4 h-4 shrink-0 mt-0.5 text-rose-400" />
              <span>{error}</span>
            </div>
          )}

          {infoMessage && (
            <div className="mb-5 p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-start gap-3 text-emerald-300 text-xs">
              <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5 text-emerald-400" />
              <div className="flex-1">
                <span>{infoMessage}</span>
                {demoOtpCode && (
                  <div className="mt-2 pt-2 border-t border-emerald-500/20 flex items-center justify-between">
                    <span className="font-mono text-xs bg-slate-900 px-2 py-0.5 rounded text-white">
                      Code: {demoOtpCode}
                    </span>
                    <button
                      type="button"
                      onClick={() => {
                        setTwoFactorOtp(demoOtpCode);
                        setOtpCode(demoOtpCode);
                      }}
                      className="text-[11px] font-bold text-emerald-400 underline hover:text-emerald-300"
                    >
                      Autofill Code
                    </button>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* CONDITIONAL RENDERING: STEP 1 (CREDENTIALS) vs STEP 2 (2FA OTP VERIFICATION) */}
          {isOtpStep ? (
            /* STEP 2: 2FA OTP VERIFICATION VIA WHATSAPP & EMAIL */
            <form onSubmit={handleValidateTwoFactorOtp} className="space-y-4 animate-in fade-in duration-300">
              <div className="text-center p-3.5 rounded-2xl bg-slate-900/80 border border-slate-700/80 space-y-2">
                <div className="flex items-center justify-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center">
                    <Smartphone className="w-4 h-4" />
                  </div>
                  <div className="w-8 h-8 rounded-xl bg-sky-500/20 text-sky-400 border border-sky-500/30 flex items-center justify-center">
                    <Mail className="w-4 h-4" />
                  </div>
                </div>
                <h4 className="text-sm font-bold text-white">Two-Factor OTP Verification</h4>
                <p className="text-[11px] text-slate-400 leading-relaxed">
                  Enter the 6-digit code delivered to your registered WhatsApp & Email:
                </p>
                <div className="flex flex-col sm:flex-row items-center justify-center gap-2 pt-1 text-[11px]">
                  <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-300 border border-emerald-500/20 font-mono">
                    WhatsApp: {otpSentDetails?.maskedPhone || 'Registered Mobile'}
                  </span>
                  <span className="px-2.5 py-0.5 rounded-full bg-sky-500/10 text-sky-300 border border-sky-500/20 font-mono">
                    Email: {otpSentDetails?.maskedEmail || 'Registered Email'}
                  </span>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5 text-center">
                  6-Digit Verification OTP
                </label>
                <input
                  type="text"
                  required
                  autoFocus
                  maxLength={6}
                  value={twoFactorOtp}
                  onChange={(e) => setTwoFactorOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  placeholder="000000"
                  className="block w-full py-3 px-4 bg-slate-900 border border-slate-700 rounded-xl text-center text-2xl font-mono tracking-widest text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500 transition"
                />
              </div>

              {demoOtpCode && (
                <button
                  type="button"
                  onClick={() => setTwoFactorOtp(demoOtpCode)}
                  className="w-full text-center text-[11px] font-semibold text-emerald-400 hover:text-emerald-300 flex items-center justify-center gap-1.5"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>Demo Code: <span className="font-mono underline">{demoOtpCode}</span> (Click to autofill)</span>
                </button>
              )}

              <button
                type="submit"
                disabled={isSubmitting || twoFactorOtp.length < 6}
                className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl shadow-lg shadow-emerald-500/25 bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white font-bold text-xs transition disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span>Verifying Code...</span>
                ) : (
                  <>
                    <span>Verify & Complete Sign In</span>
                    <Check className="w-4 h-4" />
                  </>
                )}
              </button>

              <div className="flex items-center justify-between pt-2 border-t border-slate-700/60 text-xs">
                <button
                  type="button"
                  onClick={handleResendTwoFactorOtp}
                  disabled={isSubmitting}
                  className="text-slate-400 hover:text-brand-300 flex items-center gap-1 transition disabled:opacity-50"
                >
                  <RotateCcw className="w-3.5 h-3.5" />
                  <span>Resend Code</span>
                </button>

                <button
                  type="button"
                  onClick={handleDirectPasswordLogin}
                  disabled={isSubmitting}
                  className="text-emerald-400 hover:text-emerald-300 font-semibold transition disabled:opacity-50"
                >
                  Skip OTP & Sign In →
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setIsOtpStep(false);
                    setTwoFactorOtp('');
                    setError('');
                  }}
                  className="text-slate-400 hover:text-white transition"
                >
                  ← Re-enter Password
                </button>
              </div>
            </form>
          ) : (
            /* PRIMARY LOGIN FORM: Standard Username and Password */
            <form onSubmit={handleStandardLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                  Username or Email Address
                </label>
                <div className="relative rounded-xl">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    required
                    autoFocus
                    value={usernameOrEmail}
                    onChange={(e) => setUsernameOrEmail(e.target.value)}
                    placeholder="e.g. rushikesh or admin or email"
                    className="block w-full pl-10 pr-3 py-2.5 bg-slate-900/90 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500 text-xs transition"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                  Password
                </label>
                <div className="relative rounded-xl">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter your account password"
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

              <div className="flex items-center justify-between text-xs py-0.5">
                <label className="flex items-center gap-2 cursor-pointer text-slate-300 hover:text-white select-none">
                  <input
                    type="checkbox"
                    checked={useTwoFactor}
                    onChange={(e) => setUseTwoFactor(e.target.checked)}
                    className="w-4 h-4 rounded border-slate-700 bg-slate-900 text-brand-500 focus:ring-brand-500"
                  />
                  <span>Two-Factor OTP (WhatsApp + Email)</span>
                </label>
                {!useTwoFactor && (
                  <span className="text-[10px] text-amber-400 font-bold px-2 py-0.5 rounded bg-amber-500/10 border border-amber-500/20">
                    Direct Mode
                  </span>
                )}
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl shadow-lg shadow-brand-500/25 bg-gradient-to-r from-brand-600 via-sky-600 to-sky-500 hover:from-brand-500 hover:to-sky-400 text-white font-bold text-xs transition disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span>{useTwoFactor ? 'Sending Verification Codes...' : 'Authenticating...'}</span>
                ) : (
                  <>
                    <span>{useTwoFactor ? 'Sign In with Verification (OTP)' : 'Sign In with Credentials'}</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          )}

          {/* PROMINENT REGISTER AS A RESIDENT ACTION */}
          <div className="mt-4 pt-3 border-t border-slate-700/60">
            <button
              type="button"
              onClick={() => navigate('/register')}
              className="w-full flex items-center justify-between p-3 rounded-2xl bg-gradient-to-r from-emerald-950/60 via-slate-900 to-sky-950/60 border border-emerald-500/40 hover:border-emerald-400 shadow-lg shadow-emerald-950/30 transition group hover:scale-[1.01]"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400 group-hover:bg-emerald-500 group-hover:text-white transition">
                  <UserPlus className="w-4 h-4" />
                </div>
                <div className="text-left">
                  <div className="text-xs font-bold text-white group-hover:text-emerald-300 transition">
                    Register as a Resident
                  </div>
                  <div className="text-[11px] text-slate-400">
                    Self-onboarding for flats &bull; Pending Super Admin verification
                  </div>
                </div>
              </div>
              <ArrowRight className="w-4 h-4 text-emerald-400 group-hover:translate-x-1 transition shrink-0" />
            </button>
          </div>

          {/* OPTIONAL ALTERNATIVE: Direct Email OTP / Magic Link Toggle */}
          <div className="mt-5 pt-4 border-t border-slate-700/60">
            <button
              type="button"
              onClick={() => {
                setShowOtpAlternative(!showOtpAlternative);
                setError('');
                setInfoMessage('');
              }}
              className="w-full text-center text-xs font-semibold text-slate-400 hover:text-brand-300 flex items-center justify-center gap-1.5 transition"
            >
              <KeyRound className="w-3.5 h-3.5 text-brand-400" />
              <span>
                {showOtpAlternative ? 'Hide Direct Email OTP Login' : 'Or login with Email Directly (OTP / Magic Link)'}
              </span>
            </button>

            {showOtpAlternative && (
              <div className="mt-4 p-4 rounded-2xl bg-slate-900/90 border border-slate-700/80 space-y-3 animate-in fade-in duration-200">
                <span className="text-[11px] uppercase tracking-wider font-bold text-brand-400 block">
                  Passwordless Email Login
                </span>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-400 mb-1">
                    Enter Registered Email
                  </label>
                  <input
                    type="email"
                    value={otpEmail}
                    onChange={(e) => setOtpEmail(e.target.value)}
                    placeholder="rushikesh@jaltracker.com"
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500"
                  />
                </div>

                {!otpSent ? (
                  <button
                    type="button"
                    onClick={handleSendOtp}
                    disabled={isSubmitting || !otpEmail}
                    className="w-full py-2.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-semibold text-xs border border-slate-600 flex items-center justify-center gap-1.5 transition disabled:opacity-50"
                  >
                    <Send className="w-3.5 h-3.5" />
                    <span>Send Verification OTP</span>
                  </button>
                ) : (
                  <form onSubmit={handleVerifyOtp} className="space-y-3">
                    <div>
                      <label className="block text-[11px] font-semibold text-slate-400 mb-1">
                        Enter 6-Digit Code
                      </label>
                      <input
                        type="text"
                        maxLength={6}
                        value={otpCode}
                        onChange={(e) => setOtpCode(e.target.value)}
                        placeholder="123456"
                        className="w-full px-3 py-2 bg-slate-800 border border-brand-500 rounded-xl text-center font-mono text-sm tracking-widest text-white focus:outline-none"
                      />
                    </div>
                    <button
                      type="submit"
                      disabled={isSubmitting || otpCode.length < 6}
                      className="w-full py-2.5 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs transition disabled:opacity-50"
                    >
                      Verify & Sign In
                    </button>
                  </form>
                )}
              </div>
            )}
          </div>

          {/* Quick Demo Switcher for Rushikesh Waghmare Accounts */}
          <div className="mt-6 pt-5 border-t border-slate-700/60">
            <div className="flex items-center justify-between mb-2.5">
              <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">
                1-Click Resident & Admin Accounts
              </span>
              <span className="text-[10px] text-brand-400 font-medium">Click to Autofill</span>
            </div>

            <div className="space-y-2">
              {demoAccounts.map((acc, index) => (
                <div
                  key={index}
                  onClick={() => fillQuickCredentials(acc.username, acc.password)}
                  className={`w-full text-left p-2.5 rounded-xl border transition flex items-center justify-between hover:scale-[1.01] cursor-pointer ${acc.badgeColor}`}
                >
                  <div className="flex-1 min-w-0 pr-2">
                    <div className="flex items-center gap-1.5">
                      <span className={`w-2 h-2 rounded-full shrink-0 ${acc.pillColor}`}></span>
                      <span className="font-bold text-xs text-white truncate">{acc.fullName}</span>
                      <span className="text-[10px] text-slate-400 font-mono shrink-0">({acc.roleTitle})</span>
                    </div>
                    <p className="text-[11px] text-slate-400 mt-0.5 truncate">
                      Username: <span className="font-mono text-white font-bold">{acc.username}</span> &bull; Password: <span className="font-mono text-slate-300">{acc.password}</span>
                    </p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-slate-800/80 text-slate-300">
                      {acc.tag}
                    </span>
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        fillQuickCredentials(acc.username, acc.password);
                        handleDirectLoginAccount(acc.username, acc.password);
                      }}
                      className="text-[10px] font-bold px-2 py-1 rounded-lg bg-sky-500/20 hover:bg-sky-500 text-sky-300 hover:text-white border border-sky-500/40 transition shadow-sm"
                      title="Direct 1-Click Sign In"
                    >
                      Instant Login ⚡
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default LoginView;

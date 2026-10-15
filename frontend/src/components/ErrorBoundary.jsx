import React from 'react';
import { AlertOctagon, RefreshCw, Home, LogIn } from 'lucide-react';

export class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ errorInfo });
    console.error('ErrorBoundary caught an unhandled runtime error:', error, errorInfo);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null, errorInfo: null });
  };

  handleGoLogin = () => {
    localStorage.clear();
    window.location.href = '/login';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-slate-900 text-slate-100 flex items-center justify-center p-6">
          <div className="max-w-2xl w-full bg-slate-800 border border-rose-500/40 rounded-2xl p-6 sm:p-8 shadow-2xl space-y-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-rose-500/20 text-rose-400 border border-rose-500/30 flex items-center justify-center shrink-0">
                <AlertOctagon className="w-7 h-7" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-white tracking-tight">Application Error Encountered</h1>
                <p className="text-sm text-slate-400 mt-0.5">
                  An unhandled runtime error occurred in this view.
                </p>
              </div>
            </div>

            <div className="bg-slate-950/80 rounded-xl p-4 border border-slate-700/80 font-mono text-xs text-rose-300 overflow-x-auto">
              <p className="font-semibold text-rose-400 text-sm mb-1">
                {this.state.error?.name}: {this.state.error?.message}
              </p>
              {this.state.errorInfo?.componentStack && (
                <details className="mt-3 text-slate-400 cursor-pointer">
                  <summary className="hover:text-slate-200 select-none">View Component Hierarchy</summary>
                  <pre className="mt-2 text-[11px] leading-relaxed whitespace-pre-wrap text-slate-400">
                    {this.state.errorInfo.componentStack}
                  </pre>
                </details>
              )}
              {this.state.error?.stack && (
                <details className="mt-2 text-slate-400 cursor-pointer">
                  <summary className="hover:text-slate-200 select-none">View Stack Trace</summary>
                  <pre className="mt-2 text-[11px] leading-relaxed whitespace-pre-wrap text-slate-400">
                    {this.state.error.stack}
                  </pre>
                </details>
              )}
            </div>

            <div className="flex flex-wrap items-center gap-3 pt-2">
              <button
                onClick={this.handleReset}
                className="flex items-center gap-2 px-4 py-2.5 bg-brand-600 hover:bg-brand-500 text-white rounded-xl text-xs font-semibold shadow-md shadow-brand-600/30 transition"
              >
                <RefreshCw className="w-4 h-4" />
                <span>Retry Component</span>
              </button>
              <button
                onClick={() => (window.location.href = '/')}
                className="flex items-center gap-2 px-4 py-2.5 bg-slate-700 hover:bg-slate-600 text-slate-200 rounded-xl text-xs font-semibold transition"
              >
                <Home className="w-4 h-4" />
                <span>Go to Home</span>
              </button>
              <button
                onClick={this.handleGoLogin}
                className="flex items-center gap-2 px-4 py-2.5 bg-rose-600/20 hover:bg-rose-600/30 border border-rose-500/30 text-rose-300 rounded-xl text-xs font-semibold transition ml-auto"
              >
                <LogIn className="w-4 h-4" />
                <span>Clear Cache & Sign In</span>
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

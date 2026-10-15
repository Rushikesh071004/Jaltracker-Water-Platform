import React, { useState, useEffect } from 'react';
import api from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Cell,
} from 'recharts';
import {
  Compass,
  Sparkles,
  TrendingDown,
  TrendingUp,
  Award,
  Droplets,
  IndianRupee,
  ShieldCheck,
  CheckCircle2,
} from 'lucide-react';

export const ResidentComparisonView = () => {
  const { user } = useAuth();
  const householdId = user?.householdId || 1;

  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);

  useEffect(() => {
    const fetchComparison = async () => {
      try {
        const res = await api.get(`/resident/analytics/comparison?householdId=${householdId}`);
        setData(res.data);
      } catch (err) {
        console.error('Failed to load comparison data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchComparison();
  }, [householdId]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  const householdUsage = Number(data?.householdUsageKl) || 0;
  const apartmentAverage = Number(data?.apartmentAverageKl) || 0;
  const similarAverage = Number(data?.similarSizeAverageKl) || 0;
  const diffPct = data?.diffPercentageVsAverage || 0;
  const isEfficient = diffPct <= 0;

  const chartData = [
    { name: `Your Unit (${data?.flatNumber})`, consumption: householdUsage, fill: isEfficient ? '#10b981' : '#f43f5e' },
    { name: 'Apartment Average', consumption: apartmentAverage, fill: '#0284c7' },
    { name: 'Similar Size Units', consumption: similarAverage, fill: '#8b5cf6' },
  ];

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Conservation Benchmarks</h1>
          <p className="text-sm text-slate-500 mt-1">
            Compare your household's water consumption against building averages and similar-sized homes.
          </p>
        </div>
      </div>

      {/* Top Highlight Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-850 to-slate-900 text-white p-6 rounded-2xl shadow-lg border border-slate-800 grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
        <div className="md:col-span-2">
          <div className="flex items-center gap-2 mb-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-400"></span>
            <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
              Efficiency Classification
            </span>
          </div>
          <h2 className="text-2xl font-bold text-white flex items-center gap-2">
            <span>{data?.conservationStatus}</span>
            <Award className="w-6 h-6 text-amber-400 shrink-0" />
          </h2>
          <p className="text-xs text-slate-300 mt-2 leading-relaxed">
            {isEfficient ? (
              <>
                Excellent work! Your household consumes{' '}
                <span className="font-bold text-emerald-400">{Math.abs(diffPct)}% less water</span> than the building average. Your conservation helps prevent expensive tanker refills for the entire community.
              </>
            ) : (
              <>
                Your household is using{' '}
                <span className="font-bold text-rose-400">{diffPct}% more water</span> than the building average. Review fixtures for slow leaks and check the tips feed below to lower your monthly tiered tariff bracket.
              </>
            )}
          </p>
        </div>

        {/* Delta Card */}
        <div className="bg-slate-800/80 p-4 rounded-xl border border-slate-700/80 text-center">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 block">
            Variance vs Average
          </span>
          <div className="text-3xl font-extrabold font-mono mt-1 flex items-center justify-center gap-1">
            {isEfficient ? (
              <>
                <TrendingDown className="w-6 h-6 text-emerald-400" />
                <span className="text-emerald-400">-{Math.abs(diffPct)}%</span>
              </>
            ) : (
              <>
                <TrendingUp className="w-6 h-6 text-rose-400" />
                <span className="text-rose-400">+{diffPct}%</span>
              </>
            )}
          </div>
          <span className="text-[11px] text-slate-400 mt-1 block">
            {isEfficient ? 'Eco-Positive Footprint' : 'Higher Consumption Band'}
          </span>
        </div>
      </div>

      {/* Comparison Visualizer Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recharts Bar Comparison Chart */}
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h3 className="font-bold text-slate-900 text-base">Monthly Usage Comparison (kL)</h3>
              <p className="text-xs text-slate-500">Volumetric side-by-side benchmark</p>
            </div>
          </div>

          <div className="h-64 sm:h-80 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 20, right: 30, left: 10, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="name" stroke="#64748b" fontSize={11} tickLine={false} />
                <YAxis stroke="#64748b" fontSize={11} tickLine={false} label={{ value: 'kL Consumed', angle: -90, position: 'insideLeft', fill: '#94a3b8', fontSize: 10 }} />
                <Tooltip
                  formatter={(val) => [`${Number(val).toFixed(2)} kL`, 'Consumption']}
                  contentStyle={{ backgroundColor: '#0f172a', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Bar dataKey="consumption" radius={[8, 8, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Savings & Impact Estimator */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between space-y-4">
          <div>
            <h3 className="font-bold text-slate-900 text-base pb-3 border-b border-slate-100">
              Community Conservation Impact
            </h3>

            <div className="space-y-4 mt-4 text-xs">
              <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200">
                <span className="font-semibold text-slate-500 uppercase text-[10px] block">Your Measured Volume</span>
                <span className="text-xl font-bold font-mono text-slate-900 mt-1 block">{householdUsage.toFixed(2)} kL</span>
                <span className="text-slate-400 text-[11px]">~{(householdUsage * 1000).toLocaleString()} Liters</span>
              </div>

              <div className="p-3.5 rounded-xl bg-purple-50/70 border border-purple-200">
                <span className="font-semibold text-purple-700 uppercase text-[10px] block">Similar Homes Baseline</span>
                <span className="text-xl font-bold font-mono text-purple-900 mt-1 block">{similarAverage.toFixed(2)} kL</span>
                <span className="text-purple-600 text-[11px]">Units with matched area & occupant count</span>
              </div>

              <div className="p-3.5 rounded-xl bg-emerald-50/70 border border-emerald-200">
                <span className="font-semibold text-emerald-700 uppercase text-[10px] block">Potential Bill Reduction</span>
                <span className="text-xl font-bold font-mono text-emerald-800 mt-1 block">
                  ~₹{((householdUsage * 0.15) * 35.0).toFixed(2)} / month
                </span>
                <span className="text-emerald-600 text-[11px]">Estimated monthly savings with 15% water reduction</span>
              </div>
            </div>
          </div>

          <div className="pt-3 border-t border-slate-100 text-[11px] text-slate-400 flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4 text-brand-600 shrink-0" />
            <span>Progressive tiered tariffs reward lower volume bands automatically.</span>
          </div>
        </div>
      </div>
    </div>
  );
};

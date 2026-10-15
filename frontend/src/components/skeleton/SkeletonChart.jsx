import React from 'react';

export const SkeletonChart = ({ height = 'h-72', title = 'Loading Analytics...' }) => {
  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm animate-pulse flex flex-col">
      <div className="flex items-center justify-between mb-6">
        <div>
          <div className="h-4 w-40 bg-slate-200 rounded-md mb-2" />
          <div className="h-3 w-56 bg-slate-100 rounded-md" />
        </div>
        <div className="h-8 w-24 bg-slate-100 rounded-lg" />
      </div>

      <div className={`w-full ${height} bg-slate-50 rounded-xl p-4 flex items-end justify-between gap-3 border border-dashed border-slate-200`}>
        {[45, 70, 30, 85, 60, 95, 40, 75, 55, 80, 65, 90].map((h, idx) => (
          <div key={idx} className="flex-1 flex flex-col items-center gap-2 h-full justify-end">
            <div
              className="w-full bg-slate-200 rounded-t-md transition-all"
              style={{ height: `${h}%` }}
            />
            <div className="h-2.5 w-6 bg-slate-200 rounded" />
          </div>
        ))}
      </div>
    </div>
  );
};

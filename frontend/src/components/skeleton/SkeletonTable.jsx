import React from 'react';

export const SkeletonTable = ({ rows = 5, cols = 5 }) => {
  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden animate-pulse">
      {/* Header bar placeholder */}
      <div className="p-4 border-b border-slate-100 flex items-center justify-between">
        <div className="space-y-1.5">
          <div className="h-4 w-36 bg-slate-200 rounded-md" />
          <div className="h-3 w-48 bg-slate-100 rounded-md" />
        </div>
        <div className="h-8 w-24 bg-slate-200 rounded-lg" />
      </div>

      {/* Table rows placeholder */}
      <div className="divide-y divide-slate-100">
        {/* Table Head */}
        <div className="bg-slate-50/75 p-3 flex gap-4">
          {Array.from({ length: cols }).map((_, i) => (
            <div key={i} className="flex-1 h-3.5 bg-slate-200 rounded" />
          ))}
        </div>

        {/* Rows */}
        {Array.from({ length: rows }).map((_, rIdx) => (
          <div key={rIdx} className="p-3.5 flex items-center gap-4">
            {Array.from({ length: cols }).map((_, cIdx) => (
              <div
                key={cIdx}
                className="flex-1 h-4 bg-slate-100 rounded"
                style={{ width: `${Math.max(40, 90 - (cIdx * 15))}%` }}
              />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
};

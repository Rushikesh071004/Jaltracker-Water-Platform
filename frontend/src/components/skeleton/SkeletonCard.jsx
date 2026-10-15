import React from 'react';

export const SkeletonCard = ({ count = 1 }) => {
  return (
    <>
      {Array.from({ length: count }).map((_, idx) => (
        <div
          key={idx}
          className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm animate-pulse flex flex-col justify-between"
        >
          <div className="flex items-center justify-between mb-4">
            <div className="h-4 w-28 bg-slate-200 rounded-md" />
            <div className="h-8 w-8 bg-slate-200 rounded-xl" />
          </div>
          <div className="space-y-2">
            <div className="h-8 w-36 bg-slate-200 rounded-lg" />
            <div className="h-3 w-20 bg-slate-100 rounded-md" />
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
            <div className="h-3 w-24 bg-slate-100 rounded-md" />
            <div className="h-4 w-12 bg-slate-200 rounded-full" />
          </div>
        </div>
      ))}
    </>
  );
};

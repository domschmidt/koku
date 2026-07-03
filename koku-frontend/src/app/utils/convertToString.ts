export function convertToString(v: unknown): string {
  if (v === null) return 'null';
  if (v === undefined) return 'undefined';

  const t = typeof v;
  if (t === 'string') return v as string;
  if (t === 'number') return (v as number).toString();
  if (t === 'boolean') return (v as boolean).toString();
  if (t === 'bigint') return (v as bigint).toString();
  if (t === 'symbol') return (v as symbol).toString(); // z.B. "Symbol(foo)"
  if (t === 'function') return `[function ${(v as { name?: string }).name || 'anonymous'}]`;
  // Objekt (Array wurde vorher ausgeschlossen): JSON.stringify, fallback falls zirkulär
  try {
    return JSON.stringify(v);
  } catch {
    // bei zirkulären Strukturen: toString-Fallback
    // (JSON.stringify würde eine Exception werfen)
    return Object.prototype.toString.call(v);
  }
}

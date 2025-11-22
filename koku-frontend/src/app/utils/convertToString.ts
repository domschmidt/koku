
export function convertToString(v: unknown): string {
  if (v === null) return 'null';
  if (v === undefined) return 'undefined';

  const t = typeof v;
  if (t === 'string') return v as string;
  if (t === 'number' || t === 'boolean' || t === 'bigint') return String(v);
  if (t === 'symbol') return (v as symbol).toString(); // z.B. "Symbol(foo)"
  if (t === 'function') return (v as Function).toString(); // Funktionstext
  // Objekt (Array wurde vorher ausgeschlossen): JSON.stringify, fallback falls zirkulär
  try {
    return JSON.stringify(v);
  } catch {
    // bei zirkulären Strukturen: toString-Fallback
    // (JSON.stringify würde eine Exception werfen)
    try {
      return String(v);
    } catch {
      return '[unserializable]';
    }
  }
}

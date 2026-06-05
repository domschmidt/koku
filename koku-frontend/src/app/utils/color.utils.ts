export type ColorShade = 50 | 200 | 300 | 400 | 500 | 600 | 700 | 900;

const SEMANTIC_COLORS: Readonly<Record<string, string>> = {
  PRIMARY: 'primary',
  SECONDARY: 'secondary',
  ACCENT: 'accent',
  INFO: 'info',
  SUCCESS: 'success',
  WARNING: 'warning',
  ERROR: 'error',
  NEUTRAL: 'neutral',
};

const PALETTE_COLORS = new Set([
  'RED',
  'ORANGE',
  'AMBER',
  'YELLOW',
  'LIME',
  'GREEN',
  'EMERALD',
  'TEAL',
  'CYAN',
  'SKY',
  'BLUE',
  'INDIGO',
  'VIOLET',
  'PURPLE',
  'FUCHSIA',
  'PINK',
  'ROSE',
  'SLATE',
  'GRAY',
  'ZINC',
  'STONE',
]);

export function colorValue(color: string | undefined, shade: ColorShade = 600): string {
  const resolved = resolveColor(color);
  if (!resolved) {
    return '';
  }
  return resolved.semantic ? `var(--color-${resolved.name})` : `var(--color-${resolved.name}-${shade})`;
}

export function colorTextClass(color: string | undefined, fallback = 'PRIMARY'): string {
  const resolved = resolveColor(color ?? fallback);
  return resolved ? `text-${resolved.name}${resolved.semantic ? '' : '-600'}` : '';
}

export function colorBadgeClass(color: string | undefined, fallback = 'PRIMARY'): string {
  const resolved = resolveColor(color ?? fallback);
  if (!resolved) {
    return '';
  }
  return resolved.semantic
    ? `badge-${resolved.name}`
    : `border-${resolved.name}-600 bg-${resolved.name}-600 text-white`;
}

export function colorBackgroundClasses(color: string | undefined): string {
  const resolved = resolveColor(color);
  if (!resolved) {
    return '';
  }
  return resolved.semantic ? `bg-${resolved.name} text-${resolved.name}-content` : `bg-${resolved.name}-600 text-white`;
}

export function colorBorderClass(color: string | undefined, shade: 400 | 600 = 600): string {
  const resolved = resolveColor(color);
  if (!resolved) {
    return '';
  }
  return `border-${resolved.name}${resolved.semantic ? '' : `-${shade}`}`;
}

function resolveColor(color: string | undefined): { name: string; semantic: boolean } | undefined {
  if (!color) {
    return undefined;
  }
  const normalized = color.toUpperCase();
  const semantic = SEMANTIC_COLORS[normalized];
  if (semantic) {
    return { name: semantic, semantic: true };
  }
  return PALETTE_COLORS.has(normalized) ? { name: normalized.toLowerCase(), semantic: false } : undefined;
}

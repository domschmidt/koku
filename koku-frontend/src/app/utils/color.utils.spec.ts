import { colorBackgroundClasses, colorBadgeClass, colorBorderClass, colorTextClass, colorValue } from './color.utils';

describe('color utilities', () => {
  it('resolves semantic colors through their theme variables', () => {
    expect(colorValue('PRIMARY')).toBe('var(--color-primary)');
    expect(colorBorderClass('PRIMARY', 400)).toBe('border-primary');
    expect(colorBackgroundClasses('SUCCESS')).toBe('bg-success text-success-content');
  });

  it('resolves palette colors with explicit shades', () => {
    expect(colorValue('RED', 300)).toBe('var(--color-red-300)');
    expect(colorBorderClass('BLUE', 400)).toBe('border-blue-400');
    expect(colorTextClass('VIOLET')).toBe('text-violet-600');
    expect(colorBadgeClass('ROSE')).toBe('border-rose-600 bg-rose-600 text-white');
  });

  it('uses the configured fallback and rejects unknown colors', () => {
    expect(colorTextClass(undefined)).toBe('text-primary');
    expect(colorValue('UNKNOWN')).toBe('');
    expect(colorTextClass('UNKNOWN')).toBe('');
    expect(colorBadgeClass('UNKNOWN')).toBe('');
    expect(colorBackgroundClasses('UNKNOWN')).toBe('');
    expect(colorBorderClass('UNKNOWN')).toBe('');
  });
});

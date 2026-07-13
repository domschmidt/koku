import { formularAliasTestId } from './formular-alias';

describe('formularAliasTestId', () => {
  it('returns no test id for a missing or blank alias', () => {
    expect(formularAliasTestId(undefined)).toBeNull();
    expect(formularAliasTestId('  ')).toBeNull();
  });

  it('replaces whitespace with dashes', () => {
    expect(formularAliasTestId('customer appointment')).toBe('customer-appointment-form');
    expect(formularAliasTestId(' customer   appointment ')).toBe('customer-appointment-form');
  });
});

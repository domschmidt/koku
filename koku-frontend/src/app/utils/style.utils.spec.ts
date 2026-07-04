import { cssPixelValue } from './style.utils';

describe('style utilities', () => {
  it('keeps zero as an explicit pixel value', () => {
    expect(cssPixelValue(0)).toBe('0px');
  });

  it('omits undefined pixel values', () => {
    expect(cssPixelValue(undefined)).toBeUndefined();
  });

  it('formats numeric values as CSS pixels', () => {
    expect(cssPixelValue(799)).toBe('799px');
  });
});

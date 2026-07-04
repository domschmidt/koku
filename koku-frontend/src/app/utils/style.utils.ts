export const cssPixelValue = (value: number | undefined): string | undefined =>
  value === undefined ? undefined : `${value}px`;

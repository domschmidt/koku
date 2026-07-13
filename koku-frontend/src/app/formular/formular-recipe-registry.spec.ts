import { describe, expect, it } from 'vitest';
import { assertFormularRecipeCoverage } from './formular-recipe-registry';

describe('assertFormularRecipeCoverage', () => {
  it('accepts registered recipes and reports the first unsupported content type', () => {
    const view = {
      contents: {
        root: { id: 'root', '@type': 'container' },
        name: { id: 'name', '@type': 'input' },
      },
    } as unknown as KokuDto.FormViewDto;

    expect(() => assertFormularRecipeCoverage(view, { container: {} as never, input: {} as never })).not.toThrow();
    expect(() => assertFormularRecipeCoverage(view, { container: {} as never })).toThrow(
      'No recipe registered for content type: input',
    );
    expect(() => assertFormularRecipeCoverage({} as KokuDto.FormViewDto, {})).not.toThrow();
  });
});

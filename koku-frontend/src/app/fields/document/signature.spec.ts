import { describe, expect, it, vi } from 'vitest';

const signatureMocks = vi.hoisted(() => {
  const instances: any[] = [];
  class SignaturePad {
    static throwOnLoad = false;
    fromDataURL = vi.fn(() => {
      if (SignaturePad.throwOnLoad) throw new Error('bad image');
    });
    clear = vi.fn();
    off = vi.fn();
    on = vi.fn();
    toDataURL = vi.fn(() => 'data:image/png;base64,signature');
    addEventListener = vi.fn((name: string, callback: () => void) => {
      this.listeners[name] = callback;
    });
    listeners: Record<string, () => void> = {};
    constructor(readonly canvas: HTMLCanvasElement) {
      instances.push(this);
    }
  }
  return { SignaturePad, instances };
});

vi.mock('signature_pad', () => ({ default: signatureMocks.SignaturePad }));

import { signature } from './signature';

describe('signature PDFMe plugin', () => {
  it('renders editable signatures, clears and emits newly drawn image data', async () => {
    const scale = vi.fn();
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue({ scale } as any);
    const outer = document.createElement('div');
    outer.style.transform = 'matrix(1, 0, 0, 2, 0, 0)';
    const root = document.createElement('div');
    outer.append(root);
    document.body.append(outer);
    const onChange = vi.fn();
    await signature.ui!({
      schema: { width: 20, height: 10, readOnly: false } as any,
      value: 'data:image/png;base64,existing',
      onChange,
      rootElement: root,
      mode: 'form',
      i18n: () => 'Clear',
    } as any);
    const pad = signatureMocks.instances.at(-1);
    expect(scale).toHaveBeenCalledWith(0.5, 0.5);
    expect(pad.fromDataURL).toHaveBeenCalledWith('data:image/png;base64,existing', { ratio: 0.5 });
    expect(pad.on).toHaveBeenCalled();
    const button = root.querySelector('button')!;
    expect(button.textContent).toBe('Clear');
    button.click();
    expect(onChange).toHaveBeenCalledWith({ key: 'content', value: '' });
    pad.listeners['endStroke']();
    expect(onChange).toHaveBeenCalledWith({ key: 'content', value: 'data:image/png;base64,signature' });
    expect(root.querySelector('canvas')).not.toBeNull();
    outer.remove();
    vi.restoreAllMocks();
  });

  it('disables viewer and readonly forms and tolerates invalid existing data', async () => {
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue({ scale: vi.fn() } as any);
    const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    for (const [mode, readOnly] of [
      ['viewer', false],
      ['form', true],
    ] as const) {
      const root = document.createElement('div');
      await signature.ui!({
        schema: { width: 10, height: 10, readOnly } as any,
        value: '',
        rootElement: root,
        mode,
        i18n: () => '',
      } as any);
      const pad = signatureMocks.instances.at(-1);
      expect(pad.clear).toHaveBeenCalled();
      expect(pad.off).toHaveBeenCalled();
      expect(root.querySelector('button')).toBeNull();
    }

    const root = document.createElement('div');
    signatureMocks.SignaturePad.throwOnLoad = true;
    await signature.ui!({
      schema: { width: 10, height: 10 } as any,
      value: 'broken',
      rootElement: root,
      mode: 'viewer',
      i18n: () => '',
    } as any);
    expect(error).toHaveBeenCalled();
    signatureMocks.SignaturePad.throwOnLoad = false;
    vi.restoreAllMocks();
  });
});

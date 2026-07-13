import { afterEach, describe, expect, it, vi } from 'vitest';
import { FullscreenService } from './fullscreen.service';

describe('FullscreenService', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('uses fallback fullscreen and visual viewport sizing when native APIs are absent', () => {
    const service = new FullscreenService();
    const element = document.createElement('div');
    const viewport = Object.assign(new EventTarget(), { height: 640 });
    vi.stubGlobal('visualViewport', viewport);
    service.enter(element);
    expect(element.classList.contains('pseudo-fullscreen')).toBe(true);
    expect(document.body.classList.contains('no-scroll')).toBe(true);
    expect(document.documentElement.style.getPropertyValue('--vhvv')).toBe('640px');
    expect(service.isFallbackFullscreen()).toBe(true);
    service.exit();
    expect(element.classList.contains('pseudo-fullscreen')).toBe(false);
    expect(service.isFallbackFullscreen()).toBe(false);
  });

  it('uses standard native enter and exit APIs', () => {
    const service = new FullscreenService();
    const element = document.createElement('div');
    const requestFullscreen = vi.fn();
    Object.defineProperty(element, 'requestFullscreen', { configurable: true, value: requestFullscreen });
    Object.defineProperty(document.documentElement, 'requestFullscreen', { configurable: true, value: vi.fn() });
    const exitFullscreen = vi.fn();
    Object.defineProperty(document, 'exitFullscreen', { configurable: true, value: exitFullscreen });
    Object.defineProperty(document, 'fullscreenElement', { configurable: true, value: element });
    expect(service.canUseNative()).toBe(true);
    service.enter(element);
    service.exit();
    expect(requestFullscreen).toHaveBeenCalled();
    expect(exitFullscreen).toHaveBeenCalled();
    Object.defineProperty(document, 'fullscreenElement', { configurable: true, value: null });
  });
});

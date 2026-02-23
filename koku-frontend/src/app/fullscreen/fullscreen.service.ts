import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class FullscreenService {
  private fallbackEl?: HTMLElement;

  isFallbackFullscreen = signal(false);

  canUseNative(): boolean {
    const el: any = document.documentElement;
    return !!(el.requestFullscreen || el.webkitRequestFullscreen || el.mozRequestFullScreen || el.msRequestFullscreen);
  }

  enter(el: HTMLElement) {
    if (this.canUseNative()) {
      this.enterNative(el);
    } else {
      this.enterFallback(el);
    }
  }

  exit() {
    if (this.isNativeActive()) {
      this.exitNative();
    } else if (this.isFallbackFullscreen()) {
      this.exitFallback();
    }
    this.isFallbackFullscreen.set(false);
  }

  private enterNative(el: HTMLElement) {
    const anyEl: any = el;
    (
      anyEl.requestFullscreen ||
      anyEl.webkitRequestFullscreen ||
      anyEl.mozRequestFullScreen ||
      anyEl.msRequestFullscreen
    ).call(anyEl);
  }

  private exitNative() {
    const d: any = document;
    (d.exitFullscreen || d.webkitExitFullscreen || d.mozCancelFullScreen || d.msExitFullscreen).call(d);
  }

  private isNativeActive(): boolean {
    return !!(document.fullscreenElement || (document as any).webkitFullscreenElement);
  }

  private enterFallback(el: HTMLElement) {
    this.fallbackEl = el;
    el.classList.add('pseudo-fullscreen');
    document.body.classList.add('no-scroll');
    this.fixIOSViewport();
    this.isFallbackFullscreen.set(true);
  }

  private exitFallback() {
    this.fallbackEl?.classList.remove('pseudo-fullscreen');
    document.body.classList.remove('no-scroll');
    this.isFallbackFullscreen.set(false);
  }

  private fixIOSViewport() {
    if (!window.visualViewport) return;

    const update = () => {
      document.documentElement.style.setProperty('--vhvv', `${window.visualViewport!.height}px`);
    };

    window.visualViewport.addEventListener('resize', update);
    update();
  }
}

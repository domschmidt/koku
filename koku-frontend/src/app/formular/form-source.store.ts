import { computed, Signal, WritableSignal } from '@angular/core';
import { get } from '../utils/get';
import type { FormularSourceWriter } from './formular.component';

export class FormSourceStore {
  private readonly selectors = new Map<string, Signal<any>>();

  constructor(private readonly source: WritableSignal<any>) {}

  value(path: string | undefined, defaultValue?: any): any {
    if (!path) {
      return defaultValue;
    }
    let selector = this.selectors.get(path);
    if (!selector) {
      selector = computed(() => get(this.source(), path));
      this.selectors.set(path, selector);
    }
    const value = selector();
    return value === undefined ? defaultValue : value;
  }

  update(update: (source: FormularSourceWriter) => void) {
    const [snapshot, changed] = this.apply(this.source(), update);
    if (!changed) {
      return;
    }
    this.source.set(snapshot);
  }

  replaceAndUpdate(source: any, update: (source: FormularSourceWriter) => void) {
    const [snapshot] = this.apply(source, update);
    this.source.set(snapshot);
  }

  replace(source: any) {
    this.source.set(source);
  }

  resetSelectors() {
    this.selectors.clear();
  }

  private apply(source: any, update: (source: FormularSourceWriter) => void): [any, boolean] {
    let snapshot = source;
    let changed = false;
    update({
      set: (path, value) => {
        if (!path || Object.is(get(snapshot, path), value)) {
          return;
        }
        snapshot = this.setIn(snapshot, this.pathParts(path), value);
        changed = true;
      },
    });
    return [snapshot, changed];
  }

  private pathParts(path: string): string[] {
    const parts = path.match(/([^[.\]])+/g);
    if (!parts?.length) {
      throw new Error(`Invalid source path: ${path}`);
    }
    return parts;
  }

  private setIn(current: any, path: string[], value: any, index = 0): any {
    if (index === path.length) {
      return value;
    }
    const key = path[index];
    const currentValue = current?.[key];
    const clone = this.cloneForPathPart(current, key);
    clone[key] = this.setIn(currentValue, path, value, index + 1);
    return clone;
  }

  private cloneForPathPart(current: any, key: string): any {
    if (Array.isArray(current)) {
      return [...current];
    }
    if (current && typeof current === 'object') {
      return { ...current };
    }
    if (/^\d+$/.test(key)) {
      return [];
    }
    return {};
  }
}

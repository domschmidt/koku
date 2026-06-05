import { signal, WritableSignal } from '@angular/core';
import { Subject } from 'rxjs';
import type {
  ContentEvent,
  FormularContentHandle,
  FormularContentEvent,
  FormularContentInstance,
  FormularSourceWriter,
} from './formular.component';

interface MutableFormularContentHandle extends FormularContentHandle {
  events: Subject<FormularContentEvent>;
  value?: WritableSignal<any>;
  writeSource?: (source: FormularSourceWriter, value: any) => void;
  disabledCauses: WritableSignal<Set<string>>;
  requiredCauses: WritableSignal<Set<string>>;
  readonlyCauses: WritableSignal<Set<string>>;
  loadingCauses: WritableSignal<Set<string>>;
  instance?: FormularContentInstance;
}

export class FormContentStore {
  private readonly handles = new Map<string, MutableFormularContentHandle>();
  private expectedIds = new Set<string>();
  private acceptingRegistrations = true;
  private initialization = Promise.resolve();
  private resolveInitialization?: () => void;
  private rejectInitialization?: (error: unknown) => void;

  reconcile(contentIds: ReadonlySet<string>) {
    this.acceptingRegistrations = true;
    for (const [id, handle] of this.handles) {
      if (!contentIds.has(id)) {
        handle.events.complete();
        this.handles.delete(id);
      }
    }
    this.expectedIds = new Set(contentIds);
    this.initialization = new Promise<void>((resolve, reject) => {
      this.resolveInitialization = resolve;
      this.rejectInitialization = reject;
    });
    this.resolveWhenInitialized();
  }

  reset() {
    this.acceptingRegistrations = false;
    for (const handle of this.handles.values()) {
      handle.events.complete();
    }
    this.handles.clear();
    this.expectedIds.clear();
    this.resolveInitialization = undefined;
    this.rejectInitialization = undefined;
    this.initialization = Promise.resolve();
  }

  register(
    id: string,
    createValue?: () => WritableSignal<any>,
    writeSource?: (source: FormularSourceWriter, value: any) => void,
  ): FormularContentHandle {
    if (!this.acceptingRegistrations) {
      throw new Error(`Cannot register handle after runtime reset: ${id}`);
    }
    if (this.expectedIds.size > 0 && !this.expectedIds.has(id)) {
      throw new Error(`Cannot register handle outside the active form definition: ${id}`);
    }
    const existing = this.handles.get(id);
    if (existing) {
      if (Boolean(existing.value) !== Boolean(createValue) || Boolean(existing.writeSource) !== Boolean(writeSource)) {
        throw new Error(`Content control recipe cannot change for a stable id: ${id}`);
      }
      return existing;
    }
    const contentHandle: MutableFormularContentHandle = {
      events: new Subject<FormularContentEvent>(),
      value: createValue?.(),
      writeSource,
      disabledCauses: signal(new Set<string>()),
      requiredCauses: signal(new Set<string>()),
      readonlyCauses: signal(new Set<string>()),
      loadingCauses: signal(new Set<string>()),
    };
    this.handles.set(id, contentHandle);
    return contentHandle;
  }

  contentHandle(id: string): FormularContentHandle | undefined {
    return this.handles.get(id);
  }

  setValue(id: string, value: any) {
    const handle = this.required(id);
    if (!handle.value) {
      throw new Error(`Content handle has no value: ${id}`);
    }
    handle.value.set(value);
  }

  writeValue(id: string, source: FormularSourceWriter, value: any) {
    this.required(id).writeSource?.(source, value);
  }

  writeAll(source: FormularSourceWriter) {
    for (const handle of this.handles.values()) {
      if (handle.value && handle.writeSource) {
        handle.writeSource(source, handle.value());
      }
    }
  }

  updateLoading(id: string, cause: string, loading: boolean) {
    const handle = this.required(id);
    const causes = new Set(handle.loadingCauses());
    if (loading) {
      causes.add(cause);
    } else {
      causes.delete(cause);
    }
    handle.loadingCauses.set(causes);
  }

  emit(id: string, eventName: ContentEvent, payload?: any) {
    this.required(id).events.next({ eventName, payload });
  }

  ids(): Iterable<string> {
    return this.handles.keys();
  }

  attachInstance(id: string, instance: FormularContentInstance) {
    this.required(id).instance = instance;
    this.resolveWhenInitialized();
  }

  whenInitialized(): Promise<void> {
    return this.initialization;
  }

  failInitialization(error: unknown) {
    this.rejectInitialization?.(error);
    this.resolveInitialization = undefined;
    this.rejectInitialization = undefined;
  }

  firstInvalidInstance(): FormularContentInstance | undefined {
    for (const handle of this.handles.values()) {
      if (handle.instance?.validate && !handle.instance.validate()) {
        return handle.instance;
      }
    }
    return undefined;
  }

  private required(id: string): MutableFormularContentHandle {
    const handle = this.handles.get(id);
    if (!handle) {
      throw new Error(`Content state is not initialized yet: ${id}`);
    }
    return handle;
  }

  private resolveWhenInitialized() {
    if (!this.resolveInitialization) {
      return;
    }
    for (const id of this.expectedIds) {
      if (!this.handles.get(id)?.instance) {
        return;
      }
    }
    this.resolveInitialization();
    this.resolveInitialization = undefined;
    this.rejectInitialization = undefined;
  }
}

import {
  Component,
  inject,
  InjectionToken,
  input,
  OnChanges,
  OnDestroy,
  output,
  signal,
  SimpleChanges,
  TemplateRef,
  Signal,
  WritableSignal,
  effect,
  untracked,
} from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ToastService } from '../toast/toast.service';
import { Observable, Subscriber, Subscription } from 'rxjs';
import { OutletDirective } from '../portal/outlet.directive';
import { DynamicRenderRecipe } from '../dynamic-host/dynamic-host.directive';
import { FormularContentRendererComponent } from './content-renderer/formular-content-renderer.component';
import { FormDefinitionStore } from './form-definition.store';
import { FormSourceStore } from './form-source.store';
import { FormContentStore } from './form-content.store';
import { assertFormularRecipeCoverage } from './formular-recipe-registry';
import type { FormOutlet } from './form-outlet';

export interface FormularFieldOverride {
  value: any;
  alias: string;
  disable?: boolean;
}

export interface FormularSourceOverride {
  value: any;
  path: string;
}

export type ContentEvent = 'CHANGE' | 'INPUT' | 'CLICK' | 'BLUR' | 'FOCUS' | 'INIT' | 'REINIT';

export interface FormularContentEvent {
  eventName: ContentEvent;
  payload?: any;
}

export interface FormularContentHandle {
  events: Observable<FormularContentEvent>;
  value?: Signal<any>;
  disabledCauses: Signal<ReadonlySet<string>>;
  requiredCauses: Signal<ReadonlySet<string>>;
  readonlyCauses: Signal<ReadonlySet<string>>;
  loadingCauses: Signal<ReadonlySet<string>>;
}

export interface FormularContentInstance {
  validate?: () => boolean;
  focus?: () => void;
}

export interface FormularSourceWriter {
  set(path: string, value: any): void;
}

export interface FormularContentControlRecipe {
  createValue?: () => WritableSignal<any>;
  writeSource?: (source: FormularSourceWriter, value: any) => void;
}

export type FormularContent = KokuDto.AbstractFormularContent;

export interface FormularContentRenderContext<TContent extends FormularContent = FormularContent> {
  id: string;
  content: Signal<TContent>;
  runtime: FormularRuntime;
  loading: Signal<boolean>;
  submitting: Signal<boolean>;
  contentRegistry: Signal<FormularContentRegistry>;
  buttonDockOutlet: Signal<OutletDirective | undefined>;
  enableDockedOutput: Signal<boolean>;
  placementOutlet: Signal<FormOutlet>;
  context: Signal<Record<string, any> | undefined>;
}

export interface FormularContentRecipe {
  control?: FormularContentControlRecipe;
  render(handle: FormularContentHandle): DynamicRenderRecipe;
}

export type FormularContentRecipeFactory<TContent extends FormularContent = any> = (
  context: FormularContentRenderContext<TContent>,
) => FormularContentRecipe;

export type FormularContentRegistry = Readonly<Record<string, FormularContentRecipeFactory | undefined>>;

export type FormularPluginFactory = (instance: FormularComponent) => FormularPlugin;

export const FORMULAR_PLUGIN = new InjectionToken<FormularPluginFactory | FormularPluginFactory[]>('Formular Plugins');

export interface FormularPlugin {
  onFormularLoaded?(formularData: KokuDto.FormViewDto): void;

  onSourceLoaded?(source: any): void;

  onSubmitError?(
    error: HttpErrorResponse,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
  ): boolean;

  destroy?(): void;
}

export class FormularRuntime {
  private readonly definitions = new FormDefinitionStore();
  private readonly handles = new FormContentStore();
  private readonly sourceState = signal<any>({});
  private readonly contentOverridesByAliasState = signal<ReadonlyMap<string, FormularFieldOverride>>(new Map());
  private readonly sources = new FormSourceStore(this.sourceState);
  readonly source = this.sourceState.asReadonly();
  readonly contentOverridesByAlias = this.contentOverridesByAliasState.asReadonly();

  constructor(private readonly markDirty: () => void) {}

  reset() {
    this.handles.reset();
    this.definitions.reset();
    this.contentOverridesByAliasState.set(new Map());
    this.sources.resetSelectors();
  }

  setFormView(formView: KokuDto.FormViewDto) {
    this.sources.resetSelectors();
    this.definitions.setFormView(formView);
    this.handles.reconcile(this.definitions.contentIds());
  }

  content(id: string | undefined): FormularContent | undefined {
    return this.definitions.content(id);
  }

  contentSignal(id: string): Signal<FormularContent | undefined> {
    return this.definitions.contentSignal(id);
  }

  childIds(parentId: string | undefined, outlet: string): Signal<readonly string[]> {
    return this.definitions.childIds(parentId, outlet);
  }

  failInitialization(error: unknown) {
    this.handles.failInitialization(error);
  }

  whenInitialized(): Promise<void> {
    return this.handles.whenInitialized();
  }

  resolveContent(id: string, control?: FormularContentControlRecipe): FormularContentHandle {
    return this.handles.register(id, control?.createValue, control?.writeSource);
  }

  contentHandle(id: string): FormularContentHandle | undefined {
    return this.handles.contentHandle(id);
  }

  updateContentValue(id: string, value: any) {
    this.handles.setValue(id, value);
    this.sources.update((source) => this.handles.writeValue(id, source, value));
    this.markDirty();
  }

  sourceValue(path: string | undefined, defaultValue?: any): any {
    return this.sources.value(path, defaultValue);
  }

  replaceSource(source: any) {
    this.sources.replace(source);
  }

  initializeSource(source: any, sourceOverrides: FormularSourceOverride[]): any {
    this.sources.replaceAndUpdate(source, (sourceWriter) => {
      for (const sourceOverride of sourceOverrides) {
        if (sourceOverride.path && sourceOverride.value !== undefined) {
          sourceWriter.set(sourceOverride.path, sourceOverride.value);
        }
      }
    });
    this.sources.update((sourceWriter) => this.handles.writeAll(sourceWriter));
    return this.source();
  }

  updateContentLoading(id: string, cause: string, loading: boolean) {
    this.handles.updateLoading(id, cause, loading);
  }

  contentIds(): Iterable<string> {
    return this.handles.ids();
  }

  setContentOverrides(overrides: readonly FormularFieldOverride[] | null | undefined) {
    const overridesByAlias = new Map<string, FormularFieldOverride>();
    for (const override of overrides ?? []) {
      if (override.alias && !overridesByAlias.has(override.alias)) {
        overridesByAlias.set(override.alias, override);
      }
    }
    this.contentOverridesByAliasState.set(overridesByAlias);
  }

  syncContentValuesToSource() {
    this.sources.update((sourceWriter) => this.handles.writeAll(sourceWriter));
  }

  updateContentConfig(id: string, updater: (config: FormularContent) => FormularContent) {
    this.definitions.updateContent(id, updater);
  }

  contentConfig(content: FormularContent): FormularContent {
    return content.id ? (this.definitions.content(content.id) ?? content) : content;
  }

  emit(contentId: string, eventName: ContentEvent, payload?: any) {
    this.handles.emit(contentId, eventName, payload);
  }

  attachInstance(id: string, instance: FormularContentInstance) {
    this.handles.attachInstance(id, instance);
  }

  firstInvalidInstance(): FormularContentInstance | undefined {
    return this.handles.firstInvalidInstance();
  }
}

@Component({
  selector: 'formular',
  imports: [FormularContentRendererComponent],
  templateUrl: './formular.component.html',
  styleUrl: './formular.component.css',
})
export class FormularComponent implements OnDestroy, OnChanges {
  httpClient = inject(HttpClient);
  toastService = inject(ToastService);
  formularPluginsConfig = inject(FORMULAR_PLUGIN, {
    optional: true,
  });

  formularUrl = input.required<string>();
  contentRegistry = input.required<FormularContentRegistry>();

  sourceUrl = input<string>();
  submitUrl = input<string>();
  submitMethod = input<string>();
  maxWidth = input<string | number>();
  buttonDockOutlet = input<OutletDirective>();
  headerTemplate = input<TemplateRef<any>>();
  fieldOverrides = input<FormularFieldOverride[]>([]);
  sourceOverrides = input<FormularSourceOverride[]>([]);
  context = input<Record<string, any>>();

  sourceLoading = signal(false);
  submitting = signal(false);
  dirty = signal(false);
  formularData = signal<KokuDto.FormViewDto | null>(null);

  saved = output<any>();

  runtime = new FormularRuntime(() => this.setDirty());
  readonly source = this.runtime.source;

  private lastSourceSubscription: Subscription | undefined;
  private lastFormularSubscription: Subscription | undefined;
  private pluginInstances: FormularPlugin[] = [];

  constructor() {
    const pluginInstances = [];
    let pluginsConfig = this.formularPluginsConfig;
    if (pluginsConfig) {
      if (!Array.isArray(pluginsConfig)) {
        pluginsConfig = [pluginsConfig];
      }
      for (const currentPlugin of pluginsConfig || []) {
        pluginInstances.push(currentPlugin(this));
      }
    }
    this.pluginInstances = pluginInstances;

    effect(() => {
      const overrides = this.fieldOverrides();
      untracked(() => {
        this.runtime.setContentOverrides(overrides);
        this.applyConfiguredContentOverrides();
      });
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['formularUrl']) {
      this.formularData.set(null);
      if (this.lastFormularSubscription && !this.lastFormularSubscription.closed) {
        this.lastFormularSubscription.unsubscribe();
      }
      this.lastFormularSubscription = this.loadFormular().subscribe({
        next: () => this.loadSource(),
        error: () => undefined,
      });
    } else if (changes['sourceUrl']) {
      this.loadSource();
    }
  }

  ngOnDestroy(): void {
    for (const currentPluginInstance of this.pluginInstances || []) {
      currentPluginInstance.destroy?.();
    }
    this.lastFormularSubscription?.unsubscribe();
    this.lastSourceSubscription?.unsubscribe();
    this.runtime.reset();
  }

  private loadFormular() {
    return new Observable((subscriber) => {
      const formularUrlSnapshot = this.formularUrl();
      if (formularUrlSnapshot) {
        const requestSubscription = this.httpClient.get<KokuDto.FormViewDto>(formularUrlSnapshot).subscribe({
          next: (formularData) => {
            try {
              assertFormularRecipeCoverage(formularData, this.contentRegistry());
              this.runtime.setFormView(formularData);
              this.formularData.set(formularData);
            } catch (error) {
              this.toastService.add('Fehlerhafte Formularkonfiguration', 'error');
              subscriber.error(error);
              return;
            }
            void this.runtime
              .whenInitialized()
              .then(() => {
                if (subscriber.closed) {
                  return;
                }
                for (const currentPluginInstance of this.pluginInstances || []) {
                  currentPluginInstance.onFormularLoaded?.(formularData);
                }
                subscriber.next(formularData);
                subscriber.complete();
              })
              .catch((error) => {
                if (!subscriber.closed) {
                  this.toastService.add('Fehler bei der Formularinitialisierung', 'error');
                  subscriber.error(error);
                }
              });
          },
          error: (err) => {
            this.toastService.add('Fehler beim Laden der Daten! Versuchs später erneut!', 'error');
            subscriber.error(err);
          },
        });
        subscriber.add(requestSubscription);
      } else {
        subscriber.error('missing formularurl');
      }
    });
  }

  private afterSourceLoaded(source: any) {
    const initializedSource = this.runtime.initializeSource(source, this.sourceOverrides() || []);

    try {
      for (const currentPluginInstance of this.pluginInstances || []) {
        currentPluginInstance.onSourceLoaded?.(initializedSource);
      }
    } catch (error) {
      this.toastService.add('Fehler bei der Formularinitialisierung', 'error');
      throw error;
    }
  }

  private applyConfiguredContentOverrides() {
    if (!this.formularData()) {
      return;
    }
    this.runtime.syncContentValuesToSource();
  }

  loadSource() {
    const sourceUrlSnapshot = this.sourceUrl();
    if (sourceUrlSnapshot) {
      this.sourceLoading.set(true);
      if (this.lastSourceSubscription && !this.lastSourceSubscription.closed) {
        this.lastSourceSubscription.unsubscribe();
      }
      this.lastSourceSubscription = this.httpClient.get(sourceUrlSnapshot).subscribe({
        next: (source) => {
          try {
            this.afterSourceLoaded(source);
          } catch {
            this.sourceLoading.set(false);
          }
        },
        error: () => {
          this.sourceLoading.set(false);
        },
        complete: () => {
          this.sourceLoading.set(false);
        },
      });
    } else {
      this.afterSourceLoaded({});
    }
  }

  submit(submitPayload?: any) {
    if (this.submitting()) {
      throw new Error('submit is in progress');
    } else {
      return new Observable((observer) => {
        try {
          if (this.verifyNoFieldError()) {
            const submitUrl = this.submitUrl() || this.sourceUrl();
            if (!submitUrl) {
              throw new Error('target url is unresolvable');
            }
            this.submitting.set(true);

            this.requestSubmit(this.submitMethod() || 'POST', submitUrl, {
              ...this.source(),
              ...(submitPayload ? submitPayload : {}),
            }).subscribe({
              next: (response: any) => {
                this.runtime.replaceSource(response);
                this.saved.emit(response);
                observer.next(response);
                observer.complete();
              },
              error: (error: any) => {
                this.submitting.set(false);
                observer.error(error);
              },
              complete: () => {
                this.submitting.set(false);
                this.dirty.set(false);
                observer.complete();
              },
            });
          } else {
            observer.complete();
          }
        } catch (error) {
          observer.error(error);
        }
      });
    }
  }

  private verifyNoFieldError(): boolean {
    const invalidInstance = this.runtime.firstInvalidInstance();
    if (invalidInstance) {
      this.toastService.add('Überprüfe die Eingaben', 'warning');
      invalidInstance.focus?.();
      return false;
    }
    return true;
  }

  private setDirty() {
    const dirtySnapshot = this.dirty();
    if (!dirtySnapshot) {
      this.dirty.set(true);
    }
  }

  requestSubmit(submitMethod: string, submitUrl: string, submitData: any): Observable<any> {
    return new Observable((observer) => {
      this.httpClient
        .request(submitMethod, submitUrl, {
          body: submitData,
        })
        .subscribe({
          next: (response) => {
            observer.next(response);
            observer.complete();
          },
          error: (error: HttpErrorResponse) => {
            let handled = false;
            for (const currentPluginInstance of this.pluginInstances || []) {
              const handledTmp =
                currentPluginInstance.onSubmitError?.(error, observer, submitMethod, submitUrl, submitData) || false;
              handled = handled || handledTmp;
            }
            if (!handled) {
              observer.error(error);
            }
          },
        });
    });
  }
}
